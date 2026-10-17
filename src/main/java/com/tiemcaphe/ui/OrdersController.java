package com.tiemcaphe.ui;

import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.OrderStatus;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.repository.OrderRepository;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.ui.dialog.ReceiptDialog;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Money;
import com.tiemcaphe.util.Session;
import com.tiemcaphe.util.Texts;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Tra cứu đơn hàng, xem chi tiết, in lại, hủy đơn (Quản trị). */
public class OrdersController implements View {

    private static final int LIMIT = 2000;
    private static final Account ALL_STAFF = new Account(-1, "", "Tất cả nhân viên", null, true, null);

    @FXML
    private DatePicker dpFrom;
    @FXML
    private DatePicker dpTo;
    @FXML
    private ComboBox<OrderStatus> cbStatus;
    @FXML
    private ComboBox<Account> cbStaff;
    @FXML
    private TextField txtKeyword;
    @FXML
    private TableView<Order> tblOrders;
    @FXML
    private TableColumn<Order, String> colCode;
    @FXML
    private TableColumn<Order, String> colTime;
    @FXML
    private TableColumn<Order, String> colStaff;
    @FXML
    private TableColumn<Order, String> colTable;
    @FXML
    private TableColumn<Order, Number> colTotal;
    @FXML
    private TableColumn<Order, String> colMethod;
    @FXML
    private TableColumn<Order, Order> colStatus;
    @FXML
    private Label lblSummary;
    @FXML
    private Label lblDetailTitle;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblStaff;
    @FXML
    private Label lblTable;
    @FXML
    private Label lblNote;
    @FXML
    private Label lblStatus;
    @FXML
    private TableView<OrderLine> tblLines;
    @FXML
    private TableColumn<OrderLine, String> colLineName;
    @FXML
    private TableColumn<OrderLine, Number> colLineQty;
    @FXML
    private TableColumn<OrderLine, Number> colLinePrice;
    @FXML
    private TableColumn<OrderLine, Number> colLineTotal;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblMethod;
    @FXML
    private Label lblPaid;
    @FXML
    private Button btnPrint;
    @FXML
    private Button btnCancel;

    private Order selected;

