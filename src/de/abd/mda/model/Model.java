package de.abd.mda.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.util.FacesUtil;

public class Model {

	// Domain model related variables
	private List<Country> countries;
	private List<String> statusList;
	private List<String> supplierList;
		
	public static String COUNTRY_GERMANY = "DE";
	public static String COUNTRY_AUSTRIA = "AT";
	
	public static String STATUS_ACTIVE = "Aktiv";
	public static String STATUS_INACTIVE = "Inaktiv";
	public static String STATUS_DUMMY = "Dummy";
	
	public static String SUPPLIER_TELEKOM = "Telekom";
	public static String SUPPLIER_TELEKOM_AUSTRIA = "Telekom Austria";
	
	public static String FORMAT_HOCHFORMAT = "Hochformat";
	public static String FORMAT_QUERFORMAT = "Querformat";
	
	public static String FREQUENCY_MONTHLY = "monatlich";
	public static String FREQUENCY_QUARTERLY = "quartalsweise";
	public static String FREQUENCY_HALFYEARLY = "halbjährlich";
	public static String FREQUENCY_YEARLY = "jährlich";
	
	public static String COLUMN_POS = "Pos.";
	public static String COLUMN_AMOUNT = "Menge";
	public static String COLUMN_DESCRIPTION = "Bezeichnung";
	public static String COLUMN_PLANT_NUMBER = "Anlagen Nr.";
	public static String COLUMN_SINGLE_PRICE = "Einzelpreis";
	public static String COLUMN_TOTAL_PRICE = "Gesamtpreis";
	
	
	public static String GENDER_MAN = "Herr";
	public static String GENDER_WOMAN = "Frau";
	public static String GENDER_COMPANY = "Firma";

	public Map<Integer, Double> simPrices;
	public Map<Integer, Double> dataOptionSurcharges;
	
	public List<String> getSupplierList() {
		return supplierList;
	}

	public void setSupplierList(List<String> supplierList) {
		this.supplierList = supplierList;
	}

	private HashMap<String, Float> columnSize;
	private List<String> invoiceFormats;
	private List<String> invoiceCreationFrequencies;
	private List<String> invoiceColumns;

	public Model() {
	}

	public void createModel() {
		countries = new ArrayList<Country>();
		countries.add(new Country("Deutschland", "DE", "+49"));
		countries.add(new Country("Österreich", "AT", "+43"));
		
		statusList = new ArrayList<String>();
		statusList.add(Model.STATUS_ACTIVE);
		statusList.add(Model.STATUS_INACTIVE);
		statusList.add(Model.STATUS_DUMMY);
		
		supplierList = new ArrayList<String>();
		supplierList.add(Model.SUPPLIER_TELEKOM);
		supplierList.add(Model.SUPPLIER_TELEKOM_AUSTRIA);
		
		invoiceFormats = new ArrayList<String>();
		invoiceFormats.add(Model.FORMAT_HOCHFORMAT);
		invoiceFormats.add(Model.FORMAT_QUERFORMAT);
		
		invoiceCreationFrequencies = new ArrayList<String>();
		invoiceCreationFrequencies.add(Model.FREQUENCY_MONTHLY);
		invoiceCreationFrequencies.add(Model.FREQUENCY_QUARTERLY);
		invoiceCreationFrequencies.add(Model.FREQUENCY_HALFYEARLY);
		invoiceCreationFrequencies.add(Model.FREQUENCY_YEARLY);
		
		invoiceColumns = new ArrayList<String>();
//		invoiceColumns.add(COLUMN_POS);
		invoiceColumns.add(COLUMN_AMOUNT);
		invoiceColumns.add(COLUMN_DESCRIPTION);
		invoiceColumns.add(COLUMN_PLANT_NUMBER);
		invoiceColumns.add(COLUMN_SINGLE_PRICE);
		invoiceColumns.add(COLUMN_TOTAL_PRICE);
		
		columnSize = new HashMap<String, Float>();
		columnSize.put(COLUMN_POS, 1.5f);
		columnSize.put(COLUMN_AMOUNT, 2f);
		columnSize.put(COLUMN_DESCRIPTION, 12.5f);
		columnSize.put(COLUMN_PLANT_NUMBER, 9f);
		columnSize.put(COLUMN_SINGLE_PRICE, 4f);
		columnSize.put(COLUMN_TOTAL_PRICE, 4f);
	}

	public List<SelectItem> getCountryCodes() {
		List<SelectItem> codes = new ArrayList<SelectItem>();
		Iterator<Country> it = countries.iterator();
		while (it.hasNext()) {
			codes.add(new SelectItem(it.next().getInternationalAreaCode()));
		}
		return codes;
	}

	public Country getCountryByShortName(String shortName) {
		Iterator<Country> it = countries.iterator();
		while (it.hasNext()) {
			Country c = it.next();
			if (c.getShortName().equals(shortName))
				return c;
		}
		
		return null;
	}
	
	public List<SelectItem> getCountryNames() {
		List<SelectItem> countryNames = new ArrayList<SelectItem>();
		Iterator<Country> it = countries.iterator();
		while (it.hasNext()) {
			countryNames.add(new SelectItem(it.next().getShortName()));
		}
		return countryNames;
	}
	
	public List<SelectItem> getGenders() {
		List<SelectItem> genders = new ArrayList<SelectItem>();
		genders.add(new SelectItem(Model.GENDER_MAN));
		genders.add(new SelectItem(Model.GENDER_WOMAN));
		genders.add(new SelectItem(Model.GENDER_COMPANY));
		return genders;
	}
	
	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}
	
	public List<String> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<String> statusList) {
		this.statusList = statusList;
	}

	public List<String> getInvoiceFormats() {
		return invoiceFormats;
	}

	public void setInvoiceFormats(List<String> invoiceFormats) {
		this.invoiceFormats = invoiceFormats;
	}

	public List<String> getInvoiceCreationFrequencies() {
		return invoiceCreationFrequencies;
	}

	public void setInvoiceCreationFrequencies(
			List<String> invoiceCreationFrequencies) {
		this.invoiceCreationFrequencies = invoiceCreationFrequencies;
	}

	public Map<Integer, Double> getSimPrices() {
		if (simPrices == null || (simPrices != null && simPrices.size() == 0) || (FacesUtil.getAttributeFromRequest("updateSimPrices") != null)) {
			ConfigurationController c = new ConfigurationController();
			simPrices = c.getSimPricesFromDB();
		}
		return simPrices;
	}
	
	public Map<Integer, Double> getDataOptionSurcharges() {
		if (dataOptionSurcharges == null || (dataOptionSurcharges != null && dataOptionSurcharges.size() == 0)  || (FacesUtil.getAttributeFromRequest("updateDataOptions") != null)) {
			ConfigurationController c = new ConfigurationController();
			dataOptionSurcharges = c.getDataOptionPricesFromDB();
		}
		return dataOptionSurcharges;
	}

	public List<String> getInvoiceColumns() {
		return invoiceColumns;
	}

	public void setInvoiceColumns(List<String> invoiceColumns) {
		this.invoiceColumns = invoiceColumns;
	}

	public HashMap<String, Float> getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(HashMap<String, Float> columnSize) {
		this.columnSize = columnSize;
	}

}