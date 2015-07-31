package de.abd.mda.model;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import de.abd.mda.controller.SearchCardController;
import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.persistence.dao.controller.CountryController;
import de.abd.mda.util.FacesUtil;

public class Model {

	private final static Logger LOGGER = Logger.getLogger(Model.class .getName()); 

	// Domain model related variables
	private List<Country> countries;
	private List<String> statusList;
	private List<String> supplierList;
	private List<String> paymentModaltyList;
		
	public static String COUNTRY_GERMANY = "DE";
	public static String COUNTRY_AUSTRIA = "AT";
	
	public static String STATUS_ACTIVE = "Aktiv";
	public static String STATUS_INACTIVE = "Inaktiv";
	public static String STATUS_DUMMY = "Dummy";
	
	public static String SUPPLIER_TELEKOM = "Deutschland";
	public static String SUPPLIER_TELEKOM_AUSTRIA = "Austria";
	
	public static String FORMAT_HOCHFORMAT = "Hochformat";
	public static String FORMAT_QUERFORMAT = "Querformat";
	
	public static String FREQUENCY_MONTHLY = "monatlich";
	public static String FREQUENCY_QUARTERLY = "quartalsweise";
	public static String FREQUENCY_HALFYEARLY = "halbjährlich";
	public static String FREQUENCY_YEARLY = "jährlich";
	
	public static String COLUMN_POS = "Pos.";
	public static String COLUMN_AMOUNT = "Monate";
	public static String COLUMN_DESCRIPTION = "Bezeichnung";
	public static String COLUMN_PLANT_NUMBER = "Anlagen Nr.";
	public static String COLUMN_INST_PLZ = "PLZ";
	public static String COLUMN_EINSATZORT = "Einsatzort";
	public static String COLUMN_INST_STREET = "Straße";
	public static String COLUMN_EQUIP_NR = "Equipment Nr.";
	public static String COLUMN_AUFTRAGS_NR = "Auftrags-Nr.";
	public static String COLUMN_CARD_NR = "Kartennummer";
	public static String COLUMN_TEL_NR = "Rufnummer";
	public static String COLUMN_BESTELL_NR = "Bestell-Nr.";
	public static String COLUMN_VERTRAG_NR = "Vertragsnummer";
	public static String COLUMN_BA_NR = "BA-Nummer";
	public static String COLUMN_WE_NR = "WE-Nummer";
	public static String COLUMN_COST_CENTER = "Kostenstelle";
//	public static String COLUMN_SINGLE_PRICE = "Preis";
	public static String COLUMN_TOTAL_PRICE = "Preis";

	public static String BILLING_WE_NR = "WE-Nummer";
	public static String BILLING_BESTELL_NR = "Bestell-Nr.";
	
	public static String GENDER_MAN = "Herr";
	public static String GENDER_WOMAN = "Frau";
	public static String GENDER_COMPANY = "Firma";

	public List<String> relationList;
	public static String RELATION_0 = "";
	public static String RELATION_1 = "Relation1";
	public static String RELATION_2 = "Relation2";

	public static String JAN = "Januar";
	public static String FEB = "Februar";
	public static String MAR = "März";
	public static String APR = "April";
	public static String MAY = "Mai";
	public static String JUN = "Juni";
	public static String JUL = "Juli";
	public static String AUG = "August";
	public static String SEP = "September";
	public static String OCT = "Oktober";
	public static String NOV = "November";
	public static String DEC = "Dezember";
	
	public static String CSV = "CSV";
	public static String EXC = "EXCEL";
	
	
	public String cardDeM2m = "M2M SW APN";
	public String cardDeM2MOtis = "M2M OTIS APN";
	public String cardDeHbn = "HBN-Data";
	public String cardDeTc = "Testkarte";
	
	public String cardDeEmptyCmt = "";
	public String cardDeM2mCmt = "??? Frau Kiesenbauer, hier schauen wir noch ein mal";
	public String cardDeM2MOtisCmt = "";
	public String cardDeHbnCmt = "Unsere neue deutsche Karte für das HBN Modul mit fixer IP und ohne Sprachanteil";
	public String cardDeTcCmt = "Bei Einsatz als Testkarte, zB wird nicht abgerechnet";

