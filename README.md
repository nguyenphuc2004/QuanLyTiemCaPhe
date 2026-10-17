# Quản lý tiệm cà phê

Phần mềm bán hàng và quản lý dành cho tiệm cà phê, quán nước, quán ăn nhỏ. Chạy trên máy tính Windows hoặc macOS, **không cần máy chủ, không cần cài cơ sở dữ liệu**: toàn bộ dữ liệu nằm trong một file SQLite trên máy của quán.

Ứng dụng viết bằng Java 21 + JavaFX, đóng gói thành file cài đặt (`.msi` cho Windows, `.dmg` cho macOS) kèm sẵn môi trường chạy, nên máy khách không cần cài Java.

## Tính năng

**Bán hàng (thu ngân)**
- Chọn món theo danh mục, tìm kiếm nhanh, giỏ hàng tăng/giảm/xóa từng món.
- Ghi số bàn, ghi chú, giảm giá; thanh toán tiền mặt hoặc chuyển khoản; nhập tiền khách đưa, tự tính tiền thừa.
- In hóa đơn qua máy in của hệ điều hành (hỗ trợ máy in hóa đơn khổ 80 mm). In lại hóa đơn bất kỳ lúc nào.

**Quản lý (chủ quán)**
- Tổng quan: doanh thu hôm nay và tháng này, số đơn, biểu đồ 7 ngày, món bán chạy, nguyên liệu sắp hết.
- Thực đơn: thêm/sửa/xóa món, ảnh món, danh mục, bật/tắt còn bán, định lượng nguyên liệu cho từng món.
- Nguyên liệu: tồn kho, đơn vị tính, ngưỡng cảnh báo, nhập kho / xuất kho / kiểm kê, lịch sử kho; tự trừ kho khi bán và hoàn kho khi hủy đơn.
- Đơn hàng: lọc theo ngày, trạng thái, nhân viên, mã đơn; xem chi tiết; in lại hóa đơn; hủy đơn có lý do.
- Thống kê: theo ngày, theo tháng, món bán chạy, theo nhân viên, theo hình thức thanh toán; xuất CSV mở bằng Excel.
- Tài khoản: hai vai trò Quản trị và Nhân viên; thêm, sửa, khóa, xóa, đặt lại mật khẩu; luôn giữ ít nhất một Quản trị.
- Cài đặt: thông tin quán in trên hóa đơn, đổi mật khẩu, sao lưu và khôi phục dữ liệu, tạo đơn hàng mẫu hoặc xóa toàn bộ đơn hàng.

**An toàn dữ liệu**
- Mật khẩu băm PBKDF2-SHA256 có salt, không lưu dạng rõ.
- Dữ liệu là một file duy nhất `~/TiemCaPhe/tiemcaphe.db`, sao lưu bằng một nút bấm trong Cài đặt.

## Tài liệu

| Dành cho | Tài liệu |
|---|---|
| Chủ quán, nhân viên | [docs/HUONG_DAN_SU_DUNG.md](docs/HUONG_DAN_SU_DUNG.md) |
| Người cài đặt, kỹ thuật viên | [docs/HUONG_DAN_CAI_DAT.md](docs/HUONG_DAN_CAI_DAT.md) |
| Lập trình viên | [docs/KIEN_TRUC.md](docs/KIEN_TRUC.md) |

## Chạy nhanh

Yêu cầu khi build từ mã nguồn: JDK 21 trở lên. Không cần cài Maven (đã có Maven Wrapper) và không cần cài JavaFX riêng.

```bash
# Chạy thử ở chế độ phát triển
./mvnw javafx:run            # macOS / Linux
mvnw.cmd javafx:run          # Windows

# Build file jar chạy độc lập cho máy đang build
./mvnw -DskipTests package
java -jar target/tiemcaphe.jar

# Tạo file cài đặt (cần JDK đầy đủ có thư mục jmods, xem docs/HUONG_DAN_CAI_DAT.md)
scripts/package-mac.sh       # macOS  -> target/installer/*.dmg
scripts\package-win.bat      # Windows -> target\installer\*.msi
```

Tài khoản đăng nhập lần đầu:

| Tên đăng nhập | Mật khẩu | Vai trò |
|---|---|---|
| `admin` | `admin123` | Quản trị |
| `nhanvien` | `123456` | Nhân viên |

**Đổi cả hai mật khẩu ngay sau khi cài** (mục Cài đặt hoặc Tài khoản).

Thêm tham số `--demo` khi chạy để sinh đơn hàng mẫu 30 ngày gần nhất phục vụ trình diễn, ví dụ `scripts/run-dev.sh --demo` hoặc `java -jar target/tiemcaphe.jar --demo`. Dữ liệu mẫu có thể xóa trong Cài đặt.

## Dữ liệu và cấu hình

| Mục | Mặc định | Ghi đè |
|---|---|---|
| Thư mục dữ liệu | `~/TiemCaPhe` (Windows: `C:\Users\<tên>\TiemCaPhe`) | `-Dtiemcaphe.home=<thư mục>` hoặc biến môi trường `TIEMCAPHE_HOME` |
| File cơ sở dữ liệu | `<thư mục dữ liệu>/tiemcaphe.db` | `-Dtiemcaphe.db=<file>` |
| Nhật ký lỗi | `<thư mục dữ liệu>/logs/app.0.log` (xoay vòng 3 file x 1 MB) | |
| Bản sao lưu | `<thư mục dữ liệu>/backup/` | |

## Cấu trúc thư mục

```
pom.xml                 Cấu hình Maven (JavaFX 21, sqlite-jdbc, shade plugin tạo fat jar)
mvnw, mvnw.cmd, .mvn/   Maven Wrapper, tự tải Maven khi chạy lần đầu
src/main/java/          Mã nguồn, package gốc com.tiemcaphe
src/main/resources/     FXML, CSS, ảnh, script SQL
src/test/java/          Kiểm thử JUnit 5
scripts/                Script chạy dev, build jar, đóng gói .dmg/.msi, tạo icon
packaging/              Icon ứng dụng cho trình cài đặt (icon.icns, icon.ico)
docs/                   Tài liệu
```

## Nguồn gốc

Dự án bắt nguồn từ đồ án môn học [nguyenphuc2004/QuanLyTiemCaPhe](https://github.com/nguyenphuc2004/QuanLyTiemCaPhe) (JavaFX + SQL Server, project Eclipse). Bản này được tái cấu trúc để có thể triển khai cho khách hàng thật:

- Chuyển từ SQL Server dùng Windows Authentication trên một máy cố định sang SQLite nhúng, không cần cài máy chủ.
- Chuyển từ project Eclipse sang Maven, có Maven Wrapper và script đóng gói trình cài đặt.
- Sửa lỗi nghiệp vụ: băm mật khẩu không nhất quán khiến tài khoản mới không đăng nhập được, thanh toán không lưu chi tiết món, ngày thống kê viết cứng, biểu đồ dữ liệu giả, lỗi nạp giao diện trên hệ máy phân biệt chữ hoa thường.
- Hoàn thiện nghiệp vụ bán hàng: giỏ hàng, giảm giá, tiền thừa, in hóa đơn, hủy đơn, trừ kho theo định lượng, thống kê, sao lưu.
- Toàn bộ giao diện tiếng Việt, bố cục co giãn theo cửa sổ.

Mã nguồn gốc vẫn xem được trong lịch sử git (commit đầu tiên).
