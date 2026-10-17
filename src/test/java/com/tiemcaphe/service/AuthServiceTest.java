package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private AppContext ctx;

    @BeforeEach
    void setUp() {
        ctx = AppContext.create(Database.inMemory());
    }

    @Test
    void seedCreatesDefaultData() {
        assertEquals(2, ctx.auth().all().size());
        assertEquals(4, ctx.catalog().allCategories().size());
        assertEquals(11, ctx.catalog().allProducts().size());
        assertEquals(12, ctx.inventory().all().size());
        assertEquals("Tiệm Cà Phê", ctx.settings().shopInfo().name());
        assertTrue(ctx.catalog().allProducts().stream().anyMatch(p -> p.image() != null && p.image().length > 0),
                "món mẫu phải có ảnh");
    }

    @Test
    void loginRules() {
        Account a = ctx.auth().login("admin", "admin123");
        assertTrue(a.isAdmin());
        assertEquals(Role.ADMIN, ctx.auth().login("ADMIN", "admin123").role(), "tên đăng nhập không phân biệt hoa thường");
        assertThrows(BusinessException.class, () -> ctx.auth().login("admin", "sai"));
        assertThrows(BusinessException.class, () -> ctx.auth().login("khongco", "admin123"));
        assertThrows(BusinessException.class, () -> ctx.auth().login("", ""));

        Account staff = ctx.auth().login("nhanvien", "123456");
        ctx.auth().update(staff.id(), "nhanvien", "Nhân viên 1", Role.NHAN_VIEN, false);
        BusinessException locked = assertThrows(BusinessException.class, () -> ctx.auth().login("nhanvien", "123456"));
        assertTrue(locked.getMessage().contains("khóa"));
    }

    @Test
    void protectsLastAdmin() {
        Account admin = ctx.auth().login("admin", "admin123");
        assertThrows(BusinessException.class,
                () -> ctx.auth().update(admin.id(), "admin", "Chủ quán", Role.NHAN_VIEN, true));
        assertThrows(BusinessException.class,
                () -> ctx.auth().update(admin.id(), "admin", "Chủ quán", Role.ADMIN, false));
        assertThrows(BusinessException.class, () -> ctx.auth().delete(admin.id(), admin));

        Account second = ctx.auth().create("quanly2", "matkhau2", "Quản lý 2", Role.ADMIN, true);
        ctx.auth().delete(admin.id(), second);
        assertEquals(2, ctx.auth().all().size());
    }

    @Test
    void validationAndPasswords() {
        assertThrows(BusinessException.class, () -> ctx.auth().create("ab", "123456", "X", Role.NHAN_VIEN, true));
        assertThrows(BusinessException.class, () -> ctx.auth().create("co dau", "123456", "X", Role.NHAN_VIEN, true));
        assertThrows(BusinessException.class, () -> ctx.auth().create("Admin", "123456", "X", Role.NHAN_VIEN, true));
        assertThrows(BusinessException.class, () -> ctx.auth().create("moi", "123", "X", Role.NHAN_VIEN, true));
        assertThrows(BusinessException.class, () -> ctx.auth().create("moi", "123456", " ", Role.NHAN_VIEN, true));

        Account staff = ctx.auth().login("nhanvien", "123456");
        assertThrows(BusinessException.class, () -> ctx.auth().changePassword(staff.id(), "sai", "matkhaumoi"));
        ctx.auth().changePassword(staff.id(), "123456", "matkhaumoi");
        assertEquals(staff.id(), ctx.auth().login("nhanvien", "matkhaumoi").id());

        ctx.auth().resetPassword(staff.id(), "datlai99");
        assertEquals(staff.id(), ctx.auth().login("nhanvien", "datlai99").id());
    }
}
