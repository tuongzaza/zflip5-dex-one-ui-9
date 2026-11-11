#!/usr/bin/env python3
"""Install the opt-in OpenCover input patch for TwinDesk 3.0.0 on Linux."""
import argparse
import hashlib
from pathlib import Path
import shutil

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--app-dir', type=Path, default=Path('/opt/TwinDesk/resources/app'))
parser.add_argument('--check', action='store_true', help='validate only; do not modify files')
args = parser.parse_args()
source = Path(__file__).resolve().parent
bridge = args.app_dir / 'linux/phone-bridge.js'
original_hash = '41267cbe9df6d29ae13fb6c09225c0940f2d965323d001614f4d807d23a8cd29'
patched_hash = '7e2dcbe24866498cf33e7f8e6aa07cdb0b9090d909dc73464f347be6b4f16cef'
current = hashlib.sha256(bridge.read_bytes()).hexdigest()
if current not in (original_hash, patched_hash):
    raise SystemExit('Unsupported TwinDesk version or local modifications. No files changed.')
if hashlib.sha256((source / 'phone-bridge.js').read_bytes()).hexdigest() != patched_hash:
    raise SystemExit('Patched source checksum mismatch. No files changed.')
if args.check:
    print('Compatible TwinDesk 3.0.0 installation; no files changed.')
else:
    backup = bridge.with_name('phone-bridge.js.opencover-backup')
    if current == original_hash and not backup.exists():
        shutil.copy2(bridge, backup)
    for name in ('opencover-display.js', 'desktop-cursor.js', 'phone-bridge.js'):
        target = bridge.parent / name
        shutil.copyfile(source / name, target)
        target.chmod(0o644)
    print('Installed. Start TwinDesk with TWINDESK_CONTROL_DISPLAY=opencover.')
