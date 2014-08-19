package de.abd.mda.persistence.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.abd.mda.model.Model;


public class CardBean extends DaoObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2744842668281743491L;
//	private int id;
	private String cardNumberFirst;
	private String cardNumberSecond;
	private String countryCode;
	private String country;
	private String phoneNrFirst;
	private String phoneNrSecond;
	private int sequenceNumber;
	private String supplier;
	private int invoiceRows;
	
	private String status;
	private Date activationDate;
	private Date deactivationDate;

	private String customerOrderNumber;
//	private String orderNumber;
	private Date deliverySlipDate;
	private String deliverySlipNumber;
	private Person contactPerson;
	private Customer customer;
	private Address installAddress;
	private String factoryNumber;
	private String vpnProfile;
	private String comment;
//	private Date lastCalculationDate;
	private int lastCalculationYear;
	private int lastCalculationMonth;
	private String project;
	private Boolean standardPrice;
	private Boolean flatrateCard;
	private int simPrice;
	private String relation;	
	
	private String cardDeType;
	private String cardAutType;
	private String cardAutActivatedAs;
	
	private String anlagenNr;
	private String equipmentNr;
	private String vertrag;
	private String soNr;
	private String leitstand;
	private String lokation;
	private String we;
	private String sachkonto;
	private String auftragsNr;
	private String notrufNr;
	private String kostenstelle;
	private String einsatzort;
	private String bestellNummer;
	private boolean typeDeDisabled;
	private boolean typeAutDisabled;
	private boolean actAsDisabled;
	private String pin;
	private String baNummer;
	
	private Map<Integer, Double> simPriceMap;
	
//	private List comments;
	
