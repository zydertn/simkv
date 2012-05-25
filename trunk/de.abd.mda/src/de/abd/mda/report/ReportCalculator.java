package de.abd.mda.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class ReportCalculator {

	public boolean calculate() {
		Calendar startDate = Calendar.getInstance();
		startDate.set(2012, Calendar.JANUARY, 1);

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		System.out.println("Reporterstellungslauf gestartet, "
				+ sdf.format(now));
		System.out.println("Startdatum, " + sdf.format(startDate.getTime()));

		/***** 1. Alle Kunden ermitteln *****/
		List<DaoObject> customers = searchCustomers();
		if (customers != null) {
			now = new Date();
			System.out.println("Anzahl gefundene Kunden = " + customers.size()
					+ ",      Uhrzeit: " + sdf.format(now));
		}

		/***** 2. Für jeden Kunden die Karten ermitteln *****/
		Iterator cusIt = customers.iterator();
		while (cusIt.hasNext()) {
			Customer customer = (Customer) cusIt.next();
			System.out.println("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());
			String frequency = customer.getInvoiceConfiguration()
					.getCreationFrequency();

			Session session = SessionFactoryUtil.getInstance()
					.getCurrentSession();
			Transaction tx = createTransaction(session);
			List<DaoObject> customerCards = searchCards(customer, tx, session);
			now = new Date();
			System.out.println("Anzahl gefundener Karten zu diesem Kunden: "
					+ customerCards.size() + ",      Uhrzeit: "
					+ sdf.format(now));

			if (customerCards != null && customerCards.size() > 0) {
				if (frequency.equals(Model.FREQUENCY_MONTHLY)) {
					calculateInvoices(customerCards, 1);
				} else {

				}
			}

			tx.commit();

		}

		return true;
	}

	private List<DaoObject> searchCustomers() {
		String select = "select distinct customer from Customer customer where customer.name != ''";
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = createTransaction(session);
		List<DaoObject> customerList = searchObjects(select, tx, session);
		tx.commit();
		return customerList;
	}

	private List<DaoObject> searchCards(Customer customer, Transaction tx,
			Session session) {
		String select = "select distinct card from CardBean card where card.customer = '"
				+ customer.getId() + "' and card.status = 'Aktiv'";

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
	
	private void calculateInvoices(List<DaoObject> cardList, int increase) {
		Calendar today = Calendar.getInstance();

		//	Berechnungszeitraum setzen: Startdatum: 1.1.2010, Enddatum: 1.2.2010 / 1.4.2010 / 1.7.2010 / 1.1.2011
		GregorianCalendar startDate = new GregorianCalendar();
		startDate.set(Calendar.YEAR, 2010);
		startDate.set(Calendar.MONTH, Calendar.JANUARY);
		startDate.set(Calendar.DATE, 1);
		
		GregorianCalendar endDate = new GregorianCalendar();
		if (increase < 12) {
			endDate.set(Calendar.YEAR, 2010);
			endDate.set(Calendar.MONTH, Calendar.JANUARY + increase);
			endDate.set(Calendar.DATE, 1);
		} else {
			endDate.set(Calendar.YEAR, 2011);
			endDate.set(Calendar.MONTH, Calendar.JANUARY);
			endDate.set(Calendar.DATE, 1);
		}

		while (endDate.before(today)) {
			List<CardBean> cardsForCalc = addCardsForCalculation(cardList, startDate, endDate);

			if (cardsForCalc != null && cardsForCalc.size() > 0) {
				//	Rechnung für Abrechnungszeitraum erstellen mit allen Karten aus der Liste für den Abrechnungszeitraum
				ReportGenerator rp = new ReportGenerator();
				GregorianCalendar calcDate = new  GregorianCalendar();
				calcDate.set(Calendar.YEAR, startDate.get(Calendar.YEAR));
				calcDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH));
				calcDate.set(Calendar.DATE, today.get(Calendar.DATE));
				
				boolean generationWithoutErrors = true;
//				generationWithoutErrors = rp.generateReport(cardsForCalc, calcDate, startDate, endDate);

				//	Neues Abrechnungsdatum für alle in Abrechnung einbezogenen Karten in Datenbank speichern
				if (generationWithoutErrors) {
					Iterator<CardBean> cIt = cardsForCalc.iterator();
					while (cIt.hasNext()) {
						cIt.next().setLastCalculationDate(calcDate.getTime());
					}
				} else {
					System.out.println("Error in calculation!");
//					break;
				}
			}
			
			//	Start- und Enddatum auf einen x Monate (increase) später setzen ==> (Neues Startdatum:1.2.2010, neues Enddatum: 1.3.2010); Vorsicht mit Jahreswechsel
			if (startDate.get(Calendar.MONTH) + increase > 11) {
				startDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH) + increase - 12);
				startDate.set(Calendar.YEAR, startDate.get(Calendar.YEAR) + 1);
			} else {
				startDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH) + increase);
			}

			if (endDate.get(Calendar.MONTH) + increase > 11) {
				endDate.set(Calendar.MONTH, endDate.get(Calendar.MONTH) + increase - 12);
				endDate.set(Calendar.YEAR, endDate.get(Calendar.YEAR) + 1);
			} else {
				endDate.set(Calendar.MONTH, endDate.get(Calendar.MONTH) + increase);
			}
		}



//		Bei Quartalslauf: 
//		Startdatum: 1.1.2010, Enddatum: 1.4.2012
//		Am Schluss um 3 Monate erhöhen