	public String cardAut1 = "Ö1";
	public String cardAut2 = "Ö3";
	public String cardAut3 = "Ö5";
	public String cardAut4 = "EU";
	public String cardAut5 = "NTS";
	public String cardAut6 = "HBN-EU";
	public String cardAut7 = "HBN-NTS";
	public String cardAut8 = "Testkarte";
	public String cardAut9 = "Sonderkarte";

	public String cardAutAA1 = "Austria Intern";
	public String cardAutAA2 = "Europa";

	public String paymentInvoice = "Rechnung";
	public String paymentDebit = "Lastschrift";
	
	public static String PAYMENT_MODALTY_MONTHLY = "monatlich";
	public static String PAYMENT_MODALTY_QUARTERLY = "quartalsweise";
	public static String PAYMENT_MODALTY_HALFYEARLY = "halbjährlich";
	public static String PAYMENT_MODALTY_YEARLY = "jährlich";
	public static String PAYMENT_MODALTY_DIRECT_DEBIT = "Einzugsermächtigung";

	
	
	public Map<Integer, Double> simPrices;
	public Map<Integer, Double> dataOptionSurcharges;
	public Map<Integer, String> sortingOptions;
	
	private HashMap<String, Float> columnSize;
	private List<String> invoiceFormats;
	private List<String> invoiceCreationFrequencies;
	private List<String> invoiceColumns;
	private List<String> paymentMethods;
	private List<Integer> invoiceRowList;
	private List<String> billingCriteria;
	private HashMap<Integer, String> months;
	private List<Integer> years;
	private HashMap<Integer, String> formats;
	
	private String pdfPath = "";
	private String zipPath = "";
	private String exportPath = "";
	
	public static int SORTING_ACTIVATION_DATE = 1;
	public static int SORTING_ALPHABETICAL = 2; 
	
	public Model() {
		LOGGER.info("Instantiate: Model");
	}

