package application;



import Model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class itemController {
	   //Item
    @FXML
    private ImageView ImgItem;

    @FXML
    private Label NameItem;

    @FXML
    private Label priceItem;
    
    
    private Item it;

    public void setDataItem(Item it) {
        this.it = it;
        NameItem.setText(it.getName());
        priceItem.setText(it.getPrice() + Main.CURRENCY);

        if (it.getImageSrc() != null) {  // Kiểm tra nếu imageSrc không null
            ImgItem.setImage(it.getImageSrc());
        } else {
            // Sử dụng ảnh mặc định nếu imageSrc là null
            ImgItem.setImage(new Image(getClass().getResourceAsStream("/img/Tableware.png")));
        }
    }



}
