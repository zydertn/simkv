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

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
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
		now.set(Calendar.YEAR, 2014);
		now.set(Calendar.MONTH, 0);
		now.set(Calendar.DAY_OF_MONTH, 5);
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

		while (cusIt.hasNext()) {
			cusNum++;
			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
			calcMonth.set(Calendar.MILLISECOND, 0);
			
			
//			System.out.println(calcMonth.get(Calendar.MONTH));

			Customer customer = (Customer) cusIt.next();
			logger.info("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());

			if (customer.getCustomernumber().equals("20147")) {
				System.out.println("Jetzt");
			} else {
				System.out.println(customer.getCustomernumber() + ", " + customer.getName());
				continue;
			}
			
			Calendar maxCalcDate = getMaxCalcDate(customer.getInvoiceConfiguration().getCreationFrequency(), now);
			
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY)) {
				System.out.println("YEARLY CUSTOMER ****************; SPEZIALBEHANDLUNG *************");
				System.out.println("Customer: " + customer.getCustomernumber() + ", " + customer.getName());
				continue;
			}
			
			while (calcMonth.before(maxCalcDate)) {
				session = SessionFactoryUtil.getInstance().getCurrentSession();
				tx = createTransaction(session);
				
				List<DaoObject> customerCards = searchCards(customer, calcMonth, tx, session);

				if (customerCards != null && customerCards.size() > 0) {
					i++;
					IReportGenerator rp = null;
//					if (customer.getInvoiceConfiguration().getFormat().equals(Model.FORMAT_QUERFORMAT)) {
//						rp = new ReportGenerator_landscape();
//					} else {
						rp = new ReportGenerator_portrait();
//					}

					boolean generatedWithoutError = rp.generateReport(customerCards, customer, calcMonth);
					if (generatedWithoutError) {
//						Iterator<DaoObject> cIt = customerCards.iterator();
//						while (cIt.hasNext()) {
//							CardBean card = (CardBean) cIt.next();
//							card.setLastCalculationDate(calcMonth.getTime());
//						}
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

	private Calendar getMaxCalcDate(String creationFrequency, Calendar now) {
		Calendar maxCalcDate = Calendar.getInstance();
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
			// Jährliche Rechnung - darf aktuell auch erst abgerechnet werden, wenn das Jahr zuende ist
			maxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
		}
		
		return maxCalcDate;
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
			Session session) {
		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate = addMonthsToMaxActivationDate(customer.getInvoiceConfiguration().getCreationFrequency(), maxActivationDate);
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String select = "select distinct card from CardBean card where card.customer = '"
				+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (customer.lastCalculationDate IS NULL or customer.lastCalculationDate < '" + sdf.format(maxLastCalculationDate.getTime()) + "')";
		
		DateComparator dc = new DateComparator();
		List<DaoObject> cardList = searchObjects(select, tx, session);
		Collections.sort(cardList, dc);
		return cardList;
	}

	private Calendar addMonthsToMaxActivationDate(String creationFrequency, Calendar maxActivationDate) {
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY))
			maxActivationDate.add(Calendar.MONTH, 1);
		else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY))
			maxActivationDate.add(Calendar.MONTH, 3);
		else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY))
			maxActivationDate.add(Calendar.MONTH, 6);
		else
			maxActivationDate.add(Calendar.MONTH, 12);
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
