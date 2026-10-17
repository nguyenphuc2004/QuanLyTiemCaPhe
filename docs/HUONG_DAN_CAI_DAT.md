# Hướng dẫn cài đặt và triển khai

Tài liệu dành cho người cài phần mềm cho quán (kỹ thuật viên hoặc chủ quán có kiến thức máy tính cơ bản). Phần build từ mã nguồn ở cuối dành cho lập trình viên.

## 1. Yêu cầu máy tính

| Hạng mục | Yêu cầu |
|---|---|
| Hệ điều hành | Windows 10/11 64-bit, hoặc macOS 12 trở lên (Intel hoặc Apple Silicon) |
| Bộ nhớ RAM | 4 GB trở lên |
| Ổ đĩa trống | 500 MB |
| Màn hình | Tối thiểu 1280 x 720 |
| Máy in hóa đơn | Tùy chọn. Máy in nhiệt khổ 80 mm hoặc máy in A4 đã cài driver và in thử được từ Windows/macOS |
| Mạng | Không cần. Phần mềm chạy hoàn toàn trên máy |

Mỗi bản cài dùng cho **một máy tính**. Dữ liệu nằm trên máy đó. Nếu quán có nhiều máy thu ngân cần dùng chung dữ liệu, liên hệ đơn vị triển khai; phiên bản này không hỗ trợ chia sẻ file dữ liệu qua mạng.

## 2. Cài đặt từ file cài đặt (khuyến nghị)

File cài đặt đã kèm môi trường chạy Java, máy khách **không cần cài Java**.

### 2.1 Windows (`QuanLyTiemCaPhe-<phiên bản>.msi`)

1. Chạy file `.msi`. Nếu Windows SmartScreen cảnh báo "ứng dụng không xác định", chọn **Thông tin thêm** rồi **Vẫn chạy** (bộ cài chưa ký số).
2. Chọn thư mục cài hoặc để mặc định, bấm **Next** đến khi hoàn tất.
3. Biểu tượng xuất hiện trên Desktop và trong Start Menu, nhóm **Tiem Ca Phe**.
4. Mở ứng dụng, đăng nhập bằng tài khoản mặc định ở mục 4.

Nâng cấp phiên bản mới: chạy file `.msi` mới, bộ cài tự thay bản cũ, dữ liệu giữ nguyên. Nên sao lưu trước (xem mục 5).

Gỡ cài đặt: **Settings → Apps → QuanLyTiemCaPhe → Uninstall**. Thư mục dữ liệu `C:\Users\<tên>\TiemCaPhe` không bị xóa, muốn xóa hẳn thì xóa tay.

### 2.2 macOS (`QuanLyTiemCaPhe-<phiên bản>.dmg`)

1. Mở file `.dmg`, kéo `QuanLyTiemCaPhe` vào thư mục **Applications**.
2. Lần đầu mở, macOS có thể báo "không thể mở vì từ nhà phát triển không xác định". Bấm chuột phải vào ứng dụng, chọn **Open**, rồi **Open** lần nữa. Nếu vẫn bị chặn, mở Terminal và chạy:

   ```bash
   xattr -dr com.apple.quarantine /Applications/QuanLyTiemCaPhe.app
   ```

3. Gỡ: kéo ứng dụng vào Thùng rác. Thư mục dữ liệu `~/TiemCaPhe` giữ lại.

## 3. Cài đặt bằng file jar (khi không có file cài đặt)

Dùng khi chỉ có file `tiemcaphe.jar`, ví dụ trên Linux hoặc khi muốn cài nhanh để thử.

1. Cài Java 21 trở lên. Khuyên dùng Temurin: https://adoptium.net (chọn JRE hoặc JDK 21, đúng hệ điều hành).
2. Lưu ý file jar **đi theo nền tảng**: jar build cho Windows không chạy trên macOS và ngược lại (vì JavaFX được nhúng sẵn theo nền tảng). Hỏi người build để lấy đúng file.
3. Chạy:

   ```bash
   java -jar tiemcaphe.jar
   ```

   Trên Windows, để không hiện cửa sổ đen, tạo shortcut với lệnh `javaw -jar "C:\duong\dan\tiemcaphe.jar"`.

## 4. Lần đầu khởi động

Lần chạy đầu ứng dụng tự tạo cơ sở dữ liệu với:

