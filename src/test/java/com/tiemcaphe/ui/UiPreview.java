package com.tiemcaphe.ui;

import com.tiemcaphe.App;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.util.Session;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Công cụ kiểm thử thủ công (không đóng gói theo sản phẩm): mở lần lượt mọi màn hình cho cả hai vai trò
 * và chụp ảnh ra thư mục chỉ định. Chạy: java -cp ... com.tiemcaphe.ui.UiPreview &lt;thư mục ảnh&gt;
 */
public class UiPreview extends Application {

    private static Path outDir;
    private final List<Runnable> steps = new ArrayList<>();
    private Stage stage;

    public static void main(String[] args) {
        outDir = Path.of(args.length > 0 ? args[0] : "preview");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        Files.createDirectories(outDir);
        AppContext ctx = AppContext.get();
        ctx.demo().generateIfNoOrders();
        App.initStage(stage);
        App.showLogin();
        stage.show();

        steps.add(() -> snap("00-login"));
        MainController[] main = new MainController[1];
        steps.add(() -> {
            Account admin = ctx.auth().login("admin", "admin123");
            Session.set(admin);
            main[0] = App.showMain();
        });
        for (ViewId v : ViewId.values()) {
            steps.add(() -> main[0].select(v));
            steps.add(() -> snap("10-admin-" + v.name().toLowerCase()));
        }
        steps.add(() -> {
            // Tạo một đơn thật rồi chụp hóa đơn trong cửa sổ riêng
            Account admin = ctx.auth().login("admin", "admin123");
            var p1 = ctx.catalog().availableProducts().get(1);
            var p2 = ctx.catalog().availableProducts().get(4);
            var l1 = new com.tiemcaphe.model.CartLine(p1);
            l1.setQuantity(2);
            var l2 = new com.tiemcaphe.model.CartLine(p2);
            var order = ctx.orders().checkout(new com.tiemcaphe.service.OrderService.CheckoutRequest(
                    java.util.List.of(l1, l2), "Bàn 3", "Ít đá", 5_000, 100_000,
                    com.tiemcaphe.model.PaymentMethod.TIEN_MAT), admin);
            javafx.scene.layout.VBox receipt = com.tiemcaphe.ui.dialog.ReceiptDialog.build(order,
                    ctx.orders().linesOf(order.id()), ctx.settings().shopInfo());
            Stage s = new Stage();
            javafx.scene.Scene sc = new javafx.scene.Scene(new javafx.scene.layout.StackPane(receipt));
            sc.getStylesheets().add(Fx.css());
            s.setScene(sc);
            s.show();
            PauseTransition wait = new PauseTransition(Duration.millis(500));
            wait.setOnFinished(ev -> {
                snapScene(sc, "15-receipt");
                s.close();
            });
            wait.play();
        });
        steps.add(() -> {
            App.showLogin();
            Account staff = ctx.auth().login("nhanvien", "123456");
            Session.set(staff);
            main[0] = App.showMain();
        });
        for (ViewId v : ViewId.values()) {
            if (!v.allowedFor(new Account(0, "x", "x", com.tiemcaphe.model.Role.NHAN_VIEN, true, null))) {
                continue;
            }
            steps.add(() -> main[0].select(v));
            steps.add(() -> snap("20-staff-" + v.name().toLowerCase()));
        }
        steps.add(() -> {
            System.out.println("PREVIEW DONE");
            Platform.exit();
        });
        run(0);
    }

    private void run(int i) {
        if (i >= steps.size()) {
            return;
        }
        try {
            steps.get(i).run();
        } catch (Throwable t) {
            System.out.println("STEP " + i + " FAILED: " + t);
            t.printStackTrace();
        }
        PauseTransition p = new PauseTransition(Duration.millis(700));
        p.setOnFinished(e -> run(i + 1));
        p.play();
    }

    private void snap(String name) {
        snapScene(stage.getScene(), name);
    }

    private void snapScene(javafx.scene.Scene scene, String name) {
        try {
            WritableImage img = scene.snapshot(null);
            int w = (int) img.getWidth();
            int h = (int) img.getHeight();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            PixelReader pr = img.getPixelReader();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    bi.setRGB(x, y, pr.getArgb(x, y));
                }
            }
            ImageIO.write(bi, "png", outDir.resolve(name + ".png").toFile());
            System.out.println("SNAP " + name + " " + w + "x" + h);
        } catch (Exception e) {
            System.out.println("SNAP FAILED " + name + ": " + e);
        }
    }
}
