# Hướng dẫn sử dụng

Tài liệu dành cho chủ quán và nhân viên. Không cần kiến thức máy tính chuyên sâu.

## 1. Đăng nhập và phân quyền

Mở ứng dụng, nhập tên đăng nhập và mật khẩu. Có nút hiện/ẩn mật khẩu để kiểm tra khi gõ. Tài khoản bị khóa sẽ không đăng nhập được, kể cả đúng mật khẩu.

Có hai vai trò:

| Vai trò | Được làm gì |
|---|---|
| **Quản trị** (chủ quán, quản lý) | Toàn bộ: Tổng quan, Bán hàng, Thực đơn, Nguyên liệu, Đơn hàng, Thống kê, Tài khoản, Cài đặt |
| **Nhân viên** (thu ngân, phục vụ) | Bán hàng; xem và in lại Đơn hàng (không hủy đơn); xem Nguyên liệu và nhập kho; đổi mật khẩu của mình |

Lần đầu cài, phần mềm có sẵn hai tài khoản `admin` / `admin123` và `nhanvien` / `123456`. **Hãy đổi mật khẩu ngay** (xem mục 9) vì ai cũng có thể đọc được tài liệu này.

Đăng xuất bằng nút ở thanh bên trái. Nên đăng xuất khi đổi ca để đơn hàng ghi đúng tên người bán.

## 2. Thiết lập lần đầu (làm một lần, theo thứ tự)

1. **Cài đặt** → đổi mật khẩu `admin`.
2. **Tài khoản** → đổi mật khẩu hoặc khóa tài khoản `nhanvien` mẫu; tạo tài khoản cho từng nhân viên thật (mỗi người một tài khoản để biết ai bán đơn nào).
3. **Cài đặt** → nhập tên quán, địa chỉ, số điện thoại và lời cảm ơn. Các thông tin này in ở đầu và cuối hóa đơn.
4. **Thực đơn** → xem danh mục và món mẫu. Sửa lại cho đúng quán: xóa món không bán, thêm món mới, đặt giá, gắn ảnh (ảnh giúp thu ngân chọn nhanh).
5. **Nguyên liệu** → nhập danh sách nguyên liệu quán theo dõi (cà phê, sữa, đường, ly, ...), đơn vị tính và ngưỡng cảnh báo sắp hết. Nhập kho số lượng hiện có.
6. (Tùy chọn) **Thực đơn** → với từng món, khai báo định lượng nguyên liệu (ví dụ 1 ly cà phê sữa dùng 20 g cà phê, 30 ml sữa). Có định lượng, phần mềm tự trừ kho mỗi lần bán và cảnh báo khi sắp hết.
7. Bán thử một đơn và in hóa đơn để kiểm tra máy in. Sau đó vào **Đơn hàng** hủy đơn thử đó.

## 3. Bán hàng (màn hình thu ngân)

Đây là màn hình dùng nhiều nhất trong ngày.

1. **Chọn món**: bấm vào danh mục để lọc, hoặc gõ tên vào ô tìm kiếm. Bấm vào món để thêm vào giỏ. Bấm lại để tăng số lượng.
2. **Sửa giỏ hàng**: ở bảng giỏ hàng bên phải, dùng nút tăng/giảm số lượng hoặc xóa món. Có nút xóa toàn bộ giỏ để bắt đầu lại.
3. **Thông tin đơn**: nhập số bàn (nếu quán có bàn), ghi chú (ít đường, mang đi, ...), giảm giá nếu có.
4. **Thanh toán**: chọn **Tiền mặt** hoặc **Chuyển khoản**. Với tiền mặt, nhập số tiền khách đưa, phần mềm hiện tiền thừa phải trả lại.
5. Bấm **Thanh toán**. Đơn được lưu, kho được trừ theo định lượng (nếu có), và hóa đơn hiện ra để xem trước.
6. **In hóa đơn**: bấm nút **In hóa đơn** trên hóa đơn xem trước, hộp thoại in của Windows/macOS hiện ra, chọn máy in hóa đơn rồi in. Nếu quán không in, bấm **Đóng**.

