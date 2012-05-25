package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
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
				System.out.println(customer.getCustomernumber());
			}
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
	
	public Customer searchCard(String customerNumber) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Customer customer = null;
		try {
			tx = session.beginTransaction();
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
			}
			
			Query query = session.createQuery("select card from Customer as customer " + whereClause);
			session.createQuery("from Customer as customer, Address as address, Person as person").list();
			for (Iterator it=query.iterate();it.hasNext();) {
				customer = (Customer) it.next();
				System.out.println(customer.getName());
			}
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
		return customer;
	}
}