# Kiến trúc và hướng dẫn phát triển

Tài liệu dành cho lập trình viên tiếp nhận hoặc mở rộng dự án.

## 1. Công nghệ

| Thành phần | Lựa chọn | Lý do |
|---|---|---|
| Ngôn ngữ, runtime | Java 21 (LTS) | Hỗ trợ dài hạn, `jpackage` có sẵn trong JDK |
| Giao diện | JavaFX 21, FXML + CSS | Giữ hướng của mã nguồn gốc, bố cục co giãn, in ấn qua `javafx.print` |
| Cơ sở dữ liệu | SQLite qua `sqlite-jdbc` | Một file, không cần máy chủ, hợp với quán một máy tính |
| Build | Maven 3.9 qua Maven Wrapper | Không cần cài Maven; `maven-shade-plugin` tạo fat jar |
| Đóng gói | `jpackage` (JDK) | Tạo `.msi`/`.dmg` kèm runtime, khách không cần cài Java |
| Kiểm thử | JUnit 5 | `./mvnw test` |
| Băm mật khẩu | PBKDF2WithHmacSHA256 có salt (JDK chuẩn) | Không cần thư viện ngoài |

Ứng dụng là **non-modular** (không có `module-info.java`): JavaFX được nhúng vào fat jar theo nền tảng và nạp từ classpath. Vì vậy điểm vào của jar là `com.tiemcaphe.Launcher` (lớp thường, gọi `App.main`) thay vì lớp kế thừa `Application`, tránh lỗi "JavaFX runtime components are missing" khi chạy `java -jar`.

## 2. Bố cục mã nguồn

```
src/main/java/com/tiemcaphe/
  App.java            Lớp JavaFX Application: khởi tạo CSDL, mở màn đăng nhập
  Launcher.java       Điểm vào cho fat jar / jpackage (gọi App.main)
  config/             AppConfig: thư mục dữ liệu, file CSDL, log; đọc -Dtiemcaphe.home, TIEMCAPHE_HOME, -Dtiemcaphe.db
  db/                 Database (mở kết nối SQLite, chạy schema.sql, PRAGMA user_version, transaction dùng chung),
                      Sql (tiện ích JDBC), DataAccessException
  model/              Record thuần: Account, Role, Category, Product, Ingredient, RecipeLine, Order, OrderLine,
                      OrderStatus, PaymentMethod, CartLine, StockEntry, StockEntryType, ShopInfo,
                      DashboardSummary, DailyRevenue, ProductSales, StaffRevenue
  repository/         Một lớp cho mỗi bảng/nhóm truy vấn: Account, Category, Product, Ingredient, Recipe, Order,
                      Stock, Settings, Statistics. Mọi SQL nằm ở đây, dùng PreparedStatement
  service/            AppContext (khởi tạo repository/service), AuthService, CatalogService, InventoryService,
                      OrderService (thanh toán, hủy đơn trong một transaction), BackupService, SeedService (dữ liệu
                      khởi tạo lần đầu), DemoDataService (--demo), BusinessException (lỗi hiển thị cho người dùng)
  ui/                 MainController (khung + menu theo vai trò, ViewId), controller từng màn hình, Fx/Dialogs/Icons/
                      Widgets/Charts (tiện ích), ui/dialog/ (hóa đơn + in, định lượng, số lượng, danh mục, lịch sử kho)
  util/               Money (định dạng VNĐ), Dates, Images (đọc/thu nhỏ ảnh), PasswordHasher (PBKDF2), Session, Texts (tìm không dấu)
src/main/resources/
  fxml/               Giao diện từng màn hình
  css/                Giao diện chung
  img/                logo.png, ảnh món mẫu
  db/schema.sql       Script tạo bảng, có chú thích từng bảng
src/test/java/        Kiểm thử đơn vị
```

Nguyên tắc phân lớp:

- **Controller (ui)** chỉ lo hiển thị và thu thập dữ liệu nhập, gọi service/repository, hiển thị thông báo. Không viết SQL trong controller.
- **Repository** nhận và trả về model, dùng `PreparedStatement` cho mọi truy vấn có tham số, một lớp cho một bảng hoặc một nhóm truy vấn thống kê.
- **Service** gom các bước ghi của một nghiệp vụ vào **một transaction**: thanh toán (tạo đơn + chi tiết + trừ kho theo định lượng + ghi biến động kho), hủy đơn (đổi trạng thái + hoàn kho + ghi biến động). Nhờ vậy không bao giờ có đơn thiếu chi tiết hay kho bị trừ hai lần.

## 3. Dữ liệu

