package de.abd.mda.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sun.org.apache.xpath.internal.operations.Mod;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.DateUtils;
import de.abd.mda.util.FacesUtil;

public class ReportCalculator implements Runnable {
	static final Logger logger = Logger.getLogger(ReportCalculator.class);
	HttpSession facesSession;
	private boolean taskRunning = true;
	Thread thread;
	
	public ReportCalculator() {
		facesSession = (new FacesUtil()).getSession();
	}
	
	public boolean calculate() {
		if (facesSession != null && facesSession.getAttribute("reportProgress") != null) {
			System.out.println("Reporterstellung läuft bereits!");
			return false;
		}
		if (facesSession != null)
			facesSession.setAttribute("reportProgress", 0);

		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();
		String select = "from Configuration";

		List<Configuration> list = session.createQuery(select).list();
		Configuration c = null;
		if (list.size() > 0) {
			c = list.get(0);
		} else {
			c = new Configuration();
		}
		
		c.setReportProgress(0);
		tx.commit();

		
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
//		now.set(Calendar.YEAR, 2014);
//		now.set(Calendar.MONTH, 1);
//		now.set(Calendar.DAY_OF_MONTH, 5);
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
		Iterator<DaoObject> cusIt = customers.iterator();
		int i = 0;
		int cusNum = 0;
		boolean flatrateCalc = false;

		while (cusIt.hasNext()) {
			cusNum++;
			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(2013, Calendar.JANUARY, 1, 0, 0, 0);
			calcMonth.set(Calendar.MILLISECOND, 0);
			
			
//			System.out.println(calcMonth.get(Calendar.MONTH));

			Customer customer = (Customer) cusIt.next();
			logger.info("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());

			if (customer.getCustomernumber().equals("20124")) {
				System.out.println("Jetzt");
			}
			
			Calendar maxCalcDate = getMaxCalcDate(customer.getInvoiceConfiguration().getCreationFrequency(), now);
			
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY)) {
				System.out.println("YEARLY CUSTOMER ****************; SPEZIALBEHANDLUNG *************");
				System.out.println("Customer: " + customer.getCustomernumber() + ", " + customer.getName());
			}
			
			if (customer.getCustomernumber().equals("20057")) {
				System.out.println("Jetzt");
			}
			
			while (calcMonth.before(maxCalcDate)) {
				session = SessionFactoryUtil.getInstance().getCurrentSession();
				tx = createTransaction(session);
				
				List<DaoObject> customerCards = searchCards(customer, calcMonth, tx, session, flatrateCalc);

				if (customerCards != null && customerCards.size() > 0) {
					i++;

					boolean generatedWithoutErrors = generateReport(customer, customerCards, calcMonth, false);
					if (!generatedWithoutErrors)
						break;
					
					if (customer.getCustomernumber().equals("20243")) {
						// Kunde OTIS - braucht auch eine Flatrate-Rechnung
						customerCards = searchCards(customer, calcMonth, tx, session, true);
						generatedWithoutErrors = generateReport(customer, customerCards, calcMonth, true);
						if (!generatedWithoutErrors)
							break;
					}
				}
				

				tx.commit();
				calcMonth = raiseMonth(calcMonth, customer);
				String dfs = "yyyy-MM-dd_HH-mm-ss";
				SimpleDateFormat sd = new SimpleDateFormat(dfs);
				
				System.out.println("CalcMonth: " + sd.format(calcMonth.getTime()));
				System.out.println("maxCalcDate: " + sd.format(maxCalcDate.getTime()));
				System.out.println("-------------------------------------------------");
			}

			Double prog = (new Double(cusNum) / new Double(customers.size())) * 100.0;
			int progress = prog.intValue();
			if (facesSession != null)
				facesSession.setAttribute("reportProgress", prog.intValue());
			
			session = SessionFactoryUtil.getInstance().getCurrentSession();
			tx = session.beginTransaction();

			list = null;
			list = session.createQuery(select).list();
			c = null;
			if (list.size() > 0) {
				c = list.get(0);
			} else {
				c = new Configuration();
			}
			
			c.setReportProgress(prog.intValue());
			c.setCustomer(new Integer(customer.getCustomernumber()));
			c.setLastReportUpdate(System.currentTimeMillis());
			tx.commit();
		}

		System.out.println(i + " Reports wurden erstellt!");
		if (facesSession != null)
			facesSession.removeAttribute("reportProgress");

