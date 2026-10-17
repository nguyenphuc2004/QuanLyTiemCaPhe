package com.tiemcaphe.ui;

import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.ui.dialog.QuantityDialog;
import com.tiemcaphe.ui.dialog.StockHistoryDialog;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.Money;
import com.tiemcaphe.util.Session;
import com.tiemcaphe.util.Texts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/** Nguyên liệu & kho: Quản trị sửa danh mục nguyên liệu; mọi người đều nhập kho và xem lịch sử. */
public class IngredientsController implements View {

    private static final List<String> UNITS = List.of("kg", "g", "lít", "ml", "lon", "hộp", "gói", "chai", "túi", "cái", "trái", "bịch");

    @FXML
    private TextField txtSearch;
    @FXML
    private CheckBox chkLowOnly;
    @FXML
    private Button btnStockOut;
    @FXML
    private Button btnAdjust;
    @FXML
    private TableView<Ingredient> tblIngredients;
    @FXML
    private TableColumn<Ingredient, byte[]> colImage;
    @FXML
    private TableColumn<Ingredient, String> colName;
    @FXML
    private TableColumn<Ingredient, Number> colQty;
    @FXML
    private TableColumn<Ingredient, String> colUnit;
    @FXML
    private TableColumn<Ingredient, Number> colThreshold;
    @FXML
    private TableColumn<Ingredient, Ingredient> colStatus;
    @FXML
    private TableColumn<Ingredient, String> colUpdated;
    @FXML
    private Label lblCount;
    @FXML
    private VBox formPane;
    @FXML
    private Label lblFormTitle;
    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cbUnit;
    @FXML
    private TextField txtThreshold;
    @FXML
    private VBox initialBox;
    @FXML
    private TextField txtInitial;
    @FXML
    private ImageView imgPreview;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnDelete;

    private final ObservableList<Ingredient> all = FXCollections.observableArrayList();
    private final FilteredList<Ingredient> filtered = new FilteredList<>(all);
    private Ingredient editing;
    private byte[] imageBytes;

