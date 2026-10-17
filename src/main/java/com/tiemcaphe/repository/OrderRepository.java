package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.OrderStatus;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OrderRepository {

    private static final String COLS = """
            id, ma_don, tai_khoan_id, ten_nhan_vien, ban, ghi_chu, tong_tien, giam_gia, thanh_tien,
            khach_dua, tien_thua, phuong_thuc, trang_thai, ly_do_huy, ngay_tao
            """;

    /** Bộ lọc tìm đơn hàng; trường null nghĩa là không lọc. Từ khóa được xử lý ở OrderService (so không dấu). */
    public record Filter(LocalDate from, LocalDate to, OrderStatus status, Long accountId, String keyword) {
    }

    private final Database db;

    public OrderRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    static Order map(ResultSet rs) throws SQLException {
        return new Order(rs.getLong("id"), rs.getString("ma_don"), Sql.longOrNull(rs, "tai_khoan_id"),
                rs.getString("ten_nhan_vien"), rs.getString("ban"), rs.getString("ghi_chu"), rs.getLong("tong_tien"),
                rs.getLong("giam_gia"), rs.getLong("thanh_tien"), rs.getLong("khach_dua"), rs.getLong("tien_thua"),
                PaymentMethod.valueOf(rs.getString("phuong_thuc")), OrderStatus.valueOf(rs.getString("trang_thai")),
                rs.getString("ly_do_huy"), Dates.fromDb(rs.getString("ngay_tao")));
    }

    static OrderLine mapLine(ResultSet rs) throws SQLException {
        return new OrderLine(rs.getLong("id"), rs.getLong("don_hang_id"), Sql.longOrNull(rs, "mon_id"),
                rs.getString("ten_mon"), rs.getLong("don_gia"), rs.getInt("so_luong"), rs.getLong("thanh_tien"));
    }

    /** Sinh mã đơn dạng HDyyyyMMdd-0001, tăng dần trong ngày. Gọi bên trong giao dịch tạo đơn. */
    public String nextCode(LocalDate day) {
        String prefix = "HD" + day.format(Dates.CODE) + "-";
        long max = Sql.scalarLong(c(),
                "SELECT COALESCE(MAX(CAST(substr(ma_don, ?) AS INTEGER)), 0) FROM don_hang WHERE ma_don LIKE ?",
                ps -> {
                    ps.setInt(1, prefix.length() + 1);
                    ps.setString(2, prefix + "%");
                });
        return prefix + String.format("%04d", max + 1);
    }

    public long insert(Order o) {
        return Sql.insert(c(), """
                        INSERT INTO don_hang (ma_don, tai_khoan_id, ten_nhan_vien, ban, ghi_chu, tong_tien, giam_gia,
                          thanh_tien, khach_dua, tien_thua, phuong_thuc, trang_thai, ly_do_huy, ngay_tao)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                        """,
                ps -> {
                    ps.setString(1, o.code());
                    Sql.setLongOrNull(ps, 2, o.accountId());
                    ps.setString(3, o.staffName());
                    ps.setString(4, Sql.blank(o.tableName()));
                    ps.setString(5, Sql.blank(o.note()));
                    ps.setLong(6, o.subtotal());
                    ps.setLong(7, o.discount());
                    ps.setLong(8, o.total());
                    ps.setLong(9, o.paid());
                    ps.setLong(10, o.change());
                    ps.setString(11, o.method().name());
                    ps.setString(12, o.status().name());
                    ps.setString(13, Sql.blank(o.cancelReason()));
                    ps.setString(14, Dates.toDb(o.createdAt()));
                });
    }

    public void insertLine(OrderLine l) {
        Sql.insert(c(),
                "INSERT INTO chi_tiet_don_hang (don_hang_id, mon_id, ten_mon, don_gia, so_luong, thanh_tien) VALUES (?,?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, l.orderId());
                    Sql.setLongOrNull(ps, 2, l.productId());
                    ps.setString(3, l.productName());
                    ps.setLong(4, l.unitPrice());
                    ps.setInt(5, l.quantity());
                    ps.setLong(6, l.lineTotal());
                });
    }

    public Optional<Order> findById(long id) {
        return Sql.one(c(), "SELECT " + COLS + " FROM don_hang WHERE id = ?", ps -> ps.setLong(1, id),
                OrderRepository::map);
    }

    public List<OrderLine> linesOf(long orderId) {
        return Sql.list(c(), "SELECT id, don_hang_id, mon_id, ten_mon, don_gia, so_luong, thanh_tien "
                        + "FROM chi_tiet_don_hang WHERE don_hang_id = ? ORDER BY id", ps -> ps.setLong(1, orderId),
                OrderRepository::mapLine);
    }

    public List<Order> find(Filter f, int limit) {
        StringBuilder sql = new StringBuilder("SELECT " + COLS + " FROM don_hang WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (f.from() != null) {
            sql.append(" AND ngay_tao >= ?");
            params.add(Dates.dayStart(f.from()));
        }
        if (f.to() != null) {
            sql.append(" AND ngay_tao < ?");
            params.add(Dates.dayEnd(f.to()));
        }
        if (f.status() != null) {
            sql.append(" AND trang_thai = ?");
            params.add(f.status().name());
        }
        if (f.accountId() != null) {
            sql.append(" AND tai_khoan_id = ?");
            params.add(f.accountId());
        }
        sql.append(" ORDER BY ngay_tao DESC, id DESC LIMIT ").append(limit);
        return Sql.list(c(), sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, OrderRepository::map);
    }

    public boolean cancel(long id, String reason) {
        return Sql.update(c(), "UPDATE don_hang SET trang_thai = 'DA_HUY', ly_do_huy = ? WHERE id = ? AND trang_thai = 'DA_THANH_TOAN'",
                ps -> {
                    ps.setString(1, Sql.blank(reason));
                    ps.setLong(2, id);
                }) == 1;
    }

    public long count() {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM don_hang", Sql.NO_PARAMS);
    }

    /**
     * Xóa toàn bộ đơn hàng (dùng khi bàn giao / xóa dữ liệu mẫu). Lịch sử kho được giữ nguyên để vẫn
     * giải thích được tồn kho hiện tại; liên kết tới đơn trong lịch sử kho trở thành NULL.
     */
    public void deleteAll() {
        db.inTransaction(c -> {
            Sql.exec(c, "DELETE FROM chi_tiet_don_hang");
            Sql.exec(c, "DELETE FROM don_hang");
            return null;
        });
    }
}
