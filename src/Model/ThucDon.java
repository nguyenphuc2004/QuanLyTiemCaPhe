package Model;

import javafx.scene.image.Image;

public class ThucDon {
	private String itemName;
	private String category;
	private double price;
	private Image imagePath;
	public Image getImagePath() {
		return imagePath;
	}
	public void setImagePath(Image imagePath) {
		this.imagePath = imagePath;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
}
