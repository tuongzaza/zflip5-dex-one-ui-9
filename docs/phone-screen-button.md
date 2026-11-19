# Nút tắt màn hình điện thoại — 0.2.10

Khi desktop đang chạy, mở bảng truy cập nhanh OpenCover bằng cách vuốt từ trên xuống trên điện thoại. Nút **Tắt màn hình điện thoại** nằm ngay dưới thanh độ sáng và pin. Nút chỉ hiện ngoài chế độ sửa các ô truy cập nhanh.

Nút làm đen màn hình điện thoại đang dùng (màn hình gập hoặc màn hình trong), để desktop tiếp tục hoạt động. Chạm vào màn hình điện thoại để hiện lại; điện thoại không tự làm đen sau 30 giây. Đây là lớp đen trên màn hình, không phải tắt nguồn panel hay khóa máy.

Phần làm tối cũ không còn được tạo khi chuyển sang chế độ điện thoại bình thường, nên lời gọi nghỉ màn hình không có tác dụng. Bản này tạo lớp đen khi người dùng yêu cầu; lớp đen không lấy focus bàn phím và trả focus cho desktop sau khi bật. Khi desktop dừng, lớp đen được gỡ.

Kiểm tra trên Z Flip5 One UI 8.5: bản 0.2.10 cài cập nhật cùng khóa; nút được tìm và bấm qua cây Trợ năng. Ảnh màn hình điện thoại có mọi pixel RGB bằng 0 khi bật lớp đen. Focus vẫn ở desktop; Máy tính nhận chuỗi phím thử thành 2.468. Một lần chạm khôi phục hình ảnh điện thoại. 302 kiểm thử JVM, build release và lintVitalRelease đạt. Chưa thử thao tác này khi mở điện thoại ra.
