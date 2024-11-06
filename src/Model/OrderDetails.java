package Model;

import java.sql.Timestamp;

public class OrderDetails {
	private String tenMon;
	private int soLuong;
	private Double gia;
	private Double tongGia;
	private String tenNguoiNhap;
	private Timestamp ngayDatHang;
	
	public OrderDetails(String tenMon, int soLuong, Double gia, Double tongGia, String tenNguoiNhap,
			Timestamp ngayDatHang) {
		this.tenMon = tenMon;
		this.soLuong = soLuong;
		this.gia = gia;
		this.tongGia = tongGia;
		this.tenNguoiNhap = tenNguoiNhap;
		this.ngayDatHang = ngayDatHang;
	}

	public String getTenMon() {
		return tenMon;
	}

	public void setTenMon(String tenMon) {
		this.tenMon = tenMon;
	}

	public int getSoLuong() {
		return soLuong;
	}

	public void setSoLuong(int soLuong) {
		this.soLuong = soLuong;
	}

	public Double getGia() {
		return gia;
	}

	public void setGia(Double gia) {
		this.gia = gia;
	}

	public Double getTongGia() {
		return tongGia;
	}

	public void setTongGia(Double tongGia) {
		this.tongGia = tongGia;
	}

	public String getTenNguoiNhap() {
		return tenNguoiNhap;
	}

	public void setTenNguoiNhap(String tenNguoiNhap) {
		this.tenNguoiNhap = tenNguoiNhap;
	}

	public Timestamp getNgayDatHang() {
		return ngayDatHang;
	}

	public void setNgayDatHang(Timestamp ngayDatHang) {
		this.ngayDatHang = ngayDatHang;
	}
	
	
	
}	
