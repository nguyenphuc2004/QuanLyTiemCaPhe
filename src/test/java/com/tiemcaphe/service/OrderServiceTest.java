package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.CartLine;
import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.OrderStatus;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.repository.OrderRepository;
import com.tiemcaphe.repository.StatisticsRepository;
import com.tiemcaphe.service.OrderService.CheckoutRequest;
import com.tiemcaphe.util.Dates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private AppContext ctx;
    private Account admin;
    private Account staff;

    @BeforeEach
    void setUp() {
        ctx = AppContext.create(Database.inMemory());
        admin = ctx.auth().login("admin", "admin123");
        staff = ctx.auth().login("nhanvien", "123456");
    }

    private Product product(String name) {
        return ctx.catalog().allProducts().stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
    }

    private Ingredient ingredient(String name) {
        return ctx.inventory().all().stream().filter(i -> i.name().equals(name)).findFirst().orElseThrow();
    }

    private static CartLine line(Product p, int qty) {
        CartLine l = new CartLine(p);
        l.setQuantity(qty);
        return l;
    }

    @Test
    void checkoutCreatesOrderLinesAndDeductsStock() {
        Product cf = product("Cà phê sữa đá");
        double coffeeBefore = ingredient("Bột cà phê").quantity();
        double milkBefore = ingredient("Sữa đặc").quantity();

        Order o = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 2)), "Bàn 1", "ít đá", 0, 100_000,
                PaymentMethod.TIEN_MAT), staff);

        String prefix = "HD" + LocalDate.now().format(Dates.CODE) + "-";
        assertEquals(prefix + "0001", o.code());
        assertEquals(50_000, o.subtotal());
        assertEquals(50_000, o.total());
        assertEquals(100_000, o.paid());
        assertEquals(50_000, o.change());
        assertEquals(OrderStatus.DA_THANH_TOAN, o.status());
        assertEquals("Nhân viên 1", o.staffName());
        assertEquals("Bàn 1", o.tableName());

        List<OrderLine> lines = ctx.orders().linesOf(o.id());
        assertEquals(1, lines.size());
        assertEquals(2, lines.get(0).quantity());
        assertEquals(50_000, lines.get(0).lineTotal());

        assertEquals(coffeeBefore - 0.05, ingredient("Bột cà phê").quantity(), 1e-9);
        assertEquals(milkBefore - 0.2, ingredient("Sữa đặc").quantity(), 1e-9);

        Order second = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 1)), null, null, 0, 0,
                PaymentMethod.TIEN_MAT), admin);
        assertEquals(prefix + "0002", second.code());
        assertEquals(25_000, second.paid(), "không nhập tiền khách đưa thì coi như đưa đủ");
        assertEquals(0, second.change());
    }

    @Test
    void discountAndTransferPayment() {
        Product cf = product("Cà phê đen đá");
        Order o = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 3)), null, null, 10_000, 0,
                PaymentMethod.CHUYEN_KHOAN), staff);
        assertEquals(60_000, o.subtotal());
        assertEquals(10_000, o.discount());
        assertEquals(50_000, o.total());
        assertEquals(50_000, o.paid());
        assertEquals(0, o.change());
        assertEquals(PaymentMethod.CHUYEN_KHOAN, o.method());
    }

    @Test
    void rejectsInvalidRequests() {
        Product cf = product("Cà phê đen đá");
        assertThrows(BusinessException.class, () -> ctx.orders().checkout(
                new CheckoutRequest(List.of(), null, null, 0, 0, PaymentMethod.TIEN_MAT), staff));
        assertThrows(BusinessException.class, () -> ctx.orders().checkout(
                new CheckoutRequest(List.of(line(cf, 1)), null, null, 0, 10_000, PaymentMethod.TIEN_MAT), staff),
                "tiền khách đưa chưa đủ");
        assertThrows(BusinessException.class, () -> ctx.orders().checkout(
                new CheckoutRequest(List.of(line(cf, 1)), null, null, 30_000, 0, PaymentMethod.TIEN_MAT), staff),
                "giảm giá vượt tạm tính");
        assertThrows(BusinessException.class, () -> ctx.orders().checkout(
                new CheckoutRequest(List.of(line(cf, 1)), null, null, 0, 0, PaymentMethod.TIEN_MAT), null));
    }

    @Test
    void cancelRestoresStockAndIsAdminOnly() {
        Product cf = product("Cà phê sữa đá");
        double before = ingredient("Bột cà phê").quantity();
        Order o = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 2)), null, null, 0, 0,
                PaymentMethod.TIEN_MAT), staff);
        assertTrue(ingredient("Bột cà phê").quantity() < before);

        assertThrows(BusinessException.class, () -> ctx.orders().cancel(o.id(), "nhầm", staff));
        assertThrows(BusinessException.class, () -> ctx.orders().cancel(o.id(), " ", admin));

        Order cancelled = ctx.orders().cancel(o.id(), "Khách đổi ý", admin);
        assertEquals(OrderStatus.DA_HUY, cancelled.status());
        assertEquals("Khách đổi ý", cancelled.cancelReason());
        assertEquals(before, ingredient("Bột cà phê").quantity(), 1e-9);

        assertThrows(BusinessException.class, () -> ctx.orders().cancel(o.id(), "lại", admin));
    }

    @Test
    void statisticsCountOnlyPaidOrders() {
        Product cf = product("Cà phê đen đá");
        Product bm = product("Bánh mì thịt");
        LocalDate today = LocalDate.now();
        ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 1), line(bm, 2)), null, null, 0, 0,
                PaymentMethod.TIEN_MAT), staff);
        Order second = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 5)), null, null, 0, 0,
                PaymentMethod.CHUYEN_KHOAN), admin);
        ctx.orders().cancel(second.id(), "test", admin);

        StatisticsRepository stats = ctx.statistics();
        StatisticsRepository.Totals t = stats.totals(today, today);
        assertEquals(1, t.orders());
        assertEquals(60_000, t.revenue());

        assertEquals(7, stats.revenueByDay(today.minusDays(6), today).size());
        assertEquals(60_000, stats.revenueByDay(today, today).get(0).revenue());
        assertEquals(12, stats.revenueByMonth(today.getYear()).size());

        var top = stats.topProducts(today, today, 5);
        assertEquals(2, top.size());
        assertEquals("Bánh mì thịt", top.get(0).name());
        assertEquals(2, top.get(0).quantity());

        var byStaff = stats.revenueByStaff(today, today);
        assertEquals(1, byStaff.size());
        assertEquals("Nhân viên 1", byStaff.get(0).name());

        var found = ctx.orders().find(new OrderRepository.Filter(today, today, OrderStatus.DA_HUY, null, null), 100);
        assertEquals(1, found.size());
        assertEquals(second.code(), found.get(0).code());
        var byKeyword = ctx.orders().find(new OrderRepository.Filter(null, null, null, null, "-0001"), 100);
        assertEquals(1, byKeyword.size());
        assertFalse(byKeyword.get(0).isCancelled());
    }

    @Test
    void cancelRestoresExactlyWhatWasDeductedEvenIfRecipeChanged() {
        Product cf = product("Cà phê sữa đá");
        double before = ingredient("Bột cà phê").quantity();
        Order o = ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 2)), null, null, 0, 0,
                PaymentMethod.TIEN_MAT), staff);
        assertEquals(before - 0.05, ingredient("Bột cà phê").quantity(), 1e-9);

        // Đổi định lượng sau khi bán: 0.025 → 0.1 kg mỗi ly
        long coffeeId = ingredient("Bột cà phê").id();
        ctx.catalog().saveRecipe(cf.id(), List.of(new com.tiemcaphe.model.RecipeLine(cf.id(), coffeeId, "Bột cà phê", "kg", 0.1)));

        ctx.orders().cancel(o.id(), "Khách đổi ý", admin);
        assertEquals(before, ingredient("Bột cà phê").quantity(), 1e-9, "hoàn đúng 0.05 đã trừ, không phải 0.2 theo định lượng mới");
        var history = ctx.inventory().history(coffeeId, 10);
        assertEquals(com.tiemcaphe.model.StockEntryType.HUY_DON, history.get(0).type());
        assertEquals(o.id(), history.get(0).orderId());
        assertEquals(0.05, history.get(0).quantity(), 1e-9);
    }

    @Test
    void deleteAllOrdersKeepsStockHistory() {
        Product cf = product("Cà phê đen đá");
        long coffeeId = ingredient("Bột cà phê").id();
        ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 1)), null, null, 0, 0, PaymentMethod.TIEN_MAT), staff);
        int historyBefore = ctx.inventory().history(coffeeId, 100).size();
        double qtyBefore = ingredient("Bột cà phê").quantity();
        ctx.orders().deleteAllOrders();
        assertEquals(0, ctx.orders().count());
        assertEquals(historyBefore, ctx.inventory().history(coffeeId, 100).size(), "lịch sử kho phải còn để giải thích tồn");
        assertEquals(qtyBefore, ingredient("Bột cà phê").quantity(), 1e-9);
        assertTrue(ctx.inventory().history(coffeeId, 100).stream().allMatch(e -> e.orderId() == null));
    }

    @Test
    void keywordSearchIgnoresAccentsAndCase() {
        Product cf = product("Cà phê đen đá");
        ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 1)), "Bàn 1", "ít đường", 0, 0, PaymentMethod.TIEN_MAT), staff);
        ctx.orders().checkout(new CheckoutRequest(List.of(line(cf, 1)), "Mang đi", null, 0, 0, PaymentMethod.TIEN_MAT), staff);
        assertEquals(1, ctx.orders().find(new OrderRepository.Filter(null, null, null, null, "BAN 1"), 100).size());
        assertEquals(1, ctx.orders().find(new OrderRepository.Filter(null, null, null, null, "it duong"), 100).size());
        assertEquals(2, ctx.orders().find(new OrderRepository.Filter(null, null, null, null, "nhan vien"), 100).size());
        assertEquals(0, ctx.orders().find(new OrderRepository.Filter(null, null, null, null, "%"), 100).size());
    }

    @Test
    void demoDataAndReset() {
        int created = ctx.demo().generateIfNoOrders();
        assertTrue(created > 100, "phải sinh nhiều đơn mẫu, thực tế: " + created);
        assertEquals(0, ctx.demo().generateIfNoOrders(), "không sinh lại khi đã có đơn");
        assertTrue(ctx.statistics().totals(LocalDate.now().minusDays(29), LocalDate.now()).revenue() > 0);
        ctx.orders().deleteAllOrders();
        assertEquals(0, ctx.orders().count());
    }
}
