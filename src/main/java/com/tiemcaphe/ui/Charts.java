package com.tiemcaphe.ui;

import com.tiemcaphe.model.DailyRevenue;
import com.tiemcaphe.util.Dates;
import com.tiemcaphe.util.Money;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

import java.util.List;

/** Biểu đồ cột doanh thu với trục tiền rút gọn (120k, 1,5 tr). */
public final class Charts {

    private Charts() {
    }

    public static void setup(BarChart<String, Number> chart, NumberAxis yAxis) {
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCategoryGap(14);
        chart.setBarGap(2);
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(true);
        yAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return compact(n.longValue());
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });
    }

    public static void fill(BarChart<String, Number> chart, List<DailyRevenue> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DailyRevenue d : data) {
            XYChart.Data<String, Number> point = new XYChart.Data<>(Dates.keyLabel(d.key()), d.revenue());
            String tip = Dates.keyLabel(d.key()) + ": " + Money.format(d.revenue()) + " (" + d.orders() + " đơn)";
            point.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(tip));
                }
            });
            series.getData().add(point);
        }
        chart.getData().setAll(List.of(series));
    }

    public static String compact(long v) {
        if (v >= 1_000_000_000L) {
            return trim(v / 1_000_000_000.0) + " tỷ";
        }
        if (v >= 1_000_000L) {
            return trim(v / 1_000_000.0) + " tr";
        }
        if (v >= 1_000L) {
            return trim(v / 1_000.0) + "k";
        }
        return String.valueOf(v);
    }

    private static String trim(double d) {
        String s = String.format(java.util.Locale.ROOT, "%.1f", d);
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }
        return s.replace('.', ',');
    }
}
