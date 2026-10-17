package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Account;
import com.tiemcaphe.model.Ingredient;
import com.tiemcaphe.model.StockEntry;
import com.tiemcaphe.model.StockEntryType;
import com.tiemcaphe.repository.IngredientRepository;
import com.tiemcaphe.repository.StockRepository;

import java.util.List;

/** Nguyên liệu và kho. */
public final class InventoryService {

    private final Database db;
    private final IngredientRepository ingredients;
    private final StockRepository stock;

    public InventoryService(Database db, IngredientRepository ingredients, StockRepository stock) {
        this.db = db;
        this.ingredients = ingredients;
        this.stock = stock;
    }

    public List<Ingredient> all() {
        return ingredients.findAll();
    }

    public List<Ingredient> low() {
        return ingredients.findLow();
    }

    public long countLow() {
        return ingredients.countLow();
    }

    public Ingredient create(String name, String unit, double initialQuantity, double warnThreshold, byte[] image,
                             Account actor) {
        requireAdmin(actor, "thêm nguyên liệu");
        validate(name, unit, warnThreshold, null);
        if (initialQuantity < 0) {
            throw new BusinessException("Số lượng ban đầu không được âm.");
        }
        return db.inTransaction(c -> {
            long id = ingredients.insert(name, initialQuantity, unit, warnThreshold, image);
            if (initialQuantity > 0) {
                stock.insert(id, initialQuantity, StockEntryType.NHAP, "Tồn đầu", actorId(actor), actorName(actor), null);
            }
            return ingredients.findById(id).orElseThrow();
        });
    }

    public Ingredient update(long id, String name, String unit, double warnThreshold, byte[] image, Account actor) {
        requireAdmin(actor, "sửa nguyên liệu");
        validate(name, unit, warnThreshold, id);
        ingredients.update(id, name, unit, warnThreshold, image);
        return ingredients.findById(id).orElseThrow();
    }

    public void delete(long id, Account actor) {
        requireAdmin(actor, "xóa nguyên liệu");
        ingredients.delete(id);
    }

    /** Nhập thêm hàng vào kho. */
    public Ingredient stockIn(long id, double quantity, String note, Account actor) {
        if (actor == null) {
            throw new BusinessException("Chưa đăng nhập.");
        }
        if (quantity <= 0) {
            throw new BusinessException("Số lượng nhập phải lớn hơn 0.");
        }
        return db.inTransaction(c -> {
            ingredients.adjustQuantity(id, quantity);
            stock.insert(id, quantity, StockEntryType.NHAP, note, actorId(actor), actorName(actor), null);
            return ingredients.findById(id).orElseThrow();
        });
    }

    /** Xuất kho (hao hụt, hỏng...). */
    public Ingredient stockOut(long id, double quantity, String note, Account actor) {
        requireAdmin(actor, "xuất kho");
        if (quantity <= 0) {
            throw new BusinessException("Số lượng xuất phải lớn hơn 0.");
        }
        return db.inTransaction(c -> {
            ingredients.adjustQuantity(id, -quantity);
            stock.insert(id, -quantity, StockEntryType.XUAT, note, actorId(actor), actorName(actor), null);
            return ingredients.findById(id).orElseThrow();
        });
    }

    /** Kiểm kê: đặt lại tồn kho thực tế. */
    public Ingredient adjustTo(long id, double newQuantity, String note, Account actor) {
        requireAdmin(actor, "kiểm kê");
        if (newQuantity < 0) {
            throw new BusinessException("Tồn kho không được âm.");
        }
        return db.inTransaction(c -> {
            Ingredient current = ingredients.findById(id).orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại."));
            double delta = newQuantity - current.quantity();
            if (delta != 0) {
                ingredients.adjustQuantity(id, delta);
                stock.insert(id, delta, StockEntryType.DIEU_CHINH, note, actorId(actor), actorName(actor), null);
            }
            return ingredients.findById(id).orElseThrow();
        });
    }

    public List<StockEntry> history(long ingredientId, int limit) {
        return stock.findByIngredient(ingredientId, limit);
    }

    public List<StockEntry> recent(int limit) {
        return stock.findRecent(limit);
    }

    private void validate(String name, String unit, double warnThreshold, Long excludeId) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Vui lòng nhập tên nguyên liệu.");
        }
        if (unit == null || unit.isBlank()) {
            throw new BusinessException("Vui lòng nhập đơn vị tính.");
        }
        if (warnThreshold < 0) {
            throw new BusinessException("Ngưỡng cảnh báo không được âm.");
        }
        if (ingredients.existsName(name, excludeId)) {
            throw new BusinessException("Đã có nguyên liệu tên \"" + name.trim() + "\".");
        }
    }

    private static void requireAdmin(Account actor, String action) {
        if (actor == null || !actor.isAdmin()) {
            throw new BusinessException("Chỉ Quản trị mới được " + action + ".");
        }
    }

    static Long actorId(Account a) {
        return a == null ? null : a.id();
    }

    static String actorName(Account a) {
        return a == null ? "Hệ thống" : a.fullName();
    }
}
