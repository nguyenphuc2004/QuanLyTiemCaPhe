package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.RecipeLine;

import java.sql.Connection;
import java.util.List;

public final class RecipeRepository {

    private final Database db;

    public RecipeRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    public List<RecipeLine> findByProduct(long productId) {
        return Sql.list(c(), """
                        SELECT dl.mon_id, dl.nguyen_lieu_id, nl.ten, nl.don_vi, dl.so_luong
                        FROM dinh_luong dl JOIN nguyen_lieu nl ON nl.id = dl.nguyen_lieu_id
                        WHERE dl.mon_id = ? ORDER BY nl.ten
                        """, ps -> ps.setLong(1, productId),
                rs -> new RecipeLine(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getDouble(5)));
    }

    public long countByProduct(long productId) {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM dinh_luong WHERE mon_id = ?", ps -> ps.setLong(1, productId));
    }

    /** Thay toàn bộ định lượng của một món. */
    public void replaceForProduct(long productId, List<RecipeLine> lines) {
        db.inTransaction(c -> {
            Sql.update(c, "DELETE FROM dinh_luong WHERE mon_id = ?", ps -> ps.setLong(1, productId));
            for (RecipeLine line : lines) {
                if (line.quantity() <= 0) {
                    continue;
                }
                Sql.update(c, "INSERT INTO dinh_luong (mon_id, nguyen_lieu_id, so_luong) VALUES (?,?,?)", ps -> {
                    ps.setLong(1, productId);
                    ps.setLong(2, line.ingredientId());
                    ps.setDouble(3, line.quantity());
                });
            }
            return null;
        });
    }
}
