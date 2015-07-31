package de.abd.mda.controller;


import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;




import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.InvoiceConfiguration;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.Voucher;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CountryController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.CustomerReportGenerator;
import de.abd.mda.util.CustomerNumberComparator;
import de.abd.mda.util.FacesUtil;
import de.abd.mda.util.HibernateUtil;

public class CustomerActionController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(CustomerActionController.class .getName()); 

	private Customer customer;
	private boolean opened = false;
	private List<Customer> customerList;
	private String selectedCustomer;
	private boolean componentDisabled = true;
	private HtmlInputText branchBinding;
	private HtmlInputText faoBinding;
	private HtmlInputText supplierNumberBinding;
	private HtmlInputText streetBinding;
	private HtmlInputText housenumberBinding;
	private HtmlInputText postboxBinding;
	private HtmlInputText postcodeBinding;
	private HtmlInputText cityBinding;
	private HtmlSelectOneMenu genderBinding;
	private HtmlSelectOneMenu contactGenderBinding;
	private HtmlInputText contactFirstnameBinding;
	private HtmlInputText contactNameBinding;
	private HtmlInputText invoiceStreetBinding;
	private HtmlInputText invoiceHousenumberBinding;
	private HtmlInputText invoicePostboxBinding;
	private HtmlInputText invoicePostcodeBinding;
	private HtmlInputText invoiceCityBinding;
	private HtmlSelectOneMenu invoiceconfigSimpriceBinding;
	private HtmlSelectOneMenu invoiceconfigFormatBinding;
	private HtmlSelectOneMenu invoiceconfigCreationFrequencyBinding;
	private HtmlInputText emailBinding;
	private HtmlInputText de_mailBinding;
	private HtmlInputText commentBinding;
	private HtmlSelectManyCheckbox invoiceconfigColumnsBinding;
	private HtmlSelectBooleanCheckbox invoiceconfigSeparateBillingBinding;
	private HtmlSelectManyCheckbox invoiceconfigBillingCriteriaBinding;
	private Voucher voucher;
	private HtmlSelectOneMenu voucherMonthBinding;
	private HtmlSelectOneMenu voucherYearBinding;
	private HtmlInputText cardAmountBinding;
	private HtmlInputText cardVoucherBinding;
	private HtmlInputText vatNumberBinding;
	private HtmlSelectOneMenu selectCountryBinding;
	private HtmlSelectOneMenu invoiceconfigSortingBinding;
	private String countryName;
	private HtmlInputText paymentTargetBinding;
	private List<Bill> bills;
	private String chosenPaymentModalty;
	private List<Customer> modaltyCustomers;
	
	private String relation;
	private List<CardBean> cardList;
