package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class IngredientRepository {

    private static final String COLS = "id, ten, so_luong, don_vi, nguong_canh_bao, hinh, ngay_cap_nhat";

    private final Database db;

    public IngredientRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    static Ingredient map(ResultSet rs) throws SQLException {
        return new Ingredient(rs.getLong("id"), rs.getString("ten"), rs.getDouble("so_luong"), rs.getString("don_vi"),
                rs.getDouble("nguong_canh_bao"), rs.getBytes("hinh"), Dates.fromDb(rs.getString("ngay_cap_nhat")));
    }

    public List<Ingredient> findAll() {
        return Sql.list(c(), "SELECT " + COLS + " FROM nguyen_lieu ORDER BY ten", Sql.NO_PARAMS,
                IngredientRepository::map);
    }

    public Optional<Ingredient> findById(long id) {
        return Sql.one(c(), "SELECT " + COLS + " FROM nguyen_lieu WHERE id = ?", ps -> ps.setLong(1, id),
                IngredientRepository::map);
    }

    public List<Ingredient> findLow() {
        return Sql.list(c(), "SELECT " + COLS
                        + " FROM nguyen_lieu WHERE nguong_canh_bao > 0 AND so_luong <= nguong_canh_bao ORDER BY ten",
                Sql.NO_PARAMS, IngredientRepository::map);
    }

    public long countLow() {
        return Sql.scalarLong(c(),
                "SELECT COUNT(*) FROM nguyen_lieu WHERE nguong_canh_bao > 0 AND so_luong <= nguong_canh_bao",
                Sql.NO_PARAMS);
    }

    public long insert(String name, double quantity, String unit, double warnThreshold, byte[] image) {
        return Sql.insert(c(),
                "INSERT INTO nguyen_lieu (ten, so_luong, don_vi, nguong_canh_bao, hinh, ngay_cap_nhat) VALUES (?,?,?,?,?,?)",
                ps -> {
                    ps.setString(1, name.trim());
                    ps.setDouble(2, quantity);
                    ps.setString(3, unit.trim());
                    ps.setDouble(4, warnThreshold);
                    ps.setBytes(5, image);
                    ps.setString(6, Dates.nowDb());
                });
    }

    /** Cập nhật thông tin mô tả (không đổi số lượng tồn; số lượng đổi qua {@link #adjustQuantity}). */
    public void update(long id, String name, String unit, double warnThreshold, byte[] image) {
        Sql.update(c(),
                "UPDATE nguyen_lieu SET ten = ?, don_vi = ?, nguong_canh_bao = ?, hinh = ?, ngay_cap_nhat = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, name.trim());
                    ps.setString(2, unit.trim());
                    ps.setDouble(3, warnThreshold);
                    ps.setBytes(4, image);
                    ps.setString(5, Dates.nowDb());
                    ps.setLong(6, id);
                });
    }

    /** Cộng (hoặc trừ nếu âm) số lượng tồn. */
    public void adjustQuantity(long id, double delta) {
        Sql.update(c(), "UPDATE nguyen_lieu SET so_luong = so_luong + ?, ngay_cap_nhat = ? WHERE id = ?", ps -> {
            ps.setDouble(1, delta);
            ps.setString(2, Dates.nowDb());
            ps.setLong(3, id);
        });
    }

    public void delete(long id) {
        Sql.update(c(), "DELETE FROM nguyen_lieu WHERE id = ?", ps -> ps.setLong(1, id));
    }

    public boolean existsName(String name, Long excludeId) {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM nguyen_lieu WHERE ten = ? COLLATE NOCASE AND id <> ?", ps -> {
            ps.setString(1, name.trim());
            ps.setLong(2, excludeId == null ? -1 : excludeId);
        }) > 0;
    }

    public long count() {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM nguyen_lieu", Sql.NO_PARAMS);
    }
}
