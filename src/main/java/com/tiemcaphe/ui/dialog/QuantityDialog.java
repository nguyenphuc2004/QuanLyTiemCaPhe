package com.tiemcaphe.ui.dialog;

import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.ui.Fx;
import com.tiemcaphe.util.Money;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

/** Hộp thoại nhập một số lượng (nhập kho / xuất kho / kiểm kê) kèm ghi chú. */
public final class QuantityDialog {

    public record Result(double quantity, String note) {
    }

    private QuantityDialog() {
    }

    public static Optional<Result> show(String title, String message, String quantityLabel, String unit, Double initial,
                                        String noteLabel, String okLabel) {
        Dialog<Result> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(message);

        TextField txtQty = new TextField(initial == null ? "" : Money.qtyPlain(initial));
        Fx.decimalInput(txtQty);
        txtQty.setPrefWidth(140);
        Label lblUnit = new Label(unit == null ? "" : unit);
        lblUnit.getStyleClass().add("muted");
        TextField txtNote = new TextField();
        txtNote.setPromptText("Không bắt buộc");
        txtNote.setPrefWidth(300);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(10, 0, 0, 0));
        g.addRow(0, new Label(quantityLabel), new HBox(8, txtQty, lblUnit));
        if (noteLabel != null) {
            g.addRow(1, new Label(noteLabel), txtNote);
        }
        Label hint = new Label("Phần lẻ dùng dấu phẩy, ví dụ 1,5. Dấu chấm là nhóm hàng nghìn: 1.500 = 1500.");
        hint.getStyleClass().add("muted-small");
        hint.setWrapText(true);
        g.add(hint, 0, 2, 2, 1);
        d.getDialogPane().setContent(g);

        ButtonType ok = new ButtonType(okLabel == null ? "Đồng ý" : okLabel, ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().setAll(ok, Dialogs.CANCEL);
        Button okBtn = (Button) d.getDialogPane().lookupButton(ok);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.disableProperty().bind(txtQty.textProperty().isEmpty());
        okBtn.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                Money.parseQty(txtQty.getText());
            } catch (NumberFormatException ex) {
                e.consume();
                Dialogs.warn(title, "Số lượng không hợp lệ.");
            }
        });
        d.setResultConverter(bt -> bt == ok ? new Result(Money.parseQty(txtQty.getText()), txtNote.getText()) : null);
        Dialogs.style(d);
        d.setOnShown(e -> {
            txtQty.requestFocus();
            txtQty.selectAll();
        });
        return d.showAndWait();
    }
}
