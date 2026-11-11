# Build và ký APK

Toolchain đã kiểm tra: JDK21, SDK36/Build Tools36.0.0, Gradle8.14.3, AGP8.13.0, Kotlin2.2.20. Wrapper kiểm tra checksum bản phân phối Gradle.

```sh
cd OpenCover-0.2
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease
```

Mã nguồn dùng Java/Kotlin bytecode17. `minSdk=34` (Android14), `targetSdk=36`, package `dev.opencover.globalrotation`.

Nếu cần tự ký release, tạo `OpenCover-0.2/keystore.properties` (đã gitignore):

```properties
storeFile=/absolute/path/to/your-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```

Giữ khóa và mật khẩu riêng; tuyệt đối không commit hoặc đính kèm khóa vào GitHub Releases. Các bản cập nhật cùng package cần dùng cùng khóa ký. Không có keystore.properties thì assembleRelease tạo APK chưa ký; APK đó không cài trực tiếp được.

Nếu ký ngoài Gradle, dùng Android Build Tools `zipalign` trước rồi `apksigner sign`, sau đó `apksigner verify --verbose --print-certs`. Có thể dùng biến môi trường với cú pháp `--ks-pass env:...` và `--key-pass env:...` thay vì đưa mật khẩu vào lịch sử lệnh. Bản release công khai được ký bằng khóa hiện có của người quản lý repo; khóa không được phân phối.

Bản CI debug được ký bằng debug key của runner và không bảo đảm cập nhật đè bản Releases. Kiểm thử phần TwinDesk không cần dependency npm:

```sh
node --test tools/twindesk/opencover-display.test.js tools/twindesk/desktop-cursor.test.js
```

Chạy lệnh Node từ thư mục gốc repo. Kiểm thử Android chạy trên JVM; kiểm thử màn hình ngoài/cảm biến/Trợ năng cần máy thật. Xem ghi chú tính năng trong OpenCover-0.2/docs để biết phạm vi đã xác minh.
