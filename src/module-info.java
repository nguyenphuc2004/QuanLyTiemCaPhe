module UI_admin {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;
    
    opens application to javafx.graphics, javafx.fxml, javafx.base;
    opens Model to javafx.base;  // Mở gói Model cho javafx.base để truy cập class NguyenLieu
}