File: `~/TiemCaPhe/tiemcaphe.db`. Lúc khởi động, `Database` chạy `src/main/resources/db/schema.sql` (toàn bộ là `CREATE ... IF NOT EXISTS`) rồi ghi `PRAGMA user_version = 1`; nếu CSDL mới thì chèn dữ liệu khởi tạo (2 tài khoản, danh mục, món mẫu có ảnh, nguyên liệu mẫu). Tham số `--demo` sinh thêm đơn hàng mẫu 30 ngày gần nhất.

| Bảng | Nội dung chính |
|---|---|
| `cai_dat` | `khoa` → `gia_tri`: `ten_quan`, `dia_chi`, `dien_thoai`, `loi_cam_on` |
| `tai_khoan` | `ten_dang_nhap` (duy nhất, không phân biệt hoa thường), `mat_khau` (đã băm), `ho_ten`, `vai_tro` ∈ {`ADMIN`, `NHAN_VIEN`}, `hoat_dong`, `ngay_tao` |
| `danh_muc` | `ten` duy nhất, `thu_tu` để sắp xếp |
| `mon` | `ten` duy nhất, `danh_muc_id` (xóa danh mục → NULL), `gia` (số nguyên VNĐ), `con_ban`, `hinh` BLOB, ngày tạo/cập nhật |
| `dinh_luong` | khóa (`mon_id`, `nguyen_lieu_id`), `so_luong` dùng cho một phần; xóa món hoặc nguyên liệu thì xóa theo |
| `nguyen_lieu` | `ten` duy nhất, `so_luong` tồn (REAL), `don_vi`, `nguong_canh_bao`, `hinh`, `ngay_cap_nhat` |
| `don_hang` | `ma_don` dạng `HDyyyyMMdd-0001` (duy nhất), `tai_khoan_id` + `ten_nhan_vien` (snapshot), `ban`, `ghi_chu`, `tong_tien`, `giam_gia`, `thanh_tien`, `khach_dua`, `tien_thua`, `phuong_thuc` ∈ {`TIEN_MAT`, `CHUYEN_KHOAN`}, `trang_thai` ∈ {`DA_THANH_TOAN`, `DA_HUY`}, `ly_do_huy`, `ngay_tao`; index theo ngày và trạng thái |
| `chi_tiet_don_hang` | `don_hang_id` (xóa đơn → xóa theo), `mon_id` (xóa món → NULL), **`ten_mon`, `don_gia` snapshot**, `so_luong`, `thanh_tien` |
| `bien_dong_kho` | `nguyen_lieu_id`, `so_luong` (+/−), `loai` ∈ {`NHAP`, `XUAT`, `DIEU_CHINH`, `BAN_HANG`, `HUY_DON`}, `ghi_chu`, người thực hiện (id + tên snapshot), `don_hang_id` (đơn liên quan với BAN_HANG/HUY_DON, NULL khi đơn đã xóa), `ngay_tao` |

Quyết định thiết kế đáng lưu ý:

- Chi tiết đơn và đơn hàng lưu snapshot tên món, đơn giá, tên nhân viên; đổi giá, xóa món hay khóa tài khoản không làm sai lịch sử.
- Hủy đơn là đổi `trang_thai` sang `DA_HUY` kèm `ly_do_huy`, không xóa dòng, để đối soát. Doanh thu chỉ tính `DA_THANH_TOAN`. Hoàn kho khi hủy dựa trên các dòng `bien_dong_kho` loại `BAN_HANG` của chính đơn đó, nên luôn đúng bằng lượng đã trừ dù định lượng hay món đã đổi/xóa sau này. "Xóa toàn bộ đơn hàng" chỉ xóa đơn, giữ lịch sử kho.
- Tiền tệ lưu `INTEGER` VNĐ, hiển thị dạng `25.000 đ` (`util/Money`).
- Thời gian lưu `TEXT` định dạng `yyyy-MM-dd HH:mm:ss` theo giờ máy (`util/Dates`); so sánh khoảng ngày bằng chuỗi nên luôn dùng đúng định dạng này.
- Mã đơn `HDyyyyMMdd-NNNN` đếm lại từ 0001 mỗi ngày, in trên hóa đơn cho khách và nhân viên tra cứu.
- Kết nối mở qua `SQLiteConfig` với `enforceForeignKeys(true)` (SQLite mặc định tắt khóa ngoại), `busyTimeout` 5 giây và journal mode **WAL**. WAL nghĩa là khi ứng dụng đang chạy có thể thấy thêm file `tiemcaphe.db-wal` và `tiemcaphe.db-shm` cạnh file CSDL; đóng ứng dụng đúng cách thì chúng được gộp lại.
- Xóa tài khoản vẫn được (`don_hang.tai_khoan_id` → NULL, tên nhân viên đã snapshot) nhưng khuyến nghị khóa bằng `hoat_dong = 0`. Không cho xóa tài khoản đang đăng nhập, không cho khóa/hạ quyền/xóa quản trị cuối cùng.
- Kho được phép âm (không chặn bán khi định lượng vượt tồn) để không làm gián đoạn thu ngân; màn hình Nguyên liệu và Tổng quan cảnh báo để chủ quán kiểm kê.

