package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.StockEntry;
import com.tiemcaphe.model.StockEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTest {

    private AppContext ctx;
    private Account admin;

    @BeforeEach
    void setUp() {
        ctx = AppContext.create(Database.inMemory());
        admin = ctx.auth().login("admin", "admin123");
    }

    @Test
    void stockMovementsAreRecorded() {
        Ingredient i = ctx.inventory().create("Hạt điều", "kg", 2, 0.5, null, admin);
        assertEquals(2, i.quantity());
        List<StockEntry> h = ctx.inventory().history(i.id(), 50);
        assertEquals(1, h.size());
        assertEquals(StockEntryType.NHAP, h.get(0).type());
        assertEquals("Chủ quán", h.get(0).byName());

        i = ctx.inventory().stockIn(i.id(), 3, "Nhập thêm", admin);
        assertEquals(5, i.quantity());
        i = ctx.inventory().stockOut(i.id(), 1, "Hỏng", admin);
        assertEquals(4, i.quantity());
        i = ctx.inventory().adjustTo(i.id(), 2.5, "Kiểm kê", admin);
        assertEquals(2.5, i.quantity());
        h = ctx.inventory().history(i.id(), 50);
        assertEquals(4, h.size());
        assertEquals(StockEntryType.DIEU_CHINH, h.get(0).type());
        assertEquals(-1.5, h.get(0).quantity(), 1e-9);

        assertFalse(i.isLow());
        i = ctx.inventory().adjustTo(i.id(), 0.4, null, admin);
        assertTrue(i.isLow());
        assertTrue(ctx.inventory().low().stream().anyMatch(x -> x.name().equals("Hạt điều")));

        long id = i.id();
        assertThrows(BusinessException.class, () -> ctx.inventory().stockIn(id, 0, null, admin));
        assertThrows(BusinessException.class, () -> ctx.inventory().adjustTo(id, -1, null, admin));
        assertThrows(BusinessException.class, () -> ctx.inventory().create("hạt điều", "kg", 0, 0, null, admin),
                "trùng tên không phân biệt hoa thường");
        assertThrows(BusinessException.class, () -> ctx.inventory().create("Mới", " ", 0, 0, null, admin));
    }

    @Test
    void staffCanOnlyStockIn() {
        Account staff = ctx.auth().login("nhanvien", "123456");
        Ingredient i = ctx.inventory().all().get(0);
        double before = i.quantity();
        assertEquals(before + 1, ctx.inventory().stockIn(i.id(), 1, null, staff).quantity(), 1e-9);
        assertThrows(BusinessException.class, () -> ctx.inventory().stockOut(i.id(), 1, null, staff));
        assertThrows(BusinessException.class, () -> ctx.inventory().adjustTo(i.id(), 1, null, staff));
        assertThrows(BusinessException.class, () -> ctx.inventory().create("Mới", "kg", 0, 0, null, staff));
        assertThrows(BusinessException.class, () -> ctx.inventory().update(i.id(), i.name(), i.unit(), 0, null, staff));
        assertThrows(BusinessException.class, () -> ctx.inventory().delete(i.id(), staff));
        assertThrows(BusinessException.class, () -> ctx.inventory().stockIn(i.id(), 1, null, null));
    }

    @Test
    void catalogRules() {
        assertThrows(BusinessException.class,
                () -> ctx.catalog().saveProduct(null, "cà phê đen đá", null, 1000, true, null));
        assertThrows(BusinessException.class, () -> ctx.catalog().saveProduct(null, "Mới", null, -1, true, null));
        var p = ctx.catalog().saveProduct(null, "Cacao nóng", null, 30_000, true, null);
        assertEquals(12, ctx.catalog().allProducts().size());
        var renamed = ctx.catalog().saveProduct(p.id(), "Cacao đá", null, 32_000, false, null);
        assertEquals("Cacao đá", renamed.name());
        assertFalse(renamed.available());
        assertEquals(11, ctx.catalog().availableProducts().size());
        ctx.catalog().deleteProduct(p.id());
        assertEquals(11, ctx.catalog().allProducts().size());

        var c = ctx.catalog().addCategory("Sinh tố");
        assertThrows(BusinessException.class, () -> ctx.catalog().addCategory("sinh tố"));
        ctx.catalog().renameCategory(c.id(), "Sinh tố & Đá xay");
        ctx.catalog().deleteCategory(c.id());
        assertEquals(4, ctx.catalog().allCategories().size());
    }
}