    @FXML
    private void initialize() {
        Widgets.datePicker(dpFrom);
        Widgets.datePicker(dpTo);
        Fx.text(colCode, Order::code);
        Fx.dateTime(colTime, Order::createdAt);
        Fx.text(colStaff, Order::staffName);
        Fx.text(colTable, o -> Texts.orDash(o.tableName()));
        Fx.money(colTotal, Order::total);
        Fx.text(colMethod, o -> o.method().label());
        colStatus.setCellValueFactory(d -> new javafx.beans.property.ReadOnlyObjectWrapper<>(d.getValue()));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(Widgets.badge(item.status().label(), item.isCancelled() ? "badge-danger" : "badge-success"));
                }
            }
        });
        Fx.center(colStatus);
        Widgets.flex(tblOrders);
        Widgets.rowStyle(tblOrders, "row-cancelled", Order::isCancelled);
        Fx.placeholder(tblOrders, "Không có đơn hàng trong khoảng đã chọn.");
        tblOrders.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> showDetail(b));

        Fx.text(colLineName, OrderLine::productName);
        Fx.integer(colLineQty, OrderLine::quantity);
        Fx.money(colLinePrice, OrderLine::unitPrice);
        Fx.money(colLineTotal, OrderLine::lineTotal);
        Widgets.flex(tblLines);
        Fx.placeholder(tblLines, "Chọn một đơn để xem chi tiết.");

        List<OrderStatus> statuses = new ArrayList<>();
        statuses.add(null);
        statuses.addAll(List.of(OrderStatus.values()));
        cbStatus.setItems(FXCollections.observableArrayList(statuses));
        cbStatus.setConverter(new StringConverter<>() {
            @Override
            public String toString(OrderStatus s) {
                return s == null ? "Tất cả trạng thái" : s.label();
            }

            @Override
            public OrderStatus fromString(String s) {
                return null;
            }
        });
        cbStatus.setValue(null);
        cbStatus.setPromptText("Tất cả trạng thái");
        cbStaff.setConverter(new StringConverter<>() {
            @Override
            public String toString(Account a) {
                return a == null ? "Tất cả nhân viên" : a.fullName();
            }

            @Override
            public Account fromString(String s) {
                return null;
            }
        });

        boolean admin = Session.isAdmin();
        btnCancel.setVisible(admin);
        btnCancel.setManaged(admin);
        showDetail(null);
        today();
    }

    @Override
    public void onShow() {
        try {
            Account sel = cbStaff.getValue();
            List<Account> staff = new ArrayList<>();
            staff.add(ALL_STAFF);
            staff.addAll(AppContext.get().auth().all());
            cbStaff.setItems(FXCollections.observableArrayList(staff));
            cbStaff.setValue(sel != null && staff.contains(sel) ? sel : ALL_STAFF);
        } catch (Exception e) {
            Dialogs.handle(e);
        }
        search();
    }

    @FXML
    private void today() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t);
        dpTo.setValue(t);
        search();
    }

    @FXML
    private void week() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t.minusDays(6));
        dpTo.setValue(t);
        search();
    }

    @FXML
    private void month() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t.withDayOfMonth(1));
        dpTo.setValue(t);
        search();
    }

    @FXML
    private void search() {
        if (dpFrom.getValue() != null && dpTo.getValue() != null && dpFrom.getValue().isAfter(dpTo.getValue())) {
            LocalDate t = dpFrom.getValue();
            dpFrom.setValue(dpTo.getValue());
            dpTo.setValue(t);
        }
        try {
            Account staff = cbStaff.getValue();
            Long accountId = staff == null || staff == ALL_STAFF ? null : staff.id();
            OrderRepository.Filter f = new OrderRepository.Filter(dpFrom.getValue(), dpTo.getValue(), cbStatus.getValue(),
                    accountId, txtKeyword.getText());
            List<Order> orders = AppContext.get().orders().find(f, LIMIT);
            Long keep = selected == null ? null : selected.id();
            tblOrders.setItems(FXCollections.observableArrayList(orders));
            long paid = orders.stream().filter(o -> !o.isCancelled()).mapToLong(Order::total).sum();
            long cancelled = orders.stream().filter(Order::isCancelled).count();
            lblSummary.setText(orders.size() + " đơn" + (orders.size() >= LIMIT ? " (hiển thị tối đa " + LIMIT + ")" : "")
                    + "  ·  Doanh thu đã thanh toán: " + Money.format(paid)
                    + (cancelled > 0 ? "  ·  Đã hủy: " + cancelled : ""));
            if (keep != null) {
                orders.stream().filter(o -> o.id() == keep).findFirst().ifPresent(o -> tblOrders.getSelectionModel().select(o));
            }
            if (tblOrders.getSelectionModel().getSelectedItem() == null) {
                showDetail(null);
            }
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void showDetail(Order o) {
        selected = o;
        boolean has = o != null;
        btnPrint.setDisable(!has);
        btnCancel.setDisable(!has || o.isCancelled());
        if (!has) {
            lblDetailTitle.setText("Chi tiết đơn");
            for (Label l : List.of(lblTime, lblStaff, lblTable, lblNote, lblStatus, lblMethod, lblPaid)) {
                l.setText("—");
            }
            lblSubtotal.setText(Money.format(0));
            lblDiscount.setText(Money.format(0));
            lblTotal.setText(Money.format(0));
            tblLines.setItems(FXCollections.observableArrayList());
            return;
        }
        lblDetailTitle.setText("Đơn " + o.code());
        lblTime.setText(Dates.display(o.createdAt()));
        lblStaff.setText(o.staffName());
        lblTable.setText(Texts.orDash(o.tableName()));
        lblNote.setText(Texts.orDash(o.note()));
        lblStatus.setText(o.status().label() + (o.isCancelled() && o.cancelReason() != null ? " – " + o.cancelReason() : ""));
        lblSubtotal.setText(Money.format(o.subtotal()));
        lblDiscount.setText(Money.format(o.discount()));
        lblTotal.setText(Money.format(o.total()));
        lblMethod.setText(o.method().label());
        lblPaid.setText(o.method() == PaymentMethod.TIEN_MAT
                ? Money.format(o.paid()) + " / " + Money.format(o.change()) : "—");
        try {
            tblLines.setItems(FXCollections.observableArrayList(AppContext.get().orders().linesOf(o.id())));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void reprint() {
        if (selected == null) {
            return;
        }
        try {
            AppContext ctx = AppContext.get();
            ReceiptDialog.show(selected, ctx.orders().linesOf(selected.id()), ctx.settings().shopInfo());
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void cancelOrder() {
        if (selected == null) {
            return;
        }
        Optional<String> reason = Dialogs.prompt("Hủy đơn " + selected.code(),
                "Lý do hủy (bắt buộc). Kho sẽ được hoàn lại theo định lượng hiện tại.", "");
        if (reason.isEmpty()) {
            return;
        }
        try {
            AppContext.get().orders().cancel(selected.id(), reason.get(), Session.require());
            search();
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }
}
