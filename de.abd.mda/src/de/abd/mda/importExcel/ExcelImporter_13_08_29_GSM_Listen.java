package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Country;
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

public class ExcelImporter_13_08_29_GSM_Listen {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(ExcelImporter_13_08_29_GSM_Listen.class);
			
		public static void main(String[] args) {
			ExcelImporter_13_08_29_GSM_Listen c = new ExcelImporter_13_08_29_GSM_Listen();
			c.extractCardNums(null, "0172 - 51 75 360");
			c.readData();
		}
		
		public void readData() {
			try {
					FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/GSM-Listen/GSM Liste FA. Tepper mit Vertragsnummern.csv");
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
		zeile = data.readLine();
		split = zeile.split(";");
		int length = split.length;
		HashMap<String, Integer> spalten = new HashMap<String, Integer>();
		for (int i = 7; i < length; i++) {
			spalten.put(split[i], i);
		}
		
		
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card = extractCardNums(card, split[1]);
//				card.setCardNumberFirst(extractCardNum(split[0]));

				try {
					String select = "select distinct card from CardBean card where card.cardNumberFirst = '" + card.getCardNumberFirst() + "'";
					if (card.getCardNumberSecond() != null && card.getCardNumberSecond().length() > 0 && !card.getCardNumberSecond().equals(" ")) {
						select = select + "	and card.cardNumberSecond = '" + card.getCardNumberSecond() +"'";
					}
					tx = session.beginTransaction();
					List<CardBean> list = session.createQuery(select).list();
					
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
			
			if (existingCard != null) {
				setCardValues(existingCard, split, spalten);
			} else {
				setCardValues(card, split, spalten);
			}
			
			try {
				tx.commit();				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}


		}
	}
	
	private void setCardValues(CardBean card, String[] split2,
			HashMap<String, Integer> spalten) {
		if (spalten.containsKey("Fab. Nr.")) {
			card.setFactoryNumber(split[spalten.get("Fab. Nr.")]);
		}
		if (spalten.containsKey("Auftrag Nr.")) {
			card.setAuftragsNr(split[spalten.get("Auftrag Nr.")]);
		}
		if (spalten.containsKey("Vertragsnummer")) {
			card.setVertrag(split[spalten.get("Vertragsnummer")]);
		}
		if (spalten.containsKey("Vertrag. Nr.")) {
			card.setVertrag(split[spalten.get("Vertrag. Nr.")]);
		}
		if (spalten.containsKey("Vertr. Nr.")) {
			card.setVertrag(split[spalten.get("Vertr. Nr.")]);
		}
		if (spalten.containsKey("Bemerkung")) {
			card.setComment(split[spalten.get("Bemerkung")]);
		}
		if (spalten.containsKey("Bemerkungen")) {
			card.setComment(split[spalten.get("Bemerkungen")]);
		}
//		if (spalten.containsKey("Aufzug Nr.")) {
//			card.setAuf)(split[spalten.get("Aufzug Nr.")]);
//		}
		
		
	}

	private CardBean extractCardNums(CardBean card, String string) {
		string = string.replaceAll("\\s", "");
		card.setCardNumberFirst(string.substring(0, string.indexOf("-")));
		card.setCardNumberSecond(string.substring(string.indexOf("-")+1));
		return card;
	}


}