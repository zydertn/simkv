package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

import jxl.demo.CSV;

public class ExcelImporter_13_02_09_Tepper_Nachtrag {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(ExcelImporter_13_02_09_Tepper_Nachtrag.class);
			
		public static void main(String[] args) {
			ExcelImporter_13_02_09_Tepper_Nachtrag c = new ExcelImporter_13_02_09_Tepper_Nachtrag();
			c.readData();
		}
		
		public void readData() {
			try {
					FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/2013_02_09_Tepper_Nachtrag.csv");
					readDataFromFile(file);
			} catch (FileNotFoundException e) {
				logger.error("Datei nicht gefunden");
			} catch (IOException e) {
				logger.error("E/A-Fehler");
			}
			
			logger.info("Anzahl Sätze: " + list.size());
	}

		
	public void readDataFromFile(FileReader file) throws IOException {
		BufferedReader data = new BufferedReader(file);
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card.setCardNumberFirst(split[0]);
			}
			if (split[0].equals("67959200")) {
				System.out.println("bla");
			}
			
			if (split.length > 1) {
				logger.debug("split[1] == " + split[1]);
				card.setCardNumberSecond(split[1]);
				CardController controller = new CardController();
				try {
					String select = "select distinct card from CardBean card where card.cardNumberFirst = '" + card.getCardNumberFirst() + "'";
					if (card.getCardNumberSecond() != null && card.getCardNumberSecond().length() > 0 && !card.getCardNumberSecond().equals(" ")) {
						select = select + "	and card.cardNumberSecond = '" + card.getCardNumberSecond() +"'";
					}
					tx = session.beginTransaction();
					List<CardBean> list = session.createQuery(select).list();
					Iterator it = list.iterator();
					
					if (list.size() > 0) {
						if (list.size() > 1) {
							MdaLogger.warn(logger, "Mehr als eine Karte gefunden!");
							continue;
						} else {
							MdaLogger.info(logger, "Nur eine Karte gefunden!");
							existingCard = (CardBean) list.get(0);
						}
					} else {
						MdaLogger.info(logger, "Keine Karte gefunden!");
					}
				} catch (Exception e) {
					MdaLogger.error(logger, e);
				}
			}

			if (split.length > 4) {
				logger.debug("split[4] == " + split[4]);
				if (split[4].length() > 0) {
					if (existingCard != null) {
						existingCard.setOrderNumber(split[4]);
					} else {
						card.setOrderNumber(split[4]);
					}
				}
			}
			if (split.length > 5) {
				logger.debug("split[5] == " + split[5]);
				String customerOrderNumber = split[5];
				if (customerOrderNumber != null && customerOrderNumber.length() > 0) {
					if (existingCard != null) {
						existingCard.setCustomerOrderNumber(split[5]);
					} else {
						card.setCustomerOrderNumber(split[5]);
					}
				}
			}
			

			CardController cardController = new CardController();

			String select = "select sequenceNumber from SequenceNumber sequenceNumber";
			try {
				tx.commit();				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			if (existingCard == null) {
				tx = null;
				session = SessionFactoryUtil.getInstance().getCurrentSession();
				tx = session.beginTransaction();
				List<SequenceNumber> list = session.createQuery(select).list();
				Iterator it = list.iterator();

				int currentSequenceNumber = -1;
				SequenceNumber sn = null;
				if (list.size() > 0) {
					sn = (SequenceNumber) list.get(0);
					currentSequenceNumber = sn.getSequenceNumber();
					currentSequenceNumber++;
					sn.setSequenceNumber(currentSequenceNumber);
					tx.commit();
				} else {
					currentSequenceNumber = 0;
					sn = new SequenceNumber();
					sn.setSequenceNumber(currentSequenceNumber);
					String message = cardController.createObject(sn);
					MdaLogger.info(logger, message);
				}
				
				card.setSequenceNumber(currentSequenceNumber);
				String message = cardController.createObject(card);
				MdaLogger.info(logger, message);				
				MdaLogger.info(logger, "NEW CARD!!!");
			}

			i++;
		}
	}
	
	private List<DaoObject> searchCustomer (String customerNumber, String customerName) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			List<DaoObject> customers = null;
			tx = session.beginTransaction();
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
				if (customerName != null && customerName.length() > 0) {
					whereClause += " && customer.name = '" + customerName + "'";
				}
			} else if (customerName != null && customerName.length() > 0) {
				whereClause += " where customer.name = '" + customerName + "'";
			}
			
			customers = session.createQuery("from Customer as customer" + whereClause).list();

			return customers;
	}

}