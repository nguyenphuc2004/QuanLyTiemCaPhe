package com.tiemcaphe.ui;

import com.tiemcaphe.App;
import com.tiemcaphe.config.AppConfig;
import com.tiemcaphe.model.ShopInfo;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.service.BusinessException;
import com.tiemcaphe.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Cài đặt: thông tin quán, đổi mật khẩu, sao lưu / khôi phục, dữ liệu mẫu. */
public class SettingsController implements View {

    @FXML
    private VBox shopCard;
    @FXML
    private TextField txtShopName;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextArea txtFooter;
    @FXML
    private Label lblShopStatus;
    @FXML
    private PasswordField txtOldPassword;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private Label lblPasswordStatus;
    @FXML
    private VBox dataCard;
    @FXML
    private Label lblDbPath;
    @FXML
    private Label lblVersion;
    @FXML
    private Label lblHome;
    @FXML
    private Label lblLog;

    @FXML
    private void initialize() {
        boolean admin = Session.isAdmin();
        shopCard.setVisible(admin);
        shopCard.setManaged(admin);
        dataCard.setVisible(admin);
        dataCard.setManaged(admin);
        lblVersion.setText(AppConfig.APP_NAME + " – phiên bản " + AppConfig.VERSION);
        lblHome.setText("Thư mục dữ liệu: " + AppConfig.homeDir());
        lblLog.setText("File nhật ký: " + AppConfig.logDir().resolve("app.0.log"));
        lblDbPath.setText("File cơ sở dữ liệu: " + AppConfig.dbFile());
    }

    @Override
    public void onShow() {
        try {
            ShopInfo info = AppContext.get().settings().shopInfo();
            txtShopName.setText(info.name());
            txtAddress.setText(info.address());
            txtPhone.setText(info.phone());
            txtFooter.setText(info.receiptFooter());
            lblShopStatus.setText("");
            lblPasswordStatus.setText("");
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void saveShop() {
        try {
            String name = txtShopName.getText() == null ? "" : txtShopName.getText().trim();
            if (name.isEmpty()) {
                throw new BusinessException("Vui lòng nhập tên quán.");
            }
            AppContext.get().settings().saveShopInfo(new ShopInfo(name, txtAddress.getText().trim(), txtPhone.getText().trim(),
                    txtFooter.getText().trim()));
            App.stage().setTitle(name + " - " + AppConfig.APP_NAME);
            lblShopStatus.setText("Đã lưu lúc " + now() + ". Tên quán trên thanh bên cập nhật sau khi đăng nhập lại.");
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void changePassword() {
        try {
            if (!txtNewPassword.getText().equals(txtConfirmPassword.getText())) {
                throw new BusinessException("Mật khẩu mới nhập lại không khớp.");
            }
            AppContext.get().auth().changePassword(Session.require().id(), txtOldPassword.getText(), txtNewPassword.getText());
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            lblPasswordStatus.setText("Đã đổi mật khẩu lúc " + now() + ".");
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void backupNow() {
        try {
            Path p = AppContext.get().backup().backup(AppContext.get().backup().defaultBackupFile());
            Dialogs.info("Sao lưu", "Đã sao lưu dữ liệu vào:\n" + p.toAbsolutePath());
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void backupTo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn nơi lưu bản sao lưu");
        fc.setInitialFileName(AppContext.get().backup().defaultBackupFile().getFileName().toString());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Cơ sở dữ liệu SQLite", "*.db"));
        File f = fc.showSaveDialog(Fx.windowOf(txtShopName));
        if (f == null) {
            return;
        }
        try {
            Path p = AppContext.get().backup().backup(f.toPath());
            Dialogs.info("Sao lưu", "Đã sao lưu dữ liệu vào:\n" + p.toAbsolutePath());
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void restore() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file sao lưu để khôi phục");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Cơ sở dữ liệu SQLite", "*.db"));
        File dir = AppConfig.backupDir().toFile();
        if (dir.isDirectory()) {
            fc.setInitialDirectory(dir);
        }
        File f = fc.showOpenDialog(Fx.windowOf(txtShopName));
        if (f == null) {
            return;
        }
        try {
            AppContext.get().backup().validateBackupFile(f.toPath());
        } catch (Exception e) {
            Dialogs.handle(e);
            return;
        }
        if (!Dialogs.confirmDanger("Khôi phục dữ liệu",
                "Toàn bộ dữ liệu hiện tại sẽ được thay bằng file:\n" + f.getAbsolutePath()
                        + "\n\nDữ liệu hiện tại được tự động sao lưu trước khi thay. Phần mềm sẽ thoát sau khi khôi phục, bạn mở lại để dùng.",
                "Khôi phục và thoát")) {
            return;
        }
        try {
            Path safety = AppContext.get().backup().restore(f.toPath());
            AppContext.reset();
            Dialogs.info("Khôi phục", "Đã khôi phục xong. Bản sao dữ liệu cũ: " + safety.toAbsolutePath()
                    + "\n\nPhần mềm sẽ đóng, hãy mở lại.");
            Platform.exit();
        } catch (Exception e) {
            Dialogs.handle(e);
            if (!AppContext.get().db().isOpen()) {
                Dialogs.warn("Khôi phục", "Kết nối dữ liệu đã đóng, phần mềm sẽ thoát. Hãy mở lại.");
                Platform.exit();
            }
        }
    }

    @FXML
    private void generateDemo() {
        if (!Dialogs.confirm("Dữ liệu mẫu", "Tạo đơn hàng mẫu cho 30 ngày gần nhất? Việc này không làm thay đổi tồn kho.")) {
            return;
        }
        try {
            int n = AppContext.get().demo().generate(30);
            Dialogs.info("Dữ liệu mẫu", "Đã tạo " + n + " đơn hàng mẫu. Xem ở Tổng quan, Đơn hàng và Thống kê.");
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void deleteOrders() {
        long n = AppContext.get().orders().count();
        if (n == 0) {
            Dialogs.info("Xóa đơn hàng", "Hiện không có đơn hàng nào.");
            return;
        }
        if (!Dialogs.confirmDanger("Xóa toàn bộ đơn hàng",
                "Xóa vĩnh viễn " + n + " đơn hàng? Thực đơn, nguyên liệu, tài khoản và lịch sử kho được giữ nguyên.\n\nNên sao lưu trước khi xóa.",
                "Tôi hiểu, xóa hết")) {
            return;
        }
        var typed = Dialogs.prompt("Xác nhận lần cuối", "Gõ chữ XOA để xác nhận", "");
        if (typed.isEmpty() || !"XOA".equalsIgnoreCase(typed.get())) {
            return;
        }
        try {
            AppContext.get().orders().deleteAllOrders();
            Dialogs.info("Xóa đơn hàng", "Đã xóa toàn bộ đơn hàng.");
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private static String now() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
