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
	
	public static String SUPPLIER_TELEKOM = "Deutschland";
	public static String SUPPLIER_TELEKOM_AUSTRIA = "Austria";
	
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
	public static String COLUMN_INST_PLZ = "PLZ";
	public static String COLUMN_INST_CITY = "Einsatzort";
	public static String COLUMN_INST_STREET = "Straße";
	public static String COLUMN_EQUIP_NR = "Equipment Nr.";
	public static String COLUMN_ORDER_NR = "Order Nr.";
	public static String COLUMN_CARD_NR = "Kartennummer";
	public static String COLUMN_TEL_NR = "Rufnummer";
//	public static String COLUMN_SINGLE_PRICE = "Preis";
	public static String COLUMN_TOTAL_PRICE = "Preis";
	
	
	public static String GENDER_MAN = "Herr";
	public static String GENDER_WOMAN = "Frau";
	public static String GENDER_COMPANY = "Firma";

	public List<String> relationList;
	public static String RELATION_0 = "";
	public static String RELATION_1 = "Relation1";
	public static String RELATION_2 = "Relation2";
	
	public String cardDeAv = "Alt-Voice";
	public String cardDeAvIp = "Alt-Voice-IP";
	public String cardDeM2m = "M2M-Voice";
	public String cardDeHbn = "HBN-Data";
	public String cardDeTc = "Testkarte";
	public String cardDeSc = "Sonderkarte";
	public String cardDeRc = "Roamingkarte";
	
	public String cardDeEmptyCmt = "";
	public String cardDeAvCmt = "Unsere Karten für normalen Einsatz in D, ohne fixe IP";
	public String cardDeAvIpCmt = "Unsere Karten für normalen Einsatz in D, mit fixer IP für Monitoring";
	public String cardDeM2mCmt = "??? Frau Kiesenbauer, hier schauen wir noch ein mal";
	public String cardDeHbnCmt = "Unsere neue deutsche Karte für das HBN Modul mit fixer IP und ohne Sprachanteil";
	public String cardDeTcCmt = "Bei Einsatz als Testkarte, zB wird nicht abgerechnet";
	public String cardDeScCmt = "für speziellen Einsatz, z.B. als Flatratekarte";
	public String cardDeRcCmt = "deutsche Karte im Ausland im Einsatz , z.B. Hollaus Karte";

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
	
	public Map<Integer, Double> simPrices;
	public Map<Integer, Double> dataOptionSurcharges;
	
	private HashMap<String, Float> columnSize;
	private List<String> invoiceFormats;
	private List<String> invoiceCreationFrequencies;
	private List<String> invoiceColumns;
	private List<String> paymentMethods;
	
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
//		invoiceColumns.add(COLUMN_AMOUNT);
		invoiceColumns.add(COLUMN_DESCRIPTION);
		invoiceColumns.add(COLUMN_CARD_NR);
		invoiceColumns.add(COLUMN_TEL_NR);
		invoiceColumns.add(COLUMN_INST_PLZ);
		invoiceColumns.add(COLUMN_INST_CITY);
		invoiceColumns.add(COLUMN_INST_STREET);
		invoiceColumns.add(COLUMN_PLANT_NUMBER);
		invoiceColumns.add(COLUMN_EQUIP_NR);
		invoiceColumns.add(COLUMN_ORDER_NR);
//		invoiceColumns.add(COLUMN_SINGLE_PRICE);
		
		columnSize = new HashMap<String, Float>();
		columnSize.put(COLUMN_POS, 1.5f);
		columnSize.put(COLUMN_AMOUNT, 2.3f);
		columnSize.put(COLUMN_DESCRIPTION, 12.5f);
		columnSize.put(COLUMN_CARD_NR, 5f);
		columnSize.put(COLUMN_TEL_NR, 6f);
		columnSize.put(COLUMN_INST_PLZ, 1.5f);
		columnSize.put(COLUMN_INST_CITY, 4f);
		columnSize.put(COLUMN_INST_STREET, 8f);
		columnSize.put(COLUMN_PLANT_NUMBER, 3.5f);
		columnSize.put(COLUMN_EQUIP_NR, 5f);
		columnSize.put(COLUMN_ORDER_NR, 4f);
//		columnSize.put(COLUMN_SINGLE_PRICE, 4f);
		columnSize.put(COLUMN_TOTAL_PRICE, 4f);
		
		relationList = new ArrayList<String>();
		relationList.add(RELATION_0);
		relationList.add(RELATION_1);
		relationList.add(RELATION_2);
		
		paymentMethods = new ArrayList<String>();
		paymentMethods.add(paymentInvoice);
		paymentMethods.add(paymentDebit);
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

	public List<String> getSupplierList() {
		return supplierList;
	}

	public void setSupplierList(List<String> supplierList) {
		this.supplierList = supplierList;
	}

	public String getCardDeAv() {
		return cardDeAv;
	}

	public String getCardDeAvIp() {
		return cardDeAvIp;
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

	public String getCardDeSc() {
		return cardDeSc;
	}

	public String getCardDeRc() {
		return cardDeRc;
	}

	public String getCardDeAvCmt() {
		return cardDeAvCmt;
	}

	public String getCardDeAvIpCmt() {
		return cardDeAvIpCmt;
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

	public String getCardDeScCmt() {
		return cardDeScCmt;
	}

	public String getCardDeRcCmt() {
		return cardDeRcCmt;
	}

	public void setCardDeAv(String cardDeAv) {
		this.cardDeAv = cardDeAv;
	}

	public void setCardDeAvCmt(String cardDeAvCmt) {
		this.cardDeAvCmt = cardDeAvCmt;
	}

	public void setCardDeAvIp(String cardDeAvIp) {
		this.cardDeAvIp = cardDeAvIp;
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

	public void setCardDeSc(String cardDeSc) {
		this.cardDeSc = cardDeSc;
	}

	public void setCardDeRc(String cardDeRc) {
		this.cardDeRc = cardDeRc;
	}

	public void setCardDeAvIpCmt(String cardDeAvIpCmt) {
		this.cardDeAvIpCmt = cardDeAvIpCmt;
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

	public void setCardDeScCmt(String cardDeScCmt) {
		this.cardDeScCmt = cardDeScCmt;
	}

	public void setCardDeRcCmt(String cardDeRcCmt) {
		this.cardDeRcCmt = cardDeRcCmt;
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

}