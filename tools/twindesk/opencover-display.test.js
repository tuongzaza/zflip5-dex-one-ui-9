const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const {parseDesktopDisplay,desktopDisplay}=require('./opencover-display');
test('selects OpenCover virtual desktop from actual multi-display dump',()=>assert.deepEqual(parseDesktopDisplay('mBaseDisplayInfo=DisplayInfo{"Màn hình tích hợp", displayId 1, real 748 x 720, density 340\nmBaseDisplayInfo=DisplayInfo{"HDMI", displayId 381, real 2160 x 1440, density 438\nmBaseDisplayInfo=DisplayInfo{"OpenCover Desk", displayId 387, real 2160 x 1440, density 160'),{id:387,w:2160,h:1440,dpi:160}));
test('ignores cover, physical monitor and other virtual displays',()=>assert.equal(parseDesktopDisplay('mBaseDisplayInfo=DisplayInfo{"HDMI", displayId 381, real 2160 x 1440\nmBaseDisplayInfo=DisplayInfo{"Mirror", displayId 390, real 748 x 720'),null));
test('rejects malformed desktop info',()=>assert.equal(parseDesktopDisplay('mBaseDisplayInfo=DisplayInfo{"OpenCover Desk", displayId 0, real 0 x 0'),null));
test('does not treat failed adb command as desktop removal',async()=>assert.rejects(desktopDisplay({shell:async()=>({code:1,out:''})},'test')));
