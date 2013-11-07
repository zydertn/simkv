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

import de.abd.mda.controller.CardActionController;
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

public class ExcelImporter_13_11_07 {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList<CardBean> list = new ArrayList<CardBean>();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(ExcelImporter_13_11_07.class);
			
		public static void main(String[] args) {
			ExcelImporter_13_11_07 c = new ExcelImporter_13_11_07();
			c.readData();
		}
		
		public void readData() {
			try {
					logger.info("************ GSM_LISTE_ASS.CSV **************");
					FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/Rollout/2013_11_07/GSM_Liste_ASS.csv");
					readDataFromFile(file, "20216");
					writeOutList(list);
					
					logger.info("************ GSM-Tel._Gesamtliste_Fa._Aufzugbau_Dresden.csv **************");
					file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/Rollout/2013_11_07/GSM-Tel._Gesamtliste_Fa._Aufzugbau_Dresden.csv");
					readDataFromFile(file, "20016");
					writeOutList(list);
					
					logger.info("************ GSM-Tel_Fa_ATH_Aufzugtechnik_Heilbronn.csv **************");
					file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/Rollout/2013_11_07/GSM-Tel_Fa_ATH_Aufzugtechnik_Heilbronn.csv");
					readDataFromFile(file, "20166");
					writeOutList(list);

					logger.info("************ GSM-Tel_Fa_Aufzug_Roesler.csv **************");
					file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/Rollout/2013_11_07/GSM-Tel_Fa_Aufzug_Roesler.csv");
					readDataFromFile(file, "20120");
					writeOutList(list);
			} catch (FileNotFoundException e) {
				logger.error("Datei nicht gefunden");
			} catch (IOException e) {
				logger.error("E/A-Fehler");
			}
			
	}

		
	private void writeOutList(ArrayList<CardBean> list2) {
			logger.info("Anzahl Sätze: " + list.size());
			for (CardBean c : list2) {
				logger.info("Importierte Karte: " + c.getCardNumberFirst() + " " + c. getCardNumberSecond());
			}
				
			list = new ArrayList<CardBean>();
		}

	public void readDataFromFile(FileReader file, String customerNumber) throws IOException {
		BufferedReader data = new BufferedReader(file);
		zeile = data.readLine();
		zeile = data.readLine();
		split = zeile.split(";");
		int length = split.length;
		HashMap<String, Integer> spalten = new HashMap<String, Integer>();
		for (int i = 0; i < length; i++) {
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
				logger.debug("split[1] == " + split[1]);
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

			Customer customer = null;
			try {
				customer = (Customer) searchCustomer(customerNumber, null).get(0);
			} catch (Exception e) {
				MdaLogger.error(logger, "Kunde existiert nicht!!");
				break;
			}
			
			if (existingCard != null) {
				setCardValues(existingCard, split, spalten, customer);
				// Deutsche Karten
				existingCard.setSupplier(Model.SUPPLIER_TELEKOM);
				list.add(existingCard);
			} else {
				setCardValues(card, split, spalten, customer);
				// Deutsche Karten
				card.setSupplier(Model.SUPPLIER_TELEKOM);
				CardController cardController = new CardController();
				String retMessage = cardController.createObject(card);
				if (retMessage != null && retMessage.length() == 0) {
					MdaLogger.info(logger, retMessage);
				}
				list.add(card);
			}
			
			try {
				tx.commit();

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}


		}
	}
	
	private void setCardValues(CardBean card, String[] split2,
			HashMap<String, Integer> spalten, Customer customer) {
		if (spalten.containsKey("Rufnummer")) {
			extractTelNums(card, split[spalten.get("Rufnummer")]);
		}
		
		if (spalten.containsKey("PLZ")) {
			Address instAdd = getCardAddress(card);
			instAdd.setPostcode(""+split[spalten.get("PLZ")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Einsatzort")) {
			Address instAdd = getCardAddress(card);
			instAdd.setCity(""+split[spalten.get("Einsatzort")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Straße")) {
			Address instAdd = getCardAddress(card);
			instAdd.setStreet(""+split[spalten.get("Straße")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Aufzug Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Aufzug Nr.")]);
		}
		if (spalten.containsKey("Fab. Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Fab. Nr.")]);
		}
		if (spalten.containsKey("Status")) {
			card.setStatus(""+split[spalten.get("Status")]);
		}
		if (spalten.containsKey("Datum")) {
			try {
				String date = split[spalten.get("Datum")];
				if (date != null && date.length() > 1) {
					System.out.println(date);
					Calendar c = new GregorianCalendar();
					try{
						c.set(new Integer(date.substring(6, 10)), new Integer(date.substring(3, 5)) - 1, new Integer(date.substring(0, 2)));
					} catch (Exception e) {
						System.out.println(e);
					}
					card.setActivationDate(c.getTime());
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				logger.warn("ArrayIndexOutOfBounds Exception bei Spalte Datum der Karte " + card.getCardNumber());
			}
		}

		
		if (spalten.containsKey("Auftrag Nr.")) {
			card.setAuftragsNr(""+split[spalten.get("Auftrag Nr.")]);
		}
		if (spalten.containsKey("Vertragsnummer")) {
			card.setVertrag(""+split[spalten.get("Vertragsnummer")]);
		}
		if (spalten.containsKey("Vertrag. Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertrag. Nr.")]);
		}
		if (spalten.containsKey("Vertr. Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertr. Nr.")]);
		}
		if (spalten.containsKey("Bemerkung")) {
			card.setComment(split[spalten.get("Bemerkung")]);
		}
		if (spalten.containsKey("Bemerkungen")) {
			if (split2.length > spalten.get("Bemerkungen")) {
				String comment = "" + split[spalten.get("Bemerkungen")]; 
				card.setComment(comment);
			}
		}
		
		card.setCustomer(customer);
//		if (spalten.containsKey("Aufzug Nr.")) {
//			card.setAuf)(split[spalten.get("Aufzug Nr.")]);
//		}
		
		
	}

	private Address getCardAddress(CardBean card) {
		Address instAdd = card.getInstallAddress();
		if (instAdd == null) {
			instAdd = new Address();
		}
		return instAdd;
	}

	private CardBean extractCardNums(CardBean card, String string) {
		string = string.replaceAll("\\s", "");
		card.setCardNumberFirst(string.substring(0, string.indexOf("-")));
		card.setCardNumberSecond(string.substring(string.indexOf("-")+1));
		return card;
	}

	private CardBean extractTelNums(CardBean card, String string) {
		if (string != null && string.length() > 0) {
			string = string.replaceAll("\\s", "");
			card.setPhoneNrFirst(string.substring(0, string.indexOf("-")));
			card.setPhoneNrSecond(string.substring(string.indexOf("-")+1));
		}
		return card;
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