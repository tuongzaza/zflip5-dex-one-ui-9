# Cài OpenCover 0.2.6 trên thiết bị khác

## 1. Chuẩn bị và cài APK

- Android 14 trở lên; ưu tiên Galaxy Z Flip5. Đã thử SM-F731N với One UI 8.5/Android 16; model/firmware khác chưa được xác minh.
- Tải **OpenCover-0.2.6.apk** từ [GitHub Releases](https://github.com/tuongzaza/zflip5-opencover-rotation/releases/tag/v0.2.6). Có SHA256SUMS.txt để đối chiếu.
- Cho phép trình duyệt/trình quản lý tệp cài ứng dụng không rõ nguồn gốc, rồi mở APK. Hoặc dùng máy tính:

```sh
adb devices
adb install -r OpenCover-0.2.6.apk
```

Nếu báo chữ ký không khớp, đừng gỡ bản cũ khi chưa sao lưu dữ liệu cần giữ. Bản upstream `dev.opencover` và bản này `dev.opencover.globalrotation` là hai package khác nhau. Bản cũ cùng package nhưng khác khóa ký không thể cập nhật đè; cần bản ký đúng khóa hoặc gỡ bản cũ (mất dữ liệu OpenCover).

## 2. Cấp các quyền trong ứng dụng

Mở OpenCover Rotation. Nếu Android chặn quyền Trợ năng: **Cài đặt → Ứng dụng → OpenCover Rotation → ⋮ → Cho phép cài đặt bị hạn chế**, nếu mục này xuất hiện.

Làm theo checklist trong app:

- **Trợ năng/OpenCover Rotation** và **Hiển thị trên ứng dụng khác** cho launcher/điều hướng/overlay.
- Bỏ tối ưu pin cho OpenCover và Shizuku nếu hệ thống thường dừng chúng.
- Quyền thông báo, quyền truy cập thông báo, lịch sử sử dụng là các mục riêng; chỉ cấp khi sử dụng tính năng tương ứng.

Với Samsung cover, cài Good Lock/MultiStar nếu có trên thiết bị và mở **Galaxy Foldable → Launcher Widget** một lần. Có thể cấp quyền cài đặt bảo mật từ máy tính cho tính năng cover/banking của OpenCover:

```sh
adb shell pm grant dev.opencover.globalrotation android.permission.WRITE_SECURE_SETTINGS
```

Lệnh trên không tự bật Trợ năng; bạn bật bằng giao diện Android.

## 3. Khởi động Shizuku — bắt buộc cho desktop/xoay nâng cao

Cài [Shizuku chính thức](https://github.com/RikkaApps/Shizuku/releases). Bật Tùy chọn nhà phát triển và Gỡ lỗi không dây, ghép đôi trong Shizuku rồi bấm Start. Mở OpenCover và cho phép yêu cầu Shizuku.

Hoặc nối máy tính, xác nhận USB debugging trên điện thoại rồi chạy:

```sh
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

Không root thì cần khởi động lại Shizuku sau khi khởi động lại điện thoại. [Hướng dẫn chính thức](https://shizuku.rikka.app/guide/setup/).

## 4. Desktop trên màn hình ngoài

1. Kết nối màn hình/hub với điện thoại. Xác nhận Android nhận màn hình.
2. Trong OpenCover vào **Settings → Experimental → Desktop on external monitor** và bật tính năng.
3. Giữ OpenCover Trợ năng và Shizuku đang chạy. Desktop khởi động trên màn hình được nhận diện.
4. Mở ứng dụng từ desktop; dùng điều khiển cửa sổ của hệ thống để thu nhỏ/phóng to/đóng. Nhấn icon ứng dụng đang mở trên taskbar để đưa nó ra trước.

Cáp chỉ sạc không xuất hình; việc có cổng USB-C không tự bảo đảm xuất được video. Chức năng còn phụ thuộc model, firmware và màn hình/hub. Desktop OpenCover là triển khai tùy chỉnh, không biến máy thành thiết bị được Samsung hỗ trợ DeX chính thức.

## 5. Điện thoại, bàn di chuột, hình nền và xoay

Vuốt xuống **bảng truy cập nhanh của OpenCover** trên màn hình gập:

- **Chế độ điện thoại** → Điện thoại hoặc Bàn di chuột. Bàn di chuột: rê một ngón, chạm để bấm, hai ngón cuộn, giữ để kéo.
- **Hình nền** → Điện thoại (OpenCover) hoặc Desktop / DeX → chọn ảnh → chỉnh khung → **Set**. Hai hình nền độc lập. Hình nền điện thoại ở đây là màn hình Home của OpenCover; hình nền/màn hình khóa Samsung được quản lý riêng.
- **Auto-rotate** để tự xoay màn hình cover; thanh điều hướng theo cạnh dưới của nội dung. Có thể đặt góc cố định trong Settings → Phone & display.

Nếu đã chỉnh danh sách ô, mở nút bút chì để thêm lại. Khi mở máy, Android Quick Settings cũng có thể thêm ô **Chế độ điện thoại** qua trình sửa ô của hệ thống. Mục **Hình nền** hiện nằm trong bảng OpenCover, không phải ô Android riêng.

Không thay đổi DPI điện thoại để dùng desktop. Thử nghiệm DPI 240/160 không làm Cài đặt Samsung/YouTube chuyển thành giao diện tablet trên máy đã thử.

## 6. Dùng chuột/bàn phím máy tính qua TwinDesk

Đây là phần tùy chọn, không cần để sử dụng chuột USB hoặc bàn di chuột điện thoại. Xem [hướng dẫn và bản vá TwinDesk 3.0.0 Linux](../tools/twindesk/README.md). Không tự cài bản vá Linux vào TwinDesk Windows/macOS.

## Xử lý sự cố

| Hiện tượng | Kiểm tra |
|---|---|
| Không có desktop | Màn hình có được Android nhận, Shizuku có chạy và đã cho phép OpenCover, Trợ năng có bật |
| Mất desktop sau reboot | Khởi động lại Shizuku; mở OpenCover |
| Không thấy ô Hình nền/chế độ | Mở bảng truy cập nhanh OpenCover và kiểm tra phần sửa ô |
| Hình nền Samsung đổi nhưng OpenCover không đổi | Chọn qua Hình nền trong OpenCover |
| Bàn di chuột không dùng được | Desktop phải đang chạy; chọn lại Bàn di chuột |
| App vẫn giao diện điện thoại | Bố cục do từng app quyết định; phóng to không bảo đảm tablet UI |
| Bản mới không cài đè được | Kiểm tra package, version và chữ ký; dùng APK Releases thay vì APK CI |

Không có khóa ký, dữ liệu ghép đôi TwinDesk, địa chỉ điện thoại cá nhân hoặc ảnh nền người dùng trong gói phát hành này.