Sao lưu dùng `VACUUM INTO '<file>'` nên an toàn ngay khi ứng dụng đang chạy; khôi phục thì sao lưu bản hiện tại, đóng kết nối, chép file nguồn đè lên file CSDL rồi thoát ứng dụng (xem `service/BackupService`). Bản sao đặt trong `~/TiemCaPhe/backup/`.
## 4. Bảo mật

- Mật khẩu băm PBKDF2-HmacSHA256, salt ngẫu nhiên từng tài khoản, lưu cùng số vòng lặp để tăng sau này mà không phá dữ liệu cũ. So sánh theo thời gian hằng định.
- Mọi SQL có dữ liệu người dùng đều dùng tham số; chỉ `LIMIT <số nguyên do code đặt>` và `VACUUM INTO '<đường dẫn đã escape>'` được nối chuỗi. Từ khóa tìm đơn hàng được so phía Java (không dấu) thay vì `LIKE`.
- Không có dịch vụ mạng nào được mở; dữ liệu chỉ nằm trên máy. Bảo vệ máy tính (mật khẩu đăng nhập hệ điều hành, sao lưu) là trách nhiệm của quán.

## 5. Build và phát hành

```bash
./mvnw javafx:run                # chạy dev
./mvnw test                      # kiểm thử
./mvnw -DskipTests package       # target/tiemcaphe.jar (fat jar, nền tảng máy build)
./mvnw -DskipTests package -Djavafx.platform=win   # jar cho Windows từ máy khác
scripts/package-mac.sh           # .dmg (macOS, cần JDK có jmods)
scripts\package-win.bat          # .msi (Windows, cần JDK có jmods + WiX 3)
```

Cơ chế chọn nền tảng JavaFX: `pom.xml` có các profile tự kích hoạt theo hệ điều hành/kiến trúc của máy build, đặt thuộc tính `javafx.platform` làm classifier cho các artifact `org.openjfx:*`. Truyền `-Djavafx.platform=...` để ghi đè.

Quy trình phát hành một phiên bản:

1. Tăng `<version>` trong `pom.xml` (bộ cài Windows dùng số này để nhận bản nâng cấp; `--win-upgrade-uuid` cố định trong `scripts/package-win.bat`).
2. `./mvnw test`.
3. Chạy script đóng gói trên từng hệ điều hành cần phát hành.
4. Cài thử bản mới lên máy đã có bản cũ, kiểm tra dữ liệu còn nguyên và các màn hình mở được.
5. Ghi thay đổi vào mục phát hành (CHANGELOG hoặc trang phát hành của repo).

Khi cần đổi schema ở phiên bản sau: đọc `PRAGMA user_version` lúc khởi động (hiện là 1), chạy các bước migration tăng dần (`ALTER TABLE ...`) rồi ghi số mới; `schema.sql` chỉ thêm `IF NOT EXISTS`, không sửa bảng cũ theo cách làm hỏng CSDL đang có của khách.

## 6. Mở rộng thường gặp

- **Thêm màn hình**: tạo `fxml/<ten>.fxml` + controller trong `ui/`, đăng ký vào menu bên trái theo vai trò, viết repository nếu cần bảng mới.
- **Thêm trường cho món/đơn**: thêm cột qua migration, cập nhật model, repository, FXML. Nhớ snapshot vào chi tiết đơn nếu trường ảnh hưởng hóa đơn.
- **Đổi mẫu hóa đơn**: sửa phần dựng node in trong `ui/` (in bằng `javafx.print.PrinterJob`, node được dựng theo chiều rộng 80 mm).
- **Nhiều máy dùng chung dữ liệu**: SQLite trên thư mục mạng không an toàn khi ghi đồng thời. Hướng đi hợp lý là tách repository ra sau một API (ví dụ Spring Boot + PostgreSQL) và giữ nguyên lớp ui; vì mọi SQL đã nằm trong `repository/`, phần giao diện gần như không đổi.

## 7. Nguồn gốc và thay đổi so với mã gốc

Mã gốc (commit đầu của repo) là đồ án Eclipse: JavaFX + SQL Server (`jdbc:sqlserver://THANHPHUC;databaseName=TEST;integratedSecurity=true`), không có script schema, SQL viết trực tiếp trong hai controller lớn (`ControllerAdmin` 1.300 dòng, `SampleController`), băm mật khẩu khác nhau giữa lúc tạo (hex) và lúc đăng nhập (byte thô), thanh toán chỉ ghi tổng tiền. Bản này viết lại toàn bộ theo bố cục trên, giữ lại ý tưởng màn hình (thanh bên trái, lưới món, bảng giỏ hàng) và bộ ảnh mẫu.
