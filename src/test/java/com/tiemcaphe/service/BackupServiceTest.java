package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupServiceTest {

    @TempDir
    Path tmp;

    @Test
    void backupProducesValidDatabaseFile() throws Exception {
        Database db = Database.open(tmp.resolve("data").resolve("test.db"));
        try {
            AppContext ctx = AppContext.create(db);
            Path target = tmp.resolve("backup").resolve("b.db");
            ctx.backup().backup(target);
            assertTrue(Files.size(target) > 0);
            ctx.backup().validateBackupFile(target);

            Path bogus = tmp.resolve("bogus.db");
            Files.writeString(bogus, "not a database");
            assertThrows(BusinessException.class, () -> ctx.backup().validateBackupFile(bogus));
            assertThrows(BusinessException.class, () -> ctx.backup().validateBackupFile(tmp.resolve("missing.db")));
        } finally {
            db.close();
        }
    }
}
