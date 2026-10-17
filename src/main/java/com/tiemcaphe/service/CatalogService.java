package com.tiemcaphe.service;

import com.tiemcaphe.model.Category;
import com.tiemcaphe.model.Product;
import com.tiemcaphe.model.RecipeLine;
import com.tiemcaphe.repository.CategoryRepository;
import com.tiemcaphe.repository.ProductRepository;
import com.tiemcaphe.repository.RecipeRepository;

import java.util.List;

/** Thực đơn, danh mục và định lượng nguyên liệu. */
public final class CatalogService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final RecipeRepository recipes;

    public CatalogService(ProductRepository products, CategoryRepository categories, RecipeRepository recipes) {
        this.products = products;
        this.categories = categories;
        this.recipes = recipes;
    }

    // ---- Món ----

    public List<Product> allProducts() {
        return products.findAll();
    }

    public List<Product> availableProducts() {
        return products.findAvailable();
    }

    public Product saveProduct(Long id, String name, Long categoryId, long price, boolean available, byte[] image) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Vui lòng nhập tên món.");
        }
        if (price < 0) {
            throw new BusinessException("Giá bán không được âm.");
        }
        if (products.existsName(name, id)) {
            throw new BusinessException("Đã có món tên \"" + name.trim() + "\".");
        }
        long savedId;
        if (id == null) {
            savedId = products.insert(name, categoryId, price, available, image);
        } else {
            products.update(id, name, categoryId, price, available, image);
            savedId = id;
        }
        return products.findById(savedId).orElseThrow();
    }

    public void setProductAvailable(long id, boolean available) {
        products.setAvailable(id, available);
    }

    public void deleteProduct(long id) {
        products.delete(id);
    }

    // ---- Danh mục ----

    public List<Category> allCategories() {
        return categories.findAll();
    }

    public Category addCategory(String name) {
        validateCategoryName(name, null);
        long id = categories.insert(name);
        return new Category(id, name.trim(), 0);
    }

    public void renameCategory(long id, String name) {
        validateCategoryName(name, id);
        categories.rename(id, name);
    }

    public void deleteCategory(long id) {
        categories.delete(id);
    }

    public long countProductsInCategory(long id) {
        return categories.countProducts(id);
    }

    private void validateCategoryName(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Vui lòng nhập tên danh mục.");
        }
        if (categories.existsName(name, excludeId)) {
            throw new BusinessException("Danh mục \"" + name.trim() + "\" đã tồn tại.");
        }
    }

    // ---- Định lượng ----

    public List<RecipeLine> recipeOf(long productId) {
        return recipes.findByProduct(productId);
    }

    public void saveRecipe(long productId, List<RecipeLine> lines) {
        for (RecipeLine l : lines) {
            if (l.quantity() <= 0) {
                throw new BusinessException("Định lượng của \"" + l.ingredientName() + "\" phải lớn hơn 0.");
            }
        }
        recipes.replaceForProduct(productId, lines);
    }
}
