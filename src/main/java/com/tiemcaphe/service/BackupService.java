package com.tiemcaphe.service;

import com.tiemcaphe.config.AppConfig;
import com.tiemcaphe.db.Database;
import com.tiemcaphe.db.Sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Sao lưu và khôi phục file CSDL SQLite. */
public final class BackupService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Database db;

    public BackupService(Database db) {
        this.db = db;
    }

    public Path defaultBackupFile() {
        return AppConfig.backupDir().resolve("tiemcaphe-" + LocalDateTime.now().format(STAMP) + ".db");
    }

    /** Sao lưu an toàn khi app đang chạy (VACUUM INTO). File đích không được tồn tại trước. */
    public Path backup(Path target) {
        if (db.file() != null && target.toAbsolutePath().normalize().equals(db.file().toAbsolutePath().normalize())) {
            throw new BusinessException("Không thể sao lưu đè lên chính file dữ liệu đang dùng. Hãy chọn tên khác.");
        }
        try {
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException("Không tạo được thư mục sao lưu: " + e.getMessage());
        }
        String path = target.toAbsolutePath().toString().replace("'", "''");
        Sql.exec(db.connection(), "VACUUM INTO '" + path + "'");
        return target;
    }

    /** Kiểm tra file có phải CSDL của phần mềm không. */
    public void validateBackupFile(Path source) {
        if (!Files.isRegularFile(source)) {
            throw new BusinessException("File không tồn tại.");
        }
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + source.toAbsolutePath())) {
            long n = Sql.scalarLong(c,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('tai_khoan','don_hang','mon')",
                    Sql.NO_PARAMS);
            if (n < 3) {
                throw new BusinessException("File không phải bản sao lưu của phần mềm này.");
            }
        } catch (SQLException | RuntimeException e) {
            if (e instanceof BusinessException be) {
                throw be;
            }
            throw new BusinessException("Không đọc được file sao lưu: " + e.getMessage());
        }
    }

    /**
     * Khôi phục: sao lưu CSDL hiện tại sang thư mục backup, đóng kết nối, chép file nguồn đè lên file CSDL.
     * Ứng dụng cần khởi động lại sau khi gọi hàm này.
     *
     * @return đường dẫn bản sao an toàn của CSDL trước khi khôi phục
     */
    public Path restore(Path source) {
        validateBackupFile(source);
        Path dbFile = db.file();
        if (dbFile == null) {
            throw new BusinessException("CSDL hiện tại không phải file, không khôi phục được.");
        }
        Path safety = AppConfig.backupDir().resolve("truoc-khi-khoi-phuc-" + LocalDateTime.now().format(STAMP) + ".db");
        backup(safety);
        // Chép nguồn vào file tạm cạnh CSDL trước (kết nối vẫn mở, lỗi ở đây không ảnh hưởng dữ liệu đang dùng)
        Path tmp = dbFile.toAbsolutePath().resolveSibling(dbFile.getFileName() + ".khoi-phuc.tmp");
        try {
            Files.copy(source, tmp, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Không đọc được file khôi phục: " + e.getMessage());
        }
        db.close();
        try {
            try {
                Files.move(tmp, dbFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tmp, dbFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.deleteIfExists(Path.of(dbFile + "-wal"));
            Files.deleteIfExists(Path.of(dbFile + "-shm"));
        } catch (IOException e) {
            throw new BusinessException("Không thay được file dữ liệu: " + e.getMessage()
                    + ". Dữ liệu cũ đã được sao lưu tại " + safety + ". Phần mềm sẽ thoát, hãy mở lại.");
        }
        return safety;
    }
}
