package Model;

import javafx.scene.image.Image;

public class Item {
    private String name;
    private Double price;
    private int quantity; // Correct field name
    private Image imageSrc;

    // Getters and setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Image getImageSrc() {
        return imageSrc;
    }
    public void setImageSrc(Image image) {
        this.imageSrc = image;
    }
    public int getQuantity() { // Correct getter name
        return quantity;
    }
    public void setQuantity(int quantity) { // Correct setter name
        this.quantity = quantity;
    }
}
