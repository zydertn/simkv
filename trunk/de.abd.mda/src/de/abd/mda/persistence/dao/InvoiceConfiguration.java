package de.abd.mda.persistence.dao;

import de.abd.mda.model.Model;

public class InvoiceConfiguration extends DaoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 46437276670900355L;
	private int id;
	private int simPrice;
	private int dataOptionSurcharge;
	private String format;
	private String[] columns;
	private String creationFrequency;
	private Boolean separateBilling;
	private String[] separateBillingCriteria;
	
	
	public InvoiceConfiguration() {
		this.format = "Hochformat";
		this.columns = new String[5];
//		columns[0] = "1";
//		columns[1] = "3";
		this.creationFrequency = "monatlich";
	}

	public int getSimPrice() {
		return simPrice;
	}
	
	public String getSimPriceString() {
		Model model = new Model();
		model.createModel();
		Double simP = model.getSimPrices().get(simPrice);
		String s = "" + simP;
		if (s.indexOf(".") == (s.length()-2))
			s = s + "0";

		return "" + s + "€ (Preiskategorie " + simPrice + ")";
	}

	public void setSimPrice(int simPrice) {
		this.simPrice = simPrice;
	}

	public int getDataOptionSurcharge() {
		return dataOptionSurcharge;
	}

	public void setDataOptionSurcharge(int dataOptionSurcharge) {
		this.dataOptionSurcharge = dataOptionSurcharge;
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getCreationFrequency() {
		return creationFrequency;
	}

	public void setCreationFrequency(String invoiceCreationFrequency) {
		this.creationFrequency = invoiceCreationFrequency;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String[] getSeparateBillingCriteria() {
		return separateBillingCriteria;
	}

	public void setSeparateBillingCriteria(String[] separateBillingCriteria) {
		this.separateBillingCriteria = separateBillingCriteria;
	}

	public Boolean getSeparateBilling() {
		return separateBilling;
	}

	public void setSeparateBilling(Boolean separateBilling) {
		this.separateBilling = separateBilling;
	}
	
}
