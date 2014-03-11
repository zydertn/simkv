package de.abd.mda.persistence.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;

import de.abd.mda.util.CustomerComparator;
import de.abd.mda.util.VoucherComparator;

public class Customer extends DaoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -711328056445907190L;
	private int id;
	private String name;
	private String branch;
	private String fao;
	private String supplierNumber;
	
	private Address address;
	
	private Address invoiceAddress;

	private Person contactPerson;
	private String customernumber;
	private String paymentMethod;
	private String comment;
	private Date lastCalculationDate;
	private Set<Voucher> vouchers;
	private Map<String, Bill> bills;
	
	private InvoiceConfiguration invoiceConfiguration;

	public Customer() {
		this.name = "";
		this.branch = "";
		this.supplierNumber = "";
		// CASCADE: Evtl. hier drei Zeilen und die letzte Zeile auskommentieren
		this.address = new Address();
		this.invoiceAddress = new Address();
		this.contactPerson = new Person();
		this.customernumber = "";
		this.comment = "";
		this.invoiceConfiguration = new InvoiceConfiguration();
		paymentMethod = "";
	}
	
	public String getListString() {
//		String s = name + " - " + branch + "; " + address.getAddressString();
		String st = new String(name + " - " + branch + "; " + address.getAddressString());
		if (st.length() > 92) {
			st = st.substring(0, 91);
		}
		st += "; " + customernumber;
		st = st.replaceAll("&", " ");
		return st;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Person getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(Person contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getCustomernumber() {
		return customernumber;
	}

	public void setCustomernumber(String customernumber) {
		this.customernumber = customernumber;
	}

	public Address getInvoiceAddress() {
		return invoiceAddress;
	}

	public void setInvoiceAddress(Address invoiceAddress) {
		this.invoiceAddress = invoiceAddress;
	}

	public InvoiceConfiguration getInvoiceConfiguration() {
		return invoiceConfiguration;
	}

	public void setInvoiceConfiguration(InvoiceConfiguration invoiceConfiguration) {
		this.invoiceConfiguration = invoiceConfiguration;
	}

	public String getFao() {
		return fao;
	}

	public void setFao(String fao) {
		this.fao = fao;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getSupplierNumber() {
		return supplierNumber;
	}

	public void setSupplierNumber(String supplierNumber) {
		this.supplierNumber = supplierNumber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getLastCalculationDate() {
		return lastCalculationDate;
	}

	public void setLastCalculationDate(Date lastCalculationDate) {
		this.lastCalculationDate = lastCalculationDate;
	}

	public ArrayList<Voucher> getVouchersSorted() {
		if (vouchers != null) {
			ArrayList<Voucher> vl = new ArrayList<Voucher>(vouchers);
			Comparator<Voucher> comparator = new VoucherComparator();
			Collections.sort(vl, comparator);
			return vl;
		}
		return null;
	}
	
	public Set<Voucher> getVouchers() {
		return vouchers;
	}

	public void setVouchers(Set<Voucher> vouchers) {
		this.vouchers = vouchers;
	}

	public Map<String, Bill> getBills() {
		return bills;
	}

	public void setBills(Map<String, Bill> bills) {
		this.bills = bills;
	}
	
}