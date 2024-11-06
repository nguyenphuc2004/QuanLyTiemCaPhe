package Model;

import java.time.LocalDateTime;

public class DonHang {
private int idOrder;
private int idUser;
private double totalPrice;
private String status;


public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
public int getIdOrder() {
	return idOrder;
}
public void setIdOrder(int idOrder) {
	this.idOrder = idOrder;
}
public int getIdUser() {
	return idUser;
}
public void setIdUser(int idUser) {
	this.idUser = idUser;
}
public double getTotalPrice() {
	return totalPrice;
}
public void setTotalPrice(double totalPrice) {
	this.totalPrice = totalPrice;
}


}
