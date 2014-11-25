package de.abd.mda.report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.component.outputresource.OutputResource;
import com.icesoft.faces.component.outputresource.OutputResourceTag;
import com.icesoft.faces.context.Resource;
import com.icesoft.faces.context.effects.JavascriptContext;

import de.abd.mda.controller.ActionController;
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CardComparator;
import de.abd.mda.util.CustomerComparator;
import de.abd.mda.util.CustomerNumberComparator;
import de.abd.mda.util.DateUtils;
import de.abd.mda.util.FacesUtil;

public class ReportCalculator extends ActionController implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(ReportCalculator.class .getName()); 

	HttpSession facesSession;
	private boolean taskRunning = true;
	Thread thread;
	private static int CALC_CASE_MONTH = 1;
	private static int CALC_CASE_FULL = 2;
	private static int CALC_CASE_SINGLE = 3;
	private int calculateCase = -1;
	private HtmlSelectOneMenu monthRunMonthBinding;
	private HtmlSelectOneMenu singleInvMonthBinding;
	private HtmlSelectOneMenu monthRunYearBinding;
	private HtmlSelectOneMenu singleInvYearBinding;
	private int monthRunMonth;
	private int singleInvMonth;
	private int monthRunYear;
	private int singleInvYear;
	private String path;
	private boolean monthCalcStarted = false;
	private OutputResource outputLinkBinding;
	private OutputResource outputLinkPdfBinding;
	private String customerNumber;
	private String pdfPath = "";
	private String zipPath = "";
	private List<Customer> customerList;
	private Date reportDateMonthRun;
	private Date reportDateSingleInv;
	private String reportNumber;
	private Calendar cal;
		
	public static final Resource ZIP_RESOURCE = new MyResource("Siwaltec_Rechnungen.zip", "zip");
	public static final Resource PDF_RESOURCE = new MyResource("", "pdf");