- Hai tài khoản: `admin` / `admin123` (Quản trị) và `nhanvien` / `123456` (Nhân viên).
- 4 danh mục, 11 món mẫu (9 món có ảnh), 12 nguyên liệu mẫu kèm định lượng cho từng món. Không có đơn hàng mẫu.

Việc cần làm ngay sau khi cài, theo thứ tự:

1. Đăng nhập `admin`, vào **Cài đặt**, đổi mật khẩu quản trị.
2. Vào **Tài khoản**, đổi mật khẩu hoặc khóa tài khoản `nhanvien` mẫu, tạo tài khoản cho nhân viên thật.
3. Vào **Cài đặt**, nhập tên quán, địa chỉ, số điện thoại, lời cảm ơn in trên hóa đơn.
4. Vào **Thực đơn**, sửa hoặc xóa món mẫu, nhập món của quán.
5. Vào **Nguyên liệu**, nhập danh sách nguyên liệu và tồn đầu kỳ.
6. In thử một hóa đơn để kiểm tra máy in.

Muốn trình diễn cho khách xem báo cáo có số liệu, chạy ứng dụng với tham số `--demo` (sinh đơn hàng mẫu 30 ngày gần nhất) hoặc bấm **Tạo đơn hàng mẫu** trong Cài đặt. Trước khi giao cho quán dùng thật, bấm **Xóa toàn bộ đơn hàng** (cũng trong Cài đặt, phải gõ XOA để xác nhận).

## 5. Dữ liệu, sao lưu, khôi phục

| Mục | Vị trí mặc định |
|---|---|
| Thư mục dữ liệu | Windows: `C:\Users\<tên người dùng>\TiemCaPhe`. macOS/Linux: `~/TiemCaPhe` |
| Cơ sở dữ liệu | `TiemCaPhe/tiemcaphe.db` (một file duy nhất, định dạng SQLite) |
| Nhật ký lỗi | `TiemCaPhe/logs/app.0.log` (file mới nhất; xoay vòng 3 file) |
| Bản sao lưu | `TiemCaPhe/backup/` |

Sao lưu:
- Trong ứng dụng: **Cài đặt → Sao lưu ngay** (vào thư mục `backup`) hoặc **Sao lưu vào...** (tự chọn nơi lưu, ví dụ USB). Sao lưu được ngay khi ứng dụng đang chạy. Nên làm cuối mỗi ngày hoặc ít nhất mỗi tuần, rồi chép thư mục `backup` ra USB hoặc Google Drive/OneDrive.
- Bằng tay: **tắt ứng dụng** rồi chép file `tiemcaphe.db` ra nơi khác. Nếu thấy thêm file `tiemcaphe.db-wal` hoặc `tiemcaphe.db-shm` là ứng dụng chưa tắt hẳn; hãy tắt rồi chép, hoặc chép cả ba file.

Khôi phục:
- Trong ứng dụng: **Cài đặt → Khôi phục từ file sao lưu...**, chọn file. Dữ liệu hiện tại được tự sao lưu vào `backup/truoc-khi-khoi-phuc-*.db` trước khi thay, sau đó ứng dụng tự thoát, mở lại là xong.
- Bằng tay: tắt ứng dụng, thay file `tiemcaphe.db` bằng bản sao lưu, mở lại.

Đổi vị trí lưu dữ liệu (ví dụ sang ổ D): đặt biến môi trường `TIEMCAPHE_HOME=D:\TiemCaPhe` trước khi chạy, hoặc thêm tham số JVM `-Dtiemcaphe.home=D:\TiemCaPhe`. Chỉ đổi file CSDL: `-Dtiemcaphe.db=D:\du-lieu\quan.db`. Với bản cài `.msi`/`.dmg`, cách đơn giản nhất là đặt biến môi trường của người dùng Windows (Settings → System → About → Advanced system settings → Environment Variables).

Chuyển sang máy mới: cài ứng dụng trên máy mới, chép file `tiemcaphe.db` từ máy cũ vào thư mục dữ liệu của máy mới (khi ứng dụng đã tắt).

## 6. Sự cố thường gặp khi cài

