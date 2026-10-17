package com.tiemcaphe.ui;

import com.tiemcaphe.model.CartLine;
import com.tiemcaphe.model.Category;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.service.OrderService;
import com.tiemcaphe.ui.dialog.ReceiptDialog;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.Money;
import com.tiemcaphe.util.Session;
import com.tiemcaphe.util.Texts;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Màn hình bán hàng: chọn món → giỏ hàng → thanh toán → in hóa đơn. */
public class PosController implements View {

    private static final Long ALL = -1L;
    private static final long[] QUICK_CASH = {50_000, 100_000, 200_000, 500_000};

    @FXML
    private TextField txtSearch;
    @FXML
    private HBox categoryBar;
    @FXML
    private FlowPane productPane;
    @FXML
    private Label lblProductCount;
    @FXML
    private Label lblCartCount;
    @FXML
    private TableView<CartLine> tblCart;
    @FXML
    private TableColumn<CartLine, String> colCartName;
    @FXML
    private TableColumn<CartLine, Number> colCartPrice;
    @FXML
    private TableColumn<CartLine, CartLine> colCartQty;
    @FXML
    private TableColumn<CartLine, Number> colCartTotal;
    @FXML
    private TableColumn<CartLine, CartLine> colCartRemove;
    @FXML
    private TextField txtTable;
    @FXML
    private TextField txtNote;
    @FXML
    private Label lblSubtotal;
    @FXML
    private TextField txtDiscount;
    @FXML
    private Label lblTotal;
    @FXML
    private ToggleButton tgCash;
    @FXML
    private ToggleButton tgTransfer;
    @FXML
    private GridPane cashPane;
    @FXML
    private TextField txtPaid;
    @FXML
    private HBox quickCash;
    @FXML
    private Label lblChange;
    @FXML
    private Button btnCheckout;

    private final ObservableList<CartLine> cart = FXCollections.observableArrayList();
    private final ToggleGroup categoryGroup = new ToggleGroup();
    private final ToggleGroup payGroup = new ToggleGroup();
    private List<Product> products = List.of();
    private Long selectedCategory = ALL;
    private long total;

