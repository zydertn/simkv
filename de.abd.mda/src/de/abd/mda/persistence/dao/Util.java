package de.abd.mda.persistence.dao;

import java.io.File;

import com.mysql.jdbc.Blob;

public class Util extends DaoObject {

	
	private int utilId;
	private int maxBillNumber;
	private byte[] header;
	private byte[] address;
	private byte[] footer;
	
	public Util() {
		
	}

	public int getUtilId() {
		return utilId;
	}

	public void setUtilId(int utilId) {
		this.utilId = utilId;
	}

	public int getMaxBillNumber() {
		return maxBillNumber;
	}

	public void setMaxBillNumber(int maxBillNumber) {
		this.maxBillNumber = maxBillNumber;
	}

	public byte[] getHeader() {
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public byte[] getFooter() {
		return footer;
	}

	public void setFooter(byte[] footer) {
		this.footer = footer;
	}



}
