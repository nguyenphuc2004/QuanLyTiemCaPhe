package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.StockEntry;
import com.tiemcaphe.model.StockEntryType;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class StockRepository {

    private static final String SELECT = """
            SELECT b.id, b.nguyen_lieu_id, nl.ten AS ten_nl, b.so_luong, b.loai, b.ghi_chu, b.ten_nguoi_thuc_hien,
                   b.don_hang_id, b.ngay_tao
            FROM bien_dong_kho b JOIN nguyen_lieu nl ON nl.id = b.nguyen_lieu_id
            """;

    private final Database db;

    public StockRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    static StockEntry map(ResultSet rs) throws SQLException {
        return new StockEntry(rs.getLong("id"), rs.getLong("nguyen_lieu_id"), rs.getString("ten_nl"),
                rs.getDouble("so_luong"), StockEntryType.valueOf(rs.getString("loai")), rs.getString("ghi_chu"),
                rs.getString("ten_nguoi_thuc_hien"), Sql.longOrNull(rs, "don_hang_id"),
                Dates.fromDb(rs.getString("ngay_tao")));
    }

    public long insert(long ingredientId, double quantity, StockEntryType type, String note, Long accountId,
                       String byName, Long orderId) {
        return Sql.insert(c(), """
                INSERT INTO bien_dong_kho (nguyen_lieu_id, so_luong, loai, ghi_chu, tai_khoan_id, ten_nguoi_thuc_hien,
                                           don_hang_id, ngay_tao)
                VALUES (?,?,?,?,?,?,?,?)
                """, ps -> {
            ps.setLong(1, ingredientId);
            ps.setDouble(2, quantity);
            ps.setString(3, type.name());
            ps.setString(4, Sql.blank(note));
            Sql.setLongOrNull(ps, 5, accountId);
            ps.setString(6, byName);
            Sql.setLongOrNull(ps, 7, orderId);
            ps.setString(8, Dates.nowDb());
        });
    }

    /** Các dòng biến động kho gắn với một đơn hàng (để hoàn đúng lượng đã trừ khi hủy). */
    public List<StockEntry> findByOrder(long orderId, StockEntryType type) {
        return Sql.list(c(), SELECT + " WHERE b.don_hang_id = ? AND b.loai = ? ORDER BY b.id", ps -> {
            ps.setLong(1, orderId);
            ps.setString(2, type.name());
        }, StockRepository::map);
    }

    public List<StockEntry> findByIngredient(long ingredientId, int limit) {
        return Sql.list(c(), SELECT + " WHERE b.nguyen_lieu_id = ? ORDER BY b.ngay_tao DESC, b.id DESC LIMIT " + limit,
                ps -> ps.setLong(1, ingredientId), StockRepository::map);
    }

    public List<StockEntry> findRecent(int limit) {
        return Sql.list(c(), SELECT + " ORDER BY b.ngay_tao DESC, b.id DESC LIMIT " + limit, Sql.NO_PARAMS,
                StockRepository::map);
    }
}