Mẹo:
- Món đã **ngừng bán** (tắt "còn bán" trong Thực đơn) sẽ không hiện ở màn hình bán hàng, không cần xóa món khi hết hàng tạm thời.
- Nếu bấm nhầm Thanh toán, vào **Đơn hàng** để quản trị hủy đơn, kho sẽ được hoàn lại.

## 4. Đơn hàng

Xem lại các đơn đã bán.

- Lọc theo **ngày**, **trạng thái** (đã thanh toán / đã hủy), **nhân viên**, hoặc gõ **mã đơn**.
- Bấm vào một đơn để xem chi tiết từng món, số lượng, đơn giá, giảm giá, tổng tiền, hình thức thanh toán.
- **In lại** hóa đơn khi khách cần.
- **Hủy đơn** (chỉ Quản trị): phải nhập lý do. Đơn chuyển sang trạng thái đã hủy, không tính vào doanh thu, nguyên liệu được hoàn về kho đúng bằng lượng đã trừ lúc bán (kể cả khi định lượng đã đổi sau đó). Đơn không bị xóa khỏi lịch sử để còn đối soát.

## 5. Thực đơn

- **Danh mục**: nhóm món như Cà phê, Trà, Đồ ăn. Nút **Quản lý danh mục** để thêm, đổi tên, xóa. Xóa danh mục đang có món thì các món đó chuyển thành "Chưa phân loại", không mất món.
- **Món**: tên, danh mục, giá bán, ảnh, trạng thái còn bán. Chọn món trong bảng để sửa; dùng nút xóa để bỏ hẳn. Món đã có trong đơn hàng cũ vẫn giữ nguyên tên và giá trên các đơn đó.
- **Ảnh món**: chọn file ảnh từ máy (jpg, png). Ảnh được lưu vào dữ liệu của phần mềm, không phụ thuộc file gốc.
- **Định lượng nguyên liệu**: chọn món trong bảng, bấm **Định lượng nguyên liệu...**, thêm từng nguyên liệu và lượng dùng cho một phần (ví dụ 0,025 kg bột cà phê). Mỗi lần bán N phần, kho trừ N lần định lượng.

## 6. Nguyên liệu và kho

- Bảng tồn kho: tên, số lượng còn, đơn vị, ngưỡng cảnh báo, lần cập nhật cuối. Dòng có tồn bằng hoặc dưới ngưỡng cảnh báo được tô đỏ và hiện ở Tổng quan.
- **Thêm/sửa/xóa nguyên liệu** (Quản trị). Đơn vị tính tự do: g, kg, ml, lít, cái, gói, ...
- **Nhập kho** (Quản trị và Nhân viên): chọn nguyên liệu trong bảng, bấm **Nhập kho**, nhập số lượng thêm và ghi chú (nhà cung cấp, giá nhập, ...). Mỗi lần nhập được ghi vào **lịch sử kho**.
- **Xuất kho** (Quản trị): ghi nhận hao hụt, hỏng, dùng nội bộ.
- **Kiểm kê** (Quản trị): nhập số lượng thực tế đếm được, phần mềm tự ghi chênh lệch vào lịch sử.
- **Lịch sử kho**: xem các lần nhập, xuất, trừ do bán, hoàn do hủy đơn, kiểm kê. Dùng để đối soát khi số thực tế lệch số trên phần mềm.
- Tích **Chỉ hiện sắp hết** để xem nhanh những gì cần mua thêm.
- Khi nhập số lượng: phần lẻ dùng dấu phẩy (1,5 kg); dấu chấm được hiểu là nhóm hàng nghìn (1.500 g = 1500 g).

## 7. Tổng quan và Thống kê (Quản trị)

**Tổng quan** hiện ngay khi đăng nhập: doanh thu hôm nay, doanh thu tháng này, số đơn, biểu đồ doanh thu 7 ngày gần nhất, món bán chạy, nguyên liệu sắp hết.

