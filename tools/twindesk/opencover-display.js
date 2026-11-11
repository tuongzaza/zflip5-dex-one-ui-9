'use strict';

// Use the actual desktop display, not its physical HDMI surface or the cover screen.
function parseDesktopDisplay(text) {
  const line = String(text).split('\n').find(l => /mBaseDisplayInfo=DisplayInfo\{"OpenCover Desk",/.test(l));
  if (!line) return null;
  const id = line.match(/displayId (\d+)/);
  const size = line.match(/real (\d+) x (\d+)/);
  const dpi = line.match(/density (\d+)/);
  if (!id || !size || Number(id[1]) <= 1 || Number(size[1]) < 1 || Number(size[2]) < 1) return null;
  return { id: Number(id[1]), w: Number(size[1]), h: Number(size[2]), dpi: dpi ? Number(dpi[1]) : 160 };
}

async function desktopDisplay(adb, serial) {
  const result = await adb.shell(serial, 'dumpsys display', { timeout: 4000 });
  if (result.code !== 0) throw new Error('Cannot read OpenCover desktop display');
  return parseDesktopDisplay(result.out);
}
module.exports = { parseDesktopDisplay, desktopDisplay };
