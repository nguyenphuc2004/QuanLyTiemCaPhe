package com.tiemcaphe.ui.dialog;

import com.tiemcaphe.model.Category;
import com.tiemcaphe.service.CatalogService;
import com.tiemcaphe.ui.Dialogs;
import com.tiemcaphe.ui.Fx;
import com.tiemcaphe.ui.Icons;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Thêm / đổi tên / xóa danh mục món. */
public final class CategoryDialog {

    private CategoryDialog() {
    }

    public static void show(CatalogService catalog) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Danh mục món");
        d.setHeaderText("Danh mục dùng để nhóm món trên màn hình bán hàng");

        ListView<Category> list = new ListView<>();
        list.setPrefHeight(240);
        Runnable reload = () -> list.setItems(FXCollections.observableArrayList(catalog.allCategories()));
        reload.run();

        TextField txtName = new TextField();
        txtName.setPromptText("Tên danh mục");
        HBox.setHgrow(txtName, Priority.ALWAYS);
        Button btnAdd = Fx.iconButton(Icons.ADD, "Thêm", "btn-primary");
        Button btnRename = Fx.iconButton(Icons.EDIT, "Đổi tên");
        Button btnDelete = Fx.iconButton(Icons.DELETE, "Xóa", "btn-outline-danger");
        btnRename.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());

        list.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                txtName.setText(c.name());
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                catalog.addCategory(txtName.getText());
                txtName.clear();
                reload.run();
            } catch (Exception ex) {
                Dialogs.handle(ex);
            }
        });
        txtName.setOnAction(e -> btnAdd.fire());
        btnRename.setOnAction(e -> {
            Category c = list.getSelectionModel().getSelectedItem();
            try {
                catalog.renameCategory(c.id(), txtName.getText());
                reload.run();
            } catch (Exception ex) {
                Dialogs.handle(ex);
            }
        });
        btnDelete.setOnAction(e -> {
            Category c = list.getSelectionModel().getSelectedItem();
            long n = catalog.countProductsInCategory(c.id());
            String msg = n > 0
                    ? "Danh mục \"" + c.name() + "\" đang có " + n + " món. Xóa xong các món này sẽ thành \"Chưa phân loại\". Tiếp tục?"
                    : "Xóa danh mục \"" + c.name() + "\"?";
            if (Dialogs.confirmDanger("Xóa danh mục", msg, "Xóa")) {
                try {
                    catalog.deleteCategory(c.id());
                    txtName.clear();
                    reload.run();
                } catch (Exception ex) {
                    Dialogs.handle(ex);
                }
            }
        });

        Label hint = new Label("Chọn một danh mục trong danh sách để đổi tên hoặc xóa.");
        hint.getStyleClass().add("muted-small");
        VBox content = new VBox(10, list, new HBox(8, txtName, btnAdd, btnRename, btnDelete), hint);
        content.setPrefWidth(480);
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().setAll(Dialogs.CLOSE);
        Dialogs.style(d);
        d.showAndWait();
    }
}
