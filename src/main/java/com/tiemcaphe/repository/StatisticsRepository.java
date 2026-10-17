package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.DailyRevenue;
import com.tiemcaphe.model.DashboardSummary;
import com.tiemcaphe.model.ProductSales;
import com.tiemcaphe.model.StaffRevenue;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Các truy vấn thống kê; chỉ tính đơn ở trạng thái Đã thanh toán. */
public final class StatisticsRepository {

    private static final String PAID = "trang_thai = 'DA_THANH_TOAN'";

    private final Database db;

    public StatisticsRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    public record Totals(int orders, long revenue, long discount) {
        public long average() {
            return orders == 0 ? 0 : revenue / orders;
        }
    }

    public Totals totals(LocalDate from, LocalDate to) {
        return Sql.one(c(), "SELECT COUNT(*), COALESCE(SUM(thanh_tien),0), COALESCE(SUM(giam_gia),0) FROM don_hang WHERE "
                        + PAID + " AND ngay_tao >= ? AND ngay_tao < ?", ps -> {
                    ps.setString(1, Dates.dayStart(from));
                    ps.setString(2, Dates.dayEnd(to));
                }, rs -> new Totals(rs.getInt(1), rs.getLong(2), rs.getLong(3)))
                .orElse(new Totals(0, 0, 0));
    }

    public DashboardSummary summary(LocalDate today, long lowStockCount, long productCount) {
        Totals day = totals(today, today);
        Totals month = totals(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
        return new DashboardSummary(day.revenue(), day.orders(), month.revenue(), month.orders(), (int) lowStockCount,
                (int) productCount);
    }

    /** Doanh thu từng ngày trong khoảng [from, to], ngày không có đơn vẫn xuất hiện với 0. */
    public List<DailyRevenue> revenueByDay(LocalDate from, LocalDate to) {
        Map<String, DailyRevenue> byKey = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            byKey.put(d.format(Dates.DAY_KEY), new DailyRevenue(d.format(Dates.DAY_KEY), 0, 0));
        }
        Sql.list(c(), "SELECT substr(ngay_tao,1,10) AS k, COUNT(*), SUM(thanh_tien) FROM don_hang WHERE " + PAID
                + " AND ngay_tao >= ? AND ngay_tao < ? GROUP BY k", ps -> {
            ps.setString(1, Dates.dayStart(from));
            ps.setString(2, Dates.dayEnd(to));
        }, rs -> {
            byKey.put(rs.getString(1), new DailyRevenue(rs.getString(1), rs.getInt(2), rs.getLong(3)));
            return null;
        });
        return new ArrayList<>(byKey.values());
    }

    /** Doanh thu 12 tháng của một năm. */
    public List<DailyRevenue> revenueByMonth(int year) {
        Map<String, DailyRevenue> byKey = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String k = YearMonth.of(year, m).format(Dates.MONTH_KEY);
            byKey.put(k, new DailyRevenue(k, 0, 0));
        }
        Sql.list(c(), "SELECT substr(ngay_tao,1,7) AS k, COUNT(*), SUM(thanh_tien) FROM don_hang WHERE " + PAID
                + " AND ngay_tao >= ? AND ngay_tao < ? GROUP BY k", ps -> {
            ps.setString(1, Dates.dayStart(LocalDate.of(year, 1, 1)));
            ps.setString(2, Dates.dayStart(LocalDate.of(year + 1, 1, 1)));
        }, rs -> {
            byKey.put(rs.getString(1), new DailyRevenue(rs.getString(1), rs.getInt(2), rs.getLong(3)));
            return null;
        });
        return new ArrayList<>(byKey.values());
    }

    public List<ProductSales> topProducts(LocalDate from, LocalDate to, int limit) {
        return Sql.list(c(), """
                SELECT ct.ten_mon, SUM(ct.so_luong) AS sl, SUM(ct.thanh_tien) AS dt
                FROM chi_tiet_don_hang ct JOIN don_hang dh ON dh.id = ct.don_hang_id
                WHERE dh.trang_thai = 'DA_THANH_TOAN' AND dh.ngay_tao >= ? AND dh.ngay_tao < ?
                GROUP BY ct.ten_mon ORDER BY sl DESC, dt DESC LIMIT ?
                """, ps -> {
            ps.setString(1, Dates.dayStart(from));
            ps.setString(2, Dates.dayEnd(to));
            ps.setInt(3, limit);
        }, rs -> new ProductSales(rs.getString(1), rs.getInt(2), rs.getLong(3)));
    }

    public List<StaffRevenue> revenueByStaff(LocalDate from, LocalDate to) {
        return Sql.list(c(), "SELECT ten_nhan_vien, COUNT(*), SUM(thanh_tien) FROM don_hang WHERE " + PAID
                + " AND ngay_tao >= ? AND ngay_tao < ? GROUP BY ten_nhan_vien ORDER BY 3 DESC", ps -> {
            ps.setString(1, Dates.dayStart(from));
            ps.setString(2, Dates.dayEnd(to));
        }, rs -> new StaffRevenue(rs.getString(1), rs.getInt(2), rs.getLong(3)));
    }

    public record PaymentBreakdown(String method, int orders, long revenue) {
    }

    public List<PaymentBreakdown> byPaymentMethod(LocalDate from, LocalDate to) {
        return Sql.list(c(), "SELECT phuong_thuc, COUNT(*), SUM(thanh_tien) FROM don_hang WHERE " + PAID
                + " AND ngay_tao >= ? AND ngay_tao < ? GROUP BY phuong_thuc", ps -> {
            ps.setString(1, Dates.dayStart(from));
            ps.setString(2, Dates.dayEnd(to));
        }, rs -> new PaymentBreakdown(rs.getString(1), rs.getInt(2), rs.getLong(3)));
    }
}