    @FXML
    private void initialize() {
        Fx.image(colImage, Ingredient::image, 40);
        Fx.text(colName, Ingredient::name);
        Fx.qty(colQty, Ingredient::quantity);
        Fx.text(colUnit, Ingredient::unit);
        Fx.qty(colThreshold, Ingredient::warnThreshold);
        Fx.dateTime(colUpdated, Ingredient::updatedAt);
        colStatus.setCellValueFactory(d -> new javafx.beans.property.ReadOnlyObjectWrapper<>(d.getValue()));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item.isLow()) {
                    setGraphic(Widgets.badge("Sắp hết", "badge-danger"));
                } else if (item.warnThreshold() <= 0) {
                    setGraphic(Widgets.badge("Không theo dõi", "badge-muted"));
                } else {
                    setGraphic(Widgets.badge("Đủ hàng", "badge-success"));
                }
            }
        });
        Fx.center(colStatus);
        colStatus.setSortable(false);
        tblIngredients.setItems(filtered);
        Widgets.flex(tblIngredients);
        Widgets.rowStyle(tblIngredients, "row-low", Ingredient::isLow);
        Fx.placeholder(tblIngredients, "Chưa có nguyên liệu nào.");
        tblIngredients.getSelectionModel().selectedItemProperty().addListener((o, a, i) -> {
            if (i != null) {
                fill(i);
            }
        });
        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        chkLowOnly.selectedProperty().addListener((o, a, b) -> applyFilter());

        cbUnit.setItems(FXCollections.observableArrayList(UNITS));
        Fx.decimalInput(txtThreshold);
        Fx.decimalInput(txtInitial);

        boolean admin = Session.isAdmin();
        formPane.setVisible(admin);
        formPane.setManaged(admin);
        btnStockOut.setVisible(admin);
        btnStockOut.setManaged(admin);
        btnAdjust.setVisible(admin);
        btnAdjust.setManaged(admin);
        newIngredient();
    }

    @Override
    public void onShow() {
        reload();
    }

    private void reload() {
        try {
            Long keepId = editing == null ? null : editing.id();
            all.setAll(AppContext.get().inventory().all());
            applyFilter();
            if (keepId != null) {
                all.stream().filter(i -> i.id() == keepId).findFirst().ifPresent(i -> {
                    tblIngredients.getSelectionModel().select(i);
                    editing = i;
                });
            }
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void applyFilter() {
        String q = txtSearch.getText();
        boolean lowOnly = chkLowOnly.isSelected();
        filtered.setPredicate(i -> Texts.contains(i.name(), q) && (!lowOnly || i.isLow()));
        long low = all.stream().filter(Ingredient::isLow).count();
        lblCount.setText(filtered.size() + " / " + all.size() + " nguyên liệu" + (low > 0 ? "  ·  " + low + " sắp hết" : ""));
    }

    private void fill(Ingredient i) {
        editing = i;
        lblFormTitle.setText("Sửa: " + i.name() + "  (tồn " + Money.qty(i.quantity()) + " " + i.unit() + ")");
        txtName.setText(i.name());
        cbUnit.setValue(i.unit());
        txtThreshold.setText(i.warnThreshold() == 0 ? "" : Money.qtyPlain(i.warnThreshold()));
        initialBox.setVisible(false);
        initialBox.setManaged(false);
        imageBytes = i.image();
        imgPreview.setImage(Images.toImage(imageBytes));
        btnDelete.setDisable(false);
        lblStatus.setText("");
    }

    @FXML
    private void newIngredient() {
        editing = null;
        tblIngredients.getSelectionModel().clearSelection();
        lblFormTitle.setText("Thêm nguyên liệu");
        txtName.clear();
        cbUnit.setValue(null);
        cbUnit.getEditor().clear();
        txtThreshold.clear();
        txtInitial.clear();
        initialBox.setVisible(true);
        initialBox.setManaged(true);
        imageBytes = null;
        imgPreview.setImage(Images.placeholder());
        btnDelete.setDisable(true);
        lblStatus.setText("");
    }

    private String unitText() {
        String v = cbUnit.getEditor().getText();
        if (v == null || v.isBlank()) {
            v = cbUnit.getValue();
        }
        return v == null ? "" : v.trim();
    }

    @FXML
    private void save() {
        try {
            AppContext ctx = AppContext.get();
            double threshold = Fx.decimalValue(txtThreshold, "Ngưỡng cảnh báo");
            Ingredient saved;
            if (editing == null) {
                saved = ctx.inventory().create(txtName.getText(), unitText(), Fx.decimalValue(txtInitial, "Tồn kho ban đầu"),
                        threshold, imageBytes, Session.require());
            } else {
                saved = ctx.inventory().update(editing.id(), txtName.getText(), unitText(), threshold, imageBytes,
                        Session.require());
            }
            editing = saved;
            reload();
            lblStatus.setText("Đã lưu \"" + saved.name() + "\" lúc " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void delete() {
        if (editing == null) {
            return;
        }
        if (!Dialogs.confirmDanger("Xóa nguyên liệu", "Xóa \"" + editing.name() + "\" cùng toàn bộ lịch sử kho và định lượng liên quan?", "Xóa")) {
            return;
        }
        try {
            AppContext.get().inventory().delete(editing.id(), Session.require());
            newIngredient();
            reload();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private Ingredient requireSelection(String action) {
        Ingredient i = tblIngredients.getSelectionModel().getSelectedItem();
        if (i == null) {
            Dialogs.info(action, "Hãy chọn một nguyên liệu trong bảng trước.");
        }
        return i;
    }

    @FXML
    private void stockIn() {
        Ingredient i = requireSelection("Nhập kho");
        if (i == null) {
            return;
        }
        Optional<QuantityDialog.Result> r = QuantityDialog.show("Nhập kho",
                "Nhập thêm \"" + i.name() + "\" (đang có " + Money.qty(i.quantity()) + " " + i.unit() + ")",
                "Số lượng nhập", i.unit(), null, "Ghi chú", "Nhập kho");
        r.ifPresent(res -> run(() -> AppContext.get().inventory().stockIn(i.id(), res.quantity(), res.note(), Session.require())));
    }

    @FXML
    private void stockOut() {
        Ingredient i = requireSelection("Xuất kho");
        if (i == null) {
            return;
        }
        Optional<QuantityDialog.Result> r = QuantityDialog.show("Xuất kho",
                "Xuất bớt \"" + i.name() + "\" (hỏng, hao hụt...) - đang có " + Money.qty(i.quantity()) + " " + i.unit(),
                "Số lượng xuất", i.unit(), null, "Lý do", "Xuất kho");
        r.ifPresent(res -> run(() -> AppContext.get().inventory().stockOut(i.id(), res.quantity(), res.note(), Session.require())));
    }

    @FXML
    private void adjust() {
        Ingredient i = requireSelection("Kiểm kê");
        if (i == null) {
            return;
        }
        Optional<QuantityDialog.Result> r = QuantityDialog.show("Kiểm kê",
                "Nhập số lượng thực tế đếm được của \"" + i.name() + "\" (sổ sách: " + Money.qty(i.quantity()) + " " + i.unit() + ")",
                "Số lượng thực tế", i.unit(), i.quantity(), "Ghi chú", "Cập nhật tồn");
        r.ifPresent(res -> run(() -> AppContext.get().inventory().adjustTo(i.id(), res.quantity(), res.note(), Session.require())));
    }

    @FXML
    private void history() {
        Ingredient i = requireSelection("Lịch sử kho");
        if (i == null) {
            return;
        }
        try {
            StockHistoryDialog.show(i, AppContext.get().inventory().history(i.id(), 300));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void run(Runnable action) {
        try {
            action.run();
            reload();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh nguyên liệu");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File f = fc.showOpenDialog(Fx.windowOf(txtName));
        if (f == null) {
            return;
        }
        try {
            imageBytes = Images.readForStorage(f);
            imgPreview.setImage(Images.toImage(imageBytes));
        } catch (Exception e) {
            Dialogs.error("Chọn ảnh", "Không đọc được file ảnh: " + e.getMessage());
        }
    }

    @FXML
    private void removeImage() {
        imageBytes = null;
        imgPreview.setImage(Images.placeholder());
    }
}
