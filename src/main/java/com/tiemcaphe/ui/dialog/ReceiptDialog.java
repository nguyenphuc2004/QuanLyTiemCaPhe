package com.tiemcaphe.ui.dialog;

import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.ShopInfo;
import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Money;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

import java.util.List;

/** Xem trước và in hóa đơn (khổ hẹp, phù hợp máy in nhiệt 80mm hoặc in A4). */
public final class ReceiptDialog {

    private static final double WIDTH = 300;

    private ReceiptDialog() {
    }

    public static void show(Order order, List<OrderLine> lines, ShopInfo shop) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Hóa đơn " + order.code());
        d.setHeaderText(null);
        VBox receipt = build(order, lines, shop);
        ScrollPane sp = new ScrollPane(receipt);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(480);
        sp.setPrefViewportWidth(WIDTH + 20);
        d.getDialogPane().setContent(sp);

        ButtonType print = new ButtonType("In hóa đơn", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().setAll(print, Dialogs.CLOSE);
        Button printBtn = (Button) d.getDialogPane().lookupButton(print);
        printBtn.getStyleClass().add("btn-primary");
        printBtn.addEventFilter(ActionEvent.ACTION, e -> {
            e.consume();
            print(build(order, lines, shop), d.getDialogPane().getScene().getWindow());
        });
        Dialogs.style(d);
        d.showAndWait();
    }

    public static VBox build(Order o, List<OrderLine> lines, ShopInfo shop) {
        VBox box = new VBox(2);
        box.getStyleClass().add("receipt");
        box.setPrefWidth(WIDTH);
        box.setMaxWidth(WIDTH);

        box.getChildren().add(centered(shop.name(), "receipt-title"));
        if (shop.address() != null && !shop.address().isBlank()) {
            box.getChildren().add(centered(shop.address(), null));
        }
        if (shop.phone() != null && !shop.phone().isBlank()) {
            box.getChildren().add(centered("ĐT: " + shop.phone(), null));
        }
        box.getChildren().add(sep());
        box.getChildren().add(centered("HÓA ĐƠN THANH TOÁN", "receipt-heading"));
        box.getChildren().add(new Label("Số: " + o.code()));
        box.getChildren().add(new Label("Ngày: " + Dates.display(o.createdAt())));
        box.getChildren().add(new Label("Thu ngân: " + o.staffName()));
        if (o.tableName() != null && !o.tableName().isBlank()) {
            box.getChildren().add(new Label("Bàn: " + o.tableName()));
        }
        box.getChildren().add(sep());

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(2);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(34);
        c2.setHalignment(HPos.RIGHT);
        ColumnConstraints c3 = new ColumnConstraints(84);
        c3.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(c1, c2, c3);
        grid.addRow(0, bold("Món"), bold("SL"), bold("T.Tiền"));
        int r = 1;
        for (OrderLine l : lines) {
            Label name = new Label(l.productName());
            name.setWrapText(true);
            grid.addRow(r++, name, new Label(String.valueOf(l.quantity())), new Label(Money.formatPlain(l.lineTotal())));
            Label unit = new Label("  " + Money.formatPlain(l.unitPrice()) + " x " + l.quantity());
            unit.getStyleClass().add("receipt-sep");
            grid.add(unit, 0, r++, 3, 1);
        }
        box.getChildren().add(grid);
        box.getChildren().add(sep());

        box.getChildren().add(row("Tạm tính", Money.formatPlain(o.subtotal()), null));
        if (o.discount() > 0) {
            box.getChildren().add(row("Giảm giá", "-" + Money.formatPlain(o.discount()), null));
        }
        box.getChildren().add(row("TỔNG CỘNG", Money.format(o.total()), "receipt-total"));
        box.getChildren().add(row("Thanh toán", o.method().label(), null));
        if (o.method() == PaymentMethod.TIEN_MAT) {
            box.getChildren().add(row("Khách đưa", Money.formatPlain(o.paid()), null));
            box.getChildren().add(row("Tiền thừa", Money.formatPlain(o.change()), null));
        }
        if (o.note() != null && !o.note().isBlank()) {
            Label note = new Label("Ghi chú: " + o.note());
            note.setWrapText(true);
            box.getChildren().add(note);
        }
        if (o.isCancelled()) {
            box.getChildren().add(centered("*** ĐƠN ĐÃ HỦY ***", "receipt-heading"));
        }
        box.getChildren().add(sep());
        if (shop.receiptFooter() != null && !shop.receiptFooter().isBlank()) {
            box.getChildren().add(centered(shop.receiptFooter(), null));
        }
        return box;
    }

    private static Label centered(String text, String style) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        l.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        if (style != null) {
            l.getStyleClass().add(style);
        }
        return l;
    }

    private static Label bold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private static Label sep() {
        Label l = new Label("- - - - - - - - - - - - - - - - - - -");
        l.getStyleClass().add("receipt-sep");
        return l;
    }

    private static Node row(String left, String right, String style) {
        GridPane g = new GridPane();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHalignment(HPos.RIGHT);
        g.getColumnConstraints().addAll(c1, c2);
        Label a = new Label(left);
        Label b = new Label(right);
        if (style != null) {
            a.getStyleClass().add(style);
            b.getStyleClass().add(style);
        }
        g.addRow(0, a, b);
        return g;
    }

    /** In node qua hộp thoại máy in của hệ điều hành; tự thu nhỏ cho vừa bề ngang trang. */
    public static void print(Node node, Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Dialogs.warn("In hóa đơn", "Không tìm thấy máy in nào trên máy này.");
            return;
        }
        if (!job.showPrintDialog(owner)) {
            return;
        }
        // Cho node tự đo kích thước trước khi in
        javafx.scene.Scene tmp = new javafx.scene.Scene(new javafx.scene.Group(node));
        tmp.getStylesheets().add(com.tiemcaphe.ui.Fx.css());
        node.applyCss();
        if (node instanceof javafx.scene.Parent p) {
            p.layout();
        }
        PageLayout layout = job.getJobSettings().getPageLayout();
        double w = node.getBoundsInParent().getWidth();
        double scale = w > 0 ? Math.min(1.0, layout.getPrintableWidth() / w) : 1.0;
        node.getTransforms().add(new Scale(scale, scale));
        boolean ok = job.printPage(node);
        node.getTransforms().clear();
        if (ok) {
            job.endJob();
        } else {
            job.cancelJob();
            Dialogs.error("In hóa đơn", "Không in được. Kiểm tra lại máy in.");
        }
    }
}
