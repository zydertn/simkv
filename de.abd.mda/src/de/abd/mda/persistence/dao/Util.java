package de.abd.mda.persistence.dao;

import java.io.File;

public class Util extends DaoObject {

	
	private int utilId;
	private int maxBillNumber;
	
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

}
