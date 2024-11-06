package Model;

import java.sql.Timestamp;

public class DoanhThu {
	private Double tongDoanhThu;
	private Double tongNguyenLieuDaSuDung;
	private String monBanChay;
	private Timestamp ngay;
	
	public DoanhThu(Double tongDoanhThu, Double tongNguyenLieuDaSuDung, String monBanChay, Timestamp ngay) {
		this.tongDoanhThu = tongDoanhThu;
		this.tongNguyenLieuDaSuDung = tongNguyenLieuDaSuDung;
		this.monBanChay = monBanChay;
		this.ngay = ngay;
	}

	public Double getTongDoanhThu() {
		return tongDoanhThu;
	}

	public void setTongDoanhThu(Double tongDoanhThu) {
		this.tongDoanhThu = tongDoanhThu;
	}

	public Double getTongNguyenLieuDaSuDung() {
		return tongNguyenLieuDaSuDung;
	}

	public void setTongNguyenLieuDaSuDung(Double tongNguyenLieuDaSuDung) {
		this.tongNguyenLieuDaSuDung = tongNguyenLieuDaSuDung;
	}

	public String getMonBanChay() {
		return monBanChay;
	}

	public void setMonBanChay(String monBanChay) {
		this.monBanChay = monBanChay;
	}

	public Timestamp getNgay() {
		return ngay;
	}

	public void setNgay(Timestamp ngay) {
		this.ngay = ngay;
	}
	
	
}
