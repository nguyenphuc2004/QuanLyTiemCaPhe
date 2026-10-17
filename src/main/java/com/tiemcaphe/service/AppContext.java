package com.tiemcaphe.service;

import com.tiemcaphe.db.Database;
import com.tiemcaphe.repository.AccountRepository;
import com.tiemcaphe.repository.CategoryRepository;
import com.tiemcaphe.repository.IngredientRepository;
import com.tiemcaphe.repository.OrderRepository;
import com.tiemcaphe.repository.ProductRepository;
import com.tiemcaphe.repository.RecipeRepository;
import com.tiemcaphe.repository.SettingsRepository;
import com.tiemcaphe.repository.StatisticsRepository;
import com.tiemcaphe.repository.StockRepository;

/** Khởi tạo và giữ các repository/service dùng chung cho toàn ứng dụng. */
public final class AppContext {

    private static AppContext instance;

    private final Database db;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final SettingsRepository settingsRepository;
    private final StatisticsRepository statisticsRepository;

    private final AuthService auth;
    private final CatalogService catalog;
    private final InventoryService inventory;
    private final OrderService orderService;
    private final BackupService backup;
    private final SeedService seed;
    private final DemoDataService demo;

    private AppContext(Database db) {
        this.db = db;
        accountRepository = new AccountRepository(db);
        categoryRepository = new CategoryRepository(db);
        productRepository = new ProductRepository(db);
        ingredientRepository = new IngredientRepository(db);
        recipeRepository = new RecipeRepository(db);
        orderRepository = new OrderRepository(db);
        stockRepository = new StockRepository(db);
        settingsRepository = new SettingsRepository(db);
        statisticsRepository = new StatisticsRepository(db);

        auth = new AuthService(accountRepository);
        catalog = new CatalogService(productRepository, categoryRepository, recipeRepository);
        inventory = new InventoryService(db, ingredientRepository, stockRepository);
        orderService = new OrderService(db, orderRepository, recipeRepository, ingredientRepository, stockRepository);
        backup = new BackupService(db);
        seed = new SeedService(db, accountRepository, categoryRepository, productRepository, ingredientRepository,
                recipeRepository, settingsRepository);
        demo = new DemoDataService(db, orderRepository, productRepository, accountRepository);
    }

    /** Context mặc định trên CSDL theo cấu hình; tự tạo dữ liệu khởi tạo nếu CSDL trống. */
    public static synchronized AppContext get() {
        if (instance == null) {
            instance = create(Database.get());
        }
        return instance;
    }

    /** Tạo context trên một CSDL cụ thể (kiểm thử). Không gán làm mặc định. */
    public static AppContext create(Database db) {
        AppContext ctx = new AppContext(db);
        ctx.seed.seedIfEmpty();
        return ctx;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public Database db() {
        return db;
    }

    public AuthService auth() {
        return auth;
    }

    public CatalogService catalog() {
        return catalog;
    }

    public InventoryService inventory() {
        return inventory;
    }

    public OrderService orders() {
        return orderService;
    }

    public BackupService backup() {
        return backup;
    }

    public DemoDataService demo() {
        return demo;
    }

    public SettingsRepository settings() {
        return settingsRepository;
    }

    public StatisticsRepository statistics() {
        return statisticsRepository;
    }

    public AccountRepository accounts() {
        return accountRepository;
    }

    public ProductRepository products() {
        return productRepository;
    }

    public IngredientRepository ingredients() {
        return ingredientRepository;
    }
}
