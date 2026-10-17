package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ProductRepository {

    private static final String SELECT = """
            SELECT m.id, m.ten, m.danh_muc_id, d.ten AS ten_danh_muc, m.gia, m.con_ban, m.hinh, m.ngay_cap_nhat
            FROM mon m LEFT JOIN danh_muc d ON d.id = m.danh_muc_id
            """;

    private final Database db;

    public ProductRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    static Product map(ResultSet rs) throws SQLException {
        return new Product(rs.getLong("id"), rs.getString("ten"), Sql.longOrNull(rs, "danh_muc_id"),
                rs.getString("ten_danh_muc"), rs.getLong("gia"), rs.getInt("con_ban") == 1, rs.getBytes("hinh"),
                Dates.fromDb(rs.getString("ngay_cap_nhat")));
    }

    public List<Product> findAll() {
        return Sql.list(c(), SELECT + " ORDER BY d.thu_tu, m.ten", Sql.NO_PARAMS, ProductRepository::map);
    }

    public List<Product> findAvailable() {
        return Sql.list(c(), SELECT + " WHERE m.con_ban = 1 ORDER BY d.thu_tu, m.ten", Sql.NO_PARAMS,
                ProductRepository::map);
    }

    public Optional<Product> findById(long id) {
        return Sql.one(c(), SELECT + " WHERE m.id = ?", ps -> ps.setLong(1, id), ProductRepository::map);
    }

    public long insert(String name, Long categoryId, long price, boolean available, byte[] image) {
        return Sql.insert(c(),
                "INSERT INTO mon (ten, danh_muc_id, gia, con_ban, hinh, ngay_tao, ngay_cap_nhat) VALUES (?,?,?,?,?,?,?)",
                ps -> {
                    String now = Dates.nowDb();
                    ps.setString(1, name.trim());
                    Sql.setLongOrNull(ps, 2, categoryId);
                    ps.setLong(3, price);
                    ps.setInt(4, available ? 1 : 0);
                    ps.setBytes(5, image);
                    ps.setString(6, now);
                    ps.setString(7, now);
                });
    }

    public void update(long id, String name, Long categoryId, long price, boolean available, byte[] image) {
        Sql.update(c(),
                "UPDATE mon SET ten = ?, danh_muc_id = ?, gia = ?, con_ban = ?, hinh = ?, ngay_cap_nhat = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, name.trim());
                    Sql.setLongOrNull(ps, 2, categoryId);
                    ps.setLong(3, price);
                    ps.setInt(4, available ? 1 : 0);
                    ps.setBytes(5, image);
                    ps.setString(6, Dates.nowDb());
                    ps.setLong(7, id);
                });
    }

    public void setAvailable(long id, boolean available) {
        Sql.update(c(), "UPDATE mon SET con_ban = ?, ngay_cap_nhat = ? WHERE id = ?", ps -> {
            ps.setInt(1, available ? 1 : 0);
            ps.setString(2, Dates.nowDb());
            ps.setLong(3, id);
        });
    }

    public void delete(long id) {
        Sql.update(c(), "DELETE FROM mon WHERE id = ?", ps -> ps.setLong(1, id));
    }

    public boolean existsName(String name, Long excludeId) {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM mon WHERE ten = ? COLLATE NOCASE AND id <> ?", ps -> {
            ps.setString(1, name.trim());
            ps.setLong(2, excludeId == null ? -1 : excludeId);
        }) > 0;
    }

    public long count() {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM mon", Sql.NO_PARAMS);
    }
}
