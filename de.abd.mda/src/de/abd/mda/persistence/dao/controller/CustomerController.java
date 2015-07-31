package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CustomerComparator;


public class CustomerController extends DaoController implements IDaoController {
	
	private final static Logger LOGGER = Logger.getLogger(CustomerController.class .getName()); 

	public CustomerController() {
		LOGGER.info("Instantiate CustomerController");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		LOGGER.info("Method listObjects");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct customer from Customer as customer where customer.name != ''";
//			" inner join customer.address as address where customer.name != ''");

			LOGGER.info("Select = " + select);
			List<Customer> list = session.createQuery(select).list();
			if (list != null) {
				LOGGER.info(list.size() + " customers found");
			} else {
				LOGGER.warn("No customers found!");
			}
			customers = new ArrayList<DaoObject>();
			for (Iterator it=list.iterator();it.hasNext();) {
				Customer customer = (Customer) it.next();
				customers.add(customer);
			}

			tx.commit();
			
			Comparator<DaoObject> comparator = new CustomerComparator();
			Collections.sort(customers, comparator);
			
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
		
		return customers;
	}
	
	public List<DaoObject> findCustomersByPaymentModalty(String modalty) {
		LOGGER.info("Method listObjects");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();

			
			String select = "from Customer as customer "
			+ "where customer.name != '' and customer.invoiceConfiguration in ( ";

			if (!modalty.equals(Model.PAYMENT_MODALTY_DIRECT_DEBIT)) {
				select += "select ic.id from InvoiceConfiguration as ic where ic.creationFrequency = '" + modalty + "')";	
			} else {
				select += "select ic.id from InvoiceConfiguration as ic where ic.debtOrder = 1)";
			}

			
			
			LOGGER.info("Select = " + select);
			List<Customer> list = session.createQuery(select).list();
			if (list != null) {
				LOGGER.info(list.size() + " customers found");
			} else {
				LOGGER.warn("No customers found!");
			}
			customers = new ArrayList<DaoObject>();
			for (Iterator it=list.iterator();it.hasNext();) {
				Customer customer = (Customer) it.next();
				customers.add(customer);
			}

			tx.commit();
			
			Comparator<DaoObject> comparator = new CustomerComparator();
			Collections.sort(customers, comparator);
			
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
		
		return customers;		
	}
	
	@SuppressWarnings("unchecked")
	public List<DaoObject> searchCustomer(String customerNumber, String customerName) {
		LOGGER.info("Method listObjects; CustomerNumber = " + customerNumber + ", customerName = " + customerName);
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
			} else if (customerName != null && customerName.length() > 0) {
				whereClause += " where customer.name LIKE '" + customerName + "%'";
			}

			LOGGER.info("Select = from Customer as customer" + whereClause);
			customers = session.createQuery("from Customer as customer" + whereClause).list();
			if (customers != null) {
				LOGGER.info(customers.size() + " customers found");
			} else {
				LOGGER.warn("No customer found!");
			}
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
		return customers;
	}

	public Customer findCustomer(String customerNumber) {
		LOGGER.info("Method findCustomer; CustomerNumber = " + customerNumber);
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		Transaction tx = null;
		boolean transactionStarted = false;
		if (session.getTransaction() == null || !session.getTransaction().isActive()) {
			tx = session.beginTransaction();
			transactionStarted = true;
		} else {
			tx = session.getTransaction();
		}
		try {
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
			}

			LOGGER.info("Select = from Customer as customer" + whereClause);
			customers = session.createQuery("from Customer as customer" + whereClause).list();
			if (customers != null) {
				LOGGER.info(customers.size() + " customers found");
			} else {
				LOGGER.warn("No customer found!");
			}
			if (transactionStarted)
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
		Customer customer = (Customer) customers.get(0); 
		if (customer != null) {
			LOGGER.info("Customer found: ID = " + customer.getId() + ", number = " + customer.getCustomernumber());
		} else {
			LOGGER.warn("No customer found!");
		}
		return customer;
	}

	
	public List<DaoObject> searchCustomerCards(Customer cus) {
		LOGGER.info("Method searchCustomerCards");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> cards = null;
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			if (cus != null) {
				whereClause = " where card.customer = '" + cus.getId() + "'";
			}

			LOGGER.info("Select = select distinct card from CardBean card" + whereClause);
			cards = session.createQuery("select distinct card from CardBean card" + whereClause).list();
			if (cards != null) {
				LOGGER.info(cards.size() + " cards found");
			} else {
				LOGGER.warn("No cards found!");
			}
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
		return cards;
	}
	
}