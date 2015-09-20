package de.abd.mda.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.icesoft.faces.component.InputHiddenTag;

import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class DeliveryBillController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(DeliveryBillController.class .getName()); 

	private CardBean ccCardBean;
	private List<DaoObject> customerList;
	private List<HashMap<DaoObject, String>> customerMapList;
	private String customer;
	private Customer customerObject;
	private List<String> addressList;
	private List<String> customerStringList;
	
	@SuppressWarnings("unchecked")
	public DeliveryBillController() {
		LOGGER.info("Instantiate: DeliveryBillController");
		if (getRequest().getAttribute("searchedCard") != null)
			ccCardBean = (CardBean) getRequest().getAttribute("searchedCard");
		customerList = (List<DaoObject>) getSession().getAttribute("customerList");
		if (customerList != null && customerList.size() > 0) {
			customerStringList = new ArrayList<String>();
			Customer c = (Customer) customerList.get(0);
			customerStringList.add(c.getName() + "; " + c.getListString() + "; " + c.getCustomernumber());
		}
		
	}

	public void addDeliveryBillData() {
		LOGGER.info("Method: addDeliveryBillData");
		CardBean updateCard = null;
		if (getSession().getAttribute("cardToUpdate") != null) {
			updateCard = (CardBean) getSession().getAttribute("cardToUpdate");
			getSession().removeAttribute("cardToUpdate");
		}
		if (updateCard != null) {
			LOGGER.info("Updating card: " + updateCard.getCardnumberString());
		} else {
			LOGGER.warn("No card for update found in session - adding deliveryBillData is not possible");
		}
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			select += " where card.cardNumberFirst = '" + updateCard.getCardNumberFirst() + "' and card.cardNumberSecond = '" + updateCard.getCardNumberSecond() +"'";

			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
			
			if (list.size() > 0) {
				card = (CardBean) list.get(0);
				LOGGER.info("Card found in db: " + card.getCardnumberString());
				card.setCustomerOrderNumber(ccCardBean.getCustomerOrderNumber());
//				card.setOrderNumber(ccCardBean.getOrderNumber());
				card.setDeliverySlipDate(ccCardBean.getDeliverySlipDate());
				card.setDeliverySlipNumber(ccCardBean.getDeliverySlipNumber());

				boolean match = false;
				Iterator<DaoObject> itrCust = customerList.iterator();
				while (itrCust.hasNext()) {
					Customer customerFromList = (Customer) itrCust.next();
					if (customerFromList.toString().equals(customer)) {
						LOGGER.info("Customer " + customerFromList.getName()
								+ " equals my chosen customer!!!");

						String whereClause = "";
						whereClause = " where customer.id = '" + customerFromList.getId() + "'";
						
						List<Customer> customerList = session.createQuery("from Customer as customer" + whereClause).list();
						Customer dbCustomer = customerList.get(0);
						if (dbCustomer != null) {
							LOGGER.info("Found customer in database; Linking customer with card " + card.getCardnumberString());
						} else {
							LOGGER.warn("Customer " + customerFromList.getCustomernumber() + " not found in database!");
						}
						card.setCustomer(dbCustomer);
						match = true;
						break;
					}
				}
				if (!match) {
					LOGGER.warn("Kein Match!");
				}
			} else
				LOGGER.warn("No card found in database with values: " + updateCard.getCardnumberString());
			tx.commit();
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("HibernateException: Error rolling back transaction; " + e1);
				}
				// throw again the first exception
				throw e;
			}

		}

		if (getRequest() != null && card != null)
			getRequest().setAttribute("message", "Karte <font color=#000BF><i>"+card.getCardNumberFirst() + "-" + card.getCardNumberSecond() +"; " + card.getPhoneString() + "</i></font> wurde erfolgreich aktualisiert.");
		ccCardBean = new CardBean();
	}

	public String addDeliveryDataNext() {
		LOGGER.info("Method: addDeliveryBillDataNext");
		addDeliveryBillData();
		ccCardBean = new CardBean();
		return "next";
	}

	public String addDeliveryDataFinish() {
		LOGGER.info("Method: addDeliveryBillFinish");
		addDeliveryBillData();
		ccCardBean = new CardBean();
		return "finish";
	}

	public CardBean getCardBean() {
		if (getRequest().getAttribute("searchedCard") != null) {
			ccCardBean = (CardBean) getRequest().getAttribute("searchedCard");
			getRequest().removeAttribute("searchedCard");
		}
		return ccCardBean;
	}

	public void setCardBean(CardBean cardBean) {
		this.ccCardBean = cardBean;
	}

	public List<DaoObject> getCustomerList() {
		if (getSession().getAttribute("refreshCustomerList") != null && getSession().getAttribute("refreshCustomerList").equals(true)) {
			CustomerController customerController = new CustomerController();
			customerList = customerController.listObjects();
			getSession().removeAttribute("refreshCustomerList");
		}
		return customerList;
	}

	public void setCustomerList(List<DaoObject> customers) {
		this.customerList = customers;
	}

	public String getCustomer() {
		if (getRequest().getAttribute("refreshCustomerList") != null && getRequest().getAttribute("refreshCustomerList").equals(true)) {
			CustomerController customerController = new CustomerController();
			customerList = customerController.listObjects();
		}
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public Date getDeliverySlipDate() {
		if (getRequest().getAttribute("newDateSet") == null && ccCardBean != null) {
			if (ccCardBean.getDeliverySlipNumber() == null || (ccCardBean.getDeliverySlipNumber() != null && ccCardBean.getDeliverySlipNumber().length() == 0)) {
				ccCardBean.setDeliverySlipDate(new Date());
			}
		}

		return ccCardBean.getDeliverySlipDate();
	}

	public void setDeliverySlipDate(Date deliverySlipDate) {
		ccCardBean.setDeliverySlipDate(deliverySlipDate);
		getRequest().setAttribute("newDateSet", true);
	}

	public Customer getCustomerObject() {
		return customerObject;
	}

	public void setCustomerObject(Customer customerObject) {
		this.customerObject = customerObject;
	}

	public List<HashMap<DaoObject, String>> getCustomerMapList() {
		return customerMapList;
	}

	public void setCustomerMapList(List<HashMap<DaoObject, String>> customerMapList) {
		this.customerMapList = customerMapList;
	}

	public List<String> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<String> addressList) {
		this.addressList = addressList;
	}

	public List<String> getCustomerStringList() {
		return customerStringList;
	}

	public void setCustomerStringList(List<String> customerStringList) {
		this.customerStringList = customerStringList;
	}

}