package de.abd.mda.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
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
			
			
//			System.out.println(calcMonth.get(Calendar.MONTH));

			Customer customer = (Customer) cusIt.next();
			logger.info("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());

			while (calcMonth.before(now) || calcMonth.equals(now)) {
				session = SessionFactoryUtil.getInstance().getCurrentSession();
				tx = createTransaction(session);
				
				List<DaoObject> customerCards = searchCards(customer, calcMonth, tx, session);

				Calendar cal = Calendar.getInstance();
				cal.set(2012, 8, 1);
				
				if (customer.getCustomernumber().equals("20190") && calcMonth.after(cal)) {
					System.out.println(sdf.format(calcMonth.getTime()));
					
				}
				
				if (customerCards != null && customerCards.size() > 0) {
					i++;
					IReportGenerator rp = null;
					if (customer.getInvoiceConfiguration().getFormat().equals(Model.FORMAT_QUERFORMAT)) {
						rp = new ReportGenerator_landscape();
					} else {
						rp = new ReportGenerator_portrait();
					}

					boolean generatedWithoutError = rp.generateReport(customerCards, customer, calcMonth);
					if (generatedWithoutError) {
						Iterator<DaoObject> cIt = customerCards.iterator();
						while (cIt.hasNext()) {
							CardBean card = (CardBean) cIt.next();
							card.setLastCalculationDate(calcMonth.getTime());
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
//		String select = "select distinct customer from Customer customer where customer.customernumber != ''";
		String select = "select distinct customer from Customer customer where customer.customernumber IN (" +
//		String select = "select distinct customer from Customer customer where customer.customernumber IN ('20190')";
				"'20166', '20120', '20016', '20190', '20074', '20208', '20206', '20216', '20198'" +
				", '20200', '20039', '20128', '20157', '20076', '20224'" +
				", '20012', '20105', '20197', '20060', '20218', '20066', '20209', '20107', '20094'" +
				", '20069', '20112', '20201', '20079', '20098', '20165', '20214'" +
				")";

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
		String select = "select distinct card from CardBean card where card.customer = '"
				+ customer.getId() + "' and card.status = 'Aktiv' and card.activationDate < '" + sdf.format(maxActivationDate.getTime()) + "' and (card.lastCalculationDate IS NULL or card.lastCalculationDate < '" + sdf.format(maxLastCalculationDate.getTime()) + "')";
		
		return searchObjects(select, tx, session);
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
	
}
