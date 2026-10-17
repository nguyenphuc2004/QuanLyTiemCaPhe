package com.tiemcaphe.ui;

import com.tiemcaphe.model.DailyRevenue;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.ProductSales;
import com.tiemcaphe.model.StaffRevenue;
import com.tiemcaphe.repository.StatisticsRepository;
import com.tiemcaphe.service.AppContext;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Money;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Thống kê doanh thu theo ngày / tháng / món / nhân viên / hình thức thanh toán, xuất CSV. */
public class StatsController implements View {

    @FXML
    private DatePicker dpFrom;
    @FXML
    private DatePicker dpTo;
    @FXML
    private Label kpiRevenue;
    @FXML
    private Label kpiOrders;
    @FXML
    private Label kpiAvg;
    @FXML
    private Label kpiDiscount;
    @FXML
    private BarChart<String, Number> chartDay;
    @FXML
    private NumberAxis axisDayY;
    @FXML
    private TableView<DailyRevenue> tblDay;
    @FXML
    private TableColumn<DailyRevenue, String> colDayKey;
    @FXML
    private TableColumn<DailyRevenue, Number> colDayOrders;
    @FXML
    private TableColumn<DailyRevenue, Number> colDayRevenue;
    @FXML
    private ComboBox<Integer> cbYear;
    @FXML
    private BarChart<String, Number> chartMonth;
    @FXML
    private NumberAxis axisMonthY;
    @FXML
    private TableView<DailyRevenue> tblMonth;
    @FXML
    private TableColumn<DailyRevenue, String> colMonthKey;
    @FXML
    private TableColumn<DailyRevenue, Number> colMonthOrders;
    @FXML
    private TableColumn<DailyRevenue, Number> colMonthRevenue;
    @FXML
    private TableView<ProductSales> tblTop;
    @FXML
    private TableColumn<ProductSales, Number> colTopRank;
    @FXML
    private TableColumn<ProductSales, String> colTopName;
    @FXML
    private TableColumn<ProductSales, Number> colTopQty;
    @FXML
    private TableColumn<ProductSales, Number> colTopRevenue;
    @FXML
    private TableView<StaffRevenue> tblStaff;
    @FXML
    private TableColumn<StaffRevenue, String> colStaffName;
    @FXML
    private TableColumn<StaffRevenue, Number> colStaffOrders;
    @FXML
    private TableColumn<StaffRevenue, Number> colStaffRevenue;
    @FXML
    private TableView<StatisticsRepository.PaymentBreakdown> tblPay;
    @FXML
    private TableColumn<StatisticsRepository.PaymentBreakdown, String> colPayMethod;
    @FXML
    private TableColumn<StatisticsRepository.PaymentBreakdown, Number> colPayOrders;
    @FXML
    private TableColumn<StatisticsRepository.PaymentBreakdown, Number> colPayRevenue;

    private List<DailyRevenue> dayData = List.of();
    private List<ProductSales> topData = List.of();
    private List<StaffRevenue> staffData = List.of();

    @FXML
    private void initialize() {
        Widgets.datePicker(dpFrom);
        Widgets.datePicker(dpTo);
        Charts.setup(chartDay, axisDayY);
        Charts.setup(chartMonth, axisMonthY);

        Fx.text(colDayKey, d -> LocalDate.parse(d.key()).format(Dates.DISPLAY_DATE));
        Fx.integer(colDayOrders, DailyRevenue::orders);
        Fx.money(colDayRevenue, DailyRevenue::revenue);
        Widgets.flex(tblDay);
        Fx.text(colMonthKey, d -> Dates.keyLabel(d.key()));
        Fx.integer(colMonthOrders, DailyRevenue::orders);
        Fx.money(colMonthRevenue, DailyRevenue::revenue);
        Widgets.flex(tblMonth);
        colTopRank.setCellValueFactory(d -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                tblTop.getItems().indexOf(d.getValue()) + 1));
        Fx.center(colTopRank);
        Fx.text(colTopName, ProductSales::name);
        Fx.integer(colTopQty, ProductSales::quantity);
        Fx.money(colTopRevenue, ProductSales::revenue);
        Widgets.flex(tblTop);
        Fx.text(colStaffName, StaffRevenue::name);
        Fx.integer(colStaffOrders, StaffRevenue::orders);
        Fx.money(colStaffRevenue, StaffRevenue::revenue);
        Widgets.flex(tblStaff);
        Fx.text(colPayMethod, p -> PaymentMethod.valueOf(p.method()).label());
        Fx.integer(colPayOrders, StatisticsRepository.PaymentBreakdown::orders);
        Fx.money(colPayRevenue, StatisticsRepository.PaymentBreakdown::revenue);
        Widgets.flex(tblPay);
        for (TableView<?> t : List.of(tblDay, tblMonth, tblTop, tblStaff, tblPay)) {
            Fx.placeholder(t, "Không có dữ liệu trong khoảng đã chọn.");
        }

