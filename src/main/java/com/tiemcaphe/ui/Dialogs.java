package com.tiemcaphe.ui;

import com.tiemcaphe.db.DataAccessException;
import com.tiemcaphe.service.BusinessException;
import com.tiemcaphe.util.Images;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Hộp thoại tiếng Việt dùng chung. */
public final class Dialogs {

    private static final Logger LOG = Logger.getLogger(Dialogs.class.getName());

    public static final ButtonType OK = new ButtonType("Đồng ý", ButtonBar.ButtonData.OK_DONE);
    public static final ButtonType CANCEL = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
    public static final ButtonType CLOSE = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Dialogs() {
    }

    public static void info(String title, String message) {
        build(AlertType.INFORMATION, title, message, CLOSE).showAndWait();
    }

    public static void warn(String title, String message) {
        build(AlertType.WARNING, title, message, CLOSE).showAndWait();
    }

    public static void error(String title, String message) {
        build(AlertType.ERROR, title, message, CLOSE).showAndWait();
    }

    public static boolean confirm(String title, String message) {
        return build(AlertType.CONFIRMATION, title, message, OK, CANCEL).showAndWait().filter(b -> b == OK).isPresent();
    }

    /** Xác nhận hành động nguy hiểm, nút đồng ý màu đỏ với nhãn tùy chọn. */
    public static boolean confirmDanger(String title, String message, String okLabel) {
        ButtonType ok = new ButtonType(okLabel, ButtonBar.ButtonData.OK_DONE);
        Alert a = build(AlertType.CONFIRMATION, title, message, ok, CANCEL);
        Button okBtn = (Button) a.getDialogPane().lookupButton(ok);
        okBtn.getStyleClass().add("btn-danger");
        return a.showAndWait().filter(b -> b == ok).isPresent();
    }

    public static Optional<String> prompt(String title, String label, String initial) {
        TextInputDialog d = new TextInputDialog(initial == null ? "" : initial);
        d.setTitle(title);
        d.setHeaderText(null);
        d.setContentText(label);
        d.getDialogPane().getButtonTypes().setAll(OK, CANCEL);
        style(d);
        return d.showAndWait().map(String::trim);
    }

    public static Optional<String> promptPassword(String title, String label) {
        Dialog<String> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(null);
        PasswordField pf = new PasswordField();
        pf.setPromptText("Ít nhất 6 ký tự");
        PasswordField pf2 = new PasswordField();
        pf2.setPromptText("Nhập lại");
        VBox box = new VBox(8, new Label(label), pf, new Label("Nhập lại mật khẩu"), pf2);
        box.setPadding(new Insets(10, 0, 0, 0));
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().setAll(OK, CANCEL);
        Button okBtn = (Button) d.getDialogPane().lookupButton(OK);
        okBtn.disableProperty().bind(pf.textProperty().isEmpty().or(pf.textProperty().isNotEqualTo(pf2.textProperty())));
        d.setResultConverter(bt -> bt == OK ? pf.getText() : null);
        style(d);
        d.setOnShown(e -> pf.requestFocus());
        return d.showAndWait();
    }

    /** Hiển thị lỗi phù hợp: lỗi nghiệp vụ → cảnh báo nhẹ; lỗi khác → ghi log và báo lỗi. */
    public static void handle(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof BusinessException be) {
                warn("Không thực hiện được", be.getMessage());
                return;
            }
            cur = cur.getCause();
        }
        LOG.log(Level.SEVERE, "Lỗi không mong muốn", t);
        if (t instanceof DataAccessException) {
            error("Lỗi cơ sở dữ liệu", t.getMessage());
        } else {
            String msg = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
            error("Đã xảy ra lỗi", msg + "\n\nChi tiết đã được ghi vào file log.");
        }
    }

    private static Alert build(AlertType type, String title, String message, ButtonType... buttons) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getButtonTypes().setAll(buttons);
        style(a);
        return a;
    }

    /** Gắn CSS, icon và cửa sổ cha cho hộp thoại. */
    public static void style(Dialog<?> d) {
        DialogPane p = d.getDialogPane();
        p.getStylesheets().add(Fx.css());
        p.getStyleClass().add("app-dialog");
        p.setMinWidth(380);
        Window owner = focusedWindow();
        if (owner != null && d.getOwner() == null) {
            d.initOwner(owner);
        }
        d.setOnShowing(e -> {
            if (p.getScene() != null && p.getScene().getWindow() instanceof Stage s) {
                s.getIcons().add(Images.resource("/img/logo.png"));
            }
        });
    }

    private static Window focusedWindow() {
        return Window.getWindows().stream().filter(Window::isFocused).findFirst()
                .orElseGet(() -> Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null));
    }
}