		return true;
	}

		Calendar maxCalcDate = Calendar.getInstance();
		private Calendar getMaxCalcDate(String creationFrequency, Calendar now) {
		maxCalcDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0);
		maxCalcDate.set(Calendar.MILLISECOND, 0);
		
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY)) {
			// Nothing to be done - Date set to first day of current month
		} else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY)) {
			if (now.get(Calendar.MONTH) < Calendar.APRIL)
				maxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
			else if (now.get(Calendar.MONTH) < Calendar.JULY)
				maxCalcDate.set(Calendar.MONTH, Calendar.APRIL);
			else if (now.get(Calendar.MONTH) < Calendar.OCTOBER)
				maxCalcDate.set(Calendar.MONTH, Calendar.JULY);
			else
				maxCalcDate.set(Calendar.MONTH, Calendar.OCTOBER);
		} else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY)) {
			if (now.get(Calendar.MONTH) < Calendar.JULY)
				maxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
			else
				maxCalcDate.set(Calendar.MONTH, Calendar.JULY);
		} else {
			/*
			 * Jährliche Rechnung 
			 * darf jederzeit im Jahr erstellt werden; 
			 * Je nach Aktivierungsdatum wird eine bestimmte Anzahl Monate berechnet; Also z.B. 11 Monate für dieses Jahr, wenn die Karte im Februar dieses Jahres aktiviert wurde.
			 * Eine für das Jahr bereits in Rechnung gestellte Karte darf nicht erneut in Rechnung gestellt werden bei späterer erneuter Jahresabrechnungs-Erstellung.
			 * Pro Monat soll dabei eine Rechnung erstellt werden.
			 * Handling bzgl. maxCalcDate ist daher wie bei monatlicher Rechnungsstellung.
			 * Es muss also nichts getan werden.
			 */
		}
		
		return maxCalcDate;
	}
		
	private boolean generateReport(Customer customer, List<DaoObject> customerCards, Calendar calcMonth, boolean flatrateCalc) {
		IReportGenerator rp = null;
		rp = new ReportGenerator_portrait();

		boolean generatedWithoutError = rp.generateReport(customerCards, customer, calcMonth, flatrateCalc);
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
				Iterator<DaoObject> cIt = customerCards.iterator();
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
		} else {
			// Aus der Schleife für diesen Kunden aussteigen
			// Folgemonate dürfen nicht mehr berechnet werden
			// Mit nächstem Kunde weitermachen
			logger.error("Fehler bei Report-Erstellung für Kunde: " + customer.getCustomernumber() + ", Monat: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR));
			return false;
		}
		
		return true;
	}

	public int getProgress() {
		if (new FacesUtil().getSession().getAttribute("reportProgress") != null) {
			System.out.println("" + new FacesUtil().getSession().getAttribute("reportProgress") + " %");
			return (Integer) new FacesUtil().getSession().getAttribute("reportProgress");
		} else {
			System.out.println("-1 %");
			return -1;
		}
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
				// Jahreskunden bekommen monatlich eine inkrementelle Rechnung
				calcMonth.add(Calendar.MONTH, 1);
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
//		String select = "select distinct customer from Customer customer where customer.customernumber IN (" +
//				"'20166', '20120', '20016', '20190', '20074', '20208', '20206', '20216', '20198'" +
//				", '20200', '20039', '20128', '20157', '20076', '20224'" +
//				", '20012', '20105', '20197', '20060', '20218', '20066', '20209', '20107', '20094'" +
//				", '20069', '20112', '20201', '20079', '20098', '20165', '20214'" +
//				")";

		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = createTransaction(session);
		List<DaoObject> customerList = searchObjects(select, tx, session);
		tx.commit();
		return customerList;
	}

	private List<DaoObject> searchCards(Customer customer, Calendar calcMonth,Transaction tx,
			Session session, boolean flatrateCalc) {
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
			if (flatrateCalc) {
				select = "select distinct card from CardBean card where card.customer = '"
						+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and card.flatrateCard IS TRUE";
				if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY)) {
					select = "select distinct card from CardBean card where card.customer = '"
							+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationYear IS NULL or card.lastCalculationYear < '" + maxLastCalculationDate.get(Calendar.YEAR) + "') and card.flatrateCard IS TRUE";
				}
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

	private Transaction createTransaction(Session session) {
		Transaction tx = null;
		tx = session.beginTransaction();
		return tx;
	}

	@SuppressWarnings("unchecked")
	private List<DaoObject> searchObjects(String select, Transaction tx,
			Session session) {
		List<DaoObject> objects = null;
		objects = (List<DaoObject>) session.createQuery(select).list();
		return objects;
	}

	@Override
	public void run() {
		this.calculate();
	}

	public void startTask(ActionEvent event) {
		thread = new Thread(this);
		thread.start();
		ProgressBarTaskManager threadBean = (ProgressBarTaskManager) FacesUtil.getManagedBean(ProgressBarTaskManager.BEAN_NAME);
		threadBean.startThread(10, 10, 100);
	}
	
	
	public boolean getTaskRunning() {
		return taskRunning;
	}

	public void setTaskRunning(boolean taskRunning) {
		this.taskRunning = taskRunning;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}
	
	public class DateComparator implements java.util.Comparator<DaoObject> {
	      public int compare(DaoObject dao1, DaoObject dao2) {
	    	  CardBean card1 = (CardBean) dao1;
	    	  CardBean card2 = (CardBean) dao2;
	           return (card1.getActivationDate()==card2.getActivationDate() ? 0 : card1.getActivationDate().compareTo(card2.getActivationDate())); 
	      }
	}

	
}
