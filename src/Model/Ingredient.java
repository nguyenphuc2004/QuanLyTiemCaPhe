package Model;

import java.sql.Timestamp;

public class Ingredient {
	  private String tenNguyenLieu;  // Tên nguyên liệu
	    private double soLuong;        // Số lượng nguyên liệu
	    private String donVi;          // Đơn vị tính (kg, lít, gói,...)
	    private Timestamp ngayCapNhat; // Ngày cập nhật số lượng
	    private byte[] hinh;           // Hình ảnh nguyên liệu (dữ liệu nhị phân)

	    // Constructor đầy đủ
	    public Ingredient(String tenNguyenLieu, double soLuong, String donVi, Timestamp ngayCapNhat, byte[] hinh) {
	        this.tenNguyenLieu = tenNguyenLieu;
	        this.soLuong = soLuong;
	        this.donVi = donVi;
	        this.ngayCapNhat = ngayCapNhat;
	        this.hinh = hinh;
	    }

	    public String getTenNguyenLieu() {
	        return tenNguyenLieu;
	    }

	    public void setTenNguyenLieu(String tenNguyenLieu) {
	        this.tenNguyenLieu = tenNguyenLieu;
	    }

	    // Getter và Setter cho SoLuong
	    public double getSoLuong() {
	        return soLuong;
	    }

	    public void setSoLuong(double soLuong) {
	        this.soLuong = soLuong;
	    }

	    // Getter và Setter cho DonVi
	    public String getDonVi() {
	        return donVi;
	    }

	    public void setDonVi(String donVi) {
	        this.donVi = donVi;
	    }

	    // Getter và Setter cho NgayCapNhat
	    public Timestamp getNgayCapNhat() {
	        return ngayCapNhat;
	    }

	    public void setNgayCapNhat(Timestamp ngayCapNhat) {
	        this.ngayCapNhat = ngayCapNhat;
	    }

	    // Getter và Setter cho Hinh (ảnh)
	    public byte[] getHinh() {
	        return hinh;
	    }

	    public void setHinh(byte[] hinh) {
	        this.hinh = hinh;
	    }
}
