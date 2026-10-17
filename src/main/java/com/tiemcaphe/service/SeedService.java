package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.model.Role;
import com.tiemcaphe.model.ShopInfo;
import com.tiemcaphe.repository.AccountRepository;
import com.tiemcaphe.repository.CategoryRepository;
import com.tiemcaphe.repository.IngredientRepository;
import com.tiemcaphe.repository.ProductRepository;
import com.tiemcaphe.repository.RecipeRepository;
import com.tiemcaphe.repository.SettingsRepository;
import com.tiemcaphe.util.Images;
import com.tiemcaphe.util.PasswordHasher;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/** Dữ liệu khởi tạo cho lần chạy đầu tiên: tài khoản, danh mục, món mẫu, nguyên liệu mẫu và định lượng. */
public final class SeedService {

    private static final Logger LOG = Logger.getLogger(SeedService.class.getName());

    public static final String DEFAULT_ADMIN = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_STAFF = "nhanvien";
    public static final String DEFAULT_STAFF_PASSWORD = "123456";

    private final Database db;
    private final AccountRepository accounts;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final IngredientRepository ingredients;
    private final RecipeRepository recipes;
    private final SettingsRepository settings;

    public SeedService(Database db, AccountRepository accounts, CategoryRepository categories,
                       ProductRepository products, IngredientRepository ingredients, RecipeRepository recipes,
                       SettingsRepository settings) {
        this.db = db;
        this.accounts = accounts;
        this.categories = categories;
        this.products = products;
        this.ingredients = ingredients;
        this.recipes = recipes;
        this.settings = settings;
    }

    /** Chỉ chạy khi chưa có tài khoản nào (CSDL mới). */
    public boolean seedIfEmpty() {
        if (accounts.count() > 0) {
            return false;
        }
        LOG.info("CSDL trống, tạo dữ liệu khởi tạo");
        db.inTransaction(c -> {
            seedAccounts();
            settings.saveShopInfo(ShopInfo.defaults());
            seedCatalog();
            return null;
        });
        return true;
    }

    private void seedAccounts() {
        accounts.insert(DEFAULT_ADMIN, PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD), "Chủ quán", Role.ADMIN, true);
        accounts.insert(DEFAULT_STAFF, PasswordHasher.hash(DEFAULT_STAFF_PASSWORD), "Nhân viên 1", Role.NHAN_VIEN, true);
    }

    private void seedCatalog() {
        long caPhe = categories.insert("Cà phê");
        long tra = categories.insert("Trà");
        long nuocEp = categories.insert("Nước ép & Sữa");
        long doAn = categories.insert("Đồ ăn");

        Map<String, Long> nl = new HashMap<>();
        nl.put("Bột cà phê", ingredients.insert("Bột cà phê", 5, "kg", 1, img("bot-ca-phe.jpg")));
        nl.put("Sữa đặc", ingredients.insert("Sữa đặc", 24, "lon", 6, null));
        nl.put("Sữa tươi", ingredients.insert("Sữa tươi", 10, "lít", 3, img("sua-tuoi.jpg")));
        nl.put("Đường", ingredients.insert("Đường", 10, "kg", 2, null));
        nl.put("Trà đào", ingredients.insert("Trà đào", 5, "hộp", 1, null));
        nl.put("Trân châu", ingredients.insert("Trân châu", 3, "kg", 1, null));
        nl.put("Cam trái", ingredients.insert("Cam trái", 8, "kg", 2, img("cam-trai.jpg")));
        nl.put("Bột trà xanh", ingredients.insert("Bột trà xanh", 1, "kg", 0.3, img("bot-tra-xanh.jpg")));
        nl.put("Bột mì", ingredients.insert("Bột mì", 5, "kg", 1, img("bot-mi.jpg")));
        nl.put("Thịt bò", ingredients.insert("Thịt bò", 4, "kg", 1, img("thit-bo.jpg")));
        nl.put("Bánh phở", ingredients.insert("Bánh phở", 5, "kg", 1, img("banh-pho.jpg")));
        nl.put("Đá viên", ingredients.insert("Đá viên", 20, "kg", 5, null));

        long cfDen = products.insert("Cà phê đen đá", caPhe, 20_000, true, img("ca-phe-den-da.jpg"));
        long cfSua = products.insert("Cà phê sữa đá", caPhe, 25_000, true, img("ca-phe-sua.jpg"));
        long bacXiu = products.insert("Bạc xỉu", caPhe, 29_000, true, null);
        long traDao = products.insert("Trà đào cam sả", tra, 35_000, true, img("tra-dao.jpg"));
        long traSua = products.insert("Trà sữa trân châu", tra, 35_000, true, img("tra-sua.jpg"));
        long matcha = products.insert("Matcha đá xay", tra, 42_000, true, null);
        long nuocCam = products.insert("Nước cam ép", nuocEp, 30_000, true, img("nuoc-cam.jpg"));
        long suaTuoi = products.insert("Sữa tươi đường đen", nuocEp, 25_000, true, img("sua-tuoi.jpg"));
        long banhMi = products.insert("Bánh mì thịt", doAn, 20_000, true, img("banh-mi.jpg"));
        products.insert("Bánh bao", doAn, 15_000, true, img("banh-bao.jpg"));
        long phoBo = products.insert("Phở bò", doAn, 45_000, true, img("pho-bo.jpg"));

        recipe(cfDen, nl, "Bột cà phê", 0.025, "Đường", 0.01, "Đá viên", 0.15);
        recipe(cfSua, nl, "Bột cà phê", 0.025, "Sữa đặc", 0.1, "Đá viên", 0.15);
        recipe(bacXiu, nl, "Bột cà phê", 0.015, "Sữa đặc", 0.15, "Sữa tươi", 0.1, "Đá viên", 0.15);
        recipe(traDao, nl, "Trà đào", 0.05, "Đường", 0.02, "Đá viên", 0.2);
        recipe(traSua, nl, "Trân châu", 0.05, "Sữa tươi", 0.15, "Đường", 0.02, "Đá viên", 0.2);
        recipe(matcha, nl, "Bột trà xanh", 0.01, "Sữa tươi", 0.2, "Đường", 0.02, "Đá viên", 0.25);
        recipe(nuocCam, nl, "Cam trái", 0.4, "Đá viên", 0.1);
        recipe(suaTuoi, nl, "Sữa tươi", 0.25, "Đường", 0.03, "Đá viên", 0.15);
        recipe(banhMi, nl, "Bột mì", 0.1);
        recipe(phoBo, nl, "Thịt bò", 0.15, "Bánh phở", 0.2);
    }

    private void recipe(long productId, Map<String, Long> nl, Object... pairs) {
        java.util.List<com.tiemcaphe.model.RecipeLine> lines = new java.util.ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Long ingId = nl.get((String) pairs[i]);
            double qty = ((Number) pairs[i + 1]).doubleValue();
            lines.add(new com.tiemcaphe.model.RecipeLine(productId, ingId, (String) pairs[i], "", qty));
        }
        recipes.replaceForProduct(productId, lines);
    }

    private static byte[] img(String file) {
        return Images.readResourceForStorage("/img/" + file);
    }
}
