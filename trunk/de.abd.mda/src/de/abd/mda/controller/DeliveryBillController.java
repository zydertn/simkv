package de.abd.mda.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

	private CardBean ccCardBean;
	private List<DaoObject> customerList;
	private String customer;
	private Customer customerObject;
	private Date deliverySlipDate;

	public DeliveryBillController() {
		if (getRequest().getAttribute("searchedCard") != null)
			ccCardBean = (CardBean) getRequest().getAttribute("searchedCard");
		CustomerController customerController = new CustomerController();
		customerList = customerController.listObjects();
	}

	public void addDeliveryBillData() {
		CardBean updateCard = null;
		if (getSession().getAttribute("cardToUpdate") != null) {
			updateCard = (CardBean) getSession().getAttribute("cardToUpdate");
			getSession().removeAttribute("cardToUpdate");
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
				card.setCustomerOrderNumber(ccCardBean.getCustomerOrderNumber());
				card.setOrderNumber(ccCardBean.getOrderNumber());
				card.setDeliverySlipDate(ccCardBean.getDeliverySlipDate());
				card.setDeliverySlipNumber(ccCardBean.getDeliverySlipNumber());
				Iterator itrCust = customerList.iterator();
				while (itrCust.hasNext()) {
					Customer customerFromList = (Customer) itrCust.next();
					if (customerFromList.toString().equals(customer)) {
						System.out.println("Customer " + customerFromList.getName()
								+ " equals my chosen customer!!!");
						card.setCustomer(customerFromList);
						break;
					} else
						System.out.println("kein Match");
				}
			} else
				System.out.println("No card found");
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}

		}

		getRequest().setAttribute("message", "Karte <font color=#000BF><i>"+card.getCardNumberFirst() + "-" + card.getCardNumberSecond() +"; " + card.getPhoneString() + "</i></font> wurde erfolgreich aktualisiert.");
		ccCardBean = new CardBean();
	}

	public String addDeliveryDataNext() {
		addDeliveryBillData();
		ccCardBean = new CardBean();
		return "next";
	}

	public String addDeliveryDataFinish() {
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
		return deliverySlipDate;
	}

	public void setDeliverySlipDate(Date deliverySlipDate) {
		this.deliverySlipDate = deliverySlipDate;
	}

	public Customer getCustomerObject() {
		return customerObject;
	}

	public void setCustomerObject(Customer customerObject) {
		this.customerObject = customerObject;
	}

}