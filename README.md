# OpenCover Rotation / Desktop cho Galaxy Z Flip5

Bản tùy chỉnh từ [OpenCover 0.2](https://github.com/Shmubbier/OpenCover/releases/tag/v0.2), hỗ trợ màn hình ngoài Z Flip5 và desktop qua màn hình kết nối.

**[Tải APK 0.2.6](https://github.com/tuongzaza/zflip5-opencover-rotation/releases/download/v0.2.6/OpenCover-0.2.6.apk)** · **[Hướng dẫn cài đặt tiếng Việt](docs/CAI_DAT.md)** · [Các bản phát hành](https://github.com/tuongzaza/zflip5-opencover-rotation/releases)

## Tính năng

- Launcher trên màn hình gập, xoay màn hình và thanh điều hướng theo nội dung.
- Desktop trên màn hình USB-C: ứng dụng chạy trong cửa sổ, có thanh tác vụ và điều khiển cửa sổ do hệ thống cung cấp.
- Dùng điện thoại bình thường hoặc làm bàn di chuột qua mục **Chế độ điện thoại** trong truy cập nhanh.
- Đổi hình nền OpenCover trên điện thoại và desktop riêng qua mục **Hình nền**.
- Con trỏ từ chuột USB, bàn di chuột điện thoại và TwinDesk; [bản vá TwinDesk Linux](tools/twindesk/README.md) đi kèm.
- Mirror có thể chọn một desktop đang tồn tại; nguồn hình và tọa độ điều khiển dùng cùng display.

## Tương thích

APK yêu cầu **Android 14 trở lên**. Bản hiện tại đã được thử trên **Galaxy Z Flip5 SM-F731N, One UI 8.5 / Android 16**. Các tính năng cover và desktop dùng API riêng của Samsung; cài được APK không đồng nghĩa mọi chức năng hoạt động trên mọi máy. Desktop cần Shizuku đang chạy và một màn hình được Android nhận diện. Không bổ sung khả năng xuất hình cho cáp, hub hoặc thiết bị không hỗ trợ đường kết nối đó.

Phóng to cửa sổ không bảo đảm ứng dụng chuyển sang giao diện tablet: Cài đặt Samsung và YouTube vẫn dùng bố cục điện thoại trên máy thử. Bản này không giả model thiết bị và không yêu cầu root.

## Build từ mã nguồn

Dùng **JDK 21**, Android SDK Platform **36**, Build Tools **36.0.0**. Gradle Wrapper **8.14.3**, AGP **8.13.0**, Kotlin/Compose plugin **2.2.20** đã được cố định trong repo.

Linux/macOS:

```sh
cd OpenCover-0.2
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Windows PowerShell:

```powershell
cd OpenCover-0.2
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Đặt SDK qua `ANDROID_HOME` hoặc `local.properties` (`sdk.dir=...`, không commit). APK debug ở `app/build/outputs/apk/debug/app-debug.apk`. Bản release chưa ký ở `app/build/outputs/apk/release/app-release-unsigned.apk` nếu chưa cấu hình khóa. Xem [hướng dẫn ký](docs/BUILD.md).

GitHub Actions build và chạy kiểm thử Android/TwinDesk trên mỗi push hoặc pull request. Artifact CI là APK debug với khóa CI, không phải khóa của bản Releases; dùng APK trong Releases để cài và cập nhật bình thường.

## Tài liệu kỹ thuật

- [Chế độ điện thoại và bàn di chuột](OpenCover-0.2/docs/phone-modes.md)
- [Mirror desktop](OpenCover-0.2/docs/desktop-mirror.md)
- [Hình nền và điều hướng](OpenCover-0.2/docs/wallpaper-navigation.md)
- [Hướng dẫn gốc](OpenCover-0.2/docs/SETUP.md)

Package: `dev.opencover.globalrotation`. Giấy phép [GPL-3.0](OpenCover-0.2/LICENSE); ghi nhận tác giả và giấy phép upstream được giữ trong thư mục nguồn.
