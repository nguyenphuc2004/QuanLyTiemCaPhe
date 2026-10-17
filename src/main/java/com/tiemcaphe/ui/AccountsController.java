package com.tiemcaphe.ui;

import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Role;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Quản lý tài khoản đăng nhập (chỉ Quản trị). */
public class AccountsController implements View {

    @FXML
    private TableView<Account> tblAccounts;
    @FXML
    private TableColumn<Account, String> colUsername;
    @FXML
    private TableColumn<Account, String> colFullName;
    @FXML
    private TableColumn<Account, String> colRole;
    @FXML
    private TableColumn<Account, Boolean> colActive;
    @FXML
    private TableColumn<Account, String> colCreated;
    @FXML
    private Label lblFormTitle;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtFullName;
    @FXML
    private ComboBox<Role> cbRole;
    @FXML
    private CheckBox chkActive;
    @FXML
    private VBox passwordBox;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnResetPassword;
    @FXML
    private Button btnDelete;

    private Account editing;

    @FXML
    private void initialize() {
        Fx.text(colUsername, Account::username);
        Fx.text(colFullName, Account::fullName);
        Fx.text(colRole, a -> a.role().label());
        Fx.yesNo(colActive, Account::active, "Hoạt động", "Đã khóa");
        Fx.dateTime(colCreated, Account::createdAt);
        Widgets.flex(tblAccounts);
        tblAccounts.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                fill(b);
            }
        });
        cbRole.setItems(FXCollections.observableArrayList(Role.values()));
        cbRole.setValue(Role.NHAN_VIEN);
        newAccount();
    }

    @Override
    public void onShow() {
        reload();
    }

    private void reload() {
        try {
            Long keep = editing == null ? null : editing.id();
            tblAccounts.setItems(FXCollections.observableArrayList(AppContext.get().auth().all()));
            if (keep != null) {
                tblAccounts.getItems().stream().filter(a -> a.id() == keep).findFirst().ifPresent(a -> {
                    tblAccounts.getSelectionModel().select(a);
                    editing = a;
                });
            }
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void fill(Account a) {
        editing = a;
        lblFormTitle.setText("Sửa tài khoản: " + a.username());
        txtUsername.setText(a.username());
        txtFullName.setText(a.fullName());
        cbRole.setValue(a.role());
        chkActive.setSelected(a.active());
        passwordBox.setVisible(false);
        passwordBox.setManaged(false);
        txtPassword.clear();
        btnDelete.setDisable(false);
        btnResetPassword.setDisable(false);
        lblStatus.setText("");
    }

    @FXML
    private void newAccount() {
        editing = null;
        tblAccounts.getSelectionModel().clearSelection();
        lblFormTitle.setText("Thêm tài khoản");
        txtUsername.clear();
        txtFullName.clear();
        cbRole.setValue(Role.NHAN_VIEN);
        chkActive.setSelected(true);
        passwordBox.setVisible(true);
        passwordBox.setManaged(true);
        txtPassword.clear();
        btnDelete.setDisable(true);
        btnResetPassword.setDisable(true);
        lblStatus.setText("");
        txtUsername.requestFocus();
    }

    @FXML
    private void save() {
        try {
            AppContext ctx = AppContext.get();
            Account saved;
            if (editing == null) {
                saved = ctx.auth().create(txtUsername.getText(), txtPassword.getText(), txtFullName.getText(),
                        cbRole.getValue(), chkActive.isSelected());
            } else {
                saved = ctx.auth().update(editing.id(), txtUsername.getText(), txtFullName.getText(), cbRole.getValue(),
                        chkActive.isSelected());
                if (saved.id() == Session.require().id()) {
                    Session.set(saved);
                    if (!saved.isAdmin() || !saved.active()) {
                        Dialogs.info("Tài khoản", "Bạn vừa đổi quyền hoặc khóa chính tài khoản của mình, phần mềm sẽ đăng xuất.");
                        com.tiemcaphe.App.showLogin();
                        return;
                    }
                }
            }
            editing = saved;
            reload();
            fill(saved);
            lblStatus.setText("Đã lưu lúc " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void delete() {
        if (editing == null) {
            return;
        }
        if (!Dialogs.confirmDanger("Xóa tài khoản", "Xóa tài khoản \"" + editing.username() + "\"?\nĐơn hàng cũ vẫn giữ tên nhân viên đã bán.", "Xóa")) {
            return;
        }
        try {
            AppContext.get().auth().delete(editing.id(), Session.require());
            newAccount();
            reload();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void resetPassword() {
        if (editing == null) {
            return;
        }
        Dialogs.promptPassword("Đặt lại mật khẩu", "Mật khẩu mới cho \"" + editing.username() + "\"").ifPresent(pw -> {
            try {
                AppContext.get().auth().resetPassword(editing.id(), pw);
                lblStatus.setText("Đã đặt lại mật khẩu cho " + editing.username() + ".");
            } catch (Exception e) {
                Dialogs.handle(e);
            }
        });
    }
}
