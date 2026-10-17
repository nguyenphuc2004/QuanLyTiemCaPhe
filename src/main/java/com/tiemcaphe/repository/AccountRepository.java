package com.tiemcaphe.repository;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Role;
import com.tiemcaphe.util.Dates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class AccountRepository {

    private static final String COLS = "id, ten_dang_nhap, ho_ten, vai_tro, hoat_dong, ngay_tao";

    private final Database db;

    public AccountRepository(Database db) {
        this.db = db;
    }

    private Connection c() {
        return db.connection();
    }

    static Account map(ResultSet rs) throws SQLException {
        return new Account(rs.getLong("id"), rs.getString("ten_dang_nhap"), rs.getString("ho_ten"),
                Role.valueOf(rs.getString("vai_tro")), rs.getInt("hoat_dong") == 1,
                Dates.fromDb(rs.getString("ngay_tao")));
    }

    public List<Account> findAll() {
        return Sql.list(c(), "SELECT " + COLS + " FROM tai_khoan ORDER BY vai_tro, ten_dang_nhap",
                Sql.NO_PARAMS, AccountRepository::map);
    }

    public Optional<Account> findById(long id) {
        return Sql.one(c(), "SELECT " + COLS + " FROM tai_khoan WHERE id = ?", ps -> ps.setLong(1, id),
                AccountRepository::map);
    }

    public Optional<Account> findByUsername(String username) {
        return Sql.one(c(), "SELECT " + COLS + " FROM tai_khoan WHERE ten_dang_nhap = ? COLLATE NOCASE",
                ps -> ps.setString(1, username.trim()), AccountRepository::map);
    }

    public Optional<String> passwordHashOf(long id) {
        return Sql.one(c(), "SELECT mat_khau FROM tai_khoan WHERE id = ?", ps -> ps.setLong(1, id),
                rs -> rs.getString(1));
    }

    public long insert(String username, String passwordHash, String fullName, Role role, boolean active) {
        return Sql.insert(c(),
                "INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, ho_ten, vai_tro, hoat_dong, ngay_tao) VALUES (?,?,?,?,?,?)",
                ps -> {
                    ps.setString(1, username.trim());
                    ps.setString(2, passwordHash);
                    ps.setString(3, fullName.trim());
                    ps.setString(4, role.name());
                    ps.setInt(5, active ? 1 : 0);
                    ps.setString(6, Dates.nowDb());
                });
    }

    public void update(long id, String username, String fullName, Role role, boolean active) {
        Sql.update(c(), "UPDATE tai_khoan SET ten_dang_nhap = ?, ho_ten = ?, vai_tro = ?, hoat_dong = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, username.trim());
                    ps.setString(2, fullName.trim());
                    ps.setString(3, role.name());
                    ps.setInt(4, active ? 1 : 0);
                    ps.setLong(5, id);
                });
    }

    public void updatePassword(long id, String passwordHash) {
        Sql.update(c(), "UPDATE tai_khoan SET mat_khau = ? WHERE id = ?", ps -> {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
        });
    }

    public void delete(long id) {
        Sql.update(c(), "DELETE FROM tai_khoan WHERE id = ?", ps -> ps.setLong(1, id));
    }

    public boolean existsUsername(String username, Long excludeId) {
        return Sql.scalarLong(c(),
                "SELECT COUNT(*) FROM tai_khoan WHERE ten_dang_nhap = ? COLLATE NOCASE AND id <> ?", ps -> {
                    ps.setString(1, username.trim());
                    ps.setLong(2, excludeId == null ? -1 : excludeId);
                }) > 0;
    }

    public long countActiveAdmins() {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM tai_khoan WHERE vai_tro = 'ADMIN' AND hoat_dong = 1",
                Sql.NO_PARAMS);
    }

    public long count() {
        return Sql.scalarLong(c(), "SELECT COUNT(*) FROM tai_khoan", Sql.NO_PARAMS);
    }
}
