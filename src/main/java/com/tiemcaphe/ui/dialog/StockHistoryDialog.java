package com.tiemcaphe.ui.dialog;

import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.StockEntry;
import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.ui.Fx;
import com.tiemcaphe.ui.Widgets;
import com.tiemcaphe.util.Money;
import javafx.collections.FXCollections;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

/** Lịch sử biến động kho của một nguyên liệu. */
public final class StockHistoryDialog {

    private StockHistoryDialog() {
    }

    public static void show(Ingredient ing, List<StockEntry> entries) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Lịch sử kho");
        d.setHeaderText(ing.name() + "  ·  tồn hiện tại: " + Money.qty(ing.quantity()) + " " + ing.unit());

        TableView<StockEntry> table = new TableView<>(FXCollections.observableArrayList(entries));
        TableColumn<StockEntry, String> cTime = new TableColumn<>("Thời gian");
        Fx.dateTime(cTime, StockEntry::createdAt);
        cTime.setPrefWidth(130);
        TableColumn<StockEntry, String> cType = new TableColumn<>("Loại");
        Fx.text(cType, e -> e.type().label());
        cType.setPrefWidth(100);
        TableColumn<StockEntry, Number> cQty = new TableColumn<>("Số lượng");
        cQty.setCellValueFactory(x -> new javafx.beans.property.ReadOnlyObjectWrapper<>(x.getValue().quantity()));
        cQty.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                double v = item.doubleValue();
                setText((v > 0 ? "+" : "") + Money.qty(v));
                setStyle(v < 0 ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
            }
        });
        Fx.right(cQty);
        cQty.setPrefWidth(100);
        TableColumn<StockEntry, String> cNote = new TableColumn<>("Ghi chú");
        Fx.text(cNote, e -> e.note() == null ? "" : e.note());
        cNote.setPrefWidth(260);
        TableColumn<StockEntry, String> cBy = new TableColumn<>("Người thực hiện");
        Fx.text(cBy, e -> e.byName() == null ? "" : e.byName());
        cBy.setPrefWidth(140);
        table.getColumns().add(cTime);
        table.getColumns().add(cType);
        table.getColumns().add(cQty);
        table.getColumns().add(cNote);
        table.getColumns().add(cBy);
        Widgets.flex(table);
        table.setPrefSize(760, 400);
        Fx.placeholder(table, "Chưa có biến động kho.");

        Label hint = new Label("Hiển thị tối đa " + entries.size() + " dòng gần nhất.");
        hint.getStyleClass().add("muted-small");
        d.getDialogPane().setContent(new VBox(8, table, hint));
        d.getDialogPane().getButtonTypes().setAll(Dialogs.CLOSE);
        d.setResizable(true);
        Dialogs.style(d);
        d.showAndWait();
    }
}