//	private HtmlSelectManyCheckbox invoiceconfigColumnsBinding;
	
	public CustomerActionController() {
		LOGGER.info("Instantiate: CustomerActionController");
		customer = new Customer();
		customer.setAddress(new Address());
		customer.setContactPerson(new Person());
		customer.setInvoiceAddress(new Address());
		customer.setInvoiceConfiguration(new InvoiceConfiguration());
		customerList = new ArrayList<Customer>();
		cardList = new ArrayList<CardBean>();
		voucher = new Voucher();
	}
	
	public void createCustomer() {
		LOGGER.info("Method: createCustomer");
		
		CustomerController customerController = new CustomerController();
		CountryController countryController = new CountryController();
		Country country = countryController.findCountry(countryName);
		customer.setCountry(country);
		
		String retMessage = customerController.createObject(customer);

		if (!(retMessage != null && retMessage.length() > 0)) {
			retMessage = "Neuer Kunde wurde erfolgreich angelegt!" + customer.getCustomernumber();
			LOGGER.info(retMessage);
		} else {
			LOGGER.warn(retMessage);
		}
		
		getRequest().setAttribute("message", retMessage);
		getSession().setAttribute("mycustomer", customer);
		getSession().setAttribute("refreshCustomerList", true);
	}

	private void createCustomerSubObjects(Customer cus) {
		LOGGER.info("Method: createCustomerSubObjects; Kunde = " + cus.getCustomernumber());
		CustomerController customerController = new CustomerController();
		if (customer.getAddress() != null && customer.getAddress().getCity() != null && customer.getAddress().getCity().length() > 0) {
			Address a = (Address) customerController.createMyObject(customer.getAddress());
			cus.setAddress(a);
		}
		
		if (customer.getInvoiceAddress() != null && customer.getInvoiceAddress().getCity() != null && customer.getInvoiceAddress().getCity().length() > 0) {
			Address ia = (Address) customerController.createMyObject(customer.getInvoiceAddress());
			cus.setInvoiceAddress(ia);
		}
		
		if (customer.getContactPerson() != null && customer.getContactPerson().getAddress() != null && customer.getContactPerson().getAddress().getCity() != null && customer.getContactPerson().getAddress().getCity().length() > 0) {
			Address cpa = (Address) customerController.createMyObject(customer.getContactPerson().getAddress());
			cus.getContactPerson().setAddress(cpa);
		}
		
		if (customer.getInvoiceConfiguration() != null) {
			InvoiceConfiguration ic = (InvoiceConfiguration) customerController.createMyObject(customer.getInvoiceConfiguration());
			cus.setInvoiceConfiguration(ic);
		}
	}
	
	public void searchCustomer() {
		LOGGER.info("Method: searchCustomer");
		CustomerController cc = new CustomerController();
		List<DaoObject> customers =	cc.searchCustomer(customer.getCustomernumber(), customer.getName());
		customerList = new ArrayList<Customer>();
		
		if (customers != null && customers.size() > 0) {
			LOGGER.info(customers.size() + " Kunden gefunden");
			
			if (customers.size() > 1) {
				Iterator it = customers.iterator();
				while (it.hasNext()) {
					Customer c = (Customer) it.next();
					customerList.add(c);
				}
				opened = !opened;
			} else {
				customer = (Customer) customers.get(0);
				countryName = customer.getCountry().getName();
			}
			disableComponents(false);
		} else {
			LOGGER.warn("Kein Customer gefunden; " + customer.getCustomernumber() + "; " + customer.getName());
			getRequest().setAttribute("message", "Kein Customer gefunden; " + customer.getCustomernumber() + "; " + customer.getName());
		}
	}
	
	public String showInvoices() {
		LOGGER.info("Method: showInvoices; Kundennummer: " + customer.getCustomernumber());
		BillController bc = new BillController();
		Session session = HibernateUtil.getSession();
		Transaction transaction = session.beginTransaction();
		bills = bc.findCustomerBills(session, transaction, Integer.parseInt(customer.getCustomernumber()));
		getSession().setAttribute("invoicesCustomerNumber", customer.getCustomernumber());
		LOGGER.info(bills.size() + " Rechnungen gefunden!");
		return "";
	}

	
	private void disableComponents(boolean b) {
		LOGGER.info("Method: disableComponents");
		branchBinding.setDisabled(b);
		faoBinding.setDisabled(b);
		supplierNumberBinding.setDisabled(b);
		streetBinding.setDisabled(b);
		housenumberBinding.setDisabled(b);
		postboxBinding.setDisabled(b);
		postcodeBinding.setDisabled(b);
		cityBinding.setDisabled(b);
		contactGenderBinding.setDisabled(b);
		contactFirstnameBinding.setDisabled(b);
		contactNameBinding.setDisabled(b);
		invoiceStreetBinding.setDisabled(b);
		invoiceHousenumberBinding.setDisabled(b);
		invoicePostboxBinding.setDisabled(b);
		invoicePostcodeBinding.setDisabled(b);
		invoiceCityBinding.setDisabled(b);
		invoiceconfigSimpriceBinding.setDisabled(b);
		invoiceconfigFormatBinding.setDisabled(b);
		invoiceconfigCreationFrequencyBinding.setDisabled(b);
		invoiceconfigSeparateBillingBinding.setDisabled(b);
		emailBinding.setDisabled(b);
		de_mailBinding.setDisabled(b);
		commentBinding.setDisabled(b);
		voucherMonthBinding.setDisabled(b);
		voucherYearBinding.setDisabled(b);
		cardAmountBinding.setDisabled(b);
		cardVoucherBinding.setDisabled(b);
		vatNumberBinding.setDisabled(b);
		selectCountryBinding.setDisabled(b);
		invoiceconfigSortingBinding.setDisabled(b);
		paymentTargetBinding.setDisabled(b);
		getRequest().setAttribute("componentDisabled", b);
	}

	public void searchCustomerCards() {
		LOGGER.info("Method: searchCustomerCards; " + customer.getCustomernumber() + "; " + customer.getName());
		CustomerController cc = new CustomerController();
		List<DaoObject> customers =	cc.searchCustomer(customer.getCustomernumber(), customer.getName());
		Customer cus = null;
		if (customers != null && customers.size() > 0) {
			cus = (Customer) customers.get(0);
		}

		cardList = new ArrayList<CardBean>();
		if (cus != null) {
			List<DaoObject> cardsDao = cc.searchCustomerCards(cus);
			LOGGER.info(cardsDao.size() + " Cards found;");
			for (DaoObject dao : cardsDao) {
				cardList.add((CardBean) dao);
			}
		}
	}
	
	public void updateCustomerAction() {
		LOGGER.info("Method: updateCustomerAction;");
		updateCustomer();
	}
	
	public String updateCustomer() {
		LOGGER.info("Method: updateCustomer; Customer: " + customer.getCustomernumber());
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
//		List<DaoObject> customers = null;
//		CustomerController customerController = new CustomerController();

//		Person cp = null;
//		Person cpp = customer.getContactPerson();
//		if (cp == null && cpp != null && cpp.getName() != null && cpp.getName().length() > 0) {
//			// HIER GIBT ES EINEN OUT OF MEMORY ERROR
//			cp = (Person) customerController.createMyObject(cpp);
//			dbCustomer.setContactPerson(cp);
//		}

		
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			whereClause = " where customer.id = '" + customer.getId() + "'";
			
			List<Customer> customerList = session.createQuery("from Customer as customer" + whereClause).list();
			Customer dbCustomer = customerList.get(0);
			dbCustomer.setCustomernumber(customer.getCustomernumber());
			dbCustomer.setBranch(customer.getBranch());
			dbCustomer.setFao(customer.getFao());
			dbCustomer.setName(customer.getName());
			dbCustomer.setSupplierNumber(customer.getSupplierNumber());
			dbCustomer.setComment(customer.getComment());
			dbCustomer.setVatNumber(customer.getVatNumber());
			
			CountryController countryController = new CountryController();
			Country country = countryController.findCountry(countryName);
			dbCustomer.setCountry(country);
			
			Address ad = dbCustomer.getAddress();
			Address cad = customer.getAddress();
			ad.setCity(cad.getCity());
			ad.setHousenumber(cad.getHousenumber());
			ad.setPostbox(cad.getPostbox());
			ad.setPostcode(cad.getPostcode());
			ad.setStreet(cad.getStreet());
			
			Person cp = dbCustomer.getContactPerson();
			Person cpp = customer.getContactPerson();
			
			cp.setFirstname(cpp.getFirstname());
			cp.setGender(cpp.getGender());
			cp.setName(cpp.getName());
			cp.setEmail(cpp.getEmail());
			cp.setDe_mail(cpp.getDe_mail());

			Address cpa = cp.getAddress();
			Address cppa = cpp.getAddress();
			cpa.setCity(cppa.getCity());
			cpa.setHousenumber(cppa.getHousenumber());
			cpa.setPostbox(cppa.getPostbox());
			cpa.setPostcode(cppa.getPostcode());
			cpa.setStreet(cppa.getStreet());			

			Address ia = dbCustomer.getInvoiceAddress();
			Address cia = customer.getInvoiceAddress();
			ia.setCity(cia.getCity());
			ia.setHousenumber(cia.getHousenumber());
			ia.setPostbox(cia.getPostbox());
			ia.setPostcode(cia.getPostcode());
			ia.setStreet(cia.getStreet());

			InvoiceConfiguration ic = dbCustomer.getInvoiceConfiguration();
			InvoiceConfiguration cic = customer.getInvoiceConfiguration();
			ic.setColumns(cic.getColumns());
			ic.setCreationFrequency(cic.getCreationFrequency());
			ic.setDataOptionSurcharge(cic.getDataOptionSurcharge());
			ic.setSortingOption(cic.getSortingOption());
			ic.setFormat(cic.getFormat());
			ic.setSimPrice(cic.getSimPrice());
			ic.setSeparateBilling(cic.getSeparateBilling());
			ic.setSeparateBillingCriteria(cic.getSeparateBillingCriteria());
			ic.setDebtOrder(cic.getDebtOrder());
			ic.setPaymentTarget(cic.getPaymentTarget());
			
			if (voucher.getCardAmount() > 0 && voucher.getCardVoucher() > 0) {
				Set<Voucher> vouchers = dbCustomer.getVouchers();
				BigDecimal bd = new BigDecimal(voucher.getCardAmount());
				bd = bd.multiply(new BigDecimal(voucher.getCardVoucher()));
				bd = bd.setScale(2, RoundingMode.HALF_UP);
				voucher.setTotalVoucher(bd.doubleValue());
				vouchers.add(voucher);
				
				voucher = new Voucher();
				cardAmountBinding.setValue("");
				cardVoucherBinding.setValue("");
			}
			
			tx.commit();
			LOGGER.info("Customer " + customer.getCustomernumber() + " aktualisiert;");
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("Error rolling back transaction");
					LOGGER.error("HibernateException: " + e1);
				}
				// throw again the first exception
				throw e;
			}
		}

		FacesUtil.writeAttributeToRequest("message", "�nderung gespeichert!");
		customer = new Customer();
		if (FacesContext.getCurrentInstance() != null) {
			disableComponents(true);
		}

		
		return "openUpdateCustomerDialog";
	}
	
	public String createCustomerNext() {
		LOGGER.info("Method: createCustomerNext;");
		createCustomer();
		customer = new Customer();
		return null;
	}

	public String createCustomerFinish() {
		LOGGER.info("Method: updateCustomerFinish;");
		createCustomer();
		customer = new Customer();
		return "finish";
	}

	public String deleteVoucher(Voucher voucher) {
		LOGGER.info("Method: deleteVoucher; Customer = " + customer.getCustomernumber() + ", VoucherID = " + voucher.getVoucherId());
		customer.getVouchers().remove(voucher);
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		CustomerController customerController = new CustomerController();

		
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			whereClause = " where customer.id = '" + customer.getId() + "'";
			
			List<Customer> customerList = session.createQuery("from Customer as customer" + whereClause).list();
			Customer dbCustomer = customerList.get(0);
			
			dbCustomer.setVouchers(customer.getVouchers());
		} catch (Exception e) {
			LOGGER.error("Exception: " + e);
		}

		LOGGER.info("Voucher deleted successfully");
		return "openUpdateCustomerDialog";
	}
	
	public void selectCustomer() {
		Iterator<Customer> itrCust = customerList.iterator();
		boolean match = false;
		while (itrCust.hasNext()) {
			Customer customerFromList = itrCust.next();
			if (customerFromList.toString().equals(selectedCustomer)) {
				LOGGER.info("Customer " + customerFromList.getName() + " equals my chosen customer!!!");
				customer = customerFromList;
				match = true;
				break;
			}
		}

		if (!match) {
			LOGGER.warn("No customer found!");
		}
		
		disableComponents(false);
		opened = !opened;
	}
	
	public String deleteCustomer() {
		LOGGER.info("Method: deleteCustomer; Customer = " + customer.getCustomernumber());
		CustomerController customerController = new CustomerController();
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();
		String hql = "select cardBean from CardBean cardBean where customer = " + customer.getId();
		List<CardBean> results = session.createQuery(hql).list();

		if (results != null && results.size() > 0) {
			for (CardBean c: results) {
				c.setCustomer(new Customer());
			}
		}

		tx.commit();

		customerController.deleteObject(customer);
		getSession().setAttribute("refreshCustomerList", true);
		
		getRequest().setAttribute("message", "Kunde wurde erfolgreich gel�scht!");
		return "";
	}
	
	public void modaltyChange(ValueChangeEvent event) {
		changeModaltyList((String) event.getNewValue());
	}
	
	private void changeModaltyList(String modalty) {
        if (modalty != null && modalty.length() > 0) {
        	System.out.println("Chosen Payment Modalty: " + modalty);
        	CustomerController cac = new CustomerController();
        	List<DaoObject> list = cac.findCustomersByPaymentModalty(modalty);
    		modaltyCustomers = new ArrayList<Customer>();
    		for (DaoObject d : list) {
    			modaltyCustomers.add((Customer) d);
    		}
    		CustomerNumberComparator cusComp = new CustomerNumberComparator();
    		Collections.sort(modaltyCustomers, cusComp);
    		String message = modaltyCustomers.size() + " Kunden gefunden. Tabelle wurde aktualisiert.";
    		getRequest().setAttribute("message", message);
        }
	}
	
	public Customer getCustomer() {
		if (getRequest().getAttribute("newCustomer") != null) {
			customer = new Customer();
			getRequest().removeAttribute("newCustomer");
		}

		return customer;
	}

	public List<Customer> getAllCustomers(){
		CustomerController cc = new CustomerController();
		List<DaoObject> list = cc.listObjects();
		List<Customer> customers = new ArrayList<Customer>();
		for (DaoObject d : list) {
			customers.add((Customer) d);
		}
		CustomerNumberComparator cusComp = new CustomerNumberComparator();
		Collections.sort(customers, cusComp);

		return customers;
	}
	
	public List<Customer> getModaltyCustomers(){
		if (modaltyCustomers == null) {
			changeModaltyList(Model.PAYMENT_MODALTY_MONTHLY);
		}
		return modaltyCustomers;
	}
	
	public List<Customer> getModaltyCustomers(String modality){
		changeModaltyList(modality);
		return modaltyCustomers;
	}
	
	
	public String downloadPaymentModalityList() {
		List<Customer> monthly =  getModaltyCustomers(Model.PAYMENT_MODALTY_MONTHLY);
		List<Customer> quarterly =  getModaltyCustomers(Model.PAYMENT_MODALTY_QUARTERLY);
		List<Customer> halfyearly =  getModaltyCustomers(Model.PAYMENT_MODALTY_HALFYEARLY);
		List<Customer> yearly =  getModaltyCustomers(Model.PAYMENT_MODALTY_YEARLY);
		List<Customer> directDebit =  getModaltyCustomers(Model.PAYMENT_MODALTY_DIRECT_DEBIT);

		CustomerReportGenerator crg = new CustomerReportGenerator();
		crg.generateReport(null, monthly, quarterly, halfyearly, yearly, directDebit);
		return "";
	}
	
	public String downloadCustomerList() {
		List<Customer> customers =  getAllCustomers();
//		List<Customer> customers =  getModaltyCustomers();

		CustomerReportGenerator crg = new CustomerReportGenerator();
		crg.generateReport(customers, null, null, null, null, null);
//		FacesContext facesContext = FacesContext.getCurrentInstance();
//		ExternalContext externalContext = facesContext.getExternalContext();
//
//		String filename = "CustomerList.pdf";
//
//		externalContext.responseReset();
//		externalContext.setResponseContentType("application/pdf");
//		externalContext.setResponseHeader("Content-Disposition",
//				"attachment; filename=\"" + filename + "\"");
//
//		try {
//			OutputStream output = externalContext.getResponseOutputStream();
//
//			for (Customer cus : customers) {
//				output.write(cus.getCustomernumber().getBytes());
//			}
//
//			output.flush();
//			output.close();
//
//			facesContext.responseComplete();
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
		return "";
	}

	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}
	
	public List<Customer> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Customer> customerList) {
		this.customerList = customerList;
	}

	public String getSelectedCustomer() {
		return selectedCustomer;
	}

	public void setSelectedCustomer(String selectedCustomer) {
		this.selectedCustomer = selectedCustomer;
	}

	public boolean isComponentDisabled() {
		if (getRequest().getAttribute("componentDisabled") != null)
			return true;
		return false;
	}

	public void setComponentDisabled(boolean componentDisabled) {
		this.componentDisabled = componentDisabled;
	}

	public HtmlInputText getHousenumberBinding() {
//		if (invoicePostboxBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoicePostboxBinding.setDisabled(true);
		return housenumberBinding;
	}

	public void setHousenumberBinding(HtmlInputText housenumberBinding) {
		this.housenumberBinding = housenumberBinding;
	}

	public HtmlInputText getPostboxBinding() {
//		if (invoicePostboxBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoicePostboxBinding.setDisabled(true);
		return postboxBinding;
	}

	public void setPostboxBinding(HtmlInputText postboxBinding) {
		this.postboxBinding = postboxBinding;
	}

	public HtmlInputText getPostcodeBinding() {
//		if (postcodeBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			postcodeBinding.setDisabled(true);
		return postcodeBinding;
	}

	public void setPostcodeBinding(HtmlInputText postcodeBinding) {
		this.postcodeBinding = postcodeBinding;
	}

	public HtmlInputText getCityBinding() {
//		if (cityBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			cityBinding.setDisabled(true);
		return cityBinding;
	}

	public void setCityBinding(HtmlInputText cityBinding) {
		this.cityBinding = cityBinding;
	}

	public HtmlSelectOneMenu getGenderBinding() {
//		if (genderBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			genderBinding.setDisabled(true);
		return genderBinding;
	}

	public void setGenderBinding(HtmlSelectOneMenu genderBinding) {
		this.genderBinding = genderBinding;
	}

	public HtmlInputText getInvoiceStreetBinding() {
//		if (invoiceStreetBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceStreetBinding.setDisabled(true);
		return invoiceStreetBinding;
	}

	public void setInvoiceStreetBinding(HtmlInputText invoiceStreetBinding) {
		this.invoiceStreetBinding = invoiceStreetBinding;
	}

	public HtmlInputText getInvoiceHousenumberBinding() {
//		if (invoiceHousenumberBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceHousenumberBinding.setDisabled(true);
		return invoiceHousenumberBinding;
	}

	public void setInvoiceHousenumberBinding(HtmlInputText invoiceHousenumberBinding) {
		this.invoiceHousenumberBinding = invoiceHousenumberBinding;
	}

	public HtmlInputText getInvoicePostboxBinding() {
//		if (invoicePostboxBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoicePostboxBinding.setDisabled(true);
		return invoicePostboxBinding;
	}

	public void setInvoicePostboxBinding(HtmlInputText invoicePostboxBinding) {
		this.invoicePostboxBinding = invoicePostboxBinding;
	}

	public HtmlInputText getInvoicePostcodeBinding() {
//		if (invoicePostcodeBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoicePostcodeBinding.setDisabled(true);
		return invoicePostcodeBinding;
	}

	public void setInvoicePostcodeBinding(HtmlInputText invoicePostcodeBinding) {
		this.invoicePostcodeBinding = invoicePostcodeBinding;
	}

	public HtmlInputText getInvoiceCityBinding() {
//		if (invoiceCityBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceCityBinding.setDisabled(true);
		return invoiceCityBinding;
	}

	public void setInvoiceCityBinding(HtmlInputText invoiceCityBinding) {
		this.invoiceCityBinding = invoiceCityBinding;
	}

	public HtmlSelectOneMenu getInvoiceconfigSimpriceBinding() {
//		if (invoiceconfigSimpriceBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceconfigSimpriceBinding.setDisabled(true);
		return invoiceconfigSimpriceBinding;
	}

	public void setInvoiceconfigSimpriceBinding(
			HtmlSelectOneMenu invoiceconfigSimpriceBinding) {
		this.invoiceconfigSimpriceBinding = invoiceconfigSimpriceBinding;
	}

	public HtmlSelectOneMenu getInvoiceconfigFormatBinding() {
//		if (invoiceconfigFormatBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceconfigFormatBinding.setDisabled(true);
		return invoiceconfigFormatBinding;
	}

	public void setInvoiceconfigFormatBinding(
			HtmlSelectOneMenu invoiceconfigFormatBinding) {
		this.invoiceconfigFormatBinding = invoiceconfigFormatBinding;
	}

	public HtmlSelectOneMenu getInvoiceconfigCreationFrequencyBinding() {
//		if (invoiceconfigCreationFrequencyBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceconfigCreationFrequencyBinding.setDisabled(true);
		return invoiceconfigCreationFrequencyBinding;
	}

	public void setInvoiceconfigCreationFrequencyBinding(
			HtmlSelectOneMenu invoiceconfigCreationFrequencyBinding) {
		this.invoiceconfigCreationFrequencyBinding = invoiceconfigCreationFrequencyBinding;
	}

