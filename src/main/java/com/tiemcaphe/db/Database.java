package com.tiemcaphe.db;

import com.tiemcaphe.config.AppConfig;
import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kết nối SQLite dùng chung cho toàn ứng dụng (ứng dụng desktop một người dùng
 * nên một kết nối là đủ; mọi thao tác được đồng bộ hoá).
 */
public final class Database implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(Database.class.getName());
    private static final int SCHEMA_VERSION = 1;

    private static Database instance;

    private final Connection conn;
    private final Path file;
    private boolean inTransaction;

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws Exception;
    }

    private Database(Connection conn, Path file) {
        this.conn = conn;
        this.file = file;
    }

    /** Kết nối mặc định theo {@link AppConfig#dbFile()}. */
    public static synchronized Database get() {
        if (instance == null) {
            instance = open(AppConfig.dbFile());
        }
        return instance;
    }

    public static synchronized Database open(Path file) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            SQLiteConfig cfg = baseConfig();
            cfg.setJournalMode(SQLiteConfig.JournalMode.WAL);
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath(), cfg.toProperties());
            Database db = new Database(c, file);
            db.migrate();
            instance = db;
            LOG.info(() -> "Đã mở CSDL: " + file.toAbsolutePath());
            return db;
        } catch (SQLException | IOException e) {
            throw new DataAccessException("Không mở được CSDL " + file + ": " + e.getMessage(), e);
        }
    }

    /** CSDL trong bộ nhớ, dùng cho kiểm thử. Không gán làm instance mặc định. */
    public static Database inMemory() {
        try {
            Connection c = DriverManager.getConnection("jdbc:sqlite::memory:", baseConfig().toProperties());
            Database db = new Database(c, null);
            db.migrate();
            return db;
        } catch (SQLException e) {
            throw new DataAccessException("Không tạo được CSDL bộ nhớ", e);
        }
    }

    private static SQLiteConfig baseConfig() {
        SQLiteConfig cfg = new SQLiteConfig();
        cfg.enforceForeignKeys(true);
        cfg.setBusyTimeout(5000);
        return cfg;
    }

    public Connection connection() {
        return conn;
    }

    public boolean isOpen() {
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Path file() {
        return file;
    }

    /** Chạy một khối công việc trong giao dịch; hỗ trợ gọi lồng nhau (khối trong dùng chung giao dịch ngoài). */
    public synchronized <T> T inTransaction(Work<T> work) {
        if (inTransaction) {
            try {
                return work.run(conn);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new DataAccessException(e.getMessage(), e);
            }
        }
        try {
            conn.setAutoCommit(false);
            inTransaction = true;
            T result = work.run(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException re) {
                LOG.log(Level.WARNING, "Rollback thất bại", re);
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            inTransaction = false;
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Không bật lại autocommit", e);
            }
        }
    }

    private void migrate() {
        int current = currentVersion();
        if (current >= SCHEMA_VERSION) {
            return;
        }
        LOG.info(() -> "Khởi tạo/cập nhật lược đồ từ phiên bản " + current + " lên " + SCHEMA_VERSION);
        inTransaction(c -> {
            runScript(c, "/db/schema.sql");
            try (Statement st = c.createStatement()) {
                st.execute("PRAGMA user_version = " + SCHEMA_VERSION);
            }
            return null;
        });
    }

    private int currentVersion() {
        try (Statement st = conn.createStatement(); var rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Không đọc được phiên bản lược đồ", e);
        }
    }

    private static void runScript(Connection c, String resource) throws IOException, SQLException {
        try (InputStream in = Database.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Không tìm thấy " + resource);
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = c.createStatement()) {
                for (String raw : script.split(";")) {
                    String sql = stripComments(raw).trim();
                    if (!sql.isEmpty()) {
                        st.execute(sql);
                    }
                }
            }
        }
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            int i = line.indexOf("--");
            sb.append(i >= 0 ? line.substring(0, i) : line).append('\n');
        }
        return sb.toString();
    }

    /** Đóng CSDL mặc định nếu đang mở (gọi khi thoát ứng dụng hoặc trước khi khôi phục). */
    public static synchronized void closeIfOpen() {
        Database db = instance;
        instance = null;
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Lỗi đóng CSDL", e);
            }
        }
        detach(this);
    }

    private static synchronized void detach(Database db) {
        if (instance == db) {
            instance = null;
        }
    }
}