//		Bei Halbjahreslauf:
//		Startdatum: 1.1.2010, Enddatum: 1.7.2012
//		Am Schluss um 6 Monate erhöhen

//		Bei Jahreslauf:
//		Startdatum: 1.1.2010, Enddatum: 1.1.2011
//		Am Schluss um 1 Jahr erhöhen
		
		
		
/*		Calendar lastCalcDate = null;
		CardBean card = null;

		List<CardBean> cardsCalculatedBefore = new ArrayList<CardBean>();
		List<CardBean> newActivatedCards = new ArrayList<CardBean>();

		// Sort cards into lists and determine last calculation date (must be
		// same date for all cards that have a lastCalculation date)
		Iterator<DaoObject> it = cardList.iterator();
		while (it.hasNext()) {
			card = (CardBean) it.next();
			if (card.getLastCalculationDate() == null) {
				newActivatedCards.add(card);
			} else {
				cardsCalculatedBefore.add(card);
				if (lastCalcDate == null) {
					lastCalcDate = Calendar.getInstance();
					lastCalcDate.setTime(card.getLastCalculationDate());
					lastCalcDate.set(Calendar.DATE,	today.get(Calendar.DATE));
				}
			}
		}

		if (lastCalcDate == null && newActivatedCards != null && newActivatedCards.size() > 0) {
			lastCalcDate = Calendar.getInstance();
			lastCalcDate.setTime(today.getTime());
		}
		
		ReportGenerator rp = new ReportGenerator();

		while ((lastCalcDate.get(Calendar.YEAR) < today.get(Calendar.YEAR))	
				|| (lastCalcDate.get(Calendar.MONTH) < today.get(Calendar.MONTH))
				|| ((lastCalcDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) 
						&& (lastCalcDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)))) {
			if ((lastCalcDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) && (lastCalcDate.get(Calendar.MONTH) == today.get(Calendar.MONTH))) {
				// rp.generateReport(null, newActivatedCards, lastCalcDate);
				break;
			}

			// Liste mit noch nie berechneten Karten durchsuchen nach
			// Karten, die im aktuellen Betrachtungsmonat aktiviert wurden
			List<CardBean> cardsActivatedInCurrentTimeframe = new ArrayList<CardBean>();
			Iterator<CardBean> newCardIt = newActivatedCards.iterator();
			while (newCardIt.hasNext()) {
				card = (CardBean) newCardIt.next();
				Calendar newCardCal = Calendar.getInstance();
				newCardCal.setTime(card.getActivationDate());

				if ((newCardCal.get(Calendar.YEAR) == lastCalcDate
						.get(Calendar.YEAR))
						&& (newCardCal.get(Calendar.MONTH) == lastCalcDate
								.get(Calendar.MONTH))) {
					cardsActivatedInCurrentTimeframe.add(card);
				}
			}

			// rp.generateReport(cardsCalculatedBefore,
			// cardsActivatedInCurrentTimeframe, lastCalcDate);
			if (lastCalcDate.get(Calendar.MONTH) == 11) {
				lastCalcDate.set(Calendar.YEAR,
						lastCalcDate.get(Calendar.YEAR) + 1);
				lastCalcDate.set(Calendar.MONTH, 1);
			} else {
				lastCalcDate.set(Calendar.MONTH,
						lastCalcDate.get(Calendar.MONTH) + 1);
			}
		}
*/	}

	private List<CardBean> addCardsForCalculation(List<DaoObject> cardsOfCustomer, Calendar startDate, Calendar endDate) {
		List<CardBean> cardsForCalculation = new ArrayList<CardBean>();
		
		Iterator<DaoObject> cardIt = cardsOfCustomer.iterator();
		
		//	SCHLEIFE FÜR jede aktivierte Karte des Kunden mit monatlicher Abrechnung:
		while (cardIt.hasNext()) {
			CardBean card = (CardBean) cardIt.next();
			Calendar cardActivationCal = Calendar.getInstance();
			cardActivationCal.setTime(card.getActivationDate());

			//	WENN Aktivierungsdatum < Enddatum
			if (cardActivationCal.before(endDate)) {
				// WENN Datum der letzten Berechnung NICHT gesetzt ist
				if (card.getLastCalculationDate() == null) {
					// Karte in Monats-Berechnung einschließen
					cardsForCalculation.add(card);
				} else {
					//	SONST:
					//	WENN Monatszahl der letzten Berechnung = 11
					Calendar lastCalculation = Calendar.getInstance();
					lastCalculation.setTime(card.getLastCalculationDate());
					if (lastCalculation.get(Calendar.MONTH) == 11) {
						//	Monatszahl auf 0 setzen, Jahreszahl um 1 erhöhen
						lastCalculation.set(Calendar.MONTH, 0);
						lastCalculation.set(Calendar.YEAR, lastCalculation.get(Calendar.YEAR) + 1);
					} else {
						//	SONST:
						//	Monatszahl um 1 erhöhen
						lastCalculation.set(Calendar.MONTH, lastCalculation.get(Calendar.MONTH) + 1);
						//	ENDE SONST
					}

					// WENN LastCalculationDate >= Startdatum UND LastCalculationDate < Enddatum
					if (!lastCalculation.before(startDate) && lastCalculation.before(endDate)) {
						// Karte in Monatsberechnung einschließen bzw. in Liste für Monatsberechnung aufnehmen
						cardsForCalculation.add(card);
						// ENDE WENN
					}
				}
				
			}
			
		//	ENDE SCHLEIFE
			
		}
		

		return cardsForCalculation;
		
	}

	
}
