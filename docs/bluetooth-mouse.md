# Chuột Bluetooth trên desktop — 0.2.8

## Lỗi và cách sửa

One UI nhận chuột AULA-SC650 nhưng con trỏ chỉ chạy trên màn hình điện thoại. Desktop OpenCover báo `no mouse found` vì Samsung từ chối đọc `/sys/class/input/eventN/device/capabilities/rel` bằng quyền shell/Shizuku.

OpenCover dùng `/system/bin/getevent -lp` để dò thiết bị có đủ `REL_X`, `REL_Y`, `BTN_MOUSE`, rồi giữ và đọc thiết bị đó bằng cơ chế hiện có. Các sự kiện được gửi tới màn hình desktop; OpenCover vẽ con trỏ trên màn hình ngoài. Cách này dùng chung cho chuột USB và Bluetooth, không cần root. Vòng dò vẫn chạy nếu mở desktop trước khi kết nối chuột.

## Sử dụng

1. Bật Shizuku và cấp quyền cho OpenCover.
2. Kết nối màn hình ngoài, bật desktop trong OpenCover.
3. Ghép đôi chuột Bluetooth với điện thoại. Chuột được chuyển sang desktop khi desktop hoạt động; màn hình điện thoại vẫn dùng cảm ứng.
4. Ngắt màn hình/desktop để trả chuột về điện thoại.

Từ 0.2.8, theo dõi đồng thời tất cả thiết bị chuột. Bàn phím AULA-F99 cũng cung cấp một giao diện chuột; giao diện này không còn chặn việc nhận chuột AULA-SC650 kết nối sau đó. Danh sách cập nhật mỗi giây, mỗi thiết bị có luồng đọc riêng. Khi ngắt một thiết bị, trạng thái nút bấm của thiết bị đó được xóa để tránh kẹt kéo; chuột khác và bàn di chuột điện thoại giữ trạng thái riêng. Không cần sửa quyền sysfs hay `/dev/input`.

## Kiểm tra

- Build debug và 299 kiểm thử JVM đạt, gồm 4 kiểm thử dò thiết bị: AULA-SC650, cảm biến/bàn phím/màn hình cảm ứng, không trộn khả năng của hai thiết bị, đường dẫn không hợp lệ hoặc không có chuột.
- Trên SM-F731N One UI 8.5, đã cài cập nhật giữ dữ liệu bằng khóa ký hiện có. Helper mới nhận `/dev/input/event13` và con trỏ overlay desktop đã đổi tọa độ khi chuột được di chuyển.
- Chờ người dùng xác nhận thao tác bấm/cuộn; chưa kiểm tra thực tế tắt/bật lại chuột và khởi động lại điện thoại.

### Kiểm tra bản 0.2.8

- Build debug và 302 kiểm thử JVM đạt. Thêm tình huống AULA-F99 Mouse xuất hiện trước AULA-SC650 Mouse, giữ nút kéo qua báo cáo rỗng từ thiết bị thứ hai, và giải phóng nút khi thiết bị ngắt kết nối.
- Việc thử thực tế đồng thời bàn phím, chuột và ngắt/kết nối lại cần xác nhận trên thiết bị.
