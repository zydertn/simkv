package de.abd.mda.persistence.dao;

import java.io.File;

public class Bill extends DaoObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6907539712172216071L;

	private int billId;
	private int billNumber;
	private int customerNumber;
	private int year;
	private int month;
	private byte[] file;
	private String filename;
	private int mapCount;
	private boolean finalized;
	
	public Bill() {
		
	}

	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}

	public int getBillId() {
		return billId;
	}

	public void setBillId(int billId) {
		this.billId = billId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public int getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(int customerNumber) {
		this.customerNumber = customerNumber;
	}

	public int getMapCount() {
		return mapCount;
	}

	public void setMapCount(int mapCount) {
		this.mapCount = mapCount;
	}
}
