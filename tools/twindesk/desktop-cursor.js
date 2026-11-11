'use strict';
const net = require('net');

// An ADB-only cursor channel. Input still goes through scrcpy; this only draws OpenCover's pointer.
class DesktopCursor {
  constructor(adb, serial, displayId) {
    Object.assign(this, { adb, serial, displayId, active: true, socket: null, port: null, last: null });
    this.connect();
  }
  async connect() {
    if (!this.active) return;
    try {
      const port = await this.adb.forward(this.serial, `opencover_cursor_${this.displayId}`);
      if (!this.active) { this.adb.unforward(this.serial, port).catch(() => {}); return; }
      this.port = port;
      const socket = net.createConnection({ host: '127.0.0.1', port });
      this.socket = socket;
      socket.setNoDelay(true);
      socket.on('connect', () => { if (this.last) this.move(...this.last); });
      socket.on('error', () => socket.destroy());
      socket.on('close', () => {
        if (this.socket === socket) this.socket = null;
        if (this.port === port) this.port = null;
        this.adb.unforward(this.serial, port).catch(() => {});
        if (this.active) this.retry = setTimeout(() => this.connect(), 1000);
      });
    } catch (_) { if (this.active) this.retry = setTimeout(() => this.connect(), 1000); }
  }
  move(x, y, down) {
    if (!this.active || !Number.isFinite(x) || !Number.isFinite(y)) return;
    this.last = [x, y, !!down];
    const s = this.socket;
    if (!s || s.connecting || s.destroyed || s.writableLength > 65536) return;
    const frame = Buffer.alloc(16);
    frame.writeInt32BE(this.displayId, 0);
    frame.writeFloatBE(x, 4); frame.writeFloatBE(y, 8);
    frame.writeInt32BE(down ? 1 : 0, 12);
    s.write(frame);
  }
  stop() {
    this.active = false;
    clearTimeout(this.retry);
    this.socket?.destroy(); // close removes its adb forward
    if (!this.socket && this.port != null) this.adb.unforward(this.serial, this.port).catch(() => {});
  }
}
module.exports = { DesktopCursor };
