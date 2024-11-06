package application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;
import java.util.function.UnaryOperator;

import Model.DonHang;
import Model.Item;
import Model.NguyenLieu;
import Model.User;
import Model.UserSession;
import application.SampleController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SampleController   {
	
	NguyenLieu nguyenlieu = new NguyenLieu(); 
    private User currentUser;

    @FXML
    private AnchorPane goHome, goOders, goMaterial, goBill, goStatistics, goAccount;
    //Thanh chuyển hướng
    @FXML
    private ImageView Home, Oders, Material, Bill, Statistics, Account, logOut;
    // Trang Home

    @FXML
    private Label lbDay;
   
    //Trang Oders
    @FXML
    private Button btnPay;
    @FXML
    private TextField textName, textPrice;
    
//    TableView nguyen lieu
    @FXML
    private TableView<NguyenLieu> TableViewMaterial;
    @FXML
    private TableColumn<NguyenLieu, Integer> CoLumnID ;
    @FXML
    private TableColumn<NguyenLieu, String> columnName ;
    @FXML
    private TableColumn<NguyenLieu, Double > columnQuantity ;
    @FXML
    private TableColumn<NguyenLieu, String> columnUnit ;
    @FXML
    private TableColumn<NguyenLieu, ImageView> columnImage ;
    ObservableList<NguyenLieu> nguyenlieuList =
			FXCollections.observableArrayList();
    
//  TableView don hang
    @FXML
    private TableView<DonHang> CalendarTableView;
    @FXML
    private TableColumn<DonHang, Integer> OrderIdColumn;
    @FXML
    private TableColumn<DonHang, Integer> UserIdColumn;
    @FXML
    private TableColumn<DonHang, Double> TotalPriceColumn;
    @FXML
    private TableColumn<DonHang, String> StatusColumn;
    ObservableList<DonHang> donhanglist = FXCollections.observableArrayList();
    
    //TableViewPay
    @FXML
    private TableView<Item> TableViewPay;
    @FXML
    private TableColumn<Item, String> NameItemColumPay;
    @FXML
    private TableColumn<Item, Double> priceItemColumPay;
    @FXML
    private TableColumn<Item, Integer> QuantityItemColumnPay;
    @FXML
    private TableColumn<Item, Image> ImgItemColumnPay;
    ObservableList<Item> selectedItems = FXCollections.observableArrayList();
    @FXML
    private TextField TxtShowPayTotalPrice;
    
    
    //MenuItem
    @FXML
    private javafx.scene.control.ScrollPane scrollMenuItem;
    
    //Item
    @FXML
    private ImageView ImgItem;

    @FXML
    private Label NameItem;

    @FXML
    private Label priceItem;
    @FXML
    private GridPane GridPaneMenuItem;
    @FXML
    private VBox chosenFruitCard;
    
    @FXML
    public void initialize() {
    	//Render data nguyen lieu
        CoLumnID.setCellValueFactory(new PropertyValueFactory<>("id")); //("id")) la cai thuoc tinh cua class nguyen lieu
        columnName.setCellValueFactory(new PropertyValueFactory<>("name")); 
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity")); 
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        columnImage.setCellValueFactory(new PropertyValueFactory<>("imageView")); 

        TableViewMaterial.setItems(nguyenlieuList);
            loadNguyenLieu();
          //Edit Nguyên Liệu
            // Đặt chế độ có thể chỉnh sửa cho TableView
            TableViewMaterial.setEditable(true);

            // Thiết lập CellFactory cho cột ID (không cho phép chỉnh sửa ID vì đây là khóa chính)
            CoLumnID.setCellValueFactory(new PropertyValueFactory<>("id"));

            // Thiết lập CellFactory cho các cột có thể chỉnh sửa
            columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
           

      

            columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            columnQuantity.setCellFactory(column -> {
                TableCell<NguyenLieu, Double> cell = new TextFieldTableCell<>(new DoubleStringConverter()) {
                    @Override
                    public void startEdit() {
                        super.startEdit();
                        
                        // Lấy TextField từ TextFieldTableCell
                        TextField textField = (TextField) getGraphic();

                        // Đặt TextFormatter để chỉ cho phép số dương
                        UnaryOperator<TextFormatter.Change> filter = change -> {
                            String newText = change.getControlNewText();
                            if (newText.matches("\\d*\\.?\\d*") && (newText.isEmpty() || Double.parseDouble(newText) >= 0)) {
                                return change; // Chấp nhận thay đổi nếu là số dương
                            }
                            return null; // Từ chối thay đổi nếu không hợp lệ
                        };
                        textField.setTextFormatter(new TextFormatter<>(filter));
                    }
                };
                return cell;
            });

            columnQuantity.setOnEditCommit(event -> {
                Double newValue = event.getNewValue();
                if (newValue != null && newValue >= 0) {  // Chỉ cập nhật nếu giá trị hợp lệ
                    NguyenLieu nguyenLieu = event.getRowValue();
                    nguyenLieu.setQuantity(newValue);
                    editNguyenLieuInTableView(nguyenLieu);
                } else {
                    System.out.println("Lỗi: Giá trị nhập phải là số dương.");
                    // Bạn có thể hiển thị thông báo hoặc đặt lại giá trị nếu cần
                }
            });


            columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
            columnUnit.setCellFactory(TextFieldTableCell.forTableColumn());
            columnUnit.setOnEditCommit(event -> {
                NguyenLieu nguyenLieu = event.getRowValue();
                nguyenLieu.setUnit(event.getNewValue());
                editNguyenLieuInTableView(nguyenLieu);
            });

            columnImage.setCellValueFactory(new PropertyValueFactory<>("imageView"));
            columnImage.setCellFactory(column -> new TableCell<NguyenLieu, ImageView>() {
                @Override
                protected void updateItem(ImageView item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(item);
                    }
                }
            });
    
        //Render data don hang
        OrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        UserIdColumn.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        TotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        StatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
       
        CalendarTableView.setItems(donhanglist);
        loadDonHang();
        
        //Xử lí click chọn và thanh toán nguyên liệu
     // Thiết lập cột NameItemColumPay, priceItemColumPay, QuantityItemColumnPay
        NameItemColumPay.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceItemColumPay.setCellValueFactory(new PropertyValueFactory<>("price"));
        QuantityItemColumnPay.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Thiết lập ImgItemColumnPay để hiển thị hình ảnh
        ImgItemColumnPay.setCellValueFactory(new PropertyValueFactory<>("imageSrc"));
        ImgItemColumnPay.setCellFactory(column -> new TableCell<Item, Image>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);  // Không hiển thị gì khi không có ảnh
                } else {
                    imageView.setImage(item);    // Thiết lập ảnh vào ImageView
                    imageView.setFitWidth(50);   // Đặt kích thước cho ảnh nếu cần
                    imageView.setFitHeight(50);
                    setGraphic(imageView);       // Hiển thị ImageView trong ô
                }
            }
        });

        // Thiết lập dữ liệu cho TableViewPay
        TableViewPay.setItems(selectedItems);

        
        renderItemsToGridPane();
        
        


    }


    @FXML
    public void OdersClick(){
        resetStyles();  
        Oders.setOpacity(1);  
        goOders.setVisible(true);
        goMaterial.setVisible(false);
        goBill.setVisible(false);

    }

    @FXML
    public void MaterialClick(){
        resetStyles();  
        Material.setOpacity(1);  
        goOders.setVisible(false);
        goMaterial.setVisible(true);
        goBill.setVisible(false);

    }
    
    @FXML
    public void BillClick(){
        resetStyles();  
        Bill.setOpacity(1);
        goOders.setVisible(false);
        goMaterial.setVisible(false);
        goBill.setVisible(true);

    }

 
 // Phương thức mở giao diện mới
    private void openUI(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.show();
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đăng xuất và thoát ứng dụng
    @FXML
    public void logOutClick(){
        resetStyles();  
        logOut.setOpacity(1);  
        openUI("login.fxml");
        // Lấy Stage hiện tại và đóng form
        Stage currentStage = (Stage) logOut.getScene().getWindow();
        currentStage.close();
    }

    // Reset lại trạng thái ban đầu của các ImageView
    private void resetStyles() {
//        Home.setOpacity(0.37);
        Oders.setOpacity(0.37);
        Material.setOpacity(0.37);
        Bill.setOpacity(0.37);
//        Statistics.setOpacity(0.37);
//        Account.setOpacity(0.37);
    }
    
   
  //*********************************  XỬ LÍ SQL   ********************************************
 // Hàm loadNguyenLieu
    @FXML
    public void loadNguyenLieu() {
        String query = "SELECT MaMon, TenNguyenLieu, SoLuong, DonVi, Hinh FROM NguyenLieu";
        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NguyenLieu nguyenlieu = new NguyenLieu();
                nguyenlieu.setId(rs.getInt("MaMon"));
                nguyenlieu.setName(rs.getString("TenNguyenLieu"));
                nguyenlieu.setQuantity(rs.getDouble("SoLuong"));
                nguyenlieu.setUnit(rs.getString("DonVi"));
                
             // Lấy ảnh từ cột Hinh dưới dạng Blob và chuyển đổi thành Image
                Blob hinhBlob = rs.getBlob("Hinh");
                Image hinhAnh = getImagePath(hinhBlob);

                // Nếu cần, thiết lập hình ảnh cho nguyenlieu
                nguyenlieu.setImageView(hinhAnh);
                nguyenlieuList.add(nguyenlieu);
            }
            TableViewMaterial.setItems(nguyenlieuList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Hàm loadDonHang
    @FXML
    public void loadDonHang() {
        donhanglist.clear();
        String query = "SELECT MaDonHang, MaTaiKhoan, TongGia, TrangThai FROM DonHang";
        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DonHang donhang = new DonHang();
                donhang.setIdOrder(rs.getInt("MaDonHang"));
                donhang.setIdUser(rs.getInt("MaTaiKhoan"));
                donhang.setTotalPrice(rs.getDouble("TongGia"));
                donhang.setStatus(rs.getString("TrangThai"));
                
                donhanglist.add(donhang);
            }
            CalendarTableView.setItems(donhanglist);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //*********************** Xử lí render item lên gridPane***********************    
    // Hàm getDataItems
    List<Item> items = new ArrayList<>();
    private List<Item> getDataItems() {
        List<Item> items = new ArrayList<>();
        String query = "SELECT TenMon, Gia, Hinh FROM ThucDon";

        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = new Item();
                String ingredientName = rs.getString("TenMon");
                item.setName(ingredientName);
                item.setPrice(rs.getDouble("Gia"));
             // Get image as Blob and convert to Image
                Blob imageBlob = rs.getBlob("Hinh");
                Image image = getImagePath(imageBlob);
                item.setImageSrc(image); // Set image directly

                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

  
    private Image getImagePath(Blob imageBlob) {
        if (imageBlob != null) {
            try {
                // Đọc dữ liệu nhị phân từ Blob và chuyển thành mảng byte
                byte[] imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
                // Tạo đối tượng Image từ mảng byte
                return new Image(new ByteArrayInputStream(imageBytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Trả về hình ảnh mặc định nếu không có dữ liệu ảnh
        return new Image(getClass().getResource("/img/Tableware.png").toExternalForm());

    }

public void renderItemsToGridPane() {
    GridPaneMenuItem.getChildren().clear();
    items.addAll(getDataItems());
    int column = 0;
    int row = 0;

    try {
        for (int i = 0; i < items.size(); i++) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/application/NguyenLieu.fxml"));

            if (fxmlLoader.getLocation() == null) {
                throw new Exception("Không tìm thấy tệp FXML tại đường dẫn: /application/NguyenLieu.fxml");
            }

            AnchorPane anchorpane = fxmlLoader.load();
            itemController itemcontroller = fxmlLoader.getController();
            Item currentItem = items.get(i);
            itemcontroller.setDataItem(currentItem);

            // Xử lý sự kiện click cho AnchorPane
            anchorpane.setOnMouseClicked(event -> handleItemClick(currentItem));

            if (column == 2) {
                column = 0;
                row++;
            }

            GridPaneMenuItem.add(anchorpane, ++column, row);
            GridPane.setMargin(anchorpane, new Insets(20));
        }
    } catch (IOException e) {
        System.err.println("Lỗi khi tải FXML: " + e.getMessage());
        e.printStackTrace();
    } catch (Exception e) {
        System.err.println("Lỗi: " + e.getMessage());
        e.printStackTrace();
    }
}

private void handleItemClick(Item clickedItem) {
    // Kiểm tra nếu item đã tồn tại trong TableViewPay
    boolean itemExists = false;
    for (Item item : selectedItems) {
        if (item.getName().equals(clickedItem.getName())) {
            item.setQuantity(item.getQuantity() + 1); // Tăng số lượng
            itemExists = true;
            TableViewPay.refresh(); // Cập nhật TableView
            break;
        }
    }

    // Nếu item chưa tồn tại, thêm vào selectedItems
    if (!itemExists) {
        // Khởi tạo một item mới với số lượng mặc định là 1
        Item newItem = new Item();
        newItem.setName(clickedItem.getName());
        newItem.setPrice(clickedItem.getPrice());
        newItem.setImageSrc(clickedItem.getImageSrc());
        newItem.setQuantity(1); // Thiết lập số lượng ban đầu là 1
        selectedItems.add(newItem);
    }
    
    // Cập nhật tổng giá trị
    updateTotalPrice();
}
private void updateTotalPrice() {
    double totalPrice = 0.0;
    for (Item item : selectedItems) {
        totalPrice += item.getPrice() * item.getQuantity();
    }
    TxtShowPayTotalPrice.setText(String.valueOf(totalPrice));
}

@FXML
public void handlePayButtonAction() {
    // Lấy MaTaiKhoan từ đối tượng User đã đăng nhập
    User currentUser = UserSession.getCurrentUser();  // Lấy đối tượng User hiện tại
    if (currentUser == null) {
        System.out.println("Lỗi: Người dùng chưa đăng nhập.");
        return; // Kết thúc hàm nếu không có người dùng
    }
    
    int maTaiKhoan = currentUser.getId();  // Lấy MaTaiKhoan
    System.out.println("MaTaiKhoan: " + maTaiKhoan);
    
    // Lấy TongGia từ trường TxtShowPayTotalPrice
    double tongGia;
    try {
        tongGia = Double.parseDouble(TxtShowPayTotalPrice.getText());
    } catch (NumberFormatException e) {
        System.out.println("Lỗi: Tổng giá không hợp lệ.");
        return; // Kết thúc hàm nếu tổng giá không hợp lệ
    }

    // Trạng thái đơn hàng
    String trangThai = "Đã thanh toán"; // Giá trị cố định vì chúng ta đã thanh toán

    // Câu lệnh SQL để thêm đơn hàng mới
    String insertQuery = "INSERT INTO DonHang (MaTaiKhoan, TongGia, TrangThai) VALUES (?, ?, ?)";

    try (Connection conn = DataConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

        // Gán giá trị cho câu lệnh
        pstmt.setInt(1, maTaiKhoan);
        pstmt.setDouble(2, tongGia);
        pstmt.setString(3, trangThai);

        // Thực hiện câu lệnh
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            // Thêm thành công, có thể hiển thị thông báo hoặc làm gì đó khác
            System.out.println("Thanh toán thành công.");
            TableViewPay.getItems().clear();
            loadDonHang(); // Tải lại danh sách đơn hàng để cập nhật giao diện
        } else {
            // Thêm không thành công, xử lý lỗi
            System.out.println("Không thể thanh toán. Vui lòng thử lại.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Lỗi khi thực hiện thanh toán: " + e.getMessage());
    }
}





//******************************* Xử lí sửa *******************************************************
//Hàm lấy danh sách khóa chính từ bảng ThucDon
public List<Integer> getAllItemIDsFromThucDon() {
 List<Integer> itemIDs = new ArrayList<>();
 String query = "SELECT MaMon FROM ThucDon";
 try (Connection conn = DataConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(query);
      ResultSet rs = pstmt.executeQuery()) {

     while (rs.next()) {
         itemIDs.add(rs.getInt("MaMon"));
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
 return itemIDs;
}

//Hàm getAllIngredientIDs
public List<Integer> getAllIngredientIDs() {
 List<Integer> ingredientIDs = new ArrayList<>();
 String query = "SELECT MaNguyenLieu FROM NguyenLieu";
 try (Connection conn = DataConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(query);
      ResultSet rs = pstmt.executeQuery()) {

     while (rs.next()) {
         ingredientIDs.add(rs.getInt("MaNguyenLieu"));
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
 return ingredientIDs;
}

//Hàm editNguyenLieuInTableView
public void editNguyenLieuInTableView(NguyenLieu editedNguyenLieu) {
 List<Integer> thucDonItemIDs = getAllItemIDsFromThucDon();
 List<Integer> nguyenLieuIngredientIDs = getAllIngredientIDs();

 if (!thucDonItemIDs.contains(editedNguyenLieu.getId())) {
     System.out.println("MaMon không tồn tại trong ThucDon. Không thể cập nhật dữ liệu.");
     return;
 }

 if (!nguyenLieuIngredientIDs.contains(editedNguyenLieu.getId())) {
     System.out.println("MaNguyenLieu không tồn tại trong NguyenLieu. Không thể cập nhật dữ liệu.");
     return;
 }

 String updateQuery = "UPDATE NguyenLieu SET TenNguyenLieu = ?, SoLuong = ?, DonVi = ?, NgayCapNhat = GETDATE() WHERE MaNguyenLieu = ?";
 try (Connection conn = DataConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

     pstmt.setString(1, editedNguyenLieu.getName());
     pstmt.setDouble(2, editedNguyenLieu.getQuantity());
     pstmt.setString(3, editedNguyenLieu.getUnit());
     pstmt.setInt(4, editedNguyenLieu.getId());

     int rowsUpdated = pstmt.executeUpdate();
     if (rowsUpdated > 0) {
         System.out.println("Cập nhật dữ liệu thành công.");
     } else {
         System.out.println("Không có thay đổi nào được thực hiện.");
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
}


}
