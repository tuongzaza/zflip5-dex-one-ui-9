# OpenCover Rotation for Galaxy Z Flip5

This project adapts [OpenCover 0.2](https://github.com/Shmubbier/OpenCover/releases/tag/v0.2) for the Galaxy Z Flip5 cover screen. The source and original license are in [OpenCover-0.2](OpenCover-0.2/).

- Choose a fixed angle for the whole cover screen or enable Auto-rotate in OpenCover's quick settings.
- The gesture bar follows the bottom of the rotated content. At 180°, OpenCover keeps controls clear of the camera cutout and accepts a pull-down next to the camera.
- While using a USB-C desktop, choose ordinary phone use or a phone touchpad without changing phone density. See [phone mode notes](OpenCover-0.2/docs/phone-modes.md).
- Mirror can select an existing desktop display; capture and touch use the same source. See [desktop mirror notes](OpenCover-0.2/docs/desktop-mirror.md).
- Rotation requires Shizuku. The app uses a separate Android package, `dev.opencover.globalrotation`.

From the source folder, build and run the unit tests with JDK 21 and Android SDK 37:

```powershell
cd OpenCover-0.2
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

OpenCover is distributed under the [GPL-3.0 license](OpenCover-0.2/LICENSE). See the upstream [setup guide](OpenCover-0.2/docs/SETUP.md) for the cover-screen and Shizuku setup.
