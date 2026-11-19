# Thay đổi

## 0.2.14

- Nút thu nhỏ, phóng to/khôi phục, chia vùng và đóng nằm trên từng cửa sổ, đi theo vị trí/kích thước của app; taskbar giữ icon app và menu khi giữ icon.
- Phóng to ghi nhớ đúng vùng cũ để khôi phục; chia đôi hoặc chia ba desktop, chừa taskbar.
- Hover `▥` tự mở bảng chọn bằng hình, có vùng sáng khi rê và bấm chọn; `↻` tự sắp xếp 2–3 app đang mở.
- Nền cụm nút lấy theo màu thanh tiêu đề từng app, cập nhật cả khi app tải màu muộn. Bảng chọn không lấy focus bàn phím.
- Thêm nút làm đen màn hình điện thoại trong bảng truy cập nhanh OpenCover, chạm để hiện lại trong khi desktop tiếp tục chạy.
- Taskbar thử gắn lại nếu đăng ký overlay thất bại; ghi lỗi để chẩn đoán.

Kiểm tra: 315 kiểm thử JVM, build release/lint đạt. Đã thử trên Z Flip5 One UI 8.5: phóng to/khôi phục, kéo cửa sổ, thu nhỏ/hiện lại/đóng, chia 2/3, giữ icon chọn vùng, hover mở bảng/tô sáng/đóng khi rời ra. Ảnh HDMI xác nhận màu nền nút khớp Chrome, Cài đặt và Máy tính. Các app/model khác có thể có giới hạn riêng.

## 0.2.9

- Nhận chuột USB/Bluetooth bằng khả năng thiết bị do `getevent` cung cấp, tránh đường sysfs bị Samsung chặn.
- Theo dõi đồng thời nhiều giao diện chuột, gồm giao diện chuột đi kèm bàn phím Bluetooth; kết nối lại không bị chặn bởi giao diện cũ. Tách trạng thái nút bấm từng nguồn để tránh kẹt kéo.
- Dọn phiên desktop khi màn hình ảo/helper biến mất, tạo lại khi Shizuku sẵn sàng. Việc tạo lại desktop có thể đóng các cửa sổ cũ; ứng dụng không tự khởi động Shizuku khi dịch vụ đã dừng.
- Hướng dẫn dùng Samsung Keyboard với bàn phím Bluetooth sau khi Gboard tái hiện lỗi giữ chữ. Đây là cấu hình trên điện thoại, không phải thay đổi mã xử lý phím của OpenCover.

Kiểm tra: 302 kiểm thử JVM, build release và lintVitalRelease đạt. Đã thử dừng helper và chạy lại Shizuku trên Z Flip5 One UI 8.5; người dùng xác nhận chuột ở desktop ổn định và Samsung Keyboard nhập ngay sau khi mở lại tìm kiếm. Chưa kiểm tra trên model khác hoặc sau reboot.
