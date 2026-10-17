package com.tiemcaphe.ui;

import com.tiemcaphe.App;
import com.tiemcaphe.config.AppConfig;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.service.BusinessException;
import com.tiemcaphe.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Label lblShop;
    @FXML
    private Label lblError;
    @FXML
    private Label lblVersion;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private CheckBox chkShow;
    @FXML
    private Button btnLogin;

    @FXML
    private void initialize() {
        lblShop.setText(App.shopName());
        lblVersion.setText("Phiên bản " + AppConfig.VERSION + "  ·  Dữ liệu: " + AppConfig.homeDir());
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtPasswordVisible.visibleProperty().bind(chkShow.selectedProperty());
        txtPasswordVisible.managedProperty().bind(chkShow.selectedProperty());
        txtPassword.visibleProperty().bind(chkShow.selectedProperty().not());
        txtPassword.managedProperty().bind(chkShow.selectedProperty().not());
        txtUsername.textProperty().addListener((o, a, b) -> lblError.setText(""));
        txtPassword.textProperty().addListener((o, a, b) -> lblError.setText(""));
        Platform.runLater(() -> txtUsername.requestFocus());
    }

    @FXML
    private void login() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        btnLogin.setDisable(true);
        try {
            Account account = AppContext.get().auth().login(username, password);
            Session.set(account);
            App.showMain();
        } catch (BusinessException e) {
            lblError.setText(e.getMessage());
            if (chkShow.isSelected()) {
                txtPasswordVisible.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
            txtPassword.selectAll();
        } catch (Exception e) {
            Dialogs.handle(e);
        } finally {
            btnLogin.setDisable(false);
        }
    }
}
