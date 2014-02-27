package de.abd.mda.junit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.IReportGenerator;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.report.ReportGenerator_portrait;
import de.abd.mda.report.ReportGenerator_landscape;
import de.abd.mda.report.ReportCalculator.DateComparator;

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

		String select = "select distinct customer from Customer customer where customer.customernumber IN ('20002')";
		
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

			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(2014, Calendar.JANUARY, 1, 0, 0, 0);
			List<DaoObject> cards = rg.searchCards(customer, calcMonth, tx, session);

//			IReportGenerator repGen = new ReportGenerator_landscape();
			IReportGenerator repGen = new ReportGenerator_portrait();
			boolean generatedWithoutError = repGen.generateReport(cards, customer, calcMonth, false, false, 1);
			if (generatedWithoutError) {
				if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY)) {
					/*
					 * Bei Jahreskunden muss das LastCalculationDate auf den Karten gespeichert werden.
					 * Jahresrechnungen werden jeden Monat erstellt mit den Karten, die seit der letzten Rechnungserstellung (normalerweise im letzten Monat) hinzugekommen sind.
					 * Es werden nur Rechnungen für ganze Monate erstellt - das heißt, wenn am 5. eines Monats ein Rechnungslauf stattfindet, dann werden die Karten, die zw. 1. und 5. 
					 * dieses Monats aktiviert wurden, noch nicht beachtet - erst im Folgemonat.
					 * Eigentlich ist nur das Jahr relevant, aber historisch bedingt wird das ganze Datum gespeichert. Wenn eine Karte in einem Jahr schon einmal in Rechnung gestellt 
					 * wurde (für das ganze Jahr ab dem Monat der Aktivierung), dann muss die Karte erst im Folgejahr wieder in Rechnung gestellt werden.
					 */
					Iterator<DaoObject> cIt = cards.iterator();
					while (cIt.hasNext()) {
						CardBean card = (CardBean) cIt.next();
						card.setLastCalculationYear(calcMonth.get(Calendar.YEAR));
					}
				} else {
					/*
					 * Bei allen anderen Rechnungsläufen muss nur pro Kunde das LastCalculationDate gesetzt werden.
					 */
					
					customer.setLastCalculationDate(calcMonth.getTime());
				}
			}

		}

		tx.commit();
//		Customer customer = (Customer) customerList.get(0);
		
	}

	private List<DaoObject> searchCards(Customer customer, Calendar calcMonth,Transaction tx,
			Session session) {
//		String select = "select distinct card from CardBean card where card.customer = '"
//				+ customer.getId() + "'";

/*		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate.add(Calendar.MONTH, 1);
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String select = "select distinct card from CardBean card where card.customer = '"
//				+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationDate IS NULL or card.lastCalculationDate < '" + sdf.format(maxLastCalculationDate.getTime()) + "')";
				+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "')";
		
		
		
		return this.searchObjects(select, tx, session);
*/
		
		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate = addMonthsToMaxActivationDate(customer.getInvoiceConfiguration().getCreationFrequency(), maxActivationDate);
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		Calendar customerLastCalcDate = Calendar.getInstance();
		if (customer.getLastCalculationDate() != null) {
			customerLastCalcDate.setTime(customer.getLastCalculationDate());
		}

		if (customer.getLastCalculationDate() == null || customerLastCalcDate.before(maxLastCalculationDate)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String select = "select distinct card from CardBean card where card.customer = '"
					+ customer.getId() + "' and card.status = 'aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and NOT card.flatrateCard IS TRUE";
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY)) {
				select = "select distinct card from CardBean card where card.customer = '"
						+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationYear IS NULL or card.lastCalculationYear < '" + maxLastCalculationDate.get(Calendar.YEAR) + "') and NOT card.flatrateCard IS TRUE";
			}

			DateComparator dc = new DateComparator();
			List<DaoObject> cardList = searchObjects(select, tx, session);
			if (cardList != null && cardList.size() > 0) {
				System.out.println("Size = " + cardList.size());
			}
			Collections.sort(cardList, dc);
			return cardList;
		}
		return null;
		
	}

	private Calendar addMonthsToMaxActivationDate(String creationFrequency, Calendar maxActivationDate) {
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY))
			maxActivationDate.add(Calendar.MONTH, 1);
		else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY))
			maxActivationDate.add(Calendar.MONTH, (maxActivationDate.get(Calendar.MONTH) + 1) % 3);
		else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY))
			maxActivationDate.add(Calendar.MONTH, (maxActivationDate.get(Calendar.MONTH) + 1) % 6);
		else {
			// JAHRESKUNDE, zu behandeln wie Monatskunde
			maxActivationDate.add(Calendar.MONTH, 1);
//			maxActivationDate.add(Calendar.YEAR, 1);
//			maxActivationDate.set(Calendar.MONTH, 0);
		}
		return maxActivationDate;
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

	public class DateComparator implements java.util.Comparator<DaoObject> {
	      public int compare(DaoObject dao1, DaoObject dao2) {
	    	  CardBean card1 = (CardBean) dao1;
	    	  CardBean card2 = (CardBean) dao2;
	           return (card1.getActivationDate()==card2.getActivationDate() ? 0 : card1.getActivationDate().compareTo(card2.getActivationDate())); 
	      }
	}


}