**Thống kê**: chọn khoảng thời gian rồi xem:
- Doanh thu theo ngày, theo tháng.
- Món bán chạy: số lượng và doanh thu từng món.
- Theo nhân viên: số đơn và doanh thu mỗi người bán.
- Hình thức thanh toán: tiền mặt và chuyển khoản chiếm bao nhiêu.
- **Xuất CSV**: lưu doanh thu theo ngày, món bán chạy và theo nhân viên của khoảng đang xem ra một file, mở bằng Excel hoặc Google Sheets để làm báo cáo riêng. Nếu Excel hiện sai dấu tiếng Việt, trong Excel chọn **Data → From Text/CSV** và chọn mã hóa **UTF-8**.

Doanh thu chỉ tính đơn **đã thanh toán**; đơn đã hủy không tính.

## 8. Tài khoản (Quản trị)

- **Thêm** tài khoản: tên đăng nhập (không dấu, không khoảng trắng), họ tên hiển thị, vai trò, mật khẩu ban đầu.
- **Sửa** tên đăng nhập, họ tên, vai trò.
- **Khóa / mở khóa**: nhân viên nghỉ việc thì bỏ tích "Đang hoạt động". Xóa tài khoản vẫn được (đơn cũ giữ tên người bán) nhưng không xóa được tài khoản đang đăng nhập.
- **Đặt lại mật khẩu** khi nhân viên quên.
- Không thể khóa hay hạ quyền tài khoản Quản trị cuối cùng, để quán không bao giờ mất quyền quản trị.

## 9. Cài đặt

- **Thông tin quán**: tên, địa chỉ, số điện thoại, lời cảm ơn. In trên hóa đơn.
- **Đổi mật khẩu** của tài khoản đang đăng nhập (Nhân viên cũng có mục này).
- **Sao lưu ngay**: tạo một bản sao toàn bộ dữ liệu vào thư mục sao lưu (`TiemCaPhe/backup` trong thư mục người dùng). **Sao lưu vào...** để chọn nơi lưu khác, ví dụ USB. Nên sao lưu cuối mỗi ngày.
- **Khôi phục từ file sao lưu...**: quay về dữ liệu tại thời điểm sao lưu. Dữ liệu hiện tại được tự sao lưu trước khi thay; phần mềm thoát sau khi khôi phục, mở lại để dùng.
- **Tạo đơn hàng mẫu** để xem thử báo cáo khi trình diễn; **Xóa toàn bộ đơn hàng** trước khi quán dùng thật (phải gõ XOA để xác nhận; thực đơn, nguyên liệu, tài khoản và lịch sử kho được giữ nguyên).

## 10. Câu hỏi thường gặp

**Khách trả một phần tiền mặt, một phần chuyển khoản?**
Chọn hình thức chiếm phần lớn. Phiên bản này ghi một hình thức cho mỗi đơn; có thể ghi thêm vào ô ghi chú.

**Nhập sai giá món, các đơn cũ có đổi theo không?**
Không. Đơn đã bán giữ giá tại thời điểm bán. Chỉ đơn mới dùng giá mới.

**Hủy đơn rồi muốn khôi phục?**
Không khôi phục được đơn đã hủy. Tạo đơn mới ở màn hình Bán hàng.

**Máy in in ra chữ lỗi hoặc không vừa khổ giấy?**
Trong hộp thoại in, chọn đúng máy in và khổ giấy 80 mm (hoặc A4 nếu dùng máy in thường). Kiểm tra driver máy in bằng cách in thử một trang từ Windows/macOS.

**Hai máy tính trong quán dùng chung được không?**
Phiên bản này dùng dữ liệu trên một máy. Muốn nhiều máy dùng chung, liên hệ đơn vị triển khai.

**Dữ liệu nằm ở đâu, có mất khi cài lại phần mềm?**
Dữ liệu nằm trong thư mục `TiemCaPhe` của người dùng (Windows: `C:\Users\<tên>\TiemCaPhe`), tách khỏi phần mềm. Gỡ hay cài lại phần mềm không xóa dữ liệu. Vẫn nên sao lưu định kỳ để phòng hỏng máy.

**Quên mật khẩu admin?**
Nếu còn tài khoản Quản trị khác, dùng tài khoản đó đặt lại. Nếu không, khôi phục từ bản sao lưu còn nhớ mật khẩu, hoặc liên hệ đơn vị triển khai.
