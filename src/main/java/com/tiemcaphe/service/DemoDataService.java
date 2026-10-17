package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.OrderStatus;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.repository.AccountRepository;
import com.tiemcaphe.repository.OrderRepository;
import com.tiemcaphe.repository.ProductRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Sinh đơn hàng mẫu 30 ngày gần nhất để trình diễn báo cáo. Không trừ kho. */
public final class DemoDataService {

    private final Database db;
    private final OrderRepository orders;
    private final ProductRepository products;
    private final AccountRepository accounts;

    public DemoDataService(Database db, OrderRepository orders, ProductRepository products, AccountRepository accounts) {
        this.db = db;
        this.orders = orders;
        this.products = products;
        this.accounts = accounts;
    }

    /** Tạo dữ liệu mẫu nếu chưa có đơn nào. Trả về số đơn đã tạo. */
    public int generateIfNoOrders() {
        if (orders.count() > 0) {
            return 0;
        }
        return generate(30);
    }

    public int generate(int days) {
        List<Product> menu = products.findAll();
        List<Account> staff = accounts.findAll();
        if (menu.isEmpty() || staff.isEmpty()) {
            throw new BusinessException("Cần có món và tài khoản trước khi tạo dữ liệu mẫu.");
        }
        Random rnd = new Random(42);
        LocalDate today = LocalDate.now();
        return db.inTransaction(c -> {
            int created = 0;
            for (int d = days - 1; d >= 0; d--) {
                LocalDate day = today.minusDays(d);
                int weekendBonus = day.getDayOfWeek().getValue() >= 6 ? 8 : 0;
                int count = 8 + rnd.nextInt(12) + weekendBonus;
                if (d == 0) {
                    count = Math.max(3, count / 2);
                }
                List<Integer> minutes = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    minutes.add(7 * 60 + rnd.nextInt(14 * 60));
                }
                minutes.sort(Integer::compareTo);
                for (int minute : minutes) {
                    LocalDateTime at = day.atStartOfDay().plusMinutes(minute).plusSeconds(rnd.nextInt(60));
                    if (at.isAfter(LocalDateTime.now())) {
                        continue;
                    }
                    Account by = staff.get(rnd.nextInt(staff.size()));
                    int lineCount = 1 + rnd.nextInt(3);
                    List<Product> picked = new ArrayList<>();
                    while (picked.size() < lineCount) {
                        Product p = menu.get(rnd.nextInt(menu.size()));
                        if (!picked.contains(p)) {
                            picked.add(p);
                        }
                    }
                    long subtotal = 0;
                    List<int[]> qtys = new ArrayList<>();
                    for (Product p : picked) {
                        int q = 1 + (rnd.nextInt(10) < 3 ? 1 + rnd.nextInt(2) : 0);
                        qtys.add(new int[]{q});
                        subtotal += p.price() * q;
                    }
                    long discount = rnd.nextInt(10) == 0 ? Math.min(subtotal, 5_000L * (1 + rnd.nextInt(3))) : 0;
                    long total = subtotal - discount;
                    PaymentMethod method = rnd.nextInt(10) < 7 ? PaymentMethod.TIEN_MAT : PaymentMethod.CHUYEN_KHOAN;
                    long paid = method == PaymentMethod.TIEN_MAT ? roundUp(total, rnd) : total;
                    String table = rnd.nextInt(3) == 0 ? "" : "Bàn " + (1 + rnd.nextInt(12));
                    String code = orders.nextCode(day);
                    long id = orders.insert(new Order(0, code, by.id(), by.fullName(), table, null, subtotal, discount,
                            total, paid, paid - total, method, OrderStatus.DA_THANH_TOAN, null, at));
                    for (int i = 0; i < picked.size(); i++) {
                        Product p = picked.get(i);
                        int q = qtys.get(i)[0];
                        orders.insertLine(new OrderLine(0, id, p.id(), p.name(), p.price(), q, p.price() * q));
                    }
                    created++;
                }
            }
            return created;
        });
    }

    private static long roundUp(long total, Random rnd) {
        long[] notes = {total, 50_000, 100_000, 200_000, 500_000};
        for (long n : notes) {
            if (n >= total && rnd.nextInt(3) > 0) {
                return n;
            }
        }
        return ((total + 9_999) / 10_000) * 10_000;
    }
}