//	public static final Resource ZIP_RESOURCE = new MyResource("C://temp//Invoices//Siwaltec_Rechnungen.zip");
	
	
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	
	public ReportCalculator() {
		LOGGER.info("Instantiate ReportCalculator");
		Model model = new Model();
		model.createModel();
		pdfPath = model.getPdfPath();
		zipPath = model.getZipPath();
		LOGGER.info("pdfPath = " + pdfPath + ", zipPath = " + zipPath);
		cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		
		monthRunMonth = cal.get(Calendar.MONTH);
		monthRunYear = cal.get(Calendar.YEAR);
		CustomerController cc = new CustomerController();
		List<DaoObject> daoList = cc.listObjects();

		customerList = new ArrayList<Customer>();
		for (DaoObject d: daoList) {
			customerList.add((Customer) d);
		}
		
		CustomerNumberComparator cusComp = new CustomerNumberComparator();
		Collections.sort(customerList, cusComp);
	}

	public boolean calculate() {
		LOGGER.info("Method: calculate");
		if (facesSession != null
				&& facesSession.getAttribute("reportProgress") != null) {
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
//		 now.set(Calendar.YEAR, 2015);
//		 now.set(Calendar.MONTH, 1);
//		 now.set(Calendar.DAY_OF_MONTH, 5);
		// Um den Fehlerfall auszuschließen, wenn jemand exakt am 1. eines
		// Monats um 0 Uhr auf den Knopf drückt (damit dieser Monat auch
		// berechnet wird):
		now.set(Calendar.SECOND, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		LOGGER.info("Reporterstellungslauf gestartet, "
				+ sdf.format(now.getTime()));

		
		/***** 1. Alle Kunden ermitteln *****/
		List<DaoObject> customers = searchCustomers();
		if (customers != null) {
			LOGGER.info("Anzahl gefundene Kunden = " + customers.size());
		} else {
			return false;
		}

		/***** 2. Für jeden Kunden die Karten ermitteln *****/
		Iterator<DaoObject> cusIt = customers.iterator();
		int reportCount = 0;
		int cusNum = 0;
		
		while (cusIt.hasNext()) {
			cusNum++;

			Customer customer = (Customer) cusIt.next();
			LOGGER.info("Aktueller Kunde: " + customer.getListString()
					+ ", Kundennummer: " + customer.getCustomernumber());

			/**
			 * Bis hierhin bleibt alles gleich für alle
			 */

			switch (calculateCase) {
			case 1:
				/* MONTH CALCULATION */
				LOGGER.info("MONTH CALCULATION");
				monthCalcStarted = true;

				if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_QUARTERLY)) {
					if (((cal.get(Calendar.MONTH) + 1) % 3) != 0) {
						/*
						 *  Quartalskunden dürfen nur dann beim Monatslauf berücksichtigt werden, 
						 *  wenn mit diesem Monat das Quartal beginnt.
						 */
						LOGGER.info("Customer " + customer.getCustomernumber() + " is 1/4 year customer. Not calculated in this month.");
						break;
					}
				}
				
				if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_HALFYEARLY)) {
					if (((cal.get(Calendar.MONTH) + 1) % 6) != 0) {
						/*
						 *  Halbjahreskunden dürfen nur dann beim Monatslauf berücksichtigt werden, 
						 *  wenn mit diesem Monat das Halbjahr beginnt.
						 */
						LOGGER.info("Customer " + customer.getCustomernumber() + " is 1/2 year customer. Not calculated in this month.");
						break;
					}
				}
				
				reportCount = monthCalculation(customer, reportCount, this.monthRunMonth, this.monthRunYear, this.reportDateMonthRun);
				break;
			case 2:
				/* FULL CALCULATION */
				LOGGER.info("FULL CALCULATION");
				reportCount = fullCalculation(customer, now, reportCount, this.reportDateMonthRun);
				break;
			}

			Double prog = (new Double(cusNum) / new Double(customers.size())) * 100.0;
			int progress = prog.intValue();
			if (facesSession != null)
				facesSession.setAttribute("reportProgress", prog.intValue());
			LOGGER.info("Report progess = " + prog.intValue() + "%");
			
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

		if (monthCalcStarted) {
			Calendar calcMonth = Calendar.getInstance();
			calcMonth.set(Calendar.MONTH, this.monthRunMonth);
			calcMonth.set(Calendar.YEAR, this.monthRunYear);
			downloadMonthBills(calcMonth);
			outputLinkBinding.setResource(new MyResource("Siwaltec_Rechnungen.zip", "zip"));
			outputLinkBinding.setFileName("Siwaltec_Rechnungen.zip");
		}
		
		LOGGER.info(reportCount + " Reports wurden erstellt!");
		if (facesSession != null)
			facesSession.removeAttribute("reportProgress");

		return true;
	}

	private boolean downloadPdfFile(Customer customer, Calendar calcMonth) {
		LOGGER.info("Method: downloadPdfFile; Customernumber = " + customer.getCustomernumber() + ", year = " + calcMonth.get(Calendar.YEAR) + ", month = " + calcMonth.get(Calendar.MONTH));
		Bill bill = new Bill();
		bill.setCustomerNumber(new Integer(customer.getCustomernumber()));
		bill.setYear(calcMonth.get(Calendar.YEAR));
		bill.setMonth(calcMonth.get(Calendar.MONTH));
		bill.setMapCount(1);
		BillController bc = new BillController();
		Bill dbBill = bc.findBill(bill);
		if (dbBill != null) {
			LOGGER.info("found Bill");
			File file = new File(pdfPath);
			if (!file.exists()) {
				LOGGER.info("Creating directory: " + file.getPath());
				boolean result = file.mkdirs();
				
				if (result) {
					LOGGER.info("DIR " + file.getPath() + " created!");
				}
			}
			
			file = new File(pdfPath + dbBill.getFilename());
			LOGGER.info("Download file: " + file.getPath());

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();

			String filename = "Test.pdf";

			externalContext.responseReset();
			externalContext.setResponseContentType("application/pdf");
			externalContext.setResponseHeader("Content-Disposition",
					"attachment; filename=\"" + filename + "\"");

			try {
				OutputStream output = externalContext.getResponseOutputStream();

				output.write(dbBill.getFile());

				output.flush();
				output.close();

				facesContext.responseComplete();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			
			
/*			try {
				FileOutputStream fos2 = new FileOutputStream(file);
				fos2.write(dbBill.getFile());
				fos2.flush();
				fos2.close();

								
				outputLinkPdfBinding.setResource(new MyResource(dbBill.getFilename(), "pdf"));
				outputLinkPdfBinding.setFileName(dbBill.getFilename());
				outputLinkPdfBinding.setRendered(true);
				
//				getRequest().setAttribute("message", "Keine Karte in der Datenbank gefunden!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/		}
		return true;
	}

	private void download(List<String> csvLines, int month, int year) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();

		String filename = "Rechnungen_"+year+"_"+(month+1)+".csv";

		externalContext.responseReset();
		externalContext.setResponseContentType("text/comma-separated-values");
		externalContext.setResponseHeader("Content-Disposition",
				"attachment; filename=\"" + filename + "\"");

		try {
			OutputStream output = externalContext.getResponseOutputStream();

			for (String s : csvLines) {
				output.write(s.getBytes());
			}

			output.flush();
			output.close();

			facesContext.responseComplete();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
	public int monthCalc(Customer customer, int reportCount, int month, int year) {
		return monthCalculation(customer, reportCount, month, year, null);
	}
	
	private int monthCalculation(Customer customer, int reportCount, int month, int year, Date calcDate) {
		LOGGER.info("MONTH CALCULATION for customer " + customer.getCustomernumber() + ", reportCount = " + reportCount + ", year = " + year + ", month = " + month);
		
		Calendar calcMonth = Calendar.getInstance();
		calcMonth.set(Calendar.MILLISECOND, 0);
		calcMonth.set(Calendar.SECOND, 0);
		calcMonth.set(Calendar.MINUTE, 0);
		calcMonth.set(Calendar.HOUR, 0);
		calcMonth.set(Calendar.DATE, 1);
		calcMonth.set(Calendar.MONTH, month);
		calcMonth.set(Calendar.YEAR, year);

		System.out.println("CalcMonth = " + DateUtils.getCalendarString(calcMonth));
		
		reportCount = createReport(calcMonth, customer, reportCount, CALC_CASE_MONTH, calcDate);
		
		return reportCount;
	}

	private int fullCalculation(Customer customer, Calendar now, int reportCount, Date calcDate) {
		LOGGER.info("Method: fullCalculation for customer " + customer.getCustomernumber());
		Calendar calcMonth = Calendar.getInstance();
		calcMonth.set(2013, Calendar.JANUARY, 1, 0, 0, 0);
		calcMonth.set(Calendar.MILLISECOND, 0);

		Calendar maxCalcDate = getMaxCalcDate(customer
				.getInvoiceConfiguration().getCreationFrequency(), now);

		while (calcMonth.before(maxCalcDate)) {
			String dfs = "yyyy-MM-dd_HH-mm-ss";
			SimpleDateFormat sd = new SimpleDateFormat(dfs);
			LOGGER.info("CalcMonth: " + sd.format(calcMonth.getTime()));
			reportCount = createReport(calcMonth, customer, reportCount, CALC_CASE_FULL, calcDate);
			calcMonth = raiseMonth(calcMonth, customer);
		}
		
		return reportCount;		
	}

	private int createReport(Calendar calcMonth, Customer customer, int reportCount, int calcCase, Date calcDate) {
		LOGGER.info("Method: createReport for customer " + customer.getCustomernumber() + ", reportCount = " + reportCount);
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = createTransaction(session);

		List<DaoObject> customerCards = searchCards(customer, calcMonth, tx, session, false, calcCase);

		if (customerCards != null && customerCards.size() > 0) {
			LOGGER.info(customerCards.size() + " Cards found");
			HashMap<Integer, List<DaoObject>> separateBillingSortedCards = sortCards(
					customer, customerCards);

//			int mapCountSize = separateBillingSortedCards.keySet().size();
			int mapCount = 1;
			for (List<DaoObject> cusCards : separateBillingSortedCards.values()) {
				// SORTING
				if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ACTIVATION_DATE) {
					LOGGER.info("Card sorting: By activation date");
					CardComparator cc = new CardComparator();
					Collections.sort(cusCards, cc);
				} else if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ALPHABETICAL) {
					LOGGER.info("Card sorting: Alphabetical (by install address)");
					AlphabeticalComparator ac = new AlphabeticalComparator();
					Collections.sort(cusCards, ac);
				} else {
					LOGGER.warn("Customer has no sorting option configured. Cards will be sorted by activation date!");
					DateComparator dc = new DateComparator();
					Collections.sort(cusCards, dc);
				}

				long time1 = System.currentTimeMillis();
				boolean separateBilling = false;
				if (customer.getInvoiceConfiguration() != null && customer.getInvoiceConfiguration().getSeparateBilling() != null && customer.getInvoiceConfiguration().getSeparateBilling()) {
					separateBilling = true;
				}
				boolean generatedWithoutErrors = generateReport(customer, cusCards, calcMonth, false, separateBilling, mapCount, calcDate);
				long time2 = System.currentTimeMillis();
				long diff = time2-time1;
				LOGGER.info("GenerateReport Dauer = " + diff);
				if (!generatedWithoutErrors)
					break;
				else
					reportCount++;
				
				mapCount++;
			}
		}

//		tx.commit();
		
		return reportCount;
	}

	/*
	 * Alle Karten, die gleiche Rechnungskriterien haben, in einer gemeinsamen Liste erfassen
	 * Key = Summe der Hashwerte der Strings der Rechnungskriterien
	 */
	private HashMap<Integer, List<DaoObject>> sortCards(Customer customer,
			List<DaoObject> customerCards) {
		LOGGER.info("Method: sortCards");
		long time1 = System.currentTimeMillis();
		HashMap<Integer, List<DaoObject>> cusCardsSeparateBilling = new HashMap<Integer, List<DaoObject>>();
		if (customer.getInvoiceConfiguration().getSeparateBilling() != null && customer.getInvoiceConfiguration().getSeparateBilling()) {
			String[] sbCrit = customer.getInvoiceConfiguration()
					.getSeparateBillingCriteria();

			for (DaoObject dao : customerCards) {
				CardBean card = (CardBean) dao;
				int hashVal = 0;

				for (int ind = 0; ind < sbCrit.length; ind++) {
					String sbc = sbCrit[ind];
					if (sbc.equals(Model.BILLING_WE_NR)) {
						hashVal += card.getWe().hashCode();
					} else if (sbc.equals(Model.BILLING_BESTELL_NR)) {
						hashVal += card.getBestellNummer().hashCode();
					}
				}

				if (cusCardsSeparateBilling.containsKey(hashVal)) {
					cusCardsSeparateBilling.get(hashVal).add(card);
				} else {
					List cards = new ArrayList<DaoObject>();
					cards.add(card);
					cusCardsSeparateBilling.put(hashVal, cards);
				}

			}
		} else {
			
			cusCardsSeparateBilling.put(0, customerCards);
		}

		long time2 = System.currentTimeMillis();
		long diff = time2-time1;
		LOGGER.info("Sort Dauer: " + diff + " Millisekunden");
		return cusCardsSeparateBilling;
	}

	Calendar maxCalcDate = Calendar.getInstance();

	private Calendar getMaxCalcDate(String creationFrequency, Calendar now) {
		LOGGER.info("Method: getMaxCalcDate");
		maxCalcDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0,
				0, 0);
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
			 * Jährliche Rechnung darf jederzeit im Jahr erstellt werden; Je
			 * nach Aktivierungsdatum wird eine bestimmte Anzahl Monate
			 * berechnet; Also z.B. 11 Monate für dieses Jahr, wenn die Karte im
			 * Februar dieses Jahres aktiviert wurde. Eine für das Jahr bereits
			 * in Rechnung gestellte Karte darf nicht erneut in Rechnung
			 * gestellt werden bei späterer erneuter
			 * Jahresabrechnungs-Erstellung. Pro Monat soll dabei eine Rechnung
			 * erstellt werden. Handling bzgl. maxCalcDate ist daher wie bei
			 * monatlicher Rechnungsstellung. Es muss also nichts getan werden.
			 */
		}
		
		LOGGER.info("MaxCalcDate = " + DateUtils.getCalendarString(maxCalcDate)); 
		return maxCalcDate;
	}

	private boolean generateReport(Customer customer,
			List<DaoObject> customerCards, Calendar calcMonth,
			boolean flatrateCalc, boolean severalBills, int mapCount, Date calcDate) {
		LOGGER.info("Method: generateReport for customer " + customer.getCustomernumber() + ", calcMonth = " + DateUtils.getCalendarString(calcMonth) + ", Cards = " + customerCards.size());
		IReportGenerator rp = null;
		rp = new ReportGenerator_portrait();

		long time1 = System.currentTimeMillis();
		boolean generatedWithoutError = rp.generateReport(customerCards,
				customer, calcMonth, flatrateCalc, severalBills, mapCount, calcDate, reportNumber);
		long time2 = System.currentTimeMillis();
		long diff = time2 - time1;
		LOGGER.info("Inner generateReport Dauer = "+ diff);
		if (generatedWithoutError) {
			if (customer.getInvoiceConfiguration().getCreationFrequency()
					.equals(Model.FREQUENCY_YEARLY)) {
				/*
				 * Bei Jahreskunden muss das LastCalculationDate auf den Karten
				 * gespeichert werden. Jahresrechnungen werden jeden Monat
				 * erstellt mit den Karten, die seit der letzten
				 * Rechnungserstellung (normalerweise im letzten Monat)
				 * hinzugekommen sind. Es werden nur Rechnungen für ganze Monate
				 * erstellt - das heißt, wenn am 5. eines Monats ein
				 * Rechnungslauf stattfindet, dann werden die Karten, die zw. 1.
				 * und 5. dieses Monats aktiviert wurden, noch nicht beachtet -
				 * erst im Folgemonat. Eigentlich ist nur das Jahr relevant,
				 * aber historisch bedingt wird das ganze Datum gespeichert.
				 * Wenn eine Karte in einem Jahr schon einmal in Rechnung
				 * gestellt wurde (für das ganze Jahr ab dem Monat der
				 * Aktivierung), dann muss die Karte erst im Folgejahr wieder in
				 * Rechnung gestellt werden.
				 */
				Iterator<DaoObject> cIt = customerCards.iterator();
				while (cIt.hasNext()) {
					CardBean card = (CardBean) cIt.next();
					card.setLastCalculationYear(calcMonth.get(Calendar.YEAR));
					card.setLastCalculationMonth(calcMonth.get(Calendar.MONTH));
				}
			} else {
				/*
				 * Bei allen anderen Rechnungsläufen muss nur pro Kunde das
				 * LastCalculationDate gesetzt werden.
				 */
				
				long time3 = System.currentTimeMillis();
				CustomerController cc = new CustomerController();
				Customer dbCus = cc.findCustomer(customer.getCustomernumber());
				dbCus.setLastCalculationDate(calcMonth.getTime());
				long time4 = System.currentTimeMillis();
				long diff2 = time4 - time3;
				LOGGER.info("Inner generateReport Dauer = "+ diff2);

			}
		} else {
			// Aus der Schleife für diesen Kunden aussteigen
			// Folgemonate dürfen nicht mehr berechnet werden
			// Mit nächstem Kunde weitermachen
			LOGGER.error("Fehler bei Report-Erstellung für Kunde: "
					+ customer.getCustomernumber() + ", Monat: "
					+ DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH))
					+ " " + calcMonth.get(Calendar.YEAR));
			return false;
		}

		return true;
	}

	public int getProgress() {
		if (new FacesUtil().getSession().getAttribute("reportProgress") != null) {
			System.out.println(""
					+ new FacesUtil().getSession().getAttribute(
							"reportProgress") + " %");
			return (Integer) new FacesUtil().getSession().getAttribute(
					"reportProgress");
		} else {
			System.out.println("-1 %");
			return -1;
		}
	}

	private Calendar raiseMonth(Calendar calcMonth, Customer customer) {
		String creationFrequency = null;
		if (customer.getInvoiceConfiguration() != null) {
			creationFrequency = customer.getInvoiceConfiguration()
					.getCreationFrequency();
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
			LOGGER.warn("No CreationFrequency set for customer "
					+ customer.getCustomernumber() + "; Creating monthly!");
			calcMonth.add(Calendar.MONTH, 1);
		}

		return calcMonth;
	}

	private List<DaoObject> searchCustomers() {
		LOGGER.info("Method: searchCustomers");
		String select = "select distinct customer from Customer customer where customer.customernumber != ''";
		LOGGER.info("Select = " + select);
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = createTransaction(session);
		List<DaoObject> customerList = searchObjects(select, tx, session);
		if (customerList != null) {
			LOGGER.info(customerList.size() + " customers found");
		} else {
			LOGGER.warn("No customers found!");
		}
		tx.commit();
		return customerList;
	}

	private List<DaoObject> searchCards(Customer customer, Calendar calcMonth, Transaction tx, Session session, boolean flatrateCalc, int calcCase) {
		LOGGER.info("Method: searchCards");
		Calendar maxActivationDate = Calendar.getInstance();
		maxActivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxActivationDate = getMaxActivationDate(customer.getInvoiceConfiguration().getCreationFrequency(),	maxActivationDate);
		Calendar maxDeactivationDate = maxActivationDate;
		Calendar minDeactivationDate = Calendar.getInstance();
		minDeactivationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		minDeactivationDate = getMinDeactivationDate(customer.getInvoiceConfiguration().getCreationFrequency(), minDeactivationDate);
		
		Calendar maxLastCalculationDate = Calendar.getInstance();
		maxLastCalculationDate.set(new Integer(calcMonth.get(Calendar.YEAR)), new Integer(calcMonth.get(Calendar.MONTH)), 1, 0, 0, 0);
		maxLastCalculationDate.set(Calendar.MILLISECOND, 0);
		Calendar customerLastCalcDate = Calendar.getInstance();
		if (customer.getLastCalculationDate() != null) {
			customerLastCalcDate.setTime(customer.getLastCalculationDate());
		}

		switch (calcCase) {
		// MONTH CALCULATION
		case 1:
			System.out.println("maxActDat = " + DateUtils.getCalendarString(maxActivationDate) + "; minDeactivationDate == " + DateUtils.getCalendarString(minDeactivationDate) + "; maxDeactivationDate == " + DateUtils.getCalendarString(maxDeactivationDate));
			return searchMonth(customer, calcMonth, maxActivationDate, minDeactivationDate, maxDeactivationDate, flatrateCalc, tx, session);
		
		// FULL CALCULATION
		case 2:
		return searchFull(customer, customerLastCalcDate, maxLastCalculationDate, maxActivationDate, flatrateCalc, tx, session);
			
		}
		
		return null;
	}

	private List<DaoObject> searchMonth(Customer customer, Calendar calcMonth, Calendar maxActivationDate, Calendar minDeactivationDate, Calendar maxDeactivationDate, boolean flatrateCalc, Transaction tx, Session session) {
		LOGGER.info("Method: searchMonth");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String select = "select distinct card from CardBean card where card.customer = '" + customer.getId()
					+ "' and ((card.status = 'aktiv' and card.activationDate < '"	+ sdf.format(maxActivationDate.getTime()) + "') or (" +
							"card.status = 'Inaktiv' and (card.deactivationDate >= '" + sdf.format(minDeactivationDate.getTime()) +"' and card.deactivationDate < '" + sdf.format(maxDeactivationDate.getTime()) + "')))";
			if (customer.getInvoiceConfiguration().getCreationFrequency()
					.equals(Model.FREQUENCY_YEARLY)) {
				LOGGER.info("YEARLY Calculation for customer " + customer.getCustomernumber());
	
				select = "select distinct card from CardBean card where card.customer = '" + customer.getId()
						+ "' and card.status = 'Aktiv' and card.activationDate < '"	+ sdf.format(maxActivationDate.getTime())
						+ "' and ((YEAR(card.activationDate) = '" + maxActivationDate.get(Calendar.YEAR) + "' and MONTH(card.activationDate) = '" + maxActivationDate.get(Calendar.MONTH) + "')" 
						+ "or (card.lastCalculationYear != null and card.lastCalculationYear < '"	+ calcMonth.get(Calendar.YEAR) + "' and card.lastCalculationYear > '1999"
						+ "'))";
			} else {
				LOGGER.info("MONTHLY calculation for customer " + customer.getCustomernumber());
			}
			LOGGER.info("Select = " + select);
			List<DaoObject> cardList = searchObjects(select, tx, session);
			if (cardList != null) {
				LOGGER.info(cardList.size() + " cards found");
			} else {
				LOGGER.warn("No cards found!");
			}

			// SORTING
			if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ACTIVATION_DATE) {
				LOGGER.info("Card sorting: By activation date");
				CardComparator cc = new CardComparator();
				Collections.sort(cardList, cc);
			} else if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ALPHABETICAL) {
				LOGGER.info("Card sorting: Alphabetical (by install address)");
				AlphabeticalComparator ac = new AlphabeticalComparator();
				Collections.sort(cardList, ac);
			} else {
				LOGGER.warn("Customer has no sorting option configured. Cards will be sorted by activation date!");
				DateComparator dc = new DateComparator();
				Collections.sort(cardList, dc);
			}
			return cardList;
	}

	
	private List<DaoObject> searchFull(Customer customer, Calendar customerLastCalcDate, Calendar maxLastCalculationDate, Calendar maxActivationDate, boolean flatrateCalc, Transaction tx, Session session) {
		LOGGER.info("Method: searchFull");
		if (customer.getLastCalculationDate() == null
				|| customerLastCalcDate.before(maxLastCalculationDate)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String select = "select distinct card from CardBean card where card.customer = '" + customer.getId()
					+ "' and card.status = 'aktiv' and card.activationDate < '"	+ sdf.format(maxActivationDate.getTime())
					+ "' and NOT card.flatrateCard IS TRUE";
			if (customer.getInvoiceConfiguration().getCreationFrequency()
					.equals(Model.FREQUENCY_YEARLY)) {
				LOGGER.info("YEARLY calculation for customer " + customer.getCustomernumber());
				select = "select distinct card from CardBean card where card.customer = '" + customer.getId()
						+ "' and card.status = 'Aktiv' and card.activationDate < '"	+ sdf.format(maxActivationDate.getTime())
						+ "' and ((card.lastCalculationYear IS NULL and YEAR(card.activationDate) = '" + maxActivationDate.get(Calendar.YEAR) + "' and MONTH(card.activationDate) = '" + maxActivationDate.get(Calendar.MONTH) + "')" 
						+ "or (card.lastCalculationYear < '"	+ maxLastCalculationDate.get(Calendar.YEAR)
						+ "')) and NOT card.flatrateCard IS TRUE";
			} else {
				LOGGER.info("MONTHLY calculation for customer " + customer.getCustomernumber());
			}
			if (flatrateCalc) {
				LOGGER.info("FLATRATE calculation");
				select = "select distinct card from CardBean card where card.customer = '"
						+ customer.getId()
						+ "' and card.status = 'Aktiv' and card.activationDate < '"
						+ sdf.format(maxActivationDate.getTime())
						+ "' and card.flatrateCard IS TRUE";
				if (customer.getInvoiceConfiguration().getCreationFrequency()
						.equals(Model.FREQUENCY_YEARLY)) {
					select = "select distinct card from CardBean card where card.customer = '"
							+ customer.getId()
							+ "' and card.status = 'Aktiv' and card.activationDate < '"
							+ sdf.format(maxActivationDate.getTime())
							+ "' and (card.lastCalculationYear IS NULL or card.lastCalculationYear < '"
							+ maxLastCalculationDate.get(Calendar.YEAR)
							+ "') and card.flatrateCard IS TRUE";
				}
			}

			LOGGER.info("Select = " + select);
			List<DaoObject> cardList = searchObjects(select, tx, session);
			if (cardList != null) {
				LOGGER.info(cardList.size() + " cards found");
			} else {
				LOGGER.warn("No cards found!");
			}

			// SORTING
			if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ACTIVATION_DATE) {
				LOGGER.info("Card sorting: By activation date");
				DateComparator dc = new DateComparator();
				Collections.sort(cardList, dc);
			} else if (customer.getInvoiceConfiguration().getSortingOption() == Model.SORTING_ALPHABETICAL) {
				LOGGER.info("Card sorting: Alphabetical (by install address)");
				AlphabeticalComparator ac = new AlphabeticalComparator();
				Collections.sort(cardList, ac);
			} else {
				LOGGER.warn("Customer has no sorting option configured. Cards will be sorted by activation date!");
				DateComparator dc = new DateComparator();
				Collections.sort(cardList, dc);
			}

			return cardList;
		}
		return null;
	}
	
	private Calendar getMaxActivationDate(String creationFrequency,
			Calendar maxActivationDate) {
		LOGGER.info("Method: addMonthsToActivationDate");
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY))
			maxActivationDate.add(Calendar.MONTH, 1);
		else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY))
			maxActivationDate.add(Calendar.MONTH,
					3 - (maxActivationDate.get(Calendar.MONTH) % 3));
		else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY))
			maxActivationDate.add(Calendar.MONTH,
					6 - (maxActivationDate.get(Calendar.MONTH) % 6));
		else {
			// JAHRESKUNDE, zu behandeln wie Monatskunde
			maxActivationDate.add(Calendar.MONTH, 1);
			// maxActivationDate.add(Calendar.YEAR, 1);
			// maxActivationDate.set(Calendar.MONTH, 0);
		}
		return maxActivationDate;
	}

	private Calendar getMinDeactivationDate(String creationFrequency,
			Calendar minDeactivationDate) {
		LOGGER.info("Method: addMonthsToActivationDate");
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY)) {
			// Nothing to be done here
		} else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY))
			minDeactivationDate.add(Calendar.MONTH,
					-minDeactivationDate.get(Calendar.MONTH) % 3);
		else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY))
			minDeactivationDate.add(Calendar.MONTH,
					-minDeactivationDate.get(Calendar.MONTH) % 6);
		else {
			// JAHRESKUNDE, zu behandeln wie Monatskunde
			// Nichts zu tun			
		}
		return minDeactivationDate;
	}

	
	private Transaction createTransaction(Session session) {
		Transaction tx = null;
		tx = session.beginTransaction();
		return tx;
	}

	@SuppressWarnings("unchecked")
	private List<DaoObject> searchObjects(String select, Transaction tx,
			Session session) {
		LOGGER.info("Method: searchObjects");
		List<DaoObject> objects = null;
		objects = (List<DaoObject>) session.createQuery(select).list();
		return objects;
	}

	@Override
	public void run() {
		LOGGER.info("Method: run");
		this.calculate();
	}

	public String startSingleInvTask() {
		LOGGER.info("startSingleInvTask for customer " + customerNumber);
		CustomerController cc = new CustomerController();
		Customer customer = cc.findCustomer(customerNumber);

		int reportCount = 0;
		reportCount = monthCalculation(customer, reportCount, singleInvMonth, singleInvYear, reportDateSingleInv);

		String message = "";
		if (reportCount == 1) {
			message = reportCount + " Report wurde erstellt!";
		}
		
		Calendar calcMonth = Calendar.getInstance();
		calcMonth.set(Calendar.MONTH, singleInvMonth);
		calcMonth.set(Calendar.YEAR, singleInvYear);
		
		if (downloadPdfFile(customer, calcMonth)) {
			message += " File steht zum Download bereit!";
			LOGGER.info(message);
		} else {
			message += " Der Report konnte jedoch nicht lokal zum Download bereitgestellt werden!";
			LOGGER.warn(message);
		}
		
//		getRequest().setAttribute("message", message);
		
		return "";
	}
	
	
	public void startMonthRunTask(ActionEvent event) {
		LOGGER.info("Method: startMonthRunTask");
		calculateCase = CALC_CASE_MONTH;
		startTask(event);
	}

	public void startFullRunTask(ActionEvent event) {
		LOGGER.info("Method: startFullRunTask");
		calculateCase = CALC_CASE_FULL;
		startTask(event);
	}
	
	public void startTask(ActionEvent event) {
		LOGGER.info("Method: startTask");
		thread = new Thread(this);
		thread.start();
		ProgressBarTaskManager threadBean = (ProgressBarTaskManager) FacesUtil
				.getManagedBean(ProgressBarTaskManager.BEAN_NAME);
		threadBean.startThread(10, 10, 100);
	}

	public void downloadMonthBills(Calendar calcMonth) {
		LOGGER.info("Method: downloadMonthBills for month " + DateUtils.getCalendarString(calcMonth));
		BillController bc = new BillController();
		List<Bill> bills = bc.findMonthBills(calcMonth.get(Calendar.YEAR), calcMonth.get(Calendar.MONTH));
		
		if (bills != null) {

			LOGGER.info(bills.size() + " bills found");

			File file = new File(pdfPath);
			if (!file.exists()) {
				LOGGER.info("Creating directory: " + file.getPath());
				boolean result = file.mkdirs();
				
				if (result) {
					LOGGER.info("DIR " + file.getPath() + " created!");
				}
			}

			
			for (Bill bill: bills) {
				file = new File(pdfPath + bill.getFilename());
				LOGGER.info("Writing pdf file " + file.getPath());
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
					fos.write(bill.getFile());
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					LOGGER.error("FileNotFoundException: " + e);
				} catch (IOException e) {
					LOGGER.error("IOException: " + e);
				}
			}

			File zipFile = new File(zipPath);
			if (!zipFile.exists()) {
				LOGGER.info("Creating directory: " + zipFile.getPath());
				boolean result = zipFile.mkdirs();
				
				if (result) {
					System.out.println("DIR " + zipFile.getPath() + " created!");
				}
			}
			zipFile = new File(zipPath,  "Siwaltec_Rechnungen.zip");
			LOGGER.info("Writing ZIP file " + zipFile.getPath());
			
			FileOutputStream zfos;
			try {
				zfos = new FileOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(zfos);
				byte[] buffer = new byte[1024];
				for (Bill bill: bills) {
					ZipEntry ze = new ZipEntry(bill.getFilename());
					zos.putNextEntry(ze);

					File f = new File(pdfPath + bill.getFilename());
					LOGGER.info("Adding file " + f.getPath() + " to ZIP file");
					FileInputStream fis = new FileInputStream(f);
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					
					zos.closeEntry();
					fis.close();
				}
				zos.close();
			} catch (FileNotFoundException e1) {
				LOGGER.error("FileNotFoundException: " + e1);
			} catch (IOException e) {
				LOGGER.error("IOException: " + e);
			}
			
			
//			try {
//				Runtime.getRuntime().exec(
//				"rundll32 url.dll,FileProtocolHandler " + testFilename);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}

	}
	
	
	public Resource getZipResource() {
		return ZIP_RESOURCE;
	}
	
	public Resource getPdfResource() {
		return PDF_RESOURCE;
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
			return (card1.getActivationDate() == card2.getActivationDate() ? 0
					: card1.getActivationDate().compareTo(
							card2.getActivationDate()));
		}
	}

	public class AlphabeticalComparator implements java.util.Comparator<DaoObject> {
		public int compare(DaoObject dao1, DaoObject dao2) {
			CardBean card1 = (CardBean) dao1;
			CardBean card2 = (CardBean) dao2;
			return card1.getInstallAddress().getAddressString().compareTo(card2.getInstallAddress().getAddressString());
		}
	}

	
	public int getCalculateCase() {
		return calculateCase;
	}

	public void setCalculateCase(int calculateCase) {
		this.calculateCase = calculateCase;
	}

	public HtmlSelectOneMenu getMonthRunMonthBinding() {
		return monthRunMonthBinding;
	}

	public void setMonthRunMonthBinding(HtmlSelectOneMenu monthRunMonthBinding) {
		this.monthRunMonthBinding = monthRunMonthBinding;
	}

	public HtmlSelectOneMenu getSingleInvMonthBinding() {
		return singleInvMonthBinding;
	}

	public void setSingleInvMonthBinding(HtmlSelectOneMenu singleInvMonthBinding) {
		this.singleInvMonthBinding = singleInvMonthBinding;
	}

	public HtmlSelectOneMenu getMonthRunYearBinding() {
		return monthRunYearBinding;
	}

	public void setMonthRunYearBinding(HtmlSelectOneMenu monthRunYearBinding) {
		this.monthRunYearBinding = monthRunYearBinding;
	}

	public HtmlSelectOneMenu getSingleInvYearBinding() {
		return singleInvYearBinding;
	}

	public void setSingleInvYearBinding(HtmlSelectOneMenu singleInvYearBinding) {
		this.singleInvYearBinding = singleInvYearBinding;
	}

	public int getMonthRunMonth() {
		return monthRunMonth;
	}

	public void setMonthRunMonth(int monthRunMonth) {
		this.monthRunMonth = monthRunMonth;
	}

	public int getSingleInvMonth() {
		return singleInvMonth;
	}

	public void setSingleInvMonth(int singleInvMonth) {
		this.singleInvMonth = singleInvMonth;
	}

	public int getMonthRunYear() {
		return monthRunYear;
	}

	public void setMonthRunYear(int monthRunYear) {
		this.monthRunYear = monthRunYear;
	}

	public int getSingleInvYear() {
		return singleInvYear;
	}

	public void setSingleInvYear(int singleInvYear) {
		this.singleInvYear = singleInvYear;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isMonthCalcStarted() {
		return monthCalcStarted;
	}

	public void setMonthCalcStarted(boolean monthCalcStarted) {
		this.monthCalcStarted = monthCalcStarted;
	}

	public OutputResource getOutputLinkBinding() {
		return outputLinkBinding;
	}

	public void setOutputLinkBinding(OutputResource outputLinkBinding) {
		this.outputLinkBinding = outputLinkBinding;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getMonthRunMonthString() {
		return DateUtils.getMonthAsString(monthRunMonth);
	}

	public List<Customer> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Customer> customerList) {
		this.customerList = customerList;
	}

	public OutputResource getOutputLinkPdfBinding() {
		return outputLinkPdfBinding;
	}

	public void setOutputLinkPdfBinding(OutputResource outputLinkPdfBinding) {
		this.outputLinkPdfBinding = outputLinkPdfBinding;
	}

	public Date getReportDateMonthRun() {
		return reportDateMonthRun;
	}

	public void setReportDateMonthRun(Date reportDate) {
		this.reportDateMonthRun = reportDate;
	}

	public Date getReportDateSingleInv() {
		return reportDateSingleInv;
	}

	public void setReportDateSingleInv(Date reportDateSingleInv) {
		this.reportDateSingleInv = reportDateSingleInv;
	}

	public String getReportNumber() {
		return reportNumber;
	}

	public void setReportNumber(String reportNumber) {
		this.reportNumber = reportNumber;
	}

}
