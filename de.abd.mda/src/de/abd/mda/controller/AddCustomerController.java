package de.abd.mda.controller;

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.controller.CustomerController;

public class AddCustomerController extends ActionController {

	private Customer customer;

	public AddCustomerController() {
		customer = new Customer();
	}
	
	public void createCustomer() {
		CustomerController customerController = new CustomerController();

		customerController.createObject(customer);

//		getRequest().setAttribute("message", "<br/></br>Neuer Kunde wurde erfolgreich angelegt! <br/><br/>Kundennummer:"
		getRequest().setAttribute("message", "Neuer Kunde wurde erfolgreich angelegt!" + customer.getCustomernumber());
//			+ customer.getCustomernumber());
		getSession().setAttribute("mycustomer", customer);
		getSession().setAttribute("refreshCustomerList", true);
	}

	public String createCustomerNext() {
		createCustomer();
		customer = new Customer();
		return null;
	}

	public String createCustomerFinish() {
		createCustomer();
		customer = new Customer();
		return "finish";
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}



}