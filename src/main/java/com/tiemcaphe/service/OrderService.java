package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.CartLine;
import com.tiemcaphe.model.Order;
import com.tiemcaphe.model.OrderLine;
import com.tiemcaphe.model.OrderStatus;
import com.tiemcaphe.model.PaymentMethod;
import com.tiemcaphe.model.RecipeLine;
import com.tiemcaphe.model.StockEntry;
import com.tiemcaphe.model.StockEntryType;
import com.tiemcaphe.repository.IngredientRepository;
import com.tiemcaphe.repository.OrderRepository;
import com.tiemcaphe.repository.RecipeRepository;
import com.tiemcaphe.repository.StockRepository;
import com.tiemcaphe.util.Texts;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Tạo đơn, hủy đơn và tự động trừ/hoàn kho theo định lượng. */
public final class OrderService {

    public record CheckoutRequest(List<CartLine> lines, String tableName, String note, long discount, long paid,
                                  PaymentMethod method) {
    }

    private final Database db;
    private final OrderRepository orders;
    private final RecipeRepository recipes;
    private final IngredientRepository ingredients;
    private final StockRepository stock;

    public OrderService(Database db, OrderRepository orders, RecipeRepository recipes, IngredientRepository ingredients,
                        StockRepository stock) {
        this.db = db;
        this.orders = orders;
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.stock = stock;
    }

    public Order checkout(CheckoutRequest req, Account staff) {
        if (staff == null) {
            throw new BusinessException("Chưa đăng nhập.");
        }
        if (req.lines() == null || req.lines().isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống.");
        }
        long subtotal = 0;
        for (CartLine l : req.lines()) {
            if (l.getQuantity() <= 0) {
                throw new BusinessException("Số lượng món \"" + l.product().name() + "\" không hợp lệ.");
            }
            subtotal += l.getLineTotal();
        }
        long discount = req.discount();
        if (discount < 0 || discount > subtotal) {
            throw new BusinessException("Giảm giá phải từ 0 đến " + com.tiemcaphe.util.Money.format(subtotal) + ".");
        }
        long total = subtotal - discount;
        PaymentMethod method = req.method() == null ? PaymentMethod.TIEN_MAT : req.method();
        long paid;
        long change;
        if (method == PaymentMethod.TIEN_MAT) {
            paid = req.paid() <= 0 ? total : req.paid();
            if (paid < total) {
                throw new BusinessException("Tiền khách đưa chưa đủ.");
            }
            change = paid - total;
        } else {
            paid = total;
            change = 0;
        }
        final long fSubtotal = subtotal;
        final long fPaid = paid;
        final long fChange = change;

        return db.inTransaction(c -> {
            LocalDateTime now = LocalDateTime.now();
            String code = orders.nextCode(now.toLocalDate());
            Order draft = new Order(0, code, staff.id(), staff.fullName(), req.tableName(), req.note(), fSubtotal,
                    discount, total, fPaid, fChange, method, OrderStatus.DA_THANH_TOAN, null, now);
            long orderId = orders.insert(draft);
            for (CartLine l : req.lines()) {
                orders.insertLine(new OrderLine(0, orderId, l.product().id(), l.product().name(), l.product().price(),
                        l.getQuantity(), l.getLineTotal()));
            }
            applyStock(orderId, code, req.lines(), -1, StockEntryType.BAN_HANG, "Bán hàng, đơn " + code, staff);
            return orders.findById(orderId).orElseThrow();
        });
    }

    /** Hủy đơn đã thanh toán và hoàn kho đúng bằng lượng đã trừ lúc bán (theo biến động kho của đơn). */
    public Order cancel(long orderId, String reason, Account actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new BusinessException("Chỉ Quản trị mới được hủy đơn.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do hủy.");
        }
        return db.inTransaction(c -> {
            Order o = orders.findById(orderId).orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại."));
            if (o.isCancelled()) {
                throw new BusinessException("Đơn này đã bị hủy trước đó.");
            }
            if (!orders.cancel(orderId, reason)) {
                throw new BusinessException("Không hủy được đơn.");
            }
            for (StockEntry sold : stock.findByOrder(orderId, StockEntryType.BAN_HANG)) {
                double back = -sold.quantity();
                if (back == 0) {
                    continue;
                }
                ingredients.adjustQuantity(sold.ingredientId(), back);
                stock.insert(sold.ingredientId(), back, StockEntryType.HUY_DON, "Hoàn kho, hủy đơn " + o.code(),
                        actor.id(), actor.fullName(), orderId);
            }
            return orders.findById(orderId).orElseThrow();
        });
    }

    private void applyStock(long orderId, String code, List<CartLine> lines, int sign, StockEntryType type, String note,
                            Account actor) {
        Map<Long, Double> usage = new LinkedHashMap<>();
        for (CartLine l : lines) {
            for (RecipeLine r : recipes.findByProduct(l.product().id())) {
                usage.merge(r.ingredientId(), r.quantity() * l.getQuantity(), Double::sum);
            }
        }
        for (Map.Entry<Long, Double> e : usage.entrySet()) {
            double delta = sign * e.getValue();
            ingredients.adjustQuantity(e.getKey(), delta);
            stock.insert(e.getKey(), delta, type, note, actor.id(), actor.fullName(), orderId);
        }
    }

    /** Tìm đơn; từ khóa được so không dấu, không phân biệt hoa thường trên mã đơn, bàn, ghi chú, tên nhân viên. */
    public List<Order> find(OrderRepository.Filter filter, int limit) {
        String kw = filter.keyword();
        if (kw == null || kw.isBlank()) {
            return orders.find(filter, limit);
        }
        OrderRepository.Filter base = new OrderRepository.Filter(filter.from(), filter.to(), filter.status(),
                filter.accountId(), null);
        List<Order> out = new java.util.ArrayList<>();
        for (Order o : orders.find(base, Math.max(limit, 20_000))) {
            if (Texts.contains(o.code(), kw) || Texts.contains(o.tableName(), kw) || Texts.contains(o.note(), kw)
                    || Texts.contains(o.staffName(), kw)) {
                out.add(o);
                if (out.size() >= limit) {
                    break;
                }
            }
        }
        return out;
    }

    public Optional<Order> findById(long id) {
        return orders.findById(id);
    }

    public List<OrderLine> linesOf(long orderId) {
        return orders.linesOf(orderId);
    }

    public long count() {
        return orders.count();
    }

    public void deleteAllOrders() {
        orders.deleteAll();
    }
}
