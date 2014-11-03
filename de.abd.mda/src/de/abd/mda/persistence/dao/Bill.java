package de.abd.mda.persistence.dao;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

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
	private boolean flatrate;
	private boolean finalized;
	private BigDecimal bruttoPrice;
	private BigDecimal nettoPrice;
	private BigDecimal vat;
	private Calendar calcDate;
	private Timestamp updateTime;
	
	
	
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

	public boolean isFlatrate() {
		return flatrate;
	}

	public void setFlatrate(boolean flatrate) {
		this.flatrate = flatrate;
	}

	public BigDecimal getBruttoPrice() {
		return bruttoPrice;
	}

	public void setBruttoPrice(BigDecimal bruttoPrice) {
		this.bruttoPrice = bruttoPrice;
	}

	public BigDecimal getNettoPrice() {
		return nettoPrice;
	}

	public void setNettoPrice(BigDecimal nettoPrice) {
		this.nettoPrice = nettoPrice;
	}

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public Calendar getCalcDate() {
		return calcDate;
	}

	public void setCalcDate(Calendar calcDate) {
		this.calcDate = calcDate;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
