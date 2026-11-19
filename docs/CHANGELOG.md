# Thay đổi

## 0.2.9

- Nhận chuột USB/Bluetooth bằng khả năng thiết bị do `getevent` cung cấp, tránh đường sysfs bị Samsung chặn.
- Theo dõi đồng thời nhiều giao diện chuột, gồm giao diện chuột đi kèm bàn phím Bluetooth; kết nối lại không bị chặn bởi giao diện cũ. Tách trạng thái nút bấm từng nguồn để tránh kẹt kéo.
- Dọn phiên desktop khi màn hình ảo/helper biến mất, tạo lại khi Shizuku sẵn sàng. Việc tạo lại desktop có thể đóng các cửa sổ cũ; ứng dụng không tự khởi động Shizuku khi dịch vụ đã dừng.
- Hướng dẫn dùng Samsung Keyboard với bàn phím Bluetooth sau khi Gboard tái hiện lỗi giữ chữ. Đây là cấu hình trên điện thoại, không phải thay đổi mã xử lý phím của OpenCover.

Kiểm tra: 302 kiểm thử JVM, build release và lintVitalRelease đạt. Đã thử dừng helper và chạy lại Shizuku trên Z Flip5 One UI 8.5; người dùng xác nhận chuột ở desktop ổn định và Samsung Keyboard nhập ngay sau khi mở lại tìm kiếm. Chưa kiểm tra trên model khác hoặc sau reboot.
