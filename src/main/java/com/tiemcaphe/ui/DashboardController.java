package com.tiemcaphe.ui;

import com.tiemcaphe.model.DashboardSummary;
import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.ProductSales;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Money;
import com.tiemcaphe.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class DashboardController implements View {

    @FXML
    private Label lblGreeting;
    @FXML
    private Label kpiToday;
    @FXML
    private Label kpiTodayOrders;
    @FXML
    private Label kpiMonth;
    @FXML
    private Label kpiMonthOrders;
    @FXML
    private Label kpiAvg;
    @FXML
    private Label kpiProducts;
    @FXML
    private Label kpiLow;
    @FXML
    private VBox kpiLowBox;
    @FXML
    private BarChart<String, Number> chartWeek;
    @FXML
    private NumberAxis axisWeekY;
    @FXML
    private TableView<ProductSales> tblTop;
    @FXML
    private TableColumn<ProductSales, String> colTopName;
    @FXML
    private TableColumn<ProductSales, Number> colTopQty;
    @FXML
    private TableColumn<ProductSales, Number> colTopRevenue;
    @FXML
    private TableView<Ingredient> tblLow;
    @FXML
    private TableColumn<Ingredient, String> colLowName;
    @FXML
    private TableColumn<Ingredient, Number> colLowQty;
    @FXML
    private TableColumn<Ingredient, String> colLowUnit;
    @FXML
    private TableColumn<Ingredient, Number> colLowThreshold;
    @FXML
    private TableColumn<Ingredient, String> colLowUpdated;

    @FXML
    private void initialize() {
        Charts.setup(chartWeek, axisWeekY);
        Fx.text(colTopName, ProductSales::name);
        Fx.integer(colTopQty, ProductSales::quantity);
        Fx.money(colTopRevenue, ProductSales::revenue);
        Widgets.flex(tblTop);
        Fx.placeholder(tblTop, "Chưa có đơn hàng trong tháng.");

        Fx.text(colLowName, Ingredient::name);
        Fx.qty(colLowQty, Ingredient::quantity);
        Fx.text(colLowUnit, Ingredient::unit);
        Fx.qty(colLowThreshold, Ingredient::warnThreshold);
        Fx.dateTime(colLowUpdated, Ingredient::updatedAt);
        Widgets.flex(tblLow);
        Widgets.rowStyle(tblLow, "row-low", Ingredient::isLow);
        Fx.placeholder(tblLow, "Tất cả nguyên liệu đều trên ngưỡng cảnh báo.");
    }

    @Override
    public void onShow() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            AppContext ctx = AppContext.get();
            LocalDate today = LocalDate.now();
            lblGreeting.setText("Xin chào " + Session.require().fullName() + ", hôm nay " + Dates.display(today));

            DashboardSummary s = ctx.statistics().summary(today, ctx.inventory().countLow(), ctx.products().count());
            kpiToday.setText(Money.format(s.todayRevenue()));
            kpiTodayOrders.setText(s.todayOrders() + " đơn đã thanh toán");
            kpiMonth.setText(Money.format(s.monthRevenue()));
            kpiMonthOrders.setText(s.monthOrders() + " đơn trong tháng " + today.getMonthValue());
            kpiAvg.setText(Money.format(s.todayOrders() == 0 ? 0 : s.todayRevenue() / s.todayOrders()));
            kpiProducts.setText(s.productCount() + " món trong thực đơn");
            kpiLow.setText(String.valueOf(s.lowStockCount()));
            kpiLowBox.getStyleClass().removeAll("kpi-warn", "kpi-ok");
            kpiLowBox.getStyleClass().add(s.lowStockCount() > 0 ? "kpi-warn" : "kpi-ok");

            Charts.fill(chartWeek, ctx.statistics().revenueByDay(today.minusDays(6), today));
            tblTop.setItems(FXCollections.observableArrayList(
                    ctx.statistics().topProducts(today.withDayOfMonth(1), today, 8)));
            tblLow.setItems(FXCollections.observableArrayList(ctx.inventory().low()));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }
}
