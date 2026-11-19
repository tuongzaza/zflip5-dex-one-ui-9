# Điều khiển cửa sổ và chia vùng desktop — 0.2.11

Phạm vi: một màn hình desktop chia thành hai hoặc ba cột bằng nhau, chừa taskbar phía dưới. Các cửa sổ chỉ thuộc desktop OpenCover đang chạy; không đổi cửa sổ trên điện thoại.

- Menu cửa sổ trên taskbar: thu nhỏ, phóng to/khôi phục, đóng cửa sổ đang ở trước; chia hai hoặc ba ứng dụng đang mở gần nhất.
- Giữ biểu tượng ứng dụng: điều khiển cửa sổ đó và đặt riêng nó vào một nửa hoặc một phần ba màn hình.
- Phóng to nhớ vị trí/kích thước trước đó; khôi phục đưa về đúng vùng đã lưu, giới hạn trong diện tích desktop nếu cần.
- Thu nhỏ không đóng ứng dụng; bấm biểu tượng taskbar để hiện lại.
- Chia hai/ba cần đủ hai/ba ứng dụng đang mở. App có giới hạn kích thước riêng có thể không vừa vùng được yêu cầu.

Kiểm tra cần đạt: tính vùng chia không có khe hở/tràn; khôi phục độc lập từng cửa sổ; không lấy kích thước phóng to ghi đè lịch sử; thao tác helper từ chối task ngoài desktop; thử thu nhỏ/hiện lại/phóng to/khôi phục/chia2/chia3 trên Z Flip5.

## Dùng nhanh

Trên taskbar có `−` (thu nhỏ), `□`/`❐` (phóng to/khôi phục), `▥` (chia vùng) và `×` (đóng). Các nút tác động vào cửa sổ đang được chọn. Icon app có gạch sáng khi cửa sổ đó giữ focus. Bấm icon để đưa app ra trước, kể cả khi đã thu nhỏ.

Bấm `▥` rồi chọn **Chia đôi: 2 ứng dụng** hoặc **Chia ba: 3 ứng dụng**. Mở đủ số ứng dụng trên desktop trước; chọn các task gần nhất, kể cả task đang thu nhỏ. Giữ icon app để mở menu riêng, chọn **Nửa trái/phải** hoặc **Một phần ba bên trái/ở giữa/bên phải**.

## Kiểm tra

- 309 kiểm thử JVM và build release/lintVitalRelease đạt, gồm 7 kiểm thử mới cho chia vùng và khôi phục.
- Trên Z Flip5 One UI 8.5: Máy tính phóng to từ `[519,179,1599,999]` tới `[0,0,2160,1384]`, nút đổi sang khôi phục và đưa về đúng vùng ban đầu.
- Thu nhỏ giữ task, bấm icon khôi phục; chia đôi đặt Chrome/Cài đặt vào hai vùng rộng 1080 px; chia ba đặt Chrome/Cài đặt/Máy tính vào ba cột rộng 720 px, cao 1384 px trên desktop 2160×1440 (taskbar 56 px).
- Phóng to rồi khôi phục một cửa sổ từ cột phải 1/3 trả đúng `[1440,0,2160,1384]`. Giữ icon mở được menu riêng.
- Chỉ kiểm tra ba app trên; app khác có thể áp dụng giới hạn kích thước riêng. Các nút bổ sung nằm trên taskbar; nút trên tiêu đề cửa sổ của Samsung do hệ thống quản lý.

## Sửa hàng nút không xuất hiện — tích hợp trong 0.2.13

Đã ghi nhận desktop 428 vẫn chạy nhưng không có cửa sổ taskbar OpenCover; khởi động lại helper tạo desktop 429 thì hàng nút xuất hiện lại trên ảnh chụp HDMI. Bản cũ chỉ gọi `addView` một lần và bỏ qua lỗi.

Bản sửa ghi lỗi đăng ký cửa sổ, thử lại theo vòng kiểm tra hiện có khi chưa gắn được, và tạo window context mới để lấy lại token trợ năng của display. Khi dừng desktop, các callback cũ không được gắn taskbar trở lại. Nếu view bị tháo, vòng kiểm tra gắn lại nó.

Token trợ năng được lấy theo display khi tạo context: [mã nguồn AccessibilityService Android 16](https://github.com/aosp-mirror/platform_frameworks_base/blob/android16-release/core/java/android/accessibilityservice/AccessibilityService.java). Nhật ký của phiên bị mất taskbar không còn lỗi khởi tạo ban đầu, nên chưa xác định được ngoại lệ cụ thể của phiên đó.

## Nút theo từng cửa sổ — 0.2.13

Theo yêu cầu cập nhật, bốn nút `−`, `□/❐`, `▥`, `×` chuyển lên góc phải thanh tiêu đề của từng cửa sổ ứng dụng. Mỗi cụm nút điều khiển đúng task của cửa sổ đó, cập nhật vị trí khi di chuyển/đổi kích thước và mất đi khi cửa sổ thu nhỏ hoặc đóng. Khi cửa sổ phía trước che vùng tiêu đề, cụm nút phía sau được ẩn để không đè lên app phía trước.

Taskbar chỉ hiển thị icon app; giữ icon vẫn mở menu đặt cửa sổ vào nửa trái/phải hoặc một phần ba màn hình. Bấm nút `▥` trên cửa sổ mở menu chia đôi/chia ba và chọn vùng cho chính cửa sổ đó.

Kiểm tra trên Z Flip5: bấm nút của Máy tính phóng to từ `[27,27,718,1357]` tới `[0,0,2160,1384]`, nút đổi `❐` và khôi phục đúng vùng cũ. Kéo thanh tiêu đề tới `[107,90,798,1420]`, cụm nút chuyển tương ứng tới `[606,90,798,130]`. Menu `▥` chia Máy tính/Chrome/Cài đặt thành ba cột rộng 720 px, mỗi cửa sổ có cụm nút riêng. 312 kiểm thử JVM và build release/lint đạt.
