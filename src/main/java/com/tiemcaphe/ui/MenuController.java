package com.tiemcaphe.ui;

import com.tiemcaphe.model.Category;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.model.RecipeLine;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.ui.dialog.CategoryDialog;
import com.tiemcaphe.ui.dialog.RecipeDialog;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.Money;
import com.tiemcaphe.util.Texts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Quản lý thực đơn: thêm / sửa / xóa món, ảnh, danh mục, định lượng. */
public class MenuController implements View {

    private static final Category ALL = new Category(-1, "Tất cả danh mục", 0);
    private static final Category NONE = new Category(0, "Chưa phân loại", 0);

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Category> cbFilterCategory;
    @FXML
    private Label lblCount;
    @FXML
    private TableView<Product> tblProducts;
    @FXML
    private TableColumn<Product, byte[]> colImage;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Number> colPrice;
    @FXML
    private TableColumn<Product, Boolean> colAvailable;
    @FXML
    private TableColumn<Product, String> colUpdated;
    @FXML
    private Label lblFormTitle;
    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<Category> cbCategory;
    @FXML
    private TextField txtPrice;
    @FXML
    private CheckBox chkAvailable;
    @FXML
    private ImageView imgPreview;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnRecipe;
    @FXML
    private Button btnDelete;

    private final ObservableList<Product> all = FXCollections.observableArrayList();
    private final FilteredList<Product> filtered = new FilteredList<>(all);
    private Product editing;
    private byte[] imageBytes;

    @FXML
    private void initialize() {
        Fx.image(colImage, Product::image, 40);
        Fx.text(colName, Product::name);
        Fx.text(colCategory, p -> Texts.orDash(p.categoryName()));
        Fx.money(colPrice, Product::price);
        Fx.yesNo(colAvailable, Product::available, "Đang bán", "Ngừng bán");
        Fx.dateTime(colUpdated, Product::updatedAt);
        tblProducts.setItems(filtered);
        Widgets.flex(tblProducts);
        Fx.placeholder(tblProducts, "Chưa có món nào. Nhập thông tin bên phải rồi bấm Lưu.");
        tblProducts.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                fill(p);
            }
        });

        StringConverter<Category> conv = new StringConverter<>() {
            @Override
            public String toString(Category c) {
                return c == null ? "" : c.name();
            }

            @Override
            public Category fromString(String s) {
                return null;
            }
        };
        cbFilterCategory.setConverter(conv);
        cbCategory.setConverter(conv);
        cbFilterCategory.valueProperty().addListener((o, a, b) -> applyFilter());
        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        Fx.moneyInput(txtPrice);
        newProduct();
    }

    @Override
    public void onShow() {
        reloadCategories();
        reloadProducts();
    }

    private void reloadCategories() {
        List<Category> cats = AppContext.get().catalog().allCategories();
        Category filterSel = cbFilterCategory.getValue();
        List<Category> filterItems = new ArrayList<>();
        filterItems.add(ALL);
        filterItems.addAll(cats);
        filterItems.add(NONE);
        cbFilterCategory.setItems(FXCollections.observableArrayList(filterItems));
        cbFilterCategory.setValue(filterSel != null && filterItems.contains(filterSel) ? filterSel : ALL);

        Category formSel = cbCategory.getValue();
        List<Category> formItems = new ArrayList<>(cats);
        formItems.add(NONE);
        cbCategory.setItems(FXCollections.observableArrayList(formItems));
        cbCategory.setValue(formSel != null && formItems.contains(formSel) ? formSel : (cats.isEmpty() ? NONE : cats.get(0)));
    }

    private void reloadProducts() {
        Long keepId = editing == null ? null : editing.id();
        all.setAll(AppContext.get().catalog().allProducts());
        applyFilter();
        if (keepId != null) {
            all.stream().filter(p -> p.id() == keepId).findFirst().ifPresent(p -> {
                tblProducts.getSelectionModel().select(p);
                editing = p;
            });
        }
    }

    private void applyFilter() {
        Category c = cbFilterCategory.getValue();
        String q = txtSearch.getText();
        filtered.setPredicate(p -> {
            boolean okCat = c == null || c == ALL
                    || (c == NONE ? p.categoryId() == null : java.util.Objects.equals(p.categoryId(), c.id()));
            return okCat && Texts.contains(p.name(), q);
        });
        lblCount.setText(filtered.size() + " / " + all.size() + " món");
    }

    private void fill(Product p) {
        editing = p;
        lblFormTitle.setText("Sửa món: " + p.name());
        txtName.setText(p.name());
        cbCategory.setValue(cbCategory.getItems().stream().filter(c -> p.categoryId() == null ? c == NONE : c.id() == p.categoryId())
                .findFirst().orElse(NONE));
        txtPrice.setText(Money.formatPlain(p.price()));
        chkAvailable.setSelected(p.available());
        imageBytes = p.image();
        imgPreview.setImage(Images.toImage(imageBytes));
        btnDelete.setDisable(false);
        btnRecipe.setDisable(false);
        lblStatus.setText("");
    }

    @FXML
    private void newProduct() {
        editing = null;
        tblProducts.getSelectionModel().clearSelection();
        lblFormTitle.setText("Thêm món mới");
        txtName.clear();
        txtPrice.clear();
        chkAvailable.setSelected(true);
        imageBytes = null;
        imgPreview.setImage(Images.placeholder());
        btnDelete.setDisable(true);
        btnRecipe.setDisable(true);
        lblStatus.setText("");
        txtName.requestFocus();
    }

    @FXML
    private void save() {
        try {
            Category c = cbCategory.getValue();
            Long categoryId = c == null || c == NONE ? null : c.id();
            Product saved = AppContext.get().catalog().saveProduct(editing == null ? null : editing.id(), txtName.getText(),
                    categoryId, Fx.moneyValue(txtPrice, "Giá bán"), chkAvailable.isSelected(), imageBytes);
            editing = saved;
            reloadProducts();
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
        if (!Dialogs.confirmDanger("Xóa món", "Xóa món \"" + editing.name() + "\"?\nCác đơn hàng cũ vẫn giữ tên món đã bán.", "Xóa")) {
            return;
        }
        try {
            AppContext.get().catalog().deleteProduct(editing.id());
            newProduct();
            reloadProducts();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh món");
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

    @FXML
    private void editRecipe() {
        if (editing == null) {
            Dialogs.info("Định lượng", "Hãy lưu món trước khi khai báo định lượng.");
            return;
        }
        try {
            AppContext ctx = AppContext.get();
            Optional<List<RecipeLine>> result = RecipeDialog.show(editing, ctx.inventory().all(),
                    ctx.catalog().recipeOf(editing.id()));
            if (result.isPresent()) {
                ctx.catalog().saveRecipe(editing.id(), result.get());
                lblStatus.setText("Đã lưu định lượng (" + result.get().size() + " nguyên liệu).");
            }
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void manageCategories() {
        try {
            CategoryDialog.show(AppContext.get().catalog());
        } catch (Exception e) {
            Dialogs.handle(e);
        }
        reloadCategories();
        reloadProducts();
    }
}
