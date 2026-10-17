package com.tiemcaphe.ui.dialog;

import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.model.RecipeLine;
import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.ui.Fx;
import com.tiemcaphe.ui.Icons;
import com.tiemcaphe.ui.Widgets;
import com.tiemcaphe.util.Money;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Khai báo định lượng nguyên liệu cho một món (dùng để tự trừ kho khi bán). */
public final class RecipeDialog {

    private RecipeDialog() {
    }

    public static Optional<List<RecipeLine>> show(Product product, List<Ingredient> ingredients, List<RecipeLine> current) {
        Dialog<List<RecipeLine>> d = new Dialog<>();
        d.setTitle("Định lượng nguyên liệu");
        d.setHeaderText("Mỗi 1 phần \"" + product.name() + "\" dùng những nguyên liệu sau");

        ObservableList<RecipeLine> rows = FXCollections.observableArrayList(current);

        TableView<RecipeLine> table = new TableView<>(rows);
        table.setPrefHeight(260);
        TableColumn<RecipeLine, String> cName = new TableColumn<>("Nguyên liệu");
        Fx.text(cName, RecipeLine::ingredientName);
        cName.setPrefWidth(220);
        TableColumn<RecipeLine, Number> cQty = new TableColumn<>("Số lượng");
        Fx.qty(cQty, RecipeLine::quantity);
        cQty.setPrefWidth(100);
        TableColumn<RecipeLine, String> cUnit = new TableColumn<>("Đơn vị");
        Fx.text(cUnit, RecipeLine::unit);
        cUnit.setPrefWidth(80);
        TableColumn<RecipeLine, RecipeLine> cDel = new TableColumn<>("");
        cDel.setCellValueFactory(x -> new javafx.beans.property.ReadOnlyObjectWrapper<>(x.getValue()));
        cDel.setCellFactory(c -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setGraphic(Icons.icon(Icons.CLOSE, 14));
                btn.getStyleClass().addAll("btn-ghost", "btn-sm");
                btn.setOnAction(e -> rows.remove(getItem()));
            }

            @Override
            protected void updateItem(RecipeLine item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : btn);
            }
        });
        cDel.setPrefWidth(44);
        cDel.setSortable(false);
        table.getColumns().add(cName);
        table.getColumns().add(cQty);
        table.getColumns().add(cUnit);
        table.getColumns().add(cDel);
        Widgets.flex(table);
        Fx.placeholder(table, "Chưa khai báo nguyên liệu. Món này sẽ không trừ kho khi bán.");

        ComboBox<Ingredient> cb = new ComboBox<>(FXCollections.observableArrayList(ingredients));
        cb.setPromptText("Chọn nguyên liệu");
        cb.setPrefWidth(220);
        TextField txtQty = new TextField();
        txtQty.setPromptText("Số lượng");
        txtQty.setPrefWidth(100);
        Fx.decimalInput(txtQty);
        Label unit = new Label("");
        unit.getStyleClass().add("muted");
        unit.setMinWidth(50);
        cb.valueProperty().addListener((o, a, b) -> unit.setText(b == null ? "" : b.unit()));
        Button add = Fx.iconButton(Icons.ADD, "Thêm", "btn-primary");
        Runnable doAdd = () -> {
            Ingredient ing = cb.getValue();
            if (ing == null) {
                Dialogs.warn("Định lượng", "Hãy chọn nguyên liệu.");
                return;
            }
            double qty;
            try {
                qty = Money.parseQty(txtQty.getText());
            } catch (NumberFormatException e) {
                Dialogs.warn("Định lượng", "Số lượng không hợp lệ.");
                return;
            }
            if (qty <= 0) {
                Dialogs.warn("Định lượng", "Số lượng phải lớn hơn 0.");
                return;
            }
            RecipeLine line = new RecipeLine(product.id(), ing.id(), ing.name(), ing.unit(), qty);
            rows.removeIf(r -> r.ingredientId() == ing.id());
            rows.add(line);
            txtQty.clear();
            cb.getSelectionModel().clearSelection();
            cb.requestFocus();
        };
        add.setOnAction(e -> doAdd.run());
        txtQty.setOnAction(e -> doAdd.run());

        HBox addRow = new HBox(8, cb, txtQty, unit, add);
        addRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cb, Priority.ALWAYS);

        Label hint = new Label("Ví dụ: Cà phê sữa đá dùng 0,025 kg bột cà phê và 0,1 lon sữa đặc. Khi bán 1 ly, kho tự trừ đúng lượng này.");
        hint.getStyleClass().add("muted-small");
        hint.setWrapText(true);

        VBox content = new VBox(10, table, addRow, hint);
        content.setPrefWidth(520);
        d.getDialogPane().setContent(content);
        ButtonType save = new ButtonType("Lưu định lượng", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().setAll(save, Dialogs.CANCEL);
        ((Button) d.getDialogPane().lookupButton(save)).getStyleClass().add("btn-primary");
        d.setResultConverter(bt -> bt == save ? new ArrayList<>(rows) : null);
        Dialogs.style(d);
        return d.showAndWait();
    }
}
