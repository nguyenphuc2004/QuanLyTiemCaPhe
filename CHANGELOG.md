# Lịch sử phát hành

## 1.0.0 — 17/10/2026

Bản đầu tiên có thể bàn giao cho quán, viết lại từ đồ án gốc (JavaFX + SQL Server, project Eclipse).

Thay đổi nền tảng
- Chuyển sang Maven + Maven Wrapper, Java 21, JavaFX 21 lấy từ Maven Central; không cần cài Maven hay JavaFX SDK.
- Thay SQL Server (Windows Authentication, máy cố định) bằng SQLite nhúng: một file `~/TiemCaPhe/tiemcaphe.db`, tự tạo bảng và dữ liệu khởi tạo lần chạy đầu.
- Đóng gói: fat jar `target/tiemcaphe.jar` và script `jpackage` tạo `.dmg` (macOS) / `.msi` (Windows) kèm runtime.
- Băm mật khẩu PBKDF2-SHA256 có salt (bản gốc băm không nhất quán nên tài khoản mới tạo không đăng nhập được).

Nghiệp vụ
- Bán hàng: giỏ hàng tăng/giảm/xóa, bàn, ghi chú, giảm giá, tiền mặt/chuyển khoản, tiền khách đưa – tiền thừa, mã đơn `HDyyyyMMdd-NNNN`, lưu chi tiết từng món (bản gốc chỉ lưu tổng tiền).
- Hóa đơn: xem trước và in qua máy in hệ thống, in lại từ màn hình Đơn hàng.
- Thực đơn: ảnh món, danh mục, còn bán/ngừng bán, định lượng nguyên liệu cho từng món.
- Kho: nhập kho, xuất kho, kiểm kê, lịch sử biến động, ngưỡng cảnh báo sắp hết; tự trừ kho khi bán và hoàn kho khi hủy đơn.
- Đơn hàng: lọc theo ngày, trạng thái, nhân viên, từ khóa; hủy đơn có lý do (chỉ Quản trị).
- Thống kê: theo ngày, theo tháng, món bán chạy, theo nhân viên, hình thức thanh toán; xuất CSV. Bản gốc thống kê theo ngày viết cứng và biểu đồ dữ liệu giả.
- Tài khoản: hai vai trò, khóa/mở khóa, đặt lại mật khẩu, không cho mất tài khoản Quản trị cuối cùng.
- Cài đặt: thông tin quán in trên hóa đơn, đổi mật khẩu, sao lưu/khôi phục, tạo và xóa đơn hàng mẫu (`--demo`).
- Toàn bộ giao diện tiếng Việt, bố cục co giãn theo cửa sổ, ghi log ra `~/TiemCaPhe/logs/`.