//	public HtmlSelectManyCheckbox getInvoiceconfigColumnsBinding() {
//		if (invoiceconfigColumnsBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			invoiceconfigColumnsBinding.setDisabled(true);
//		return invoiceconfigColumnsBinding;
//	}

//	public void setInvoiceconfigColumnsBinding(
//			HtmlSelectManyCheckbox invoiceconfigColumnsBinding) {
//		this.invoiceconfigColumnsBinding = invoiceconfigColumnsBinding;
//	}

	public HtmlInputText getStreetBinding() {
//		if (streetBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			streetBinding.setDisabled(true);
		return streetBinding;
	}

	public void setStreetBinding(HtmlInputText streetBinding) {
		this.streetBinding = streetBinding;
	}

	public HtmlInputText getContactFirstnameBinding() {
//		if (contactFirstnameBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			contactFirstnameBinding.setDisabled(true);
		return contactFirstnameBinding;
	}

	public void setContactFirstnameBinding(HtmlInputText contactFirstnameBinding) {
		this.contactFirstnameBinding = contactFirstnameBinding;
	}

	public HtmlSelectOneMenu getContactGenderBinding() {
//		if (contactGenderBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			contactGenderBinding.setDisabled(true);
		return contactGenderBinding;
	}

	public void setContactGenderBinding(HtmlSelectOneMenu contactGenderBinding) {
		this.contactGenderBinding = contactGenderBinding;
	}

	public HtmlInputText getContactNameBinding() {
//		if (contactNameBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			contactNameBinding.setDisabled(true);
		return contactNameBinding;
	}

	public void setContactNameBinding(HtmlInputText contactNameBinding) {
		this.contactNameBinding = contactNameBinding;
	}

	public HtmlInputText getBranchBinding() {
//		if (branchBinding != null && getRequest().getAttribute("componentDisabled") != null)
//			branchBinding.setDisabled(true);
		return branchBinding;
	}

	public void setBranchBinding(HtmlInputText branchBinding) {
		this.branchBinding = branchBinding;
	}

	public HtmlInputText getEmailBinding() {
		return emailBinding;
	}

	public void setEmailBinding(HtmlInputText emailBinding) {
		this.emailBinding = emailBinding;
	}

	public HtmlInputText getDe_mailBinding() {
		return de_mailBinding;
	}

	public void setDe_mailBinding(HtmlInputText de_mailBinding) {
		this.de_mailBinding = de_mailBinding;
	}

	public HtmlInputText getFaoBinding() {
		return faoBinding;
	}

	public void setFaoBinding(HtmlInputText faoBinding) {
		this.faoBinding = faoBinding;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public List<CardBean> getCardList() {
		return cardList;
	}

	public void setCardList(List<CardBean> cardList) {
		this.cardList = cardList;
	}

	public HtmlInputText getSupplierNumberBinding() {
		return supplierNumberBinding;
	}

	public void setSupplierNumberBinding(HtmlInputText supplierNumberBinding) {
		this.supplierNumberBinding = supplierNumberBinding;
	}

	public HtmlInputText getCommentBinding() {
		return commentBinding;
	}

	public void setCommentBinding(HtmlInputText commentBinding) {
		this.commentBinding = commentBinding;
	}

	public HtmlSelectManyCheckbox getInvoiceconfigColumnsBinding() {
		return invoiceconfigColumnsBinding;
	}

	public void setInvoiceconfigColumnsBinding(
			HtmlSelectManyCheckbox invoiceconfigColumnsBinding) {
		this.invoiceconfigColumnsBinding = invoiceconfigColumnsBinding;
	}

	public HtmlSelectBooleanCheckbox getInvoiceconfigSeparateBillingBinding() {
		return invoiceconfigSeparateBillingBinding;
	}

	public void setInvoiceconfigSeparateBillingBinding(
			HtmlSelectBooleanCheckbox invoiceconfigSeparateBillingBinding) {
		this.invoiceconfigSeparateBillingBinding = invoiceconfigSeparateBillingBinding;
	}

	public HtmlSelectManyCheckbox getInvoiceconfigBillingCriteriaBinding() {
		return invoiceconfigBillingCriteriaBinding;
	}

	public void setInvoiceconfigBillingCriteriaBinding(
			HtmlSelectManyCheckbox invoiceconfigBillingCriteriaBinding) {
		this.invoiceconfigBillingCriteriaBinding = invoiceconfigBillingCriteriaBinding;
	}

	public Voucher getVoucher() {
		return voucher;
	}

	public void setVoucher(Voucher voucher) {
		this.voucher = voucher;
	}

	public HtmlSelectOneMenu getVoucherMonthBinding() {
		return voucherMonthBinding;
	}

	public void setVoucherMonthBinding(HtmlSelectOneMenu voucherMonthBinding) {
		this.voucherMonthBinding = voucherMonthBinding;
	}

	public HtmlSelectOneMenu getVoucherYearBinding() {
		return voucherYearBinding;
	}

	public void setVoucherYearBinding(HtmlSelectOneMenu voucherYearBinding) {
		this.voucherYearBinding = voucherYearBinding;
	}

	public HtmlInputText getCardAmountBinding() {
		return cardAmountBinding;
	}

	public void setCardAmountBinding(HtmlInputText cardAmountBinding) {
		this.cardAmountBinding = cardAmountBinding;
	}

	public HtmlInputText getCardVoucherBinding() {
		return cardVoucherBinding;
	}

	public void setCardVoucherBinding(HtmlInputText cardVoucherBinding) {
		this.cardVoucherBinding = cardVoucherBinding;
	}

	public HtmlInputText getVatNumberBinding() {
		return vatNumberBinding;
	}

	public void setVatNumberBinding(HtmlInputText vatNumberBinding) {
		this.vatNumberBinding = vatNumberBinding;
	}

	public HtmlSelectOneMenu getSelectCountryBinding() {
		return selectCountryBinding;
	}

	public void setSelectCountryBinding(HtmlSelectOneMenu selectCountryBinding) {
		this.selectCountryBinding = selectCountryBinding;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public HtmlInputText getPaymentTargetBinding() {
		return paymentTargetBinding;
	}

	public void setPaymentTargetBinding(HtmlInputText paymentTargetBinding) {
		this.paymentTargetBinding = paymentTargetBinding;
	}

	public HtmlSelectOneMenu getInvoiceconfigSortingBinding() {
		return invoiceconfigSortingBinding;
	}

	public void setInvoiceconfigSortingBinding(
			HtmlSelectOneMenu invoiceconfigSortingBinding) {
		this.invoiceconfigSortingBinding = invoiceconfigSortingBinding;
	}
	
	private void updateBillList() {
		BillController bc = new BillController();
		if (getSession().getAttribute("invoicesCustomerNumber") != null) {
			int customerNumber = Integer.parseInt((String) getSession().getAttribute("invoicesCustomerNumber"));
			Session session = HibernateUtil.getSession();
			Transaction transaction = session.getTransaction();
			bills = bc.findCustomerBills(session, transaction, customerNumber);
		}
	}

	public List<Bill> getBills() {
		if (getRequest().getAttribute("billListUpdate") != null && (Boolean) getRequest().getAttribute("billListUpdate")) {
			updateBillList();
		}
		return bills;
	}

	public void setBills(List<Bill> bills) {
		this.bills = bills;
	}

	public String getChosenPaymentModalty() {
		return chosenPaymentModalty;
	}

	public void setChosenPaymentModalty(String chosenPaymentModalty) {
		this.chosenPaymentModalty = chosenPaymentModalty;
	}

	public void setModaltyCustomers(List<Customer> modaltyCustomers) {
		this.modaltyCustomers = modaltyCustomers;
	}


}