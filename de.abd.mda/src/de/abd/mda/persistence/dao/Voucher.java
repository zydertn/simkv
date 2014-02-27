package de.abd.mda.persistence.dao;

import de.abd.mda.util.DateUtils;


public class Voucher extends DaoObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2530542315448818246L;
	private int voucherId;
	private int month;
	private int year;
	private int cardAmount;
	private double cardVoucher;
	private double totalVoucher;
	
	public Voucher() {
	}

	public String getMonthYearString() {
		return DateUtils.getMonthAsString(month) + " " + year;
	}
	
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getCardAmount() {
		return cardAmount;
	}
	public void setCardAmount(int cardAmount) {
		this.cardAmount = cardAmount;
	}
	public double getCardVoucher() {
		return cardVoucher;
	}
	public void setCardVoucher(double cardVoucher) {
		this.cardVoucher = cardVoucher;
	}
	public double getTotalVoucher() {
		return totalVoucher;
	}
	public void setTotalVoucher(double totalVoucher) {
		this.totalVoucher = totalVoucher;
	}


	public int getVoucherId() {
		return voucherId;
	}


	public void setVoucherId(int voucherId) {
		this.voucherId = voucherId;
	}

}
