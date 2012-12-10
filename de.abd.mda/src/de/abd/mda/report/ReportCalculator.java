package de.abd.mda.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class ReportCalculator {
	static final Logger logger = Logger.getLogger(ReportCalculator.class);

	public boolean calculate() {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		// Um den Fehlerfall auszuschließen, wenn jemand exakt am 1. eines Monats um 0 Uhr auf den Knopf drückt (damit dieser Monat auch berechnet wird):
		now.set(Calendar.SECOND, 1);
		Calendar frequencyDate = Calendar.getInstance();
		frequencyDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		System.out.println(sdf.format(frequencyDate.getTime()));
		logger.info("Reporterstellungslauf gestartet, "	+ sdf.format(now.getTime()));

		/***** 1. Alle Kunden ermitteln *****/
		List<DaoObject> customers = searchCustomers();
		if (customers != null) {
			logger.info("Anzahl gefundene Kunden = " + customers.size());
		} else {
			return false;
		}

		/***** 2. Für jeden Kunden die Karten ermitteln *****/
		Iterator cusIt = customers.iterator();
		int i = 0;

		while (cusIt.hasNext()) {
			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
			
			
//			System.out.println(calcMonth.get(Calendar.MONTH));

			Customer customer = (Customer) cusIt.next();
			logger.info("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());

			while (calcMonth.before(now) || calcMonth.equals(now)) {
				Session session = SessionFactoryUtil.getInstance()
						.getCurrentSession();
				Transaction tx = createTransaction(session);
				List<DaoObject> customerCards = searchCards(customer, calcMonth, tx, session);

				HashMap<String, List<CardBean>> cardMap = new HashMap<String, List<CardBean>>();
				Iterator<DaoObject> cardIt = customerCards.iterator();
				while (cardIt.hasNext()) {
					CardBean card = (CardBean) cardIt.next();
					if (card.getInstallAddress() != null) {
						if (cardMap.containsKey(card.getInstallAddress().getAddressString())) {
							cardMap.get(card.getInstallAddress().getAddressString()).add(card);
						} else {
							List<CardBean> cardList = new ArrayList<CardBean>();
							cardList.add(card);
							if (card.getInstallAddress().getAddressString().length() > 0) {
								cardMap.put(card.getInstallAddress().getAddressString(), cardList);
							} else {
								cardMap.put(""+card.getInstallAddress().getAddressId(), cardList);
							}
						}
					}
				}

				if (cardMap.size() > 0) {
					i++;
					ReportGenerator rp = new ReportGenerator();
					boolean generatedWithoutError = rp.generateReport(cardMap, customer, calcMonth);
					if (generatedWithoutError) {
						Set<String> keys = cardMap.keySet();
						Iterator<String> kIt = keys.iterator();
						while (kIt.hasNext()) {
							String key = kIt.next();
							List<CardBean> cards = cardMap.get(key);
							Iterator<CardBean> cIt = cards.iterator();
							while (cIt.hasNext()) {
								CardBean card = cIt.next();
								card.setLastCalculationDate(calcMonth.getTime());
							}
						}
					} else {
						// Aus der Schleife für diesen Kunden aussteigen
						// Folgemonate dürfen nicht mehr berechnet werden
						// Mit nächstem Kunde weitermachen
						break;
					}
				}
				

				tx.commit();
				calcMonth = raiseMonth(calcMonth, customer);
			}

		}

		System.out.println(i + " Reports wurden erstellt!");

		return true;
	}

	private Calendar raiseMonth(Calendar calcMonth, Customer customer) {
		String creationFrequency = null;
		if (customer.getInvoiceConfiguration() != null) {
			creationFrequency = customer.getInvoiceConfiguration().getCreationFrequency();
		}
		
		if (creationFrequency != null) {
			if (creationFrequency.equals(Model.FREQUENCY_MONTHLY)) {
				calcMonth.add(Calendar.MONTH, 1);	
			} else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY)) {
				calcMonth.add(Calendar.MONTH, 3);
			} else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY)) {
				calcMonth.add(Calendar.MONTH, 6);
			} else if (creationFrequency.equals(Model.FREQUENCY_YEARLY)) {
				calcMonth.add(Calendar.YEAR, 1);
			}
		} else {
			// treat like monthly creation
			logger.warn("No CreationFrequency set for customer " + customer.getCustomernumber() + "; Creating monthly!");
			calcMonth.add(Calendar.MONTH, 1);
		}
		
		return calcMonth;
	}

	private List<DaoObject> searchCustomers() {
		String select = "select distinct customer from Customer customer where customer.customernumber != ''";
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = createTransaction(session);
		List<DaoObject> customerList = searchObjects(select, tx, session);
		tx.commit();
		return customerList;
	}

	private List<DaoObject> searchCards(Customer customer, Calendar calcMonth,Transaction tx,
			Session session) {
		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate.add(Calendar.MONTH, 1);
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		System.out.println("maxActivationDate = " + sdf.format(maxActivationDate.getTime()));
//		System.out.println("maxLastCalculationDate = " + sdf.format(maxLastCalculationDate.getTime()));
		String select = "select distinct card from CardBean card where card.customer = '"
				+ customer.getId() + "' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationDate IS NULL or card.lastCalculationDate < '" + sdf.format(maxLastCalculationDate.getTime()) + "')";
				
		return searchObjects(select, tx, session);
	}

	private Transaction createTransaction(Session session) {
		Transaction tx = null;
		tx = session.beginTransaction();
		return tx;
	}

	private List<DaoObject> searchObjects(String select, Transaction tx,
			Session session) {
		List<DaoObject> objects = null;
		objects = session.createQuery(select).list();
		return objects;
	}
	
}
