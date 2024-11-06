package application;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import Model.User;
import Model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
	   @FXML
	    private Button btnlogin;

	    @FXML
	    private CheckBox checkPass;

	    @FXML
	    private TextField name;

	    @FXML
	    private TextField passWord;
	    
	    @FXML
	    private PasswordField passwordField;

	    @FXML
	    public void initialize() {
	  
	        passWord.setVisible(false);
	        passwordField.setVisible(true);

	        checkPass.setOnAction(event -> {
	            if (checkPass.isSelected()) {
	         
	                passWord.setText(passwordField.getText()); 
	                passWord.setVisible(true);
	                passwordField.setVisible(false);
	            } else {
	               
	            	passwordField.setVisible(true);
	                passwordField.setText(passWord.getText());
	                passWord.setVisible(false);
	            }
	        });
	    }

	    public String hashText(String text) throws Exception {
			String plainText = text;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = digest.digest(plainText.getBytes());
			String str=Base64.getEncoder().encodeToString(hashedBytes);
			return str;
		}
	    
	    @FXML
	    void exit() {
	    	System.exit(0);
	    }
	  
	    @FXML
	    void loginClick() {
	        boolean valid = true;

	        // Kiểm tra trường name trống
	        if (name.getText().isEmpty()) {
	            name.setPromptText("Name không được để trống");
	            name.setStyle("-fx-prompt-text-fill: red;");
	            valid = false;
	        } else {
	            name.setStyle("");
	        }

	        // Kiểm tra trường passWord hoặc passwordField trống
	        TextField passwordFieldToCheck = checkPass.isSelected() ? passWord : passwordField;
	        if (passwordFieldToCheck.getText().isEmpty()) {
	            passwordFieldToCheck.setPromptText("Password không được để trống");
	            passwordFieldToCheck.setStyle("-fx-prompt-text-fill: red;");
	            valid = false;
	        } else {
	            passwordFieldToCheck.setStyle("");
	        }

	        // Nếu cả hai ô đều không trống, tiếp tục xử lý đăng nhập
	        if (valid) {
	            String username = name.getText();
	            String passwordInput = passwordFieldToCheck.getText();

	            try {
	                // Băm mật khẩu người dùng nhập
	                String hashedPasswordInput = hashText(passwordInput);

	                // Sử dụng kết nối từ DataConnection
	                Connection conn = DataConnection.getConnection();

	                // Truy vấn cơ sở dữ liệu để lấy mật khẩu, vai trò và MaTaiKhoan
	                String query = "SELECT MatKhau, PhanQuyen, MaTaiKhoan FROM TaiKhoan WHERE TenDangNhap = ?";
	                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                    pstmt.setString(1, username);

	                    try (ResultSet rs = pstmt.executeQuery()) {
	                        if (rs.next()) {
	                            byte[] hashedPasswordFromDB = rs.getBytes("MatKhau");
	                            String role = rs.getString("PhanQuyen");
	                            int maTaiKhoan = rs.getInt("MaTaiKhoan"); // Lấy MaTaiKhoan

	                            // So sánh byte từ cơ sở dữ liệu với byte của mật khẩu băm từ Java
	                            if (MessageDigest.isEqual(hashedPasswordFromDB, Base64.getDecoder().decode(hashedPasswordInput))) {
	                                // Đăng nhập thành công
	                            	User user = new User();
	                            	user.setId(maTaiKhoan);
	                            	user.setUsername(username);
	                            	user.setRole(role);
	                            	UserSession.setCurrentUser(user);  // Lưu người dùng vào phiên


	                                // Mở form tương ứng với vai trò
	                                if ("Admin".equalsIgnoreCase(role)) {
	                                    openUI("AdminUI.fxml");
	                                } else if ("Nhân viên".equalsIgnoreCase(role)) {
	                                    openUI("NhanVienUI.fxml");
	                                }
	                            } else {
	                                // Sai mật khẩu
	                                showAlert(AlertType.ERROR, "Lỗi đăng nhập", "Sai mật khẩu.");
	                            }
	                        } else {
	                            // Tên người dùng không tồn tại
	                            showAlert(AlertType.ERROR, "Lỗi đăng nhập", "Tên người dùng không tồn tại.");
	                        }
	                    }
	                }

	                // Đóng kết nối
	                conn.close();

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }


// Phương thức mở giao diện mới
private void openUI(String fxmlFile) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.show();
        
        // Đóng cửa sổ đăng nhập hiện tại
        Stage loginStage = (Stage) name.getScene().getWindow();
        loginStage.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Phương thức hiển thị thông báo lỗi
private void showAlert(AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
	  }

