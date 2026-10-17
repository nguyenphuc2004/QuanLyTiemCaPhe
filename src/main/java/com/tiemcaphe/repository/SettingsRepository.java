package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.ShopInfo;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SettingsRepository {

    private final Database db;

    public SettingsRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    public Optional<String> get(String key) {
        return Sql.one(c(), "SELECT gia_tri FROM cai_dat WHERE khoa = ?", ps -> ps.setString(1, key),
                rs -> rs.getString(1));
    }

    public void set(String key, String value) {
        Sql.update(c(), "INSERT INTO cai_dat (khoa, gia_tri) VALUES (?, ?) ON CONFLICT(khoa) DO UPDATE SET gia_tri = excluded.gia_tri",
                ps -> {
                    ps.setString(1, key);
                    ps.setString(2, value);
                });
    }

    public Map<String, String> all() {
        Map<String, String> out = new HashMap<>();
        Sql.list(c(), "SELECT khoa, gia_tri FROM cai_dat", Sql.NO_PARAMS, rs -> {
            out.put(rs.getString(1), rs.getString(2));
            return null;
        });
        return out;
    }

    public ShopInfo shopInfo() {
        Map<String, String> m = all();
        ShopInfo d = ShopInfo.defaults();
        return new ShopInfo(m.getOrDefault(ShopInfo.KEY_NAME, d.name()), m.getOrDefault(ShopInfo.KEY_ADDRESS, d.address()),
                m.getOrDefault(ShopInfo.KEY_PHONE, d.phone()), m.getOrDefault(ShopInfo.KEY_FOOTER, d.receiptFooter()));
    }

    public void saveShopInfo(ShopInfo info) {
        db.inTransaction(c -> {
            set(ShopInfo.KEY_NAME, info.name());
            set(ShopInfo.KEY_ADDRESS, info.address());
            set(ShopInfo.KEY_PHONE, info.phone());
            set(ShopInfo.KEY_FOOTER, info.receiptFooter());
            return null;
        });
    }
}
