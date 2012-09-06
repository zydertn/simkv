package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CustomerComparator;


public class CustomerController extends DaoController implements IDaoController {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct customer from Customer as customer where customer.name != ''";
//			" inner join customer.address as address where customer.name != ''");

			List<Customer> list = session.createQuery(select).list();
			customers = new ArrayList<DaoObject>();
			for (Iterator it=list.iterator();it.hasNext();) {
				Customer customer = (Customer) it.next();
				customers.add(customer);
			}

			tx.commit();
			
			Comparator<DaoObject> comparator = new CustomerComparator();
			Collections.sort(customers, comparator);
			
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
		
		return customers;
	}
	
	@SuppressWarnings("unchecked")
	public List<DaoObject> searchCustomer(String customerNumber, String customerName) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
				if (customerName != null && customerName.length() > 0) {
					whereClause += " && customer.name = '" + customerName + "'";
				}
			} else if (customerName != null && customerName.length() > 0) {
				whereClause += " where customer.name = '" + customerName + "'";
			}
			
			customers = session.createQuery("from Customer as customer" + whereClause).list();
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
		return customers;
	}
	
}