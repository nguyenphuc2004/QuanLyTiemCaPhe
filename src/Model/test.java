//package application;
//
//import java.io.IOException;
//import java.net.URL;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.ResourceBundle;
//
//import Model.DonHang;
//import Model.Item;
//import Model.NguyenLieu;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.fxml.Initializable;
//import javafx.geometry.Insets;
//import javafx.scene.chart.BarChart;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
//import javafx.scene.control.TextField;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.GridPane;
//
//public class SampleController {
//	
//	 
//    @FXML
//    private AnchorPane goHome, goOders, goMaterial, goBill, goStatistics, goAccount;
//    //Thanh chuyển hướng
//    @FXML
//    private ImageView Home, Oders, Material, Bill, Statistics, Account, logOut;
//    // Trang Home
//    @FXML
//    private BarChart<String, Double> barChart;
//    @FXML
//    private Label lbDay;
//   
//    //Trang Oders
//    @FXML
//    private Button btnAdd, btnClear, btnDelete, btnEdit;
//    @FXML
//    private TextField textName, textPrice;
//    
////    TableView nguyen lieu
//    @FXML
//    private TableView<NguyenLieu> TableViewMaterial;
//    @FXML
//    private TableColumn<NguyenLieu, Integer> CoLumnID ;
//    @FXML
//    private TableColumn<NguyenLieu, String> columnName ;
//    @FXML
//    private TableColumn<NguyenLieu, Double > columnQuantity ;
//    @FXML
//    private TableColumn<NguyenLieu, String> columnUnit ;
//    @FXML
//    private TableColumn<NguyenLieu, LocalDateTime> columnUpdateDate ;
//    ObservableList<NguyenLieu> nguyenlieuList =
//			FXCollections.observableArrayList();
//    
////  TableView don hang
//    @FXML
//    private TableView<DonHang> CalendarTableView;
//    @FXML
//    private TableColumn<DonHang, Integer> OrderIdColumn;
//    @FXML
//    private TableColumn<DonHang, Integer> UserIdColumn;
//    @FXML
//    private TableColumn<DonHang, Double> TotalPriceColumn;
//    @FXML
//    private TableColumn<DonHang, LocalDateTime> OrderDateColumn;
//    ObservableList<DonHang> donhanglist = FXCollections.observableArrayList();
//    
//    
//    //Item
//    @FXML
//    private ImageView imgItem;
//
//    @FXML
//    private Label nameLableItem;
//
//    @FXML
//    private Label priceLableItem;
//
//    //MenuItem
//    @FXML
//    private javafx.scene.control.ScrollPane scrollMenuItem;
//    @FXML
//    private GridPane GridPaneMenuItem;
//    
//    
//    @FXML
//    public void initialize() {
//    	//Render data nguyen lieu
//        CoLumnID.setCellValueFactory(new PropertyValueFactory<>("id")); //("id")) la cai thuoc tinh cua class nguyen lieu
//        columnName.setCellValueFactory(new PropertyValueFactory<>("name")); 
//        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity")); 
//        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
//        columnUpdateDate.setCellValueFactory(new PropertyValueFactory<>("updateDate")); 
//
//        TableViewMaterial.setItems(nguyenlieuList);
//        loadNguyenLieu();
//        
//        //Render data don hang
//        OrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
//        UserIdColumn.setCellValueFactory(new PropertyValueFactory<>("idUser"));
//        TotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
//        OrderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
//       
//        CalendarTableView.setItems(donhanglist);
//        loadDonHang();
//        
//        renderItemsToGridPane();
//     		
//             
//    }
//
//
//    // Trang Home
//    @FXML
//    public void HomeClick() {
//        resetStyles();  
//        Home.setOpacity(1); 
//        goOders.setVisible(false);
//        goMaterial.setVisible(false);
//        goBill.setVisible(false);
//    }
//
//    @FXML
//    public void OdersClick(){
//        resetStyles();  
//        Oders.setOpacity(1);  
//        goOders.setVisible(true);
//        goMaterial.setVisible(false);
//        goBill.setVisible(false);
//
//    }
//
//    @FXML
//    public void MaterialClick(){
//        resetStyles();  
//        Material.setOpacity(1);  
//        goOders.setVisible(false);
//        goMaterial.setVisible(true);
//        goBill.setVisible(false);
//    }
//
//    @FXML
//    public void BillClick(){
//        resetStyles();  
//        Bill.setOpacity(1);
//        goOders.setVisible(false);
//        goMaterial.setVisible(false);
//        goBill.setVisible(true);
//    }
//
//    @FXML
//    public void StatisticsClick(){
//    }
//
//    @FXML
//    public void AccountClick(){
//    }
//
//    @FXML
//    public void logOutClick(){
//        resetStyles();  
//        logOut.setOpacity(1);  
//        System.exit(0);
//    }
//
//    private void resetStyles() {
//        Home.setOpacity(0.37);
//        Oders.setOpacity(0.37);
//        Material.setOpacity(0.37);
//        Bill.setOpacity(0.37);
//        Statistics.setOpacity(0.37);
//        Account.setOpacity(0.37);
//    }
//    
////    *********************************  XỬ LÍ SQL   ********************************************
//    @FXML
//	public void loadNguyenLieu() 
//		{
//		String query = "SELECT IngredientID, IngredientName, Quantity, Unit,LastUpdated  FROM NguyenLieu";
//		try (Connection conn = DataConnection.getConnection();
//		PreparedStatement pstmt = conn.prepareStatement(query);
//		ResultSet rs = pstmt.executeQuery()) 
//		{
//				
//				while (rs.next()) {
//					NguyenLieu nguyenlieu = new NguyenLieu();
//					nguyenlieu.setId(rs.getInt("IngredientID"));					
//					nguyenlieu.setName(rs.getString("IngredientName"));
//					nguyenlieu.setQuantity(rs.getDouble("Quantity"));
//					nguyenlieu.setUnit(rs.getString("Unit"));
//					nguyenlieu.setUpdateDate(rs.getTimestamp("LastUpdated").toLocalDateTime());
//
//										
//					
//					
//					nguyenlieuList.add(nguyenlieu);
//				}
//				TableViewMaterial.setItems(nguyenlieuList);
//		}
//		catch (SQLException e) {
//		e.printStackTrace();
//		}
//	}
//    
//    
//    @FXML
//   	public void loadDonHang() 
//   		{
//   		String query = "SELECT OrderID, UserID, TotalPrice, OrderDate  FROM DonHang";
//   		try (Connection conn = DataConnection.getConnection();
//   		PreparedStatement pstmt = conn.prepareStatement(query);
//   		ResultSet rs = pstmt.executeQuery()) 
//   		{
//   				
//   				while (rs.next()) {
//   					DonHang donhang = new DonHang();
//   					donhang.setIdOrder(rs.getInt("OrderID"));					
//   					donhang.setIdUser(rs.getInt("UserID"));
//   					donhang.setTotalPrice(rs.getDouble("TotalPrice"));
//   					donhang.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
//
//   										
//   					
//   					
//   					donhanglist.add(donhang);
//   				}
//   				CalendarTableView.setItems(donhanglist);
//   		}
//   		catch (SQLException e) {
//   		e.printStackTrace();
//   		}
//   	}
//    
//    private List<Item> items = new ArrayList<>();
//    private Image img;
//    private List<Item> getDataItems (){
//    	List<Item> items = new ArrayList<>();
//    	Item item;
//    	for (int i = 0; i < 20; i++) {
//    		item = new Item();
//			item.setName("Bột mì");
//			item.setPrice(50.000);
//			item.setImageSrc("..\\img\\Temp.png");
//			 items.add(item); 
//		}
//    	
//    	return items;
//    	
//    }
//
//    private Item it;
//private void setDataItem(Item it) {
//	this.it = it;
//	nameLableItem.setText(it.getName());
//	priceLableItem.setText(Main.CURRENCY + it.getPrice());
//	Image image = new Image(getClass().getResourceAsStream(it.getImageSrc()));
//	imgItem.setImage(image);
//}
//	
//public void renderItemsToGridPane() {
//    items.addAll(getDataItems());
//    int column = 0;
//    int row = 0;
//    try {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/ItemNew.fxml"));
//        AnchorPane anchorpane = fxmlLoader.load();
//        
//        if (anchorpane != null) {
//            GridPaneMenuItem.add(anchorpane, column, row); 
//        } else {
//            System.out.println("Lỗi: Không thể tải được Item.fxml");
//        }
//    } catch (IOException e) {
//        System.err.println("Error loading FXML: " + e.getMessage());
//        e.printStackTrace();
//    }
//}
//	
//}