        int year = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = year; y >= year - 4; y--) {
            years.add(y);
        }
        cbYear.setItems(FXCollections.observableArrayList(years));
        cbYear.setValue(year);
        cbYear.valueProperty().addListener((o, a, b) -> loadMonth());
        thisMonth();
    }

    @Override
    public void onShow() {
        apply();
        loadMonth();
    }

    @FXML
    private void today() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t);
        dpTo.setValue(t);
        apply();
    }

    @FXML
    private void week() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t.minusDays(6));
        dpTo.setValue(t);
        apply();
    }

    @FXML
    private void thisMonth() {
        LocalDate t = LocalDate.now();
        dpFrom.setValue(t.withDayOfMonth(1));
        dpTo.setValue(t);
        apply();
    }

    @FXML
    private void lastMonth() {
        LocalDate first = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        dpFrom.setValue(first);
        dpTo.setValue(first.withDayOfMonth(first.lengthOfMonth()));
        apply();
    }

    @FXML
    private void apply() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        if (from == null || to == null) {
            Dialogs.warn("Thống kê", "Hãy chọn đủ ngày bắt đầu và kết thúc.");
            return;
        }
        if (from.isAfter(to)) {
            LocalDate t = from;
            from = to;
            to = t;
            dpFrom.setValue(from);
            dpTo.setValue(to);
        }
        try {
            StatisticsRepository stats = AppContext.get().statistics();
            StatisticsRepository.Totals t = stats.totals(from, to);
            kpiRevenue.setText(Money.format(t.revenue()));
            kpiOrders.setText(String.valueOf(t.orders()));
            kpiAvg.setText(Money.format(t.average()));
            kpiDiscount.setText(Money.format(t.discount()));
            dayData = stats.revenueByDay(from, to);
            Charts.fill(chartDay, dayData);
            tblDay.setItems(FXCollections.observableArrayList(dayData));
            topData = stats.topProducts(from, to, 100);
            tblTop.setItems(FXCollections.observableArrayList(topData));
            staffData = stats.revenueByStaff(from, to);
            tblStaff.setItems(FXCollections.observableArrayList(staffData));
            tblPay.setItems(FXCollections.observableArrayList(stats.byPaymentMethod(from, to)));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    private void loadMonth() {
        Integer year = cbYear.getValue();
        if (year == null) {
            return;
        }
        try {
            List<DailyRevenue> data = AppContext.get().statistics().revenueByMonth(year);
            Charts.fill(chartMonth, data);
            tblMonth.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            Dialogs.handle(e);
        }
    }

    @FXML
    private void exportCsv() {
        if (dpFrom.getValue() == null || dpTo.getValue() == null) {
            return;
        }
        apply();
        FileChooser fc = new FileChooser();
        fc.setTitle("Xuất báo cáo CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (Excel)", "*.csv"));
        fc.setInitialFileName("bao-cao-" + dpFrom.getValue() + "_" + dpTo.getValue() + ".csv");
        File f = fc.showSaveDialog(Fx.windowOf(dpFrom));
        if (f == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("BAO CAO DOANH THU,").append(Dates.display(dpFrom.getValue())).append(" - ").append(Dates.display(dpTo.getValue())).append('\n');
        sb.append("\nDoanh thu theo ngay\nNgay,So don,Doanh thu\n");
        for (DailyRevenue d : dayData) {
            sb.append(LocalDate.parse(d.key()).format(Dates.DISPLAY_DATE)).append(',').append(d.orders()).append(',').append(d.revenue()).append('\n');
        }
        sb.append("\nMon ban chay\nMon,So luong,Doanh thu\n");
        for (ProductSales p : topData) {
            sb.append(csv(p.name())).append(',').append(p.quantity()).append(',').append(p.revenue()).append('\n');
        }
        sb.append("\nTheo nhan vien\nNhan vien,So don,Doanh thu\n");
        for (StaffRevenue s : staffData) {
            sb.append(csv(s.name())).append(',').append(s.orders()).append(',').append(s.revenue()).append('\n');
        }
        try {
            Files.writeString(f.toPath(), sb.toString(), StandardCharsets.UTF_8);
            Dialogs.info("Xuất CSV", "Đã lưu báo cáo:\n" + f.getAbsolutePath());
        } catch (IOException e) {
            Dialogs.error("Xuất CSV", "Không ghi được file: " + e.getMessage());
        }
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
