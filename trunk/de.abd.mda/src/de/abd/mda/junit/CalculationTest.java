package de.abd.mda.junit;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.ReportCalculator;

public class CalculationTest {

	public CalculationTest() {
		if (!createCustomer("CalcTestCustomer 1")) return;
		if (!createCustomer("CalcTestCustomer 2")) return;
		Customer cus1 = findCus("CalcTestCustomer 1");
		Customer cus2 = findCus("CalcTestCustomer 2");
		
		
		if (!createCard("000000", "1", cus1, null, null, Model.STATUS_INACTIVE)) return;
		if (!createCard("000000", "2", cus1, null, null, Model.STATUS_INACTIVE)) return;
		
		Calendar ac1 = Calendar.getInstance();
		Calendar ac2 = Calendar.getInstance();
		Calendar ac3 = Calendar.getInstance();
		Calendar ac4 = Calendar.getInstance();
		ac1.set(2011, 8, 17);
		ac2.set(2010, 10, 20);
		ac3.set(2011, 9, 2);
		ac4.set(2011, 8, 25);
		
		Calendar lc1 = Calendar.getInstance();
		Calendar lc2 = Calendar.getInstance();
		Calendar lc3 = Calendar.getInstance();
		Calendar lc4 = Calendar.getInstance();
		lc1.set(2011, 10, 12);
		lc2.set(2011, 10, 12);
		lc3.set(2011, 9, 8);
		lc4.set(2011, 10, 12);
		
		
		if (!createCard("000001", "1", cus1, ac1.getTime(), lc1.getTime(), Model.STATUS_ACTIVE)) return;
		if (!createCard("000001", "2", cus1, ac2.getTime(), lc2.getTime(), Model.STATUS_ACTIVE)) return;
		if (!createCard("000001", "3", cus1, ac3.getTime(), lc3.getTime(), Model.STATUS_ACTIVE)) return;
		
		if (!createCard("000002", "1", cus2, ac4.getTime(), lc4.getTime(), Model.STATUS_ACTIVE)) return;

		ReportCalculator rp = new ReportCalculator();
		rp.calculate();
	}
	
	
	public static void main(String args[]) {
		CalculationTest ct = new CalculationTest();
		
	}
	
	private Customer findCus(String cusName) {
		CustomerController cC = new CustomerController();
		Customer cus = null;

		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> customers = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct customer from Customer as customer where customer.name = '" + cusName + "'";

			List<Customer> list = session.createQuery(select).list();
			if (list != null && list.size() > 0) {
				cus = list.get(0);
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

		return cus;
	}
	
	private boolean createCustomer(String name) {
		CustomerController cC = new CustomerController();

		Customer cus = new Customer();
		cus.setName(name);

		String retMessage = cC.createObject(cus);
		if (retMessage.length() > 0) {
			System.out.println(retMessage);
			return false;
		} else {
			System.out.println("Customer " + cus.getName() + " created.");
		}
		return true;
	}
	
	private boolean createCard(String cF, String cS, Customer cus, Date aD, Date lCD, String status) {
		CardBean c = new CardBean();
		c.setCardNumberFirst(cF);
		c.setCardNumberSecond(cS);
		c.setCustomer(cus);
		c.setActivationDate(aD);
		c.setLastCalculationDate(lCD);
		c.setStatus(status);
		CardController cC = new CardController();
		String retMessage = cC.createObject(c);
		if (retMessage.length() > 0) {
			System.out.println(retMessage);
			return false;
		} else {
			System.out.println("Card " + c.getCardNumber() + " created.");
		}
		return true;
	}
	
	
}