//	public Date getLastCalculationDate() {
//		return lastCalculationDate;
//	}
//
//	public void setLastCalculationDate(Date lastCalculationDate) {
//		this.lastCalculationDate = lastCalculationDate;
//	}

	public CardBean() {	
		this.cardNumberFirst = "";
		this.cardNumberSecond = "";
		this.countryCode = "";
		this.country = "";
		this.phoneNrFirst = "";
		this.phoneNrSecond = "";
		this.status = "Inaktiv";
		this.customerOrderNumber = "";
		this.invoiceRows = 1;
//		this.orderNumber = "";
		this.deliverySlipDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
		df.format(deliverySlipDate);
		// CASCADE: Evtl. hier drei Zeilen auskommentieren
		this.contactPerson = new Person();
		this.customer = new Customer();
		this.installAddress = new Address();
		this.factoryNumber = "";
		this.vpnProfile = "";
		this.comment = "";
		this.project = "";
		this.standardPrice = true;
		this.flatrateCard = false;
		this.simPrice = 0;
		this.anlagenNr = "";
		this.equipmentNr = "";
		this.vertrag = "";
		this.soNr = "";
		this.leitstand = "";
		this.lokation = "";
		this.we = "";
		this.sachkonto = "";
		this.auftragsNr = "";
		this.notrufNr = "";
		this.kostenstelle = "";
		this.einsatzort = "";
		this.baNummer = "";
		this.lastCalculationYear = 1999;
		this.lastCalculationMonth = 0;
	}
	
	public CardBean(String cNFirst, String cNSecond, String countryCode, String country, String phoneFirst, String phoneSecond, int seqNum) {
		this.cardNumberFirst = cNFirst;
		this.cardNumberSecond = cNSecond;
		this.countryCode = countryCode;
		this.country = country;
		this.phoneNrFirst = phoneFirst;
		this.phoneNrSecond = phoneSecond;
		this.sequenceNumber = seqNum;
		this.status = "Inaktiv";
		this.customerOrderNumber = "";
//		this.orderNumber = "";
		this.deliverySlipDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
		df.format(deliverySlipDate);
		this.contactPerson = new Person();
		this.customer = new Customer();
		this.installAddress = new Address();
		this.factoryNumber = "";
		this.vpnProfile = "";
		this.comment = "";
		this.project = "";
		this.standardPrice = true;
		this.flatrateCard = false;
		this.simPrice = 0;
		this.anlagenNr = "";
		this.equipmentNr = "";
		this.vertrag = "";
		this.soNr = "";
		this.leitstand = "";
		this.lokation = "";
		this.we = "";
		this.sachkonto = "";
		this.auftragsNr = "";
		this.notrufNr = "";
		this.kostenstelle = "";
		this.einsatzort = "";
		this.baNummer = "";
		this.lastCalculationYear = 1999;
		this.lastCalculationMonth = 0;
	}

	public String getCardNumber() {
		if (cardNumberFirst != null && cardNumberFirst.length() > 0 && cardNumberSecond != null && cardNumberSecond.length() > 0)
			return cardNumberFirst + "-" + cardNumberSecond;
		return "";
	}
	
	public String getCardNumberFirst() {
		return cardNumberFirst;
	}

	public void setCardNumberFirst(String cardNumberFirst) {
		this.cardNumberFirst = cardNumberFirst;
	}

	public String getCardNumberSecond() {
		return cardNumberSecond;
	}

	public void setCardNumberSecond(String cardNumberSecond) {
		this.cardNumberSecond = cardNumberSecond;
	}

	public String getPhoneNr() {
		if (phoneNrFirst != null && phoneNrFirst.length() > 0 && phoneNrSecond != null && phoneNrSecond.length() > 0)
			return "0" + phoneNrFirst + phoneNrSecond;
		else return "";
	}

	public String getPhoneNrInclMinus() {
		if (phoneNrFirst != null && phoneNrFirst.length() > 0 && phoneNrSecond != null && phoneNrSecond.length() > 0)
			return phoneNrFirst + "-" + phoneNrSecond;
		else return "";
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}
	
	public String getPhoneNrFirst() {
		return phoneNrFirst;
	}

	public void setPhoneNrFirst(String phoneNrFirst) {
		this.phoneNrFirst = phoneNrFirst;
	}

	public String getPhoneNrSecond() {
		return phoneNrSecond;
	}

	public void setPhoneNrSecond(String phoneNrSecond) {
		this.phoneNrSecond = phoneNrSecond;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getPhoneString() {
		String s = "";
		if (!phoneNrFirst.startsWith("0"))
			s += "0";
		return s + phoneNrFirst + "" + phoneNrSecond;
	}
	
	public String getPhoneStringInvoice() {
		return phoneNrFirst + " - " + enhancePhoneSecond(phoneNrSecond);
	}
	
	private String enhancePhoneSecond(String phoneNrSecond) {
		String pNS = "";
		int len = phoneNrSecond.length();
		for (int i = 0; i < len; i++) {
			pNS += phoneNrSecond.charAt(i++);
			if (i <= len) {
				pNS += phoneNrSecond.charAt(i) + " ";
			}
		}
		return pNS;
	}
	
	public String getCardnumberString() {
		return cardNumberFirst + "-" + cardNumberSecond;
	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person person) {
		this.contactPerson = person;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getActivationDate() {
		return activationDate;
	}
	
	public Date getDeOrActivationDate() {
		if (status.equalsIgnoreCase(Model.STATUS_ACTIVE))
			return activationDate;
		if (status.equalsIgnoreCase(Model.STATUS_INACTIVE))
			return deactivationDate;
		return null;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}
	
	public String getCustomerOrderNumber() {
		return customerOrderNumber;
	}

	public void setCustomerOrderNumber(String customerOrderNumber) {
		this.customerOrderNumber = customerOrderNumber;
	}

//	public String getOrderNumber() {
//		return orderNumber;
//	}
//
//	public void setOrderNumber(String orderNumber) {
//		this.orderNumber = orderNumber;
//	}

	public String getDeliverySlipNumber() {
		return deliverySlipNumber;
	}

	public void setDeliverySlipNumber(String deliverySlipNumber) {
		this.deliverySlipNumber = deliverySlipNumber;
	}

	public String getDeliverySlipDateString() {
		if (deliverySlipDate != null) {
			SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
			return "" + sd.format(deliverySlipDate);
		} else
			return "";
	}
	
	public Date getDeliverySlipDate() {
		return deliverySlipDate;
	}

	public void setDeliverySlipDate(Date deliverySlipDate) {
		this.deliverySlipDate = deliverySlipDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Address getInstallAddress() {
		return installAddress;
	}

	public void setInstallAddress(Address installAddress) {
		this.installAddress = installAddress;
	}

	public String getFactoryNumber() {
		return factoryNumber;
	}

	public void setFactoryNumber(String factoryNumber) {
		this.factoryNumber = factoryNumber;
	}

	public String getVpnProfile() {
		return vpnProfile;
	}

	public void setVpnProfile(String vpnProfile) {
		this.vpnProfile = vpnProfile;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getCountry() {
		return country;
	}

	public void setCountryDates(Country country) {
		if (country != null) {
			setCountry(country.getShortName());
			setCountryCode(country.getInternationalAreaCode());
		}
	}

	private void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	private void setCountry(String country) {
		this.country = country;
	}

	public Boolean getStandardPrice() {
		return standardPrice;
	}

	public void setStandardPrice(Boolean standardPrice) {
		this.standardPrice = standardPrice;
	}

	public int getSimPrice() {
		return simPrice;
	}

	public String getSimPriceDouble() {
		Double d = 0.0;
		
		// Einmalige Initialisierung der lokalen SimPriceMap
		if (simPriceMap == null) {
			Model model = new Model();
			model.createModel();
			simPriceMap = model.getSimPrices();
		}
		if (simPrice != 0) {
			d = simPriceMap.get(simPrice);
		} else {
			if (getCustomer() != null && getCustomer().getInvoiceConfiguration() != null) {
				d = simPriceMap.get(getCustomer().getInvoiceConfiguration().getSimPrice());
			}
		}
		String s = getEuroFromDouble(d);
		return s;
	}
	
	private String getEuroFromDouble(Double d) {
		// Euro-Zeichen wird aus unbekannten Gründen in Tabelle bereits eingefügt; 
		// Evtl. wird anhand des Formats erkannt, dass es ein Preis ist.
		String s = "" + d;
		if (s.indexOf(".") == (s.length()-2))
			s = s + "0";
		return s;
	}
	
	public void setSimPrice(int simPrice) {
		this.simPrice = simPrice;
	}

	public Date getDeactivationDate() {
		return deactivationDate;
	}

	public void setDeactivationDate(Date deactivationDate) {
		this.deactivationDate = deactivationDate;
	}

	public String getAnlagenNr() {
		return anlagenNr;
	}

	public void setAnlagenNr(String anlagenNr) {
		this.anlagenNr = anlagenNr;
	}

	public String getEquipmentNr() {
		return equipmentNr;
	}

	public void setEquipmentNr(String equipmentNr) {
		this.equipmentNr = equipmentNr;
	}

	public String getVertrag() {
		return vertrag;
	}

	public void setVertrag(String vertrag) {
		this.vertrag = vertrag;
	}

	public String getSoNr() {
		return soNr;
	}

	public void setSoNr(String soNr) {
		this.soNr = soNr;
	}

	public String getLeitstand() {
		return leitstand;
	}

	public void setLeitstand(String leitstand) {
		this.leitstand = leitstand;
	}

	public String getLokation() {
		return lokation;
	}

	public void setLokation(String lokation) {
		this.lokation = lokation;
	}

	public String getWe() {
		return we;
	}

	public void setWe(String we) {
		this.we = we;
	}

	public String getSachkonto() {
		return sachkonto;
	}

	public void setSachkonto(String sachkonto) {
		this.sachkonto = sachkonto;
	}

	public String getAuftragsNr() {
		return auftragsNr;
	}

	public void setAuftragsNr(String auftragsNr) {
		this.auftragsNr = auftragsNr;
	}

	public String getNotrufNr() {
		return notrufNr;
	}

	public void setNotrufNr(String notrufNr) {
		this.notrufNr = notrufNr;
	}

	public String getKostenstelle() {
		return kostenstelle;
	}

	public void setKostenstelle(String kostenstelle) {
		this.kostenstelle = kostenstelle;
	}

	public String getEinsatzort() {
		return einsatzort;
	}

	public void setEinsatzort(String einsatzort) {
		this.einsatzort = einsatzort;
	}

	public String getBestellNummer() {
		return bestellNummer;
	}

	public void setBestellNummer(String bestellNummer) {
		this.bestellNummer = bestellNummer;
	}

	public String getCardDeType() {
		return cardDeType;
	}

	public void setCardDeType(String cardDeType) {
		this.cardDeType = cardDeType;
	}

	public String getCardAutType() {
		return cardAutType;
	}

	public void setCardAutType(String cardAutType) {
		this.cardAutType = cardAutType;
	}

	public String getCardAutActivatedAs() {
		return cardAutActivatedAs;
	}

	public void setCardAutActivatedAs(String cardAutActivatedAs) {
		this.cardAutActivatedAs = cardAutActivatedAs;
	}

	public boolean isTypeDeDisabled() {
		if (this.getSupplier() == null)
			return false;
		if (this.getSupplier().equals(Model.SUPPLIER_TELEKOM))
			return false;
		else
			return true;
	}

	public void setTypeDeDisabled(boolean typeDeDisabled) {
		this.typeDeDisabled = typeDeDisabled;
	}

	public boolean isTypeAutDisabled() {
		if (this.getSupplier() != null && this.getSupplier().equals(Model.SUPPLIER_TELEKOM_AUSTRIA))
			return false;
		else
			return true;
	}

	public void setTypeAutDisabled(boolean typeAutDisabled) {
		this.typeAutDisabled = typeAutDisabled;
	}

	public boolean isActAsDisabled() {
		if (this.cardAutActivatedAs != null && this.cardAutActivatedAs.length() > 0)
			return false;
		return true;
	}

	public void setActAsDisabled(boolean actAsDisabled) {
		this.actAsDisabled = actAsDisabled;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public int getInvoiceRows() {
		return invoiceRows;
	}

	public void setInvoiceRows(int invoiceRows) {
		this.invoiceRows = invoiceRows;
	}

	public int getLastCalculationYear() {
		return lastCalculationYear;
	}

	public void setLastCalculationYear(int lastCalculationYear) {
		this.lastCalculationYear = lastCalculationYear;
	}

	public Boolean getFlatrateCard() {
		return flatrateCard;
	}

	public void setFlatrateCard(Boolean flatrateCard) {
		this.flatrateCard = flatrateCard;
	}

	public String getBaNummer() {
		return baNummer;
	}

	public void setBaNummer(String baNummer) {
		this.baNummer = baNummer;
	}

	public int getLastCalculationMonth() {
		return lastCalculationMonth;
	}

	public void setLastCalculationMonth(int lastCalculationMonth) {
		this.lastCalculationMonth = lastCalculationMonth;
	}

//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}
//
//	public List getComments() {
//		return comments;
//	}
//
//	public void setComments(List comments) {
//		this.comments = comments;
//	}

	


}