package de.abd.mda.persistence.dao;

public class Customer extends DaoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -711328056445907190L;
	private int id;
	private String name;
	private String branch;
	private Address address;
	private Address invoiceAddress;
	private Person contactPerson;
	private String customernumber;
	private InvoiceConfiguration invoiceConfiguration;

	public Customer() {
		this.name = "";
		this.branch = "";
		this.address = new Address();
		this.invoiceAddress = new Address();
		this.contactPerson = new Person();
		this.customernumber = "";
		this.invoiceConfiguration = new InvoiceConfiguration();
	}
	
	public String getListString() {
		return name + " - " + branch + "; " + address.getAddressString() + "; " + customernumber;
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
	
}