    @FXML
    private void initialize() {
        Fx.text(colCartName, l -> l.product().name());
        Fx.money(colCartPrice, l -> l.product().price());
        colCartTotal.setCellValueFactory(d -> d.getValue().lineTotalBinding());
        colCartTotal.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Money.formatPlain(item.longValue()));
            }
        });
        Fx.right(colCartTotal);
        colCartQty.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colCartQty.setCellFactory(c -> new QtyCell());
        colCartQty.setSortable(false);
        Fx.center(colCartQty);
        colCartRemove.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue()));
        colCartRemove.setCellFactory(c -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setGraphic(Icons.icon(Icons.CLOSE, 14));
                btn.getStyleClass().addAll("btn-ghost", "btn-sm");
                btn.setOnAction(e -> cart.remove(getItem()));
            }

            @Override
            protected void updateItem(CartLine item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : btn);
            }
        });
        colCartRemove.setSortable(false);
        tblCart.setItems(cart);
        Widgets.flex(tblCart);
        Fx.placeholder(tblCart, "Chạm vào món bên trái để thêm vào đơn.");
        cart.addListener((ListChangeListener<CartLine>) c -> recompute());

        Fx.moneyInput(txtDiscount);
        Fx.moneyInput(txtPaid);
        txtDiscount.textProperty().addListener((o, a, b) -> recompute());
        txtPaid.textProperty().addListener((o, a, b) -> recompute());

        tgCash.setToggleGroup(payGroup);
        tgTransfer.setToggleGroup(payGroup);
        payGroup.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
            recompute();
        });
        cashPane.visibleProperty().bind(tgCash.selectedProperty());
        cashPane.managedProperty().bind(tgCash.selectedProperty());

        Button exact = new Button("Đưa đủ");
        exact.getStyleClass().add("btn-sm");
        exact.setOnAction(e -> txtPaid.setText(Money.formatPlain(total)));
        quickCash.getChildren().add(exact);
        for (long amount : QUICK_CASH) {
            Button b = new Button(Money.formatPlain(amount));
            b.getStyleClass().add("btn-sm");
            b.setOnAction(e -> txtPaid.setText(Money.formatPlain(amount)));
            quickCash.getChildren().add(b);
        }

        txtSearch.textProperty().addListener((o, a, b) -> renderProducts());
        recompute();
    }

    @Override
    public void onShow() {
        reload();
    }

    private void reload() {
        try {
            AppContext ctx = AppContext.get();
            products = ctx.catalog().availableProducts();
            buildCategoryBar(ctx.catalog().allCategories());
            renderProducts();
            syncCart();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void buildCategoryBar(List<Category> categories) {
        categoryBar.getChildren().clear();
        categoryGroup.getToggles().clear();
        addChip("Tất cả", ALL);
        for (Category c : categories) {
            addChip(c.name(), c.id());
        }
        boolean hasUncategorized = products.stream().anyMatch(p -> p.categoryId() == null);
        if (hasUncategorized) {
            addChip("Chưa phân loại", 0L);
        }
        boolean stillExists = categoryGroup.getToggles().stream().anyMatch(t -> selectedCategory.equals(t.getUserData()));
        if (!stillExists) {
            selectedCategory = ALL;
        }
        for (var t : categoryGroup.getToggles()) {
            if (selectedCategory.equals(t.getUserData())) {
                t.setSelected(true);
            }
        }
    }

    private void addChip(String text, Long categoryId) {
        ToggleButton chip = new ToggleButton(text);
        chip.getStyleClass().add("cat-chip");
        chip.setUserData(categoryId);
        chip.setToggleGroup(categoryGroup);
        chip.setOnAction(e -> {
            if (!chip.isSelected()) {
                chip.setSelected(true);
            }
            selectedCategory = categoryId;
            renderProducts();
        });
        categoryBar.getChildren().add(chip);
    }

    private void renderProducts() {
        productPane.getChildren().clear();
        String q = txtSearch.getText();
        int shown = 0;
        for (Product p : products) {
            boolean inCategory = ALL.equals(selectedCategory)
                    || (selectedCategory == 0L && p.categoryId() == null)
                    || selectedCategory.equals(p.categoryId());
            if (!inCategory || !Texts.contains(p.name(), q)) {
                continue;
            }
            productPane.getChildren().add(card(p));
            shown++;
        }
        lblProductCount.setText(shown + " món đang bán");
        if (shown == 0) {
            Label empty = new Label(products.isEmpty() ? "Thực đơn chưa có món nào đang bán. Vào mục Thực đơn để thêm."
                    : "Không có món phù hợp.");
            empty.getStyleClass().add("muted");
            productPane.getChildren().add(empty);
        }
    }

    private VBox card(Product p) {
        ImageView img = new ImageView(Images.toImage(p.image()));
        img.setFitWidth(118);
        img.setFitHeight(118);
        img.setPreserveRatio(true);
        img.getStyleClass().add("product-image");
        javafx.scene.layout.StackPane frame = new javafx.scene.layout.StackPane(img);
        frame.setMinSize(118, 118);
        frame.setPrefSize(118, 118);
        Label name = new Label(p.name());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxWidth(134);
        name.setAlignment(Pos.CENTER);
        name.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Label price = new Label(Money.format(p.price()));
        price.getStyleClass().add("product-price");
        VBox box = new VBox(6, frame, name, price);
        box.getStyleClass().add("product-card");
        box.setAlignment(Pos.TOP_CENTER);
        box.setOnMouseClicked(e -> addToCart(p));
        return box;
    }

    /** Sau khi rời màn hình (sửa giá, ngừng bán món...), cập nhật lại giỏ theo thực đơn mới nhất. */
    private void syncCart() {
        if (cart.isEmpty()) {
            return;
        }
        List<CartLine> fresh = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        boolean changed = false;
        for (CartLine l : cart) {
            Product now = products.stream().filter(p -> p.id() == l.product().id()).findFirst().orElse(null);
            if (now == null) {
                removed.add(l.product().name());
                changed = true;
                continue;
            }
            if (now.price() != l.product().price() || !now.name().equals(l.product().name())) {
                CartLine nl = new CartLine(now);
                nl.setQuantity(l.getQuantity());
                nl.quantityProperty().addListener((o, a, b) -> recompute());
                fresh.add(nl);
                changed = true;
            } else {
                fresh.add(l);
            }
        }
        if (changed) {
            cart.setAll(fresh);
            if (!removed.isEmpty()) {
                Dialogs.info("Giỏ hàng", "Đã bỏ khỏi giỏ vì món không còn bán: " + String.join(", ", removed));
            }
        }
    }

    private void addToCart(Product p) {
        for (CartLine l : cart) {
            if (l.product().id() == p.id()) {
                l.setQuantity(l.getQuantity() + 1);
                tblCart.refresh();
                recompute();
                return;
            }
        }
        CartLine line = new CartLine(p);
        line.quantityProperty().addListener((o, a, b) -> recompute());
        cart.add(line);
        tblCart.scrollTo(line);
    }

    private void recompute() {
        long subtotal = 0;
        int items = 0;
        for (CartLine l : cart) {
            subtotal += l.getLineTotal();
            items += l.getQuantity();
        }
        long discount = safeParse(txtDiscount.getText());
        if (discount > subtotal) {
            discount = subtotal;
        }
        total = subtotal - discount;
        lblSubtotal.setText(Money.format(subtotal));
        lblTotal.setText(Money.format(total));
        lblCartCount.setText(items == 0 ? "" : "(" + items + " phần)");
        long paid = safeParse(txtPaid.getText());
        if (!tgCash.isSelected() || paid <= 0) {
            lblChange.setText(Money.format(0));
            lblChange.getStyleClass().remove("error-text");
        } else if (paid < total) {
            lblChange.setText("Thiếu " + Money.format(total - paid));
            if (!lblChange.getStyleClass().contains("error-text")) {
                lblChange.getStyleClass().add("error-text");
            }
        } else {
            lblChange.setText(Money.format(paid - total));
            lblChange.getStyleClass().remove("error-text");
        }
        btnCheckout.setDisable(cart.isEmpty());
    }

    private static long safeParse(String s) {
        try {
            return Money.parse(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FXML
    private void clearCart() {
        if (cart.isEmpty() || Dialogs.confirm("Xóa giỏ hàng", "Bỏ toàn bộ món đang chọn?")) {
            resetCart();
        }
    }

    private void resetCart() {
        cart.clear();
        txtTable.clear();
        txtNote.clear();
        txtDiscount.clear();
        txtPaid.clear();
        tgCash.setSelected(true);
        recompute();
    }

    @FXML
    private void checkout() {
        try {
            PaymentMethod method = tgTransfer.isSelected() ? PaymentMethod.CHUYEN_KHOAN : PaymentMethod.TIEN_MAT;
            long subtotal = cart.stream().mapToLong(CartLine::getLineTotal).sum();
            long discount = Math.min(Fx.moneyValue(txtDiscount, "Giảm giá"), subtotal);
            OrderService.CheckoutRequest req = new OrderService.CheckoutRequest(new ArrayList<>(cart),
                    txtTable.getText(), txtNote.getText(), discount,
                    Fx.moneyValue(txtPaid, "Tiền khách đưa"), method);
            AppContext ctx = AppContext.get();
            Order order = ctx.orders().checkout(req, Session.require());
            resetCart();
            ReceiptDialog.show(order, ctx.orders().linesOf(order.id()), ctx.settings().shopInfo());
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    /** Ô số lượng với nút - / +. */
    private final class QtyCell extends TableCell<CartLine, CartLine> {
        private final Button minus = new Button("−");
        private final Button plus = new Button("+");
        private final Label qty = new Label();
        private final HBox box = new HBox(4, minus, qty, plus);
        private CartLine bound;

        QtyCell() {
            minus.getStyleClass().addAll("qty-button", "btn-sm");
            plus.getStyleClass().addAll("qty-button", "btn-sm");
            qty.getStyleClass().add("qty-label");
            box.setAlignment(Pos.CENTER);
            minus.setOnAction(e -> {
                CartLine l = getItem();
                if (l != null) {
                    if (l.getQuantity() <= 1) {
                        cart.remove(l);
                    } else {
                        l.setQuantity(l.getQuantity() - 1);
                    }
                }
            });
            plus.setOnAction(e -> {
                CartLine l = getItem();
                if (l != null) {
                    l.setQuantity(l.getQuantity() + 1);
                }
            });
        }

        @Override
        protected void updateItem(CartLine item, boolean empty) {
            super.updateItem(item, empty);
            if (bound != null) {
                qty.textProperty().unbind();
                bound = null;
            }
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            bound = item;
            qty.textProperty().bind(item.quantityProperty().asString());
            setGraphic(box);
        }
    }
}