	public void createModel() {
		if (countries == null) {
			CountryController cc = new CountryController();
			countries = cc.listCountries();
		}
//		countries = new ArrayList<Country>();
//		countries.add(new Country("Deutschland", "DE", "+49"));
//		countries.add(new Country("Dänemark", "DK", "+45"));
//		countries.add(new Country("Irland", "IE", "+353"));
//		countries.add(new Country("Großbritannien", "GB", "+44"));
//		countries.add(new Country("Österreich", "AT", "+43"));
//		countries.add(new Country("Polen", "PL", "+48"));
//		countries.add(new Country("Rumänien", "RO", "+40"));
//		countries.add(new Country("Tschechien", "CZ", "+420"));
		
		
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
//		invoiceColumns.add(COLUMN_AMOUNT);
		invoiceColumns.add(COLUMN_DESCRIPTION);
		invoiceColumns.add(COLUMN_CARD_NR);
		invoiceColumns.add(COLUMN_TEL_NR);
		invoiceColumns.add(COLUMN_INST_PLZ);
		invoiceColumns.add(COLUMN_EINSATZORT);
		invoiceColumns.add(COLUMN_INST_STREET);
		invoiceColumns.add(COLUMN_PLANT_NUMBER);
		invoiceColumns.add(COLUMN_EQUIP_NR);
		invoiceColumns.add(COLUMN_AUFTRAGS_NR);
		invoiceColumns.add(COLUMN_BESTELL_NR);
		invoiceColumns.add(COLUMN_VERTRAG_NR);
		invoiceColumns.add(COLUMN_BA_NR);
		invoiceColumns.add(COLUMN_WE_NR);
		invoiceColumns.add(COLUMN_COST_CENTER);
//		invoiceColumns.add(COLUMN_SINGLE_PRICE);
		
		columnSize = new HashMap<String, Float>();
		columnSize.put(COLUMN_POS, 1.8f);
		columnSize.put(COLUMN_AMOUNT, 2.5f);
		columnSize.put(COLUMN_DESCRIPTION, 12.4f);
		columnSize.put(COLUMN_CARD_NR, 4.9f);
		columnSize.put(COLUMN_TEL_NR, 5.9f);
		columnSize.put(COLUMN_INST_PLZ, 1.5f);
		columnSize.put(COLUMN_EINSATZORT, 5f);
		columnSize.put(COLUMN_INST_STREET, 8f);
		columnSize.put(COLUMN_PLANT_NUMBER, 5f);
		columnSize.put(COLUMN_EQUIP_NR, 5f);
		columnSize.put(COLUMN_AUFTRAGS_NR, 4f);
		columnSize.put(COLUMN_BESTELL_NR, 6f);
		columnSize.put(COLUMN_VERTRAG_NR, 5.3f);
		columnSize.put(COLUMN_BA_NR, 4f);
		columnSize.put(COLUMN_WE_NR, 4f);
		columnSize.put(COLUMN_COST_CENTER, 4f);
//		columnSize.put(COLUMN_SINGLE_PRICE, 4f);
		columnSize.put(COLUMN_TOTAL_PRICE, 3.3f);

		billingCriteria = new ArrayList<String>();
		billingCriteria.add(BILLING_WE_NR);
		billingCriteria.add(BILLING_BESTELL_NR);
		
		relationList = new ArrayList<String>();
		relationList.add(RELATION_0);
		relationList.add(RELATION_1);
		relationList.add(RELATION_2);
		
		paymentMethods = new ArrayList<String>();
		paymentMethods.add(paymentInvoice);
		paymentMethods.add(paymentDebit);
		
		invoiceRowList = new ArrayList<Integer>();
		invoiceRowList.add(1);
		invoiceRowList.add(2);
		invoiceRowList.add(3);
		invoiceRowList.add(4);
		invoiceRowList.add(5);
		
		months = new HashMap<Integer, String>();
		months.put(0, JAN);
		months.put(1, FEB);
		months.put(2, MAR);
		months.put(3, APR);
		months.put(4, MAY);
		months.put(5, JUN);
		months.put(6, JUL);
		months.put(7, AUG);
		months.put(8, SEP);
		months.put(9, OCT);
		months.put(10, NOV);
		months.put(11, DEC);
		
		years = addYears();
		
		paymentModaltyList = new ArrayList<String>();
		paymentModaltyList.add(PAYMENT_MODALTY_MONTHLY);
		paymentModaltyList.add(PAYMENT_MODALTY_QUARTERLY);
		paymentModaltyList.add(PAYMENT_MODALTY_HALFYEARLY);
		paymentModaltyList.add(PAYMENT_MODALTY_YEARLY);
		paymentModaltyList.add(PAYMENT_MODALTY_DIRECT_DEBIT);
		
		formats = new HashMap<Integer, String>();
		formats.put(0, CSV);
		formats.put(1, EXC);
		
		String hostname = "";
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
			LOGGER.info("Hostname = " + hostname);
		} catch (UnknownHostException e) {
			LOGGER.error("UnknownHostnameException: " + e);
		}
		
