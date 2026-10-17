package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.Category;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public final class CategoryRepository {

    private final Database db;

    public CategoryRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    public List<Category> findAll() {
        return Sql.list(c(), "SELECT id, ten, thu_tu FROM danh_muc ORDER BY thu_tu, ten", Sql.NO_PARAMS,
                rs -> new Category(rs.getLong("id"), rs.getString("ten"), rs.getInt("thu_tu")));
    }

    public Optional<Category> findByName(String name) {
        return Sql.one(c(), "SELECT id, ten, thu_tu FROM danh_muc WHERE ten = ? COLLATE NOCASE",
                ps -> ps.setString(1, name.trim()),
                rs -> new Category(rs.getLong("id"), rs.getString("ten"), rs.getInt("thu_tu")));
    }

    public long insert(String name) {
        long next = Sql.scalarLong(c(), "SELECT COALESCE(MAX(thu_tu), 0) + 1 FROM danh_muc", Sql.NO_PARAMS);
        return Sql.insert(c(), "INSERT INTO danh_muc (ten, thu_tu) VALUES (?, ?)", ps -> {
            ps.setString(1, name.trim());
            ps.setLong(2, next);
        });
    }

    public void rename(long id, String name) {
        Sql.update(c(), "UPDATE danh_muc SET ten = ? WHERE id = ?", ps -> {
            ps.setString(1, name.trim());
            ps.setLong(2, id);
        });
    }

    public void updateOrder(long id, int order) {
        Sql.update(c(), "UPDATE danh_muc SET thu_tu = ? WHERE id = ?", ps -> {
            ps.setInt(1, order);
            ps.setLong(2, id);
        });
    }

    public void delete(long id) {
        Sql.update(c(), "DELETE FROM danh_muc WHERE id = ?", ps -> ps.setLong(1, id));
    }

    public boolean existsName(String name, Long excludeId) {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM danh_muc WHERE ten = ? COLLATE NOCASE AND id <> ?", ps -> {
            ps.setString(1, name.trim());
            ps.setLong(2, excludeId == null ? -1 : excludeId);
        }) > 0;
    }

    public long countProducts(long categoryId) {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM mon WHERE danh_muc_id = ?", ps -> ps.setLong(1, categoryId));
    }
}