| Hiện tượng | Cách xử lý |
|---|---|
| Windows báo SmartScreen chặn | Thông tin thêm → Vẫn chạy. Bộ cài chưa có chữ ký số |
| macOS báo ứng dụng "bị hỏng" hoặc "không xác định" | Chuột phải → Open, hoặc chạy lệnh `xattr` ở mục 2.2 |
| Chạy jar báo `JavaFX runtime components are missing` | Chạy bằng `java -jar tiemcaphe.jar` (không chạy bằng class trực tiếp), và dùng đúng jar cho hệ điều hành |
| Chạy jar báo `UnsupportedClassVersionError` | Java trên máy cũ hơn 21. Cài Temurin 21 |
| Chữ tiếng Việt hiện ô vuông | Máy thiếu font Unicode (hiếm trên Windows 10/11 và macOS). Cài bộ font hệ thống đầy đủ |
| Không in được | Kiểm tra in thử từ Windows/macOS trước. Trong hộp thoại in, chọn đúng máy in hóa đơn và khổ giấy 80 mm |
| Ứng dụng không mở, không báo gì | Xem `TiemCaPhe/logs/app.0.log`, gửi file này cho người hỗ trợ |
| Quên mật khẩu admin | Nếu còn tài khoản Quản trị khác: dùng tài khoản đó đặt lại. Nếu không: khôi phục bản sao lưu có mật khẩu còn nhớ, hoặc liên hệ đơn vị triển khai để can thiệp vào file CSDL |

## 7. Build từ mã nguồn (lập trình viên)

### 7.1 Yêu cầu

- JDK 21 trở lên. Để **chạy và build jar** thì JDK nào cũng được (kể cả JBR của Android Studio/IntelliJ). Để **đóng gói trình cài đặt** cần JDK đầy đủ có thư mục `jmods` (Temurin, Oracle, Zulu đều có; JBR không có).
- Không cần cài Maven: repo có Maven Wrapper (`mvnw`, `mvnw.cmd`), lần chạy đầu tự tải Maven 3.9.9 về `~/.m2`.
- Không cần cài JavaFX: lấy từ Maven Central theo nền tảng của máy build.
- Windows muốn tạo `.msi`: cài thêm WiX Toolset 3.x (https://wixtoolset.org/docs/wix3/) và thêm thư mục `bin` của WiX vào `PATH`.

### 7.2 Lệnh thường dùng

```bash
./mvnw javafx:run                       # chạy dev (Windows: mvnw.cmd javafx:run)
scripts/run-dev.sh --demo               # chạy dev kèm dữ liệu mẫu
./mvnw test                             # chạy kiểm thử
./mvnw -DskipTests package              # build target/tiemcaphe.jar cho máy này
scripts/build-jar.sh win                # build jar cho Windows từ máy khác (hoặc linux | mac | mac-aarch64)
scripts/package-mac.sh                  # tạo .dmg (chạy trên macOS)
scripts\package-win.bat                 # tạo .msi (chạy trên Windows)
scripts/make-icons.sh                   # tạo lại packaging/icon.icns và icon.ico từ src/main/resources/img/logo.png
```

Biến môi trường tùy chọn cho script đóng gói: `VENDOR="Tên công ty"` để ghi tên nhà phát hành vào bộ cài; `JAVA_HOME` để chỉ định JDK.

### 7.3 Lưu ý về nền tảng

- `jpackage` chỉ tạo bộ cài cho hệ điều hành đang chạy: muốn có `.msi` phải build trên Windows, muốn có `.dmg` phải build trên macOS. Trên macOS, `.dmg` build trên máy Apple Silicon chạy trên Apple Silicon; muốn hỗ trợ máy Intel thì build trên máy Intel.
- File jar (`./mvnw package`) có thể build chéo bằng `-Djavafx.platform=<win|linux|mac|mac-aarch64>` vì chỉ cần đổi bộ thư viện JavaFX nhúng vào.
- Phiên bản ứng dụng lấy từ thẻ `<version>` trong `pom.xml`; tăng số này trước khi phát hành để bộ cài Windows nhận là bản nâng cấp.

### 7.4 Lỗi build hay gặp

| Thông báo | Nguyên nhân, cách sửa |
|---|---|
| `Error: Module java.base not found` hoặc `jlink failed` khi jpackage | JDK không có `jmods`. Đặt `JAVA_HOME` sang Temurin JDK |
| `Can not find WiX tools (light.exe, candle.exe)` | Chưa cài WiX 3.x hoặc chưa thêm vào `PATH` |
| `mvnw: Permission denied` trên macOS/Linux | `chmod +x mvnw scripts/*.sh` |
| Tải Maven hoặc thư viện thất bại | Máy build cần Internet lần đầu. Nếu dùng proxy, cấu hình trong `~/.m2/settings.xml` |
| macOS báo "jpackage" cần Xcode Command Line Tools | `xcode-select --install` |
