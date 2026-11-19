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

## Desktop biến mất sau khi dùng được một lúc — 0.2.9

Trong lần lỗi tiếp theo trên thiết bị, cả tiến trình điều khiển desktop và `shizuku_server` không còn chạy; màn hình ảo OpenCover Desk cũng biến mất. Đây là lỗi khác với việc chỉ dò được một chuột. Chưa xác định được tác nhân dừng Shizuku.

0.2.9 dọn phiên desktop đã mất màn hình ảo và xử lý ngắt kết nối helper. Nếu Shizuku còn chạy, OpenCover dựng lại desktop; nếu Shizuku dừng, desktop khởi động lại khi Shizuku sẵn sàng và đã được cấp quyền. Không thể giữ chuyển tiếp chuột khi dịch vụ đặc quyền không chạy. Khởi động lại desktop có thể đóng các cửa sổ của màn hình ảo cũ.

Với Shizuku 13.6, lệnh `start.sh` cũ có thể không còn. Dùng lệnh hiển thị trong ứng dụng Shizuku hoặc chương trình `lib/arm64/libshizuku.so` thuộc thư mục APK đang cài. Không dùng lại đường dẫn APK từ thiết bị khác.

### Kết quả kiểm tra 0.2.9 trên Z Flip5

- 302 kiểm thử JVM và build release/lintVitalRelease đạt. APK ký cùng khóa, cài cập nhật giữ dữ liệu.
- Dừng helper26058 có kiểm soát: tự tạo helper27569, desktop414 chuyển sang415, nhận lại event16 trong khoảng3giây.
- Dừng cả Shizuku và helper: desktop được dọn; sau khi chạy lại Shizuku, tự tạo helper29325, desktop416 và nhận lại chuột.
- Người dùng xác nhận sau khi dùng thử: chuột vẫn ở desktop, không còn tự nhảy về điện thoại.