		if (hostname.equals("accounting")) {
			pdfPath = "E:/tmp/Invoices/pdf/";
			zipPath = "E:/tmp/Invoices/zip/";
			exportPath = "E:/tmp/Invoices/export/";
		} else {
			pdfPath = "C:/Siwaltec/Invoices/pdf/";
			zipPath = "C:/Siwaltec/Invoices/zip/";
			exportPath = "C:/Siwaltec/Invoices/export/";
		}

	}

	private List<Integer> addYears() {
		ArrayList<Integer> years = new ArrayList<Integer>();
		int year = 2013;
		Calendar d = Calendar.getInstance();
		
		while (year <= d.get(Calendar.YEAR)) {
			years.add(year);
			year++;
		}
		return years;
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

	public Map<Integer, String> getSortingOptions() {
		if (sortingOptions == null || (sortingOptions != null && sortingOptions.size() == 0)) {
			ConfigurationController c = new ConfigurationController();
			sortingOptions = c.getSortingOptionsFromDB();
		}
		return sortingOptions;
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

	public List<String> getSupplierList() {
		return supplierList;
	}

	public void setSupplierList(List<String> supplierList) {
		this.supplierList = supplierList;
	}

	public String getCardDeM2m() {
		return cardDeM2m;
	}

	public String getCardDeHbn() {
		return cardDeHbn;
	}

	public String getCardDeTc() {
		return cardDeTc;
	}

	public String getCardDeM2mCmt() {
		return cardDeM2mCmt;
	}

	public String getCardDeHbnCmt() {
		return cardDeHbnCmt;
	}

	public String getCardDeTcCmt() {
		return cardDeTcCmt;
	}


	public void setCardDeM2m(String cardDeM2m) {
		this.cardDeM2m = cardDeM2m;
	}

	public void setCardDeHbn(String cardDeHbn) {
		this.cardDeHbn = cardDeHbn;
	}

	public void setCardDeTc(String cardDeTc) {
		this.cardDeTc = cardDeTc;
	}


	public void setCardDeM2mCmt(String cardDeM2mCmt) {
		this.cardDeM2mCmt = cardDeM2mCmt;
	}

	public void setCardDeHbnCmt(String cardDeHbnCmt) {
		this.cardDeHbnCmt = cardDeHbnCmt;
	}

	public void setCardDeTcCmt(String cardDeTcCmt) {
		this.cardDeTcCmt = cardDeTcCmt;
	}

	public String getCardDeEmptyCmt() {
		return cardDeEmptyCmt;
	}

	public void setCardDeEmptyCmt(String cardDeEmptyCmt) {
		this.cardDeEmptyCmt = cardDeEmptyCmt;
	}

	public String getCardAut1() {
		return cardAut1;
	}

	public String getCardAut2() {
		return cardAut2;
	}

	public String getCardAut3() {
		return cardAut3;
	}

	public String getCardAut4() {
		return cardAut4;
	}

	public String getCardAut5() {
		return cardAut5;
	}

	public String getCardAut6() {
		return cardAut6;
	}

	public String getCardAut7() {
		return cardAut7;
	}

	public String getCardAut8() {
		return cardAut8;
	}

	public String getCardAut9() {
		return cardAut9;
	}

	public String getCardAutAA1() {
		return cardAutAA1;
	}

	public String getCardAutAA2() {
		return cardAutAA2;
	}

	public List<String> getRelationList() {
		return relationList;
	}

	public void setRelationList(List<String> relationList) {
		this.relationList = relationList;
	}

	public List<String> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<String> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getPaymentInvoice() {
		return paymentInvoice;
	}

	public void setPaymentInvoice(String paymentInvoice) {
		this.paymentInvoice = paymentInvoice;
	}

	public String getPaymentDebit() {
		return paymentDebit;
	}

	public void setPaymentDebit(String paymentDebit) {
		this.paymentDebit = paymentDebit;
	}

	public List<Integer> getInvoiceRowList() {
		return invoiceRowList;
	}

	public void setInvoiceRowList(List<Integer> invoiceRowList) {
		this.invoiceRowList = invoiceRowList;
	}

	public List<String> getBillingCriteria() {
		return billingCriteria;
	}

	public void setBillingCriteria(List<String> billingCriteria) {
		this.billingCriteria = billingCriteria;
	}

	public List<Integer> getYears() {
		return years;
	}

	public void setYears(List<Integer> years) {
		this.years = years;
	}

	public HashMap<Integer, String> getMonths() {
		return months;
	}

	public void setMonths(HashMap<Integer, String> months) {
		this.months = months;
	}

	public String getPdfPath() {
		return pdfPath;
	}

	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
	}

	public String getZipPath() {
		return zipPath;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public String getCardDeM2MOtis() {
		return cardDeM2MOtis;
	}

	public void setCardDeM2MOtis(String cardDeM2MOtis) {
		this.cardDeM2MOtis = cardDeM2MOtis;
	}

	public String getCardDeM2MOtisCmt() {
		return cardDeM2MOtisCmt;
	}

	public void setCardDeM2MOtisCmt(String cardDeM2MOtisCmt) {
		this.cardDeM2MOtisCmt = cardDeM2MOtisCmt;
	}

	public HashMap<Integer, String> getFormats() {
		return formats;
	}

	public void setFormats(HashMap<Integer, String> formats) {
		this.formats = formats;
	}

	public String getExportPath() {
		return exportPath;
	}

	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public List<String> getPaymentModaltyList() {
		return paymentModaltyList;
	}

	public void setPaymentModaltyList(List<String> paymentModaltyList) {
		this.paymentModaltyList = paymentModaltyList;
	}

}