package com.tiemcaphe.model;

import javafx.beans.binding.LongBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/** Một dòng trong giỏ hàng ở màn hình bán hàng (có thể đổi số lượng). */
public final class CartLine {

    private final Product product;
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);
    private final LongBinding lineTotal = new LongBinding() {
        {
            bind(quantity);
        }

        @Override
        protected long computeValue() {
            return product.price() * quantity.get();
        }
    };

    public CartLine(Product product) {
        this.product = product;
    }

    public Product product() {
        return product;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int q) {
        quantity.set(Math.max(1, q));
    }

    public LongBinding lineTotalBinding() {
        return lineTotal;
    }

    public long getLineTotal() {
        return lineTotal.get();
    }
}
