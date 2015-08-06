package de.abd.mda.junit;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CustomerComparator;

public class InvoiceconfigurationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
//			String select = "select customer from Customer as customer where customer.name != ''";
//			" inner join customer.address as address where customer.name != ''");

			String select = "from Customer as customer "
			+ "where customer.name != '' and customer.invoiceConfiguration in ( "
			    + "select ic.id from InvoiceConfiguration as ic where ic.debtOrder = 1)";
			
			List<Customer> list = session.createQuery(select).list();
			if (list != null) {
				System.out.println(list.size() + " customers found");
			} else {
				System.out.println("No customers found!");
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
			System.err.println("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.err.println("HibernateException: Error rolling back transaction; " + e1);
				}
				// throw again the first exception
				throw e;
			}

		}
		
	}

}
