package com.tiemcaphe.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Tiện ích JDBC gọn nhẹ dùng lambda, tránh lặp code try/catch ở các repository. */
public final class Sql {

    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static final Binder NO_PARAMS = ps -> {
    };

    private Sql() {
    }

    public static <T> List<T> list(Connection c, String sql, Binder binder, RowMapper<T> mapper) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn: " + e.getMessage(), e);
        }
    }

    public static <T> Optional<T> one(Connection c, String sql, Binder binder, RowMapper<T> mapper) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn: " + e.getMessage(), e);
        }
    }

    public static long scalarLong(Connection c, String sql, Binder binder) {
        return one(c, sql, binder, rs -> rs.getLong(1)).orElse(0L);
    }

    public static int update(Connection c, String sql, Binder binder) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi cập nhật: " + e.getMessage(), e);
        }
    }

    /** Thực hiện INSERT và trả về id vừa sinh (last_insert_rowid). */
    public static long insert(Connection c, String sql, Binder binder) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            ps.executeUpdate();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi thêm dữ liệu: " + e.getMessage(), e);
        }
    }

    public static void exec(Connection c, String sql) {
        try (Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi thực thi SQL: " + e.getMessage(), e);
        }
    }

    public static Long longOrNull(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    public static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setLong(index, value);
        }
    }

    public static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
