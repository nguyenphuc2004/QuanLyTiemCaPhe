package Model;

import java.time.LocalDateTime;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class NguyenLieu {
private int id;
private String name;
private double quantity;
private String Unit;
private ImageView imageView;

public void setImageView(ImageView imageView) {
    this.imageView = imageView;
}

public ImageView getImageView() {
    return imageView;
}

// Cập nhật phương thức setImageView để nhận tham số kiểu Image
public void setImageView(Image image) {
    this.imageView = new ImageView(image);
    this.imageView.setFitHeight(50); // Điều chỉnh kích thước nếu cần
    this.imageView.setFitWidth(50);
}


public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public double getQuantity() {
	return quantity;
}
public void setQuantity(double quantity) {
	this.quantity = quantity;
}
public String getUnit() {
	return Unit;
}
public void setUnit(String unit) {
	Unit = unit;
}



}
