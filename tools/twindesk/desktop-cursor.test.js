const {test}=require('node:test');
const assert=require('node:assert/strict');
const net=require('node:net');
const {once}=require('node:events');
const {DesktopCursor}=require('./desktop-cursor');
test('sends display and cursor position as fixed binary frame, rejects invalid positions, removes forwarding',async()=>{
 const server=net.createServer();server.listen(0,'127.0.0.1');await once(server,'listening');
 const removed=[];
 const adb={forward:async(_s,name)=>{assert.equal(name,'opencover_cursor_387');return server.address().port},unforward:async(_s,p)=>removed.push(p)};
 const incoming=once(server,'connection');
 const cursor=new DesktopCursor(adb,'phone',387);
 const [socket]=await incoming;const data=once(socket,'data');
 cursor.move(320.5,240.25,true);
 const [frame]=await data;
 assert.equal(frame.length,16);assert.equal(frame.readInt32BE(0),387);assert.equal(frame.readFloatBE(4),320.5);assert.equal(frame.readFloatBE(8),240.25);assert.equal(frame.readInt32BE(12),1);
 cursor.move(NaN,10,false);assert.deepEqual(cursor.last,[320.5,240.25,true]);
 const closed=once(cursor.socket,'close');cursor.stop();await closed;
 assert.equal(removed.length,1);socket.destroy();await new Promise(r=>server.close(r));
});
test('stop while forwarding is pending cleans up without opening a socket',async()=>{
 let done;const removed=[];
 const cursor=new DesktopCursor({forward:()=>new Promise(r=>done=r),unforward:async(_s,p)=>removed.push(p)},'phone',387);
 cursor.stop();done(12345);await new Promise(r=>setImmediate(r));
 assert.deepEqual(removed,[12345]);assert.equal(cursor.socket,null);
});
