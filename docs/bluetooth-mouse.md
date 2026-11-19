# Chuột Bluetooth trên desktop — 0.2.7

## Lỗi và cách sửa

One UI nhận chuột AULA-SC650 nhưng con trỏ chỉ chạy trên màn hình điện thoại. Desktop OpenCover báo `no mouse found` vì Samsung từ chối đọc `/sys/class/input/eventN/device/capabilities/rel` bằng quyền shell/Shizuku.

OpenCover dùng `/system/bin/getevent -lp` để dò thiết bị có đủ `REL_X`, `REL_Y`, `BTN_MOUSE`, rồi giữ và đọc thiết bị đó bằng cơ chế hiện có. Các sự kiện được gửi tới màn hình desktop; OpenCover vẽ con trỏ trên màn hình ngoài. Cách này dùng chung cho chuột USB và Bluetooth, không cần root. Vòng dò vẫn chạy nếu mở desktop trước khi kết nối chuột.

## Sử dụng

1. Bật Shizuku và cấp quyền cho OpenCover.
2. Kết nối màn hình ngoài, bật desktop trong OpenCover.
3. Ghép đôi chuột Bluetooth với điện thoại. Chuột được chuyển sang desktop khi desktop hoạt động; màn hình điện thoại vẫn dùng cảm ứng.
4. Ngắt màn hình/desktop để trả chuột về điện thoại.

Hiện lấy một thiết bị chuột đầu tiên tìm thấy. Không cần sửa quyền sysfs hay `/dev/input`.

## Kiểm tra

- Build debug và 299 kiểm thử JVM đạt, gồm 4 kiểm thử dò thiết bị: AULA-SC650, cảm biến/bàn phím/màn hình cảm ứng, không trộn khả năng của hai thiết bị, đường dẫn không hợp lệ hoặc không có chuột.
- Trên SM-F731N One UI 8.5, đã cài cập nhật giữ dữ liệu bằng khóa ký hiện có. Helper mới nhận `/dev/input/event13` và con trỏ overlay desktop đã đổi tọa độ khi chuột được di chuyển.
- Chờ người dùng xác nhận thao tác bấm/cuộn; chưa kiểm tra thực tế tắt/bật lại chuột và khởi động lại điện thoại.
