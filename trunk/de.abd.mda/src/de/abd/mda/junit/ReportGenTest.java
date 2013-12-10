package de.abd.mda.junit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.IReportGenerator;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.report.ReportGenerator_portrait;
import de.abd.mda.report.ReportGenerator_landscape;

public class ReportGenTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ReportGenTest rg = new ReportGenTest();
		ReportCalculator rp = new ReportCalculator();
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = rg.createTransaction(session);
//		String select = "select distinct customer from Customer customer where customer.customernumber IN (" +
//				"'20224', '20216', '20208', '20206'" +
//				")";

		String select = "select distinct customer from Customer customer where customer.customernumber != ''";
		
		List<DaoObject> customerList = rg.searchObjects(select, tx, session);

//		Iterator<DaoObject> it = customerList.iterator();
//		while (it.hasNext()) {
//			Customer customer = (Customer) it.next();
//
//			Calendar calcMonth = Calendar.getInstance();
//			calcMonth.set(2013, Calendar.OCTOBER, 1, 0, 0, 0);
//			List<DaoObject> cards = rg.searchCards(customer, calcMonth, tx, session);
////			IReportGenerator repGen = new ReportGenerator_landscape();
//			IReportGenerator repGen = new ReportGenerator_portrait();
//			repGen.generateReport(cards, customer, calcMonth);
//		}

		Iterator<DaoObject> it2 = customerList.iterator();
		while (it2.hasNext()) {
			Customer customer = (Customer) it2.next();

			if (customer.getCustomernumber().equals("20165")) {
				System.out.println("Jetzt");
			}
			
			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(2013, Calendar.NOVEMBER, 1, 0, 0, 0);
			List<DaoObject> cards = rg.searchCards(customer, calcMonth, tx, session);
//			IReportGenerator repGen = new ReportGenerator_landscape();
			IReportGenerator repGen = new ReportGenerator_portrait();
			repGen.generateReport(cards, customer, calcMonth);
		}

		tx.commit();
//		Customer customer = (Customer) customerList.get(0);
		
	}

	private List<DaoObject> searchCards(Customer customer, Calendar calcMonth,Transaction tx,
			Session session) {
//		String select = "select distinct card from CardBean card where card.customer = '"
//				+ customer.getId() + "'";

		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate.add(Calendar.MONTH, 1);
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String select = "select distinct card from CardBean card where card.customer = '"
				+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationDate IS NULL or card.lastCalculationDate < '" + sdf.format(maxLastCalculationDate.getTime()) + "')";

		
		
		
		return this.searchObjects(select, tx, session);
	}

	
	@SuppressWarnings("unchecked")
	private List<DaoObject> searchObjects(String select, Transaction tx,
			Session session) {
		List<DaoObject> objects = null;
		objects = (List<DaoObject>) session.createQuery(select).list();
		return objects;
	}

	private Transaction createTransaction(Session session) {
		Transaction tx = null;
		tx = session.beginTransaction();
		return tx;
	}


}
