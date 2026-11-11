# TwinDesk 3.0.0 Linux: điều khiển OpenCover Desktop

Bản vá tùy chọn cho [TwinDesk](https://github.com/InukaAsith/TwinDesk), GPL-3.0. phone-bridge.js giữ ghi nhận bản quyền upstream; LICENSE trong thư mục này là giấy phép upstream. Chỉ đã thử TwinDesk3.0.0/Linux GNOME Wayland và Z Flip5 One UI8.5. Không hỗ trợ áp dụng nguyên trạng trên Windows/macOS hoặc bản TwinDesk khác.

## 1. Ghép đôi và sửa quyền nhập liệu

Cài TwinDesk trên máy tính/điện thoại và ghép đôi theo hướng dẫn ứng dụng. Trong General/This computer, bấm **Fix** ở **Input access**, xác thực quản trị trên máy. Helper chuột/bàn phím phải ở trạng thái đang chạy. Nếu báo `pkexec must be setuid root` dù quyền pkexec đúng, đóng TwinDesk và mở từ menu ứng dụng của desktop; tiến trình chạy dưới NoNewPrivs có thể không nâng quyền được.

Một số hệ thống để hai helper ở0755 sau Fix. Chỉ sau khi kiểm tra chúng là tệp gốc, thuộc root, nhóm input, và khớp binary đi kèm TwinDesk, quản trị viên có thể hoàn tất quyền bằng Python (không cần thêm cả người dùng vào nhóm input):

```sh
sudo python3 -c 'import os; [os.chmod("/usr/local/libexec/twindesk/"+n,0o2755) for n in ("macshare-linuxhelper","macshare-linuxhelper-multi")]'
```

Dùng trình Fix chính thức trước; không đổi quyền toàn bộ `/dev/input` hay `/dev/uinput` thành world-writable.

## 2. Cài bản vá chọn desktop và vẽ con trỏ

Đóng TwinDesk. Từ thư mục repo:

```sh
python3 tools/twindesk/install.py --check
sudo python3 tools/twindesk/install.py
```

Script chỉ chấp nhận mã băm đúng của phone-bridge.js TwinDesk3.0.0 hoặc bản đã vá, tạo `phone-bridge.js.opencover-backup`, rồi cài ba tệp JS. Nếu báo khác phiên bản hoặc có chỉnh sửa, dừng để đối chiếu; không ghi đè cưỡng bức. Có thể dùng `--app-dir` nếu thư mục ứng dụng khác `/opt/TwinDesk/resources/app`.

Không cần root điện thoại. Cần ADB đã được điện thoại cho phép, OpenCover0.2.5+ và desktop đang chạy. Không dùng địa chỉ ADB của máy khác: xác định thiết bị bằng `adb devices` hoặc ghép đôi wireless debugging của chính điện thoại.

```sh
env TWINDESK_CONTROL_DISPLAY=opencover /opt/TwinDesk/twindesk
```

Nếu có nhiều thiết bị, thêm `TWINDESK_ADB_SERIAL` với serial hiện trong `adb devices`:

```sh
env TWINDESK_CONTROL_DISPLAY=opencover TWINDESK_ADB_SERIAL="YOUR_DEVICE_SERIAL" /opt/TwinDesk/twindesk
```

Nối lại điện thoại với máy tính đã ghép đôi trong trang Connect nếu chưa tự kết nối. Không chia sẻ token ghép đôi hay khóa ADB. Trong Cursor, bật chia sẻ chuột/bàn phím, cấu hình cạnh màn hình, **tắt Shake to bring the pointer back** nếu rê nhanh làm chuột bật về máy tính. Di chuyển qua cạnh đã cấu hình để sang desktop; không phải mọi cạnh đều chuyển.

Bản vá tìm display có tên OpenCover Desk, dùng đúng kích thước/DPI và scrcpy display_id. Kiểm tra lại mỗi3giây khi kết nối, chuyển đích khi display đổi, quay về điều khiển điện thoại khi không có desktop. Con trỏ đi qua ADB local socket của OpenCover; socket chỉ nhận Android shell UID2000, đúng displayId và tọa độ hữu hạn. Luồng này chỉ vẽ con trỏ; nhập liệu vẫn qua scrcpy.

## Khôi phục và cập nhật

Đóng TwinDesk, khôi phục `linux/phone-bridge.js.opencover-backup` về `linux/phone-bridge.js` trong thư mục app và bỏ biến TWINDESK_CONTROL_DISPLAY. Hai module bổ sung không còn được nạp. Cập nhật TwinDesk có thể ghi đè bản vá; kiểm tra tương thích trước khi cài lại. Các tệp backup cấu hình/ghép đôi của máy phát triển không được phân phối.

## Kiểm thử

```sh
node --test tools/twindesk/opencover-display.test.js tools/twindesk/desktop-cursor.test.js
```

Bốn kiểm thử chọn display và hai kiểm thử kênh con trỏ. Trên máy thật đã xác nhận con trỏ theo chuột và rê nhanh ổn sau khi tắt Shake to bring the pointer back.
