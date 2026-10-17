package com.tiemcaphe;

import com.tiemcaphe.config.AppConfig;
import com.tiemcaphe.config.AppLogging;
import com.tiemcaphe.db.Database;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.ui.Fx;
import com.tiemcaphe.ui.LoginController;
import com.tiemcaphe.ui.MainController;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.Session;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Điểm vào JavaFX. Chạy với tham số {@code --demo} để sinh đơn hàng mẫu nếu chưa có đơn nào. */
public class App extends Application {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        AppLogging.init();
        LOG.info(() -> AppConfig.APP_NAME + " " + AppConfig.VERSION + " khởi động, dữ liệu tại " + AppConfig.homeDir());
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.log(Level.SEVERE, "Lỗi chưa xử lý trên luồng " + t.getName(), e);
            if (Platform.isFxApplicationThread()) {
                Dialogs.handle(e);
            } else {
                Platform.runLater(() -> Dialogs.handle(e));
            }
        });
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            AppContext ctx = AppContext.get();
            if (getParameters().getUnnamed().contains("--demo")) {
                int n = ctx.demo().generateIfNoOrders();
                LOG.info(() -> "Đã sinh " + n + " đơn hàng mẫu");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Không khởi tạo được CSDL", e);
            Dialogs.error("Không mở được cơ sở dữ liệu",
                    e.getMessage() + "\n\nFile: " + AppConfig.dbFile() + "\nKiểm tra quyền ghi thư mục hoặc file đang bị khóa.");
            Platform.exit();
            return;
        }
        initStage(stage);
        showLogin();
        stage.show();
    }

    /** Gắn cửa sổ chính (tách riêng để bộ kiểm thử giao diện dùng lại). */
    public static void initStage(Stage stage) {
        primaryStage = stage;
        stage.getIcons().add(Images.resource("/img/logo.png"));
        try {
            Images.setPlaceholder(com.tiemcaphe.ui.Widgets.renderPlaceholder(256));
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Không dựng được ảnh mặc định, dùng ảnh trong gói", e);
        }
    }

    public static Stage stage() {
        return primaryStage;
    }

    public static String shopName() {
        return AppContext.get().settings().shopInfo().name();
    }

    public static void showLogin() {
        Session.clear();
        Fx.Loaded<LoginController> loaded = Fx.load("/fxml/login.fxml");
        Scene scene = new Scene(loaded.root(), 960, 620);
        scene.getStylesheets().add(Fx.css());
        primaryStage.setMaximized(false);
        primaryStage.setTitle(shopName() + " - Đăng nhập");
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(520);
        primaryStage.setScene(scene);
        primaryStage.setWidth(960);
        primaryStage.setHeight(620);
        primaryStage.centerOnScreen();
    }

    public static MainController showMain() {
        Fx.Loaded<MainController> loaded = Fx.load("/fxml/main.fxml");
        Scene scene = new Scene(loaded.root(), 1280, 800);
        scene.getStylesheets().add(Fx.css());
        primaryStage.setTitle(shopName() + " - " + AppConfig.APP_NAME);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setMaximized(true);
        return loaded.controller();
    }

    @Override
    public void stop() {
        Database.closeIfOpen();
        LOG.info("Đã thoát ứng dụng");
    }
}
