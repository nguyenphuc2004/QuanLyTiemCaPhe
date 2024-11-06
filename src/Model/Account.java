package Model;


import java.sql.Timestamp;


public class Account {
	private String tenDangNhap;
	private String matKhau;
	private String phanQuyen;
	private String hoVaTen;
	private Timestamp ngayTao;
	
	public Account(String tenDangNhap, String matKhau, String phanQuyen, String hoVaTen, Timestamp ngayTao) {
		this.tenDangNhap = tenDangNhap;
		this.matKhau = matKhau;
		this.phanQuyen = phanQuyen;
		this.hoVaTen = hoVaTen;
		this.ngayTao = ngayTao;
	}

	public String getTenDangNhap() {
		return tenDangNhap;
	}

	public void setTenDangNhap(String tenDangNhap) {
		this.tenDangNhap = tenDangNhap;
	}

	public String getMatKhau() {
		return matKhau;
	}

	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}

	public String getPhanQuyen() {
		return phanQuyen;
	}

	public void setPhanQuyen(String phanQuyen) {
		this.phanQuyen = phanQuyen;
	}

	public String getHoVaTen() {
		return hoVaTen;
	}

	public void setHoVaTen(String hoVaTen) {
		this.hoVaTen = hoVaTen;
	}
	
	public Timestamp getNgayTao() {
		return ngayTao;
	}

	public void setNgayTao(Timestamp ngayTao) {
		this.ngayTao = ngayTao;
	}
	
}
