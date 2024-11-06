package Model;

public class Menu {
	    private String tenMon;
	    private String loaiMon;
	    private float gia;
	    private boolean conBan;
	    private byte[] hinh;

	    public Menu(String tenMon, String loaiMon, float gia, boolean conBan, byte[] hinh) {
	        this.tenMon = tenMon;
	        this.loaiMon = loaiMon;
	        this.gia = gia;
	        this.conBan = conBan;
	        this.hinh = hinh;
	    }

	    public String getTenMon() {
	        return tenMon;
	    }

	    public void setTenMon(String tenMon) {
	        this.tenMon = tenMon;
	    }

	    public String getLoaiMon() {
	        return loaiMon;
	    }

	    public void setLoaiMon(String loaiMon) {
	        this.loaiMon = loaiMon;
	    }

	    public float getGia() {
	        return gia;
	    }

	    public void setGia(float gia) {
	        this.gia = gia;
	    }

	    public boolean isConBan() {
	        return conBan;
	    }

	    public void setConBan(boolean conBan) {
	        this.conBan = conBan;
	    }

	    public byte[] getHinh() {
	        return hinh;
	    }

	    public void setHinh(byte[] hinh) {
	        this.hinh = hinh;
	    }
}
