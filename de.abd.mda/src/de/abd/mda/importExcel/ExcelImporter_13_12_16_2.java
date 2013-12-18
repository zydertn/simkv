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

public class ExcelImporter_13_12_16_2 {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList<CardBean> list = new ArrayList<CardBean>();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(ExcelImporter_13_12_16_2.class);
			
		public static void main(String[] args) {
			ExcelImporter_13_12_16_2 c = new ExcelImporter_13_12_16_2();
			c.readData();
		}
		
		public void readData() {
			try {
					String hostname = java.net.InetAddress.getLocalHost().getHostName();
					System.out.println("Hostname" + hostname);
					
					String path = "E:/ExcelImport/Rollout/";
					
					if (hostname.equals("STR00108")) {
						path = "D:/Softwareentwicklung/Excel-Lieferung-Sina/Rollout/2013_12_16_2/";
					}
					
					FileReader file = null;
	
					logger.info("************ 20230_JAHR_GSM_Liste_Bayrischer_Hausbau.csv **************");
					file = new FileReader(path + "20230_JAHR_GSM_Liste_Bayrischer_Hausbau.csv");
					readDataFromFile(file, "20230");
					writeOutList(list);
					
//					logger.info("************ 20234_GSM_Liste_Wollmann_Zahntechnik.csv **************");
//					file = new FileReader(path + "20234_GSM_Liste_Wollmann_Zahntechnik.csv");
//					readDataFromFile(file, "20234");
//					writeOutList(list);
//					
//					logger.info("************ 20194_QUARTAL_GSM-Tel._Hr._Marcus_Stein.csv **************");
//					file = new FileReader(path + "20194_QUARTAL_GSM-Tel._Hr._Marcus_Stein.csv");
//					readDataFromFile(file, "20194");
//					writeOutList(list);
//
//					logger.info("************ 20237_GSM_Liste_AWO-Leinfelden.csv **************");
//					file = new FileReader(path + "20237_GSM_Liste_AWO-Leinfelden.csv");
//					readDataFromFile(file, "20237");
//					writeOutList(list);
//
//					logger.info("************ 20238_GSM_Liste_Aqua_Hotel.csv **************");
//					file = new FileReader(path + "20238_GSM_Liste_Aqua_Hotel.csv");
//					readDataFromFile(file, "20238");
//					writeOutList(list);
//
//					logger.info("************ 20075_GSM-Tel._Fa._Alfred_Richter_Aufzüge.csv **************");
//					file = new FileReader(path + "20075_GSM-Tel._Fa._Alfred_Richter_Aufzüge.csv");
//					readDataFromFile(file, "20075");
//					writeOutList(list);
//
//					logger.info("************ 20102_GSM-Tel._Fa._Salzer.csv **************");
//					file = new FileReader(path + "20102_GSM-Tel._Fa._Salzer.csv");
//					readDataFromFile(file, "20102");
//					writeOutList(list);
//
//					logger.info("************ 20164_GSM-Tel._Karten_Haushahn_C._GmbH_&_Co._Stuttgart.csv **************");
//					file = new FileReader(path + "20164_GSM-Tel._Karten_Haushahn_C._GmbH_&_Co._Stuttgart.csv");
//					readDataFromFile(file, "20164");
//					writeOutList(list);

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
//				// Deutsche Karten
//				existingCard.setSupplier(Model.SUPPLIER_TELEKOM);
				list.add(existingCard);
			} else {
				setCardValues(card, split, spalten, customer);
//				// Deutsche Karten
//				card.setSupplier(Model.SUPPLIER_TELEKOM);
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
			if (card.getCardNumberFirst().equals("78702593")) {
				System.out.println("Jetzt");
			}
			extractTelNums(card, split[spalten.get("Rufnummer")]);
		}
		if (spalten.containsKey("Telefon Nr.")) {
			extractTelNums(card, split[spalten.get("Telefon Nr.")]);
		}
		if (spalten.containsKey("NR.Telefon")) {
			extractTelNums(card, split[spalten.get("NR.Telefon")]);
		}
		if (spalten.containsKey("Tel. Nummer")) {
			extractTelNums(card, split[spalten.get("Tel. Nummer")]);
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
		if (spalten.containsKey("Ort")) {
			Address instAdd = getCardAddress(card);
			instAdd.setCity(""+split[spalten.get("Ort")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Straße")) {
			Address instAdd = getCardAddress(card);
			instAdd.setStreet(""+split[spalten.get("Straße")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Strasse")) {
			Address instAdd = getCardAddress(card);
			instAdd.setStreet(""+split[spalten.get("Strasse")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Anschrift")) {
			Address instAdd = getCardAddress(card);
			instAdd.setStreet(""+split[spalten.get("Anschrift")]);
			card.setInstallAddress(instAdd);
		}
		if (spalten.containsKey("Aufzug Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Aufzug Nr.")]);
		}
		if (spalten.containsKey("Aufzug-Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Aufzug-Nr.")]);
		}
		if (spalten.containsKey("Fab. Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Fab. Nr.")]);
		}
		if (spalten.containsKey("Fab. Nr")) {
			card.setFactoryNumber(""+split[spalten.get("Fab. Nr")]);
		}
		if (spalten.containsKey("Fabr. Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Fabr. Nr.")]);
		}
		if (spalten.containsKey("Fabrik Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Fabrik Nr.")]);
		}
		if (spalten.containsKey("Fabrik. Nr.")) {
			card.setFactoryNumber(""+split[spalten.get("Fabrik. Nr.")]);
		}
		if (spalten.containsKey("Leitstand")) {
			card.setLeitstand(""+split[spalten.get("Leitstand")]);
		}
		if (spalten.containsKey("Leitst. Nr.")) {
			card.setLeitstand(""+split[spalten.get("Leitst. Nr.")]);
		}
		if (spalten.containsKey("zugel. Notruf Nr.")) {
			card.setLeitstand(""+split[spalten.get("zugel. Notruf Nr.")]);
		}
		if (spalten.containsKey("Status")) {
			card.setStatus(""+split[spalten.get("Status")]);
		}

		String date = null;

		if (spalten.containsKey("Datum")) {
			date = split[spalten.get("Datum")];
		} else if (spalten.containsKey("Aktiv. Datum")) {
			date = split[spalten.get("Aktiv. Datum")];
		}
		
		if (date != null && date.length() > 1) {
			System.out.println(date);
			Calendar c = new GregorianCalendar();
			try{
				c.set(new Integer(date.substring(6, 10)), new Integer(date.substring(3, 5)) - 1, new Integer(date.substring(0, 2)));
			} catch (Exception e) {
				System.out.println(e);
			}
			if (card.getStatus().equalsIgnoreCase(Model.STATUS_ACTIVE)) {
				card.setActivationDate(c.getTime());
			} else if (card.getStatus().equalsIgnoreCase(Model.STATUS_INACTIVE) || card.getStatus().equals(Model.STATUS_DUMMY)) {
				card.setDeactivationDate(c.getTime());
			}
		}

		
		if (spalten.containsKey("Auftrag Nr.")) {
			card.setAuftragsNr(""+split[spalten.get("Auftrag Nr.")]);
		}
		if (spalten.containsKey("Auftragsnummer")) {
			if (split[spalten.get("Auftragsnummer")] != null)
				card.setAuftragsNr(""+split[spalten.get("Auftragsnummer")]);
		}
		if (spalten.containsKey("Vertragsnummer")) {
			card.setVertrag(""+split[spalten.get("Vertragsnummer")]);
		}
		if (spalten.containsKey("Vertrag. Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertrag. Nr.")]);
		}
		if (spalten.containsKey("Vertrag Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertrag Nr.")]);
		}
		if (spalten.containsKey("Vertr. Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertr. Nr.")]);
		}
		if (spalten.containsKey("Vertrags Nr.")) {
			card.setVertrag(""+split[spalten.get("Vertrags Nr.")]);
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
		if (spalten.containsKey("Memo")) {
			card.setComment(split[spalten.get("Memo")]);
		}
		if (spalten.containsKey("Endkunde")) {
			card.setComment(split[spalten.get("Endkunde")]);
		}
		// prüfen, ob BestellNummer korrekt ist, oder BestellNummer (auf Karte)
		if (spalten.containsKey("Bestellnr.")) {
			card.setBestellNummer(split[spalten.get("Bestellnr.")]);
		}
		if (spalten.containsKey("Bestellnummer")) {
			card.setBestellNummer(split[spalten.get("Bestellnummer")]);
		}
		if (spalten.containsKey("Bestell Nr.")) {
			card.setBestellNummer(split[spalten.get("Bestell Nr.")]);
		}
		if (spalten.containsKey("Best. Nummer")) {
			card.setBestellNummer(split[spalten.get("Best. Nummer")]);
		}
		if (spalten.containsKey("Kartenpreis")) {
			if (split.length > spalten.get("Kartenpreis") && split[spalten.get("Kartenpreis")] != null) {
				String preis = split[spalten.get("Kartenpreis")];
				int simPrice = Integer.parseInt(preis);
				card.setStandardPrice(false);
				card.setSimPrice(simPrice);
			}
		}

		if (spalten.containsKey("PIN-Code")) {
			String pinCode = split[spalten.get("PIN-Code")];
			if (pinCode != "" && !pinCode.equalsIgnoreCase("Frei") && pinCode.length() > 1) {
				card.setPin(split[spalten.get("PIN-Code")]);
			}
		}
		
		if (spalten.containsKey("Land")) {
			String land = split[spalten.get("Land")];
			if (land != null && land.equals("A")) {
				card.setSupplier(Model.SUPPLIER_TELEKOM_AUSTRIA);
			} else {
				card.setSupplier(Model.SUPPLIER_TELEKOM);
			}
		}
		
		if (spalten.containsKey("Eqiupment Nr.")) {
			card.setEquipmentNr(split[spalten.get("Eqiupment Nr.")]);
		}
		if (spalten.containsKey("SIE IAO Equipment")) {
			card.setEquipmentNr(split[spalten.get("SIE IAO Equipment")]);
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