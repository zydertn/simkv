package de.abd.mda.persistence.dao;

import java.util.HashMap;
import java.util.Map;

public class Configuration extends DaoObject {

	/**
	 * 
	 */
	/**
	 * 
	 */
	private static final long serialVersionUID = 3429161913114726492L;
	private Map<Integer, Double> simPrices = new HashMap<Integer, Double>();
	private Map<Integer, Double> dataOptionPrices = new HashMap<Integer, Double>();
	private Map<Integer, String> sortingOptions = new HashMap<Integer, String>();
	private int id;
	private int reportProgress;
	private int customer;
	private long lastReportUpdate;

	public Configuration() {
	}

	public Map<Integer, Double> getSimPrices() {
		return simPrices;
	}

	public void setSimPrices(Map<Integer, Double> simPrices) {
		this.simPrices = simPrices;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<Integer, Double> getDataOptionPrices() {
		return dataOptionPrices;
	}

	public void setDataOptionPrices(Map<Integer, Double> dataOptionPrices) {
		this.dataOptionPrices = dataOptionPrices;
	}

	public int getReportProgress() {
		return reportProgress;
	}

	public void setReportProgress(int reportProgress) {
		this.reportProgress = reportProgress;
	}

	public int getCustomer() {
		return customer;
	}

	public void setCustomer(int customer) {
		this.customer = customer;
	}

	public long getLastReportUpdate() {
		return lastReportUpdate;
	}

	public void setLastReportUpdate(long lastReportUpdate) {
		this.lastReportUpdate = lastReportUpdate;
	}

	public Map<Integer, String> getSortingOptions() {
		return sortingOptions;
	}

	public void setSortingOptions(Map<Integer, String> sortingOptions) {
		this.sortingOptions = sortingOptions;
	}

}
