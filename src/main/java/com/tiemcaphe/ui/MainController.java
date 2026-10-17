package com.tiemcaphe.ui;

import com.tiemcaphe.App;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.util.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

/** Khung chính: thanh điều hướng theo vai trò + vùng nội dung nạp từng màn hình (có cache). */
public class MainController {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm  ·  EEEE, dd/MM/yyyy",
            java.util.Locale.forLanguageTag("vi-VN"));

    @FXML
    private VBox navBox;
    @FXML
    private Label lblShop;
    @FXML
    private Label lblUser;
    @FXML
    private Label lblRole;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblClock;
    @FXML
    private StackPane content;
    @FXML
    private Button btnLogout;

    private final Map<ViewId, Fx.Loaded<?>> cache = new EnumMap<>(ViewId.class);
    private final ToggleGroup navGroup = new ToggleGroup();
    private Timeline clock;
    private ViewId currentView;

    @FXML
    private void initialize() {
        Account me = Session.require();
        lblShop.setText(App.shopName());
        lblUser.setText(me.fullName());
        lblRole.setText(me.role().label() + "  ·  " + me.username());
        btnLogout.setGraphic(Icons.icon(Icons.LOGOUT, 20));

        ViewId first = null;
        for (ViewId v : ViewId.values()) {
            if (!v.allowedFor(me)) {
                continue;
            }
            ToggleButton b = new ToggleButton(v.title());
            b.setGraphic(Icons.icon(v.icon(), 20));
            b.getStyleClass().add("nav-button");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setToggleGroup(navGroup);
            b.setUserData(v);
            b.setOnAction(e -> navigate(v));
            navBox.getChildren().add(b);
            if (first == null) {
                first = v;
            }
        }
        // không cho bỏ chọn toàn bộ
        navGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
        });
        startClock();
        if (first != null) {
            select(first);
        }
    }

    public void select(ViewId v) {
        for (Toggle t : navGroup.getToggles()) {
            if (t.getUserData() == v) {
                t.setSelected(true);
            }
        }
        navigate(v);
    }

    private void navigate(ViewId v) {
        try {
            Fx.Loaded<?> loaded = cache.get(v);
            if (loaded == null) {
                loaded = Fx.load(v.fxml());
                cache.put(v, loaded);
            }
            content.getChildren().setAll(loaded.root());
            lblTitle.setText(v.title());
            currentView = v;
            if (loaded.controller() instanceof View view) {
                view.onShow();
            }
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    public ViewId currentView() {
        return currentView;
    }

    private void startClock() {
        lblClock.setText(LocalDateTime.now().format(CLOCK));
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> lblClock.setText(LocalDateTime.now().format(CLOCK))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void logout() {
        if (Dialogs.confirm("Đăng xuất", "Bạn muốn đăng xuất khỏi phần mềm?")) {
            if (clock != null) {
                clock.stop();
            }
            App.showLogin();
        }
    }
}
