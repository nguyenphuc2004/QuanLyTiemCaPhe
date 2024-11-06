package application;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Timer;

import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


import Model.Menu;
import Model.OrderDetails;
import Model.Account;
import Model.DoanhThu;
import Model.Ingredient;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
	import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class ControllerAdmin {

	@FXML
	private AnchorPane goHome, goOders, goMaterial, goBill, goStatistics, goAccount;
	// Thanh chuyển hướng
	@FXML
	private ImageView Home, Oders, Material, Bill, Statistics, Account, logOut;
	// Trang Home
	@FXML
	private BarChart<String, Double> barChart;
	@FXML
	private Label lbDay;

	////////////////////////////////////////////////////
	// ---- Đây là bảng tài khoản ----/////////
	
	@FXML
	private TableView<Account> tableAccount;

	@FXML
	private TableColumn<Account, String> userNameColumn;

	@FXML
	private TableColumn<Account, String> passwordColumn;

	@FXML
	private TableColumn<Account, String> roleColumn;

	@FXML
	private TableColumn<Account, String> fullNameColumn;

	@FXML
	private TableColumn<Account, String> createDayColumn;

	private ObservableList<Account> AccountList = FXCollections.observableArrayList();

	@FXML
	private TextField textUserName, textFullName, textPassWord;

	@FXML
	private ComboBox<String> comboBoxRole;
	////////////////////////////////////////////////////

	// ---- Đây là bảng menu ----/////////

	@FXML
	private TableView<Menu> tableThucDon;

	@FXML
	private TableColumn<Menu, String> columnTenMon;

	@FXML
	private TableColumn<Menu, String> columnLoaiMon;

	@FXML
	private TableColumn<Menu, Float> columnGia;

	@FXML
	private TableColumn<Menu, Boolean> columnConBan;



	private ObservableList<Menu> MenuList = FXCollections.observableArrayList();

	@FXML
	private TextField textTenMon, textGia;

	@FXML
	private ComboBox<String> cbbLoaiMon, cbbConBan;

	@FXML
	private ImageView imgIMPORTOders, imgIMPORTNguyenLieu;

	// ---- Đây là bảng NGUYÊN LIỆU ---- ////////////
	@FXML
	private TableView<Ingredient> tableNguyenLieu;

	@FXML
	private TableColumn<Ingredient, String> columnTenNL;

	@FXML
	private TableColumn<Ingredient, Double> columnSoLuongNL;

	@FXML
	private TableColumn<Ingredient, String> columnDonViNL;

	@FXML
	private TableColumn<Ingredient, String> columnNgayCapNhat;



	private ObservableList<Ingredient> ingredientList = FXCollections.observableArrayList();

	@FXML
	private ComboBox<String> cbbDonViNL;

	@FXML
	private TextField textTenNL, textSoLuongNL;

	//// ------ ĐÂY LÀ BẢNG CHI TIẾT HÓA ĐƠN ------
	@FXML
	private TableView<OrderDetails> tableChiTietHoaDon;

	@FXML
	private TableColumn<OrderDetails, String> columnTenMonCTHD;

	@FXML
	private TableColumn<OrderDetails, Integer> columnSoLuongCTHD;

	@FXML
	private TableColumn<OrderDetails, Double> columnGiaCTHD;

	@FXML
	private TableColumn<OrderDetails, Double> columnTongGiaCTHD;

	@FXML
	private TableColumn<OrderDetails, String> columnTenNguoiNhapCTHD;

	@FXML
	private TableColumn<OrderDetails, String> columnNgayDatHangCTHD;

	private ObservableList<OrderDetails> orderDetailsList = FXCollections.observableArrayList();

	@FXML
	private TextField textTimTheoTen;

	@FXML
	private DatePicker dateTimTheoNgayThang;

	////////////////////////////////////////////////////
	////------ ĐÂY LÀ BẢNG DOANH THU ------/////
	@FXML
	private TableView<DoanhThu> tableRevenue;
	
	@FXML
	private TableColumn<DoanhThu, Double> columnTongDoanhThu;

	@FXML
	private TableColumn<DoanhThu, Double> columnTongNLSD;

	@FXML
	private TableColumn<DoanhThu, String> columnMonBanChay;
	
	@FXML
	private TableColumn<DoanhThu, Date> columnNgay;
	
	private ObservableList<DoanhThu> doanhThuList = FXCollections.observableArrayList();

	@FXML
	private DatePicker textTimKiemDoanhThu;
	
	////////////////////////////////////////////////////
	//////---------BẢNG ĐƠN HÀNG--------/////////////////
	@FXML
	private Label labelMoney, labelQuantity, labelDate;
	/////////////////////////////////////////////////////////
	@FXML
	public void initialize() {
		// Create a series for the bar chart
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName("To Day");
        // Add data to the series (5 data points as requested)
        series.getData().add(new XYChart.Data<>("Cà Phê Sữa", 25000.0));
        series.getData().add(new XYChart.Data<>("Cà Phê Đen", 20000.0));
        series.getData().add(new XYChart.Data<>("Trà Đào", 25000.0));
        series.getData().add(new XYChart.Data<>("Bánh Mì", 15000.0));
        series.getData().add(new XYChart.Data<>("Phở Bò", 30000.0));

        // Add the series to the bar chart
        barChart.getData().add(series);
        barChart.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		// Phần hiển thị thông tin từ bảng xuống textField để sửa
		tableAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				displayAccountInfo(newSelection);
			}
		});

		tableThucDon.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				displayMenuInfo(newSelection);
			}
		});
		tableNguyenLieu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				displayIngredientInfo(newSelection);
			}
		});
		///////////////////////////////////////////
		///////////---- BẢNG ĐƠN HÀNG -----////////
		try {
			Connection conn = DataConnection.getConnection();

			String sql = "SELECT SUM(DH.TongGia) AS tongGia, CTDH.SoLuong AS Soluong\r\n"
					+ "	FROM DonHang DH\r\n"
					+ "	JOIN ChiTietDonHang CTDH ON DH.MaDonHang = CTDH.MaDonHang\r\n"
					+ "	WHERE CONVERT(varchar, DH.Ngaydat, 102) = '2024.10.24'\r\n"
					+ "	GROUP BY CTDH.SoLuong;";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				Double tongGia = rs.getDouble(1);
				int soLuong = rs.getInt(2);
				
				labelMoney.setText("$ " + String.valueOf(tongGia));
				labelQuantity.setText("Quantity " + String.valueOf(soLuong));
						
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//////////////////////////////////////////////
        //////--- BẢNG DOANH THU---///////////////////
		columnTongDoanhThu.setCellValueFactory(new PropertyValueFactory<>("tongDoanhThu"));
		columnTongNLSD.setCellValueFactory(new PropertyValueFactory<>("tongNguyenLieuDaSuDung"));
		columnMonBanChay.setCellValueFactory(new PropertyValueFactory<>("monBanChay"));
		columnNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
		
		tableRevenue.setItems(doanhThuList);
		
		try {
			Connection conn = DataConnection.getConnection();

			String sql = "SELECT \r\n"
					+ "    DH.NgayDat,\r\n"
					+ "    SUM(CTDH.Gia * CTDH.SoLuong) AS TongDoanhThu,  -- Tổng doanh thu\r\n"
					+ "    SUM(NL.SoLuong) AS TongNguyenLieuSuDung,  -- Tổng nguyên liệu đã sử dụng\r\n"
					+ "    (SELECT TOP 1\r\n"
					+ "        TD.TenMon \r\n"
					+ "     FROM \r\n"
					+ "        ChiTietDonHang CTDH\r\n"
					+ "     JOIN \r\n"
					+ "        ThucDon TD ON CTDH.MaMon = TD.MaMon\r\n"
					+ "     GROUP BY \r\n"
					+ "        TD.TenMon\r\n"
					+ "     ORDER BY \r\n"
					+ "        SUM(CTDH.SoLuong) DESC) AS MonBanChay  -- Món bán chạy nhất\r\n"
					+ "FROM \r\n"
					+ "    DonHang DH\r\n"
					+ "JOIN \r\n"
					+ "    ChiTietDonHang CTDH ON DH.MaDonHang = CTDH.MaDonHang\r\n"
					+ "LEFT JOIN \r\n"
					+ "    NguyenLieu NL ON CTDH.MaMon = NL.MaMon  -- Giả sử bạn cần thông tin nguyên liệu từ bảng NguyenLieu\r\n"
					+ "GROUP BY \r\n"
					+ "    DH.NgayDat;";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Timestamp ngay = rs.getTimestamp(1);
				Double tongDoanhTHu = rs.getDouble(2);
				Double tongNguyenLieuDaSuDung = rs.getDouble(3);
				String monBanChay = rs.getString(4);
				

				DoanhThu newDoanhThu = new DoanhThu(tongDoanhTHu, tongNguyenLieuDaSuDung, monBanChay,ngay);
				doanhThuList.add(newDoanhThu);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		////////////////////////////////////////////////
        //-----------BẢNG CHI TIẾT HÓA ĐƠN ----------------/////
		columnTenMonCTHD.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
		columnSoLuongCTHD.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
		columnGiaCTHD.setCellValueFactory(new PropertyValueFactory<>("gia"));
		columnTongGiaCTHD.setCellValueFactory(new PropertyValueFactory<>("tongGia"));
		columnTenNguoiNhapCTHD.setCellValueFactory(new PropertyValueFactory<>("tenNguoiNhap"));
		columnNgayDatHangCTHD.setCellValueFactory(new PropertyValueFactory<>("ngayDatHang"));
		tableChiTietHoaDon.setItems(orderDetailsList);

		try {
			Connection conn = DataConnection.getConnection();

			String sql = "SELECT \r\n"
					+ "    TD.TenMon,CTDH.SoLuong,CTDH.Gia,(CTDH.SoLuong * CTDH.Gia) AS TongGia,TK.TenDayDu AS TenNguoiNhap,DH.NgayDat\r\n"
					+ "FROM \r\n"
					+ "    ChiTietDonHang CTDH JOIN ThucDon TD ON CTDH.MaMon = TD.MaMon JOIN DonHang DH ON CTDH.MaDonHang = DH.MaDonHang\r\n"
					+ "JOIN TaiKhoan TK ON DH.MaTaiKhoan = TK.MaTaiKhoan;";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String tenMon = rs.getString(1);
				int soLuong = rs.getInt(2);
				Double gia = rs.getDouble(3);
				Double tongGia = rs.getDouble(4);
				String tenNguoiNhap = rs.getString(5);
				Timestamp ngayTao = rs.getTimestamp(6);

				OrderDetails newOrderDetails = new OrderDetails(tenMon, soLuong, gia, tongGia, tenNguoiNhap, ngayTao);
				orderDetailsList.add(newOrderDetails);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

////////////////////////////////////////////////////////////////////////////////////////
		// BẢNG TÀI KHOẢN
		userNameColumn.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
		passwordColumn.setCellValueFactory(new PropertyValueFactory<>("matKhau"));
		roleColumn.setCellValueFactory(new PropertyValueFactory<>("phanQuyen"));
		fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("hoVaTen"));
		createDayColumn.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
		tableAccount.setItems(AccountList);

		cbbLoaiMon.getItems().addAll("Thức uống", "Đồ ăn");
		cbbLoaiMon.setValue("Thức uống");

		cbbConBan.getItems().addAll("True", "False");
		cbbConBan.setValue("True");

		try {
			Connection conn = DataConnection.getConnection();

			String sql = "select * from TaiKhoan";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String TenTaiKhoan = rs.getString(2);
				String matKhau = rs.getString(3);
				String phanQuyen = rs.getString(4);
				String hoVaTen = rs.getString(5);
				Timestamp ngayTao = rs.getTimestamp(6);

				Account newAccount = new Account(TenTaiKhoan, matKhau, phanQuyen, hoVaTen, ngayTao);
				AccountList.add(newAccount);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		////////////////////////////////////////////////////////
		// BẢNG MENU
		columnTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
		columnLoaiMon.setCellValueFactory(new PropertyValueFactory<>("loaiMon"));
		columnGia.setCellValueFactory(new PropertyValueFactory<>("gia"));
		columnConBan.setCellValueFactory(new PropertyValueFactory<>("conBan"));
		
		tableThucDon.setItems(MenuList);

		comboBoxRole.getItems().addAll("Admin", "Nhân viên");
		comboBoxRole.setValue("Admin");

		try {
			Connection conn = DataConnection.getConnection();

			String sql = "select * from ThucDon";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String tenMon = rs.getString(2);
				String loaiMon = rs.getString(3);
				Float gia = rs.getFloat(4);
				boolean conBan = rs.getBoolean(5);
				byte[] hinh = rs.getBytes(6);

				Menu newMenu = new Menu(tenMon, loaiMon, gia, conBan, hinh);
				MenuList.add(newMenu);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//////////////////////////////////////////////////
		// Bảng NGUYÊN LIỆU
		columnTenNL.setCellValueFactory(new PropertyValueFactory<>("tenNguyenLieu"));
		columnSoLuongNL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
		columnDonViNL.setCellValueFactory(new PropertyValueFactory<>("donVi"));
		columnNgayCapNhat.setCellValueFactory(new PropertyValueFactory<>("ngayCapNhat"));
		
		tableNguyenLieu.setItems(ingredientList);

		cbbDonViNL.getItems().addAll("Kg", "Trái", "Lít");
		cbbDonViNL.setValue("Kg");
		try {
			Connection conn = DataConnection.getConnection();
			String sql = "select * from NguyenLieu";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String tenNL = rs.getString(3);
				Double soLuongNL = rs.getDouble(4);
				String donViNL = rs.getString(5);
				Timestamp ngayCapNhatNL = rs.getTimestamp(6);
				byte[] hinhNL = rs.getBytes(7);

				Ingredient newNguyenLieu = new Ingredient(tenNL, soLuongNL, donViNL, ngayCapNhatNL, hinhNL);
				ingredientList.add(newNguyenLieu);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		startClock();
	}
	  
	
	private void startClock() {
        // Sử dụng AnimationTimer để cập nhật giờ mỗi giây
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateDateTime();
            }
        };
        timer.start();
    }
	 // Phương thức cập nhật ngày giờ
	 private void updateDateTime() {
	        LocalDateTime now = LocalDateTime.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        labelDate.setText(now.format(formatter));
	    }
	 
///////////////////////////////////////////////////////
	///-- BẢNG DOANH THU ----/////////////////
	@FXML
	public void refeshDoanhThu() {
		doanhThuList.clear();
		try {
			Connection conn = DataConnection.getConnection();

			String sql = "SELECT \r\n"
					+ "    DH.NgayDat,\r\n"
					+ "    SUM(CTDH.Gia * CTDH.SoLuong) AS TongDoanhThu,  -- Tổng doanh thu\r\n"
					+ "    SUM(NL.SoLuong) AS TongNguyenLieuSuDung,  -- Tổng nguyên liệu đã sử dụng\r\n"
					+ "    (SELECT TOP 1\r\n"
					+ "        TD.TenMon \r\n"
					+ "     FROM \r\n"
					+ "        ChiTietDonHang CTDH\r\n"
					+ "     JOIN \r\n"
					+ "        ThucDon TD ON CTDH.MaMon = TD.MaMon\r\n"
					+ "     GROUP BY \r\n"
					+ "        TD.TenMon\r\n"
					+ "     ORDER BY \r\n"
					+ "        SUM(CTDH.SoLuong) DESC) AS MonBanChay  -- Món bán chạy nhất\r\n"
					+ "FROM \r\n"
					+ "    DonHang DH\r\n"
					+ "JOIN \r\n"
					+ "    ChiTietDonHang CTDH ON DH.MaDonHang = CTDH.MaDonHang\r\n"
					+ "LEFT JOIN \r\n"
					+ "    NguyenLieu NL ON CTDH.MaMon = NL.MaMon  -- Giả sử bạn cần thông tin nguyên liệu từ bảng NguyenLieu\r\n"
					+ "GROUP BY \r\n"
					+ "    DH.NgayDat;";
			PreparedStatement pst = conn.prepareStatement(sql);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				Timestamp ngay = rs.getTimestamp(1);
				Double tongDoanhTHu = rs.getDouble(2);
				Double tongNguyenLieuDaSuDung = rs.getDouble(3);
				String monBanChay = rs.getString(4);

				DoanhThu newDoanhThu = new DoanhThu(tongDoanhTHu, tongNguyenLieuDaSuDung, monBanChay,ngay);
				doanhThuList.add(newDoanhThu);
			}
			tableRevenue.setItems(doanhThuList);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	@FXML
	public void btnTimKiemDoanhThu() {
	    // Lấy ngày cần tìm từ DatePicker
	    LocalDate ngayCanTim = textTimKiemDoanhThu.getValue();

	    // Tạo danh sách kết quả tìm kiếm
	    ObservableList<DoanhThu> orderSeachList = FXCollections.observableArrayList();

	    // Kiểm tra điều kiện ngày đặt hàng
	    if (ngayCanTim != null) {
	        // Duyệt qua danh sách để tìm kiếm
	        for (DoanhThu doanhThu : doanhThuList) {
	            // Chuyển đổi từ Timestamp sang LocalDate để so sánh
	            LocalDate ngayDatHang = doanhThu.getNgay().toLocalDateTime().toLocalDate();
	            
	            // Kiểm tra xem ngày đặt hàng có khớp với ngày cần tìm không
	            if (ngayDatHang.equals(ngayCanTim)) {
	                // Nếu khớp, thêm vào danh sách kết quả tìm kiếm
	                orderSeachList.add(doanhThu);
	            }
	        }
	    }

	    // Cập nhật TableView với danh sách kết quả tìm kiếm
	    tableRevenue.setItems(orderSeachList);

	    // Kiểm tra nếu không có kết quả
	    if (orderSeachList.isEmpty()) {
	        System.out.println("Không tìm thấy doanh thu cho ngày đã chọn.");
	    }
	}
	///////////////--KẾT THÚC BẢNG DOANH ////////////////////////////
////////////////////////////////////////////////
	// -------------BẢNG CHI TIẾT HÓA ĐƠN-----------///////////
	// Hàm tìm kiếm theo tên
	@FXML
	public void orderDetailsSearch() {
		// Lấy tên cần tìm từ TextField và chuyển thành chữ thường để tìm kiếm
		String tenCanTim = textTimTheoTen.getText().toLowerCase().trim();
		LocalDate ngayCanTim = dateTimTheoNgayThang.getValue();

		// Tạo danh sách kết quả tìm kiếm
		ObservableList<OrderDetails> orderSeachList = FXCollections.observableArrayList();

		// Duyệt qua danh sách để tìm kiếm
		for (OrderDetails orderDetails : orderDetailsList) {
			boolean khopTen = true;
			boolean khopNgay = true;

			// Kiểm tra điều kiện tên món
			if (!tenCanTim.isEmpty()) {
				khopTen = orderDetails.getTenMon().toLowerCase().contains(tenCanTim);
			}

			// Kiểm tra điều kiện ngày đặt hàng
			if (ngayCanTim != null) {
				// Chuyển đổi từ Timestamp sang LocalDate để so sánh
				LocalDate ngayDatHang = orderDetails.getNgayDatHang().toLocalDateTime().toLocalDate();
				khopNgay = ngayDatHang.equals(ngayCanTim);
			}

			// Chỉ thêm vào danh sách nếu thỏa mãn cả điều kiện tên và ngày (nếu có)
			if (khopTen && khopNgay) {
				orderSeachList.add(orderDetails);
			}
		}

		// Cập nhật TableView với danh sách kết quả tìm kiếm
		tableChiTietHoaDon.setItems(orderSeachList);
	}

	@FXML
	public void clearCTHD() {
		textTimTheoTen.setText("");
		dateTimTheoNgayThang.setValue(null);
	}
	// ---------KẾT THÚC BẢNG CHI TIẾT HÓA ĐƠN------------///////////
////////////////////////////////////////////////////////
	// ----------- BẢNG NGUYÊN LIỆU --------------
	@FXML
	public void importImageNL() {

		FileChooser fileChooser = new FileChooser();
		// Chỉ cho phép người dùng chọn tệp ảnh
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
				"*.jpeg", "*.gif");
		fileChooser.getExtensionFilters().add(imageFilter);

		// Hiển thị hộp thoại chọn tệp
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Hiển thị hình ảnh trong ImageView
			Image image = new Image(selectedFile.toURI().toString());
			imgIMPORTNguyenLieu.setImage(image); // imageView là ImageView trong giao diện của bạn
		} else {
			System.out.println("File not selected");
		}
	}

	public void clearIngredient() {

		textTenNL.setText("");
		textSoLuongNL.setText("");
		imgIMPORTNguyenLieu.setImage(null);
	}
	
	@FXML
	public void addIngredient() {
		String tenNL = textTenNL.getText();
		String soLuongNL = textSoLuongNL.getText();
		String donViNL = cbbDonViNL.getValue();
		Timestamp ngayCapNhatNL = new Timestamp(System.currentTimeMillis());
		byte[] hinh = getImageBytes(imgIMPORTNguyenLieu);

		if (tenNL == "" || soLuongNL == "") {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please fill out this form!");
			alert.showAndWait();
			return;
		}
		if (hinh == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please select an image!");
			alert.showAndWait();
			return; // Trả về nếu không có hình
		}

		try {
			Double.valueOf(soLuongNL);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please enter quantity as a number!");
			alert.showAndWait();
			textSoLuongNL.clear();
			return;
		}
		Ingredient selectedProduct = tableNguyenLieu.getSelectionModel().getSelectedItem();
		if(selectedProduct.getTenNguyenLieu().contains(tenNL)) {
			return;
		}
		try {
			Connection conn = DataConnection.getConnection();
			String sql = "INSERT INTO NguyenLieu (TenNguyenLieu, SoLuong, DonVi, Hinh) VALUES (?, ?, ?, ?)";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, tenNL); // Tên món
			pst.setDouble(2, Double.valueOf(soLuongNL)); // Loại món
			pst.setString(3, donViNL); // Giá món (parse giá từ String sang float)
			pst.setBytes(4, hinh); // Hình ảnh (dưới dạng byte[])

			pst.executeUpdate();

			Ingredient newIngredient = new Ingredient(tenNL, Double.valueOf(soLuongNL), donViNL, ngayCapNhatNL, hinh);
			ingredientList.add(newIngredient);
			textTenNL.clear();
			textSoLuongNL.clear();
			imgIMPORTNguyenLieu.setImage(null);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void displayIngredientInfo(Ingredient selectedProduct) {
		textTenNL.setText(selectedProduct.getTenNguyenLieu());
		textSoLuongNL.setText(String.valueOf(selectedProduct.getSoLuong()));
		cbbDonViNL.setValue(selectedProduct.getDonVi());
		byte[] imageData = selectedProduct.getHinh();
		try {
			// Chuyển đổi byte[] thành BufferedImage
			ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
			BufferedImage bufferedImage = ImageIO.read(bis);

			// Chuyển đổi BufferedImage thành Image JavaFX
			Image image = SwingFXUtils.toFXImage(bufferedImage, null);
			imgIMPORTNguyenLieu.setImage(image);
		} catch (Exception e) {
			e.printStackTrace();
			// Có thể hiển thị một hình ảnh mặc định nếu có lỗi
			imgIMPORTNguyenLieu.setImage(new Image("path/to/default/image.png"));
		}

	}

	@FXML
	public void editIngredient() {
		Ingredient selectedProduct = tableNguyenLieu.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			String tenNL = textTenNL.getText();
			String soLuongNL = textSoLuongNL.getText();
			String donViNL = cbbDonViNL.getValue();
			Timestamp ngayCapNhatNL = new Timestamp(System.currentTimeMillis());
			byte[] hinh = getImageBytes(imgIMPORTNguyenLieu);

			try {
				Connection conn = DataConnection.getConnection();
				String sql = "UPDATE NguyenLieu SET tenNguyenLieu = ?, soLuong = ?, donVi = ?, hinh = ? WHERE tenNguyenLieu = ?";
				PreparedStatement pst = conn.prepareStatement(sql);
				pst.setString(1, tenNL); // Tên món
				pst.setDouble(2, Double.valueOf(soLuongNL)); // Loại món
				pst.setString(3, donViNL); // Giá món (parse giá từ String sang float)
				pst.setBytes(4, hinh); // Hình ảnh (dưới dạng byte[])
				pst.setString(5, selectedProduct.getTenNguyenLieu());

				pst.executeUpdate();

				// Cập nhật thông tin trong ObservableList
				selectedProduct.setTenNguyenLieu(tenNL);
				selectedProduct.setSoLuong(Double.valueOf(soLuongNL));
				selectedProduct.setDonVi(donViNL);
				selectedProduct.setNgayCapNhat(ngayCapNhatNL);
				selectedProduct.setHinh(hinh);

				textTenNL.setText("");
				textSoLuongNL.setText("");
				imgIMPORTNguyenLieu.setImage(null);

				tableNguyenLieu.refresh(); // Làm mới bảng để hiển thị sự thay đổi

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to edit!");
			alert.showAndWait();
		}
	}

	@FXML
	public void deleteIngredient() {
		// Lấy sản phẩm được chọn từ bảng
		Ingredient selectedProduct = tableNguyenLieu.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Are you sure you want to delete it!");
			alert.showAndWait().ifPresent(rs -> {
				if (rs == ButtonType.CANCEL) {
					iscancel = true;
				} else {
					try {
						Connection conn = DataConnection.getConnection();
						String sql = "DELETE FROM NguyenLieu WHERE tenNguyenLieu = ?";
						PreparedStatement pst = conn.prepareStatement(sql);

						pst.setString(1, selectedProduct.getTenNguyenLieu());
						pst.executeUpdate();

						// Xóa sản phẩm khỏi ObservableList
						ingredientList.remove(selectedProduct);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			if (iscancel == true)
				return;
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to delete!");
			alert.showAndWait();
		}
	}


///////////////////////////////////////////////////////
	// ------------ BẢNG MENU----------------------

	public void importImage() {

		FileChooser fileChooser = new FileChooser();
		// Chỉ cho phép người dùng chọn tệp ảnh
		FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
				"*.jpeg", "*.gif");
		fileChooser.getExtensionFilters().add(imageFilter);

		// Hiển thị hộp thoại chọn tệp
		File selectedFile = fileChooser.showOpenDialog(null);

		if (selectedFile != null) {
			// Hiển thị hình ảnh trong ImageView
			Image image = new Image(selectedFile.toURI().toString());
			imgIMPORTOders.setImage(image); // imageView là ImageView trong giao diện của bạn
		} else {
			System.out.println("File not selected");
		}
	}

	private byte[] getImageBytes(ImageView imageView) {
		Image image = imageView.getImage();
		if (image != null) {
			// Chuyển đổi hình ảnh thành BufferedImage
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				ImageIO.write(bufferedImage, "png", outputStream); // Hoặc "jpg" tùy thuộc vào định dạng bạn sử dụng
				return outputStream.toByteArray(); // Trả về mảng byte
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null; // Trả về null nếu không có hình ảnh
	}

	public void clearMenu() {
		textTenMon.setText("");
		textGia.setText("");
		imgIMPORTOders.setImage(null);
	}

	@FXML
	public void addMenu() {
		String tenMon = textTenMon.getText();
		String gia = textGia.getText();
		String loaiMon = cbbLoaiMon.getValue();
		String conBan = cbbConBan.getValue();

		byte[] hinh = getImageBytes(imgIMPORTOders);

		if (tenMon == "" || gia == "") {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please fill out this form!");
			alert.showAndWait();
			return;
		}
		if (hinh == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please select an image!");
			alert.showAndWait();
			return; // Trả về nếu không có hình
		}
		try {
			Float.parseFloat(gia);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please enter price as a number!");
			alert.showAndWait();
			textGia.clear();
			return;
		}
		Menu selectedProduct = tableThucDon.getSelectionModel().getSelectedItem();
		if(selectedProduct.getTenMon().contains(tenMon)){
			return;
		}
		try {

			Connection conn = DataConnection.getConnection();
			String sql = "INSERT INTO ThucDon (TenMon, LoaiMon, Gia, ConBan, Hinh) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, tenMon); // Tên món
			pst.setString(2, loaiMon); // Loại món
			pst.setFloat(3, Float.parseFloat(gia)); // Giá món (parse giá từ String sang float)
			pst.setBoolean(4, Boolean.parseBoolean(conBan)); // Trạng thái còn bán
			pst.setBytes(5, hinh); // Hình ảnh (dưới dạng byte[])

			pst.executeUpdate();

			Menu newMenu = new Menu(tenMon, loaiMon, Float.parseFloat(gia), Boolean.parseBoolean(conBan), hinh);
			MenuList.add(newMenu);
			textTenMon.clear();
			textGia.clear();
			imgIMPORTOders.setImage(null);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void displayMenuInfo(Menu selectedProduct) {
		// Gán giá trị từ bảng cho textField
		textTenMon.setText(selectedProduct.getTenMon());
		cbbLoaiMon.setValue(selectedProduct.getLoaiMon());
		textGia.setText(String.valueOf(selectedProduct.getGia()));
		// Kiểm tra xem hình có khác null không
		byte[] imageData = selectedProduct.getHinh(); // Giả sử hinh là byte[]
		try {
			// Chuyển đổi byte[] thành BufferedImage
			ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
			BufferedImage bufferedImage = ImageIO.read(bis);

			// Chuyển đổi BufferedImage thành Image JavaFX
			Image image = SwingFXUtils.toFXImage(bufferedImage, null);
			imgIMPORTOders.setImage(image);
		} catch (Exception e) {
			e.printStackTrace();
			// Có thể hiển thị một hình ảnh mặc định nếu có lỗi
			imgIMPORTOders.setImage(new Image("path/to/default/image.png"));
		}

	}

	@FXML
	public void editMenu() {
		Menu selectedProduct = tableThucDon.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			String tenMon = textTenMon.getText();
			String gia = textGia.getText();
			String loaiMon = cbbLoaiMon.getValue();
			String conBan = cbbConBan.getValue();
			byte[] hinh = getImageBytes(imgIMPORTOders);

			try {
				Connection conn = DataConnection.getConnection();
				String sql = "UPDATE ThucDon SET tenMon = ?, loaiMon = ?, gia = ?,conBan = ?, hinh = ? WHERE tenMon = ?";
				PreparedStatement pst = conn.prepareStatement(sql);
				pst.setString(1, tenMon); // Tên món
				pst.setString(2, loaiMon); // Loại món
				pst.setFloat(3, Float.parseFloat(gia)); // Giá món (parse giá từ String sang float)
				pst.setBoolean(4, Boolean.parseBoolean(conBan)); // Trạng thái còn bán
				pst.setBytes(5, hinh); // Hình ảnh (dưới dạng byte[])
				pst.setString(6, selectedProduct.getTenMon());

				pst.executeUpdate();

				// Cập nhật thông tin trong ObservableList
				selectedProduct.setTenMon(tenMon);
				selectedProduct.setLoaiMon(loaiMon);
				selectedProduct.setGia(Float.parseFloat(gia));
				selectedProduct.setConBan(Boolean.parseBoolean(conBan));
				selectedProduct.setHinh(hinh);

				textTenMon.setText("");
				textGia.setText("");
				imgIMPORTOders.setImage(null);

				tableThucDon.refresh(); // Làm mới bảng để hiển thị sự thay đổi

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to edit!");
			alert.showAndWait();
		}
	}

	@FXML
	public void deleteMenu() {

		// Lấy sản phẩm được chọn từ bảng
		Menu selectedProduct = tableThucDon.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Are you sure you want to delete it!");
			alert.showAndWait().ifPresent(rs -> {
				if (rs == ButtonType.CANCEL) {
					iscancel = true;
				} else {
					try {
						Connection conn = DataConnection.getConnection();
						String sql = "DELETE FROM ThucDon WHERE tenMon = ?";
						PreparedStatement pst = conn.prepareStatement(sql);

						pst.setString(1, selectedProduct.getTenMon());
						pst.executeUpdate();

						// Xóa sản phẩm khỏi ObservableList
						MenuList.remove(selectedProduct);

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			if (iscancel == true)
				return;
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to delete!");
			alert.showAndWait();
		}
	}

	// ------- KẾT THÚC BẢNG MENU ----------------/////
//////////////////////////////////////////////////
	// ----------- BẢNG TÀI KHOẢN -----------////////
	@FXML
	public void refeshTable() {
		AccountList.clear(); // Xóa danh sách hiện tại
		try {
			Connection conn = DataConnection.getConnection();
			String sql = "SELECT TenDangNhap, MatKhau, PhanQuyen, TenDayDu, NgayTao FROM TaiKhoan";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				String tenTaiKhoan = rs.getString(1);
				String matKhau = rs.getString(2);
				String phanQuyen = rs.getString(3);
				String hoVaTen = rs.getString(4);
				Timestamp ngayTao = rs.getTimestamp(5);

				Account newAccount = new Account(tenTaiKhoan, matKhau, phanQuyen, hoVaTen, ngayTao);
				AccountList.add(newAccount);
			}

			// Cập nhật lại bảng để hiển thị dữ liệu mới
			tableAccount.setItems(AccountList);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Hàm băm mật khẩu sử dụng SHA-256
	private String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(password.getBytes());

			// Chuyển byte array thành chuỗi thập lục phân (hex)
			return bytesToHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private String bytesToHex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	@FXML
	public void addAccount() {
		String tenTaiKhoan = textUserName.getText();
		String matKhau = textPassWord.getText();
		String hoVaTen = textFullName.getText();
		String phanQuyen = comboBoxRole.getValue();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		if (tenTaiKhoan == "" || matKhau == "" || hoVaTen == "") {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification..");
			alert.setHeaderText("Please fill out this form!");
			alert.showAndWait();
			return;
		}
		Account selectedProduct = tableAccount.getSelectionModel().getSelectedItem();
		if(selectedProduct.getTenDangNhap().contains(tenTaiKhoan)) {
			return;
		}
		try {
			String hashedPassword = hashPassword(matKhau);

			Connection conn = DataConnection.getConnection();
			String sql = "INSERT INTO TaiKhoan (TenDangNhap,MatKhau,PhanQuyen,TenDayDu) \r\n" + "VALUES (?, ?, ?, ?);";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, tenTaiKhoan);
			pst.setString(2, hashedPassword);
			pst.setString(3, phanQuyen);
			pst.setString(4, hoVaTen);

			pst.executeUpdate();

			Account newAccount = new Account(tenTaiKhoan, matKhau, phanQuyen, hoVaTen, timestamp);
			AccountList.add(newAccount);
			textUserName.clear();
			textPassWord.clear();
			textFullName.clear();
			refeshTable();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void clearAccount() {
		textUserName.clear();
		textPassWord.clear();
		textFullName.clear();
	}

	private void displayAccountInfo(Account selectedProduct) {
		// Hiển thị thông tin tài khoản được chọn lên các textField
		textUserName.setText(selectedProduct.getTenDangNhap());
		textFullName.setText(selectedProduct.getHoVaTen());
		comboBoxRole.setValue(selectedProduct.getPhanQuyen());
	}

	@FXML
	public void editAccount() {
		Account selectedProduct = tableAccount.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			String tenTaiKhoan = textUserName.getText();
			String matKhau = textPassWord.getText();
			String hoVaTen = textFullName.getText();
			String phanQuyen = comboBoxRole.getValue();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			try {

				String hashedPassword = hashPassword(matKhau);

				Connection conn = DataConnection.getConnection();
				String sql = "UPDATE TaiKhoan SET tenDangNhap = ?, matKhau = ?, phanQuyen = ?,tenDayDu = ? WHERE tenDangNhap = ?";
				PreparedStatement pst = conn.prepareStatement(sql);
				pst.setString(1, tenTaiKhoan);
				pst.setString(2, hashedPassword);
				pst.setString(3, phanQuyen);
				pst.setString(4, hoVaTen);
				pst.setString(5, selectedProduct.getTenDangNhap());

				pst.executeUpdate();

				// Cập nhật thông tin trong ObservableList
				selectedProduct.setTenDangNhap(tenTaiKhoan);
				selectedProduct.setMatKhau(matKhau);
				selectedProduct.setPhanQuyen(phanQuyen);
				selectedProduct.setHoVaTen(hoVaTen);
				selectedProduct.setNgayTao(timestamp);

				tableAccount.refresh(); // Làm mới bảng để hiển thị sự thay đổi

				refeshTable();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to edit!");
			alert.showAndWait();
		}
	}

	boolean iscancel = false;

	@FXML
	public void deleteAccount() {
		// Lấy sản phẩm được chọn từ bảng
		Account selectedProduct = tableAccount.getSelectionModel().getSelectedItem();
		if (selectedProduct != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Are you sure you want to delete it!");
			alert.showAndWait().ifPresent(rs -> {
				if (rs == ButtonType.CANCEL) {
					iscancel = true;
				} else {
					try {
						Connection conn = DataConnection.getConnection();
						String sql = "DELETE FROM TaiKhoan WHERE tenDangNhap = ?";
						PreparedStatement pst = conn.prepareStatement(sql);

						pst.setString(1, selectedProduct.getTenDangNhap());
						pst.executeUpdate();

						// Xóa sản phẩm khỏi ObservableList
						AccountList.remove(selectedProduct);

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			if (iscancel == true)
				return;
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notification...");
			alert.setHeaderText("Please select the item to delete!");
			alert.showAndWait();
		}
	}

	// -------------- KẾT THÚC BẢNG TÀI KHOẢN -----------///
////////////////////////////////////////////////////////////////////////

	// Trang Home
	@FXML
	public void HomeClick() {
		resetStyles();
		Home.setOpacity(1);
		goHome.setVisible(true);
		goOders.setVisible(false);
		goMaterial.setVisible(false);
		goBill.setVisible(false);
		goStatistics.setVisible(false);
		goAccount.setVisible(false);
	}

	// Trang Oders
	@FXML
	public void OdersClick() {
		resetStyles();
		Oders.setOpacity(1);
		goHome.setVisible(false);
		goOders.setVisible(true);
		goMaterial.setVisible(false);
		goBill.setVisible(false);
		goStatistics.setVisible(false);
		goAccount.setVisible(false);
	}

	// Trang Material
	@FXML
	public void MaterialClick() {
		resetStyles();
		Material.setOpacity(1);
		goHome.setVisible(false);
		goOders.setVisible(false);
		goMaterial.setVisible(true);
		goBill.setVisible(false);
		goStatistics.setVisible(false);
		goAccount.setVisible(false);
	}

	// Trang Bill
	@FXML
	public void BillClick() {
		resetStyles();
		Bill.setOpacity(1);
		goHome.setVisible(false);
		goOders.setVisible(false);
		goMaterial.setVisible(false);
		goBill.setVisible(true);
		goStatistics.setVisible(false);
		goAccount.setVisible(false);
	}

	// Trang Statistics
	@FXML
	public void StatisticsClick() {
		resetStyles();
		Statistics.setOpacity(1);
		goHome.setVisible(false);
		goOders.setVisible(false);
		goMaterial.setVisible(false);
		goBill.setVisible(false);
		goStatistics.setVisible(true);
		goAccount.setVisible(false);
	}

	// Trang Account
	@FXML
	public void AccountClick() {
		resetStyles();
		Account.setOpacity(1);
		goHome.setVisible(false);
		goOders.setVisible(false);
		goMaterial.setVisible(false);
		goBill.setVisible(false);
		goStatistics.setVisible(false);
		goAccount.setVisible(true);
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
		Home.setOpacity(0.37);
		Oders.setOpacity(0.37);
		Material.setOpacity(0.37);
		Bill.setOpacity(0.37);
		Statistics.setOpacity(0.37);
		Account.setOpacity(0.37);
	}
}
