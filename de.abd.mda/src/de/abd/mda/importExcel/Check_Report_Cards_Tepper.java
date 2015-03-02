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
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

import jxl.demo.CSV;

public class Check_Report_Cards_Tepper {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList<CardBean> list = new ArrayList<CardBean>();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(Check_Report_Cards_Tepper.class);
		private ArrayList<String> cardsInCSV = new ArrayList<String>();
			
		public static void main(String[] args) {
			Check_Report_Cards_Tepper c = new Check_Report_Cards_Tepper();
			c.readData();
		}
		
		public void readData() {
			try {
					String hostname = java.net.InetAddress.getLocalHost().getHostName();
					System.out.println("Hostname" + hostname);
					
					String path = "D:/Softwareentwicklung/Reports/Tepper/";
					
					FileReader file = null;
	
					logger.info("************ GSM **************");
					file = new FileReader(path + "GSM.csv");
					readDataFromFile(file, "20091");
					CardController cc = new CardController();
					List<CardBean> cusCards = cc.searchCustomerActiveCards(437);
					System.out.println("Anzahl Karten : " + cusCards);
					for (CardBean card : cusCards) {
						if (cardsInCSV.contains(card.getCardnumberString())) {
							System.out.println("Karte " + card.getCardnumberString() + " enthalten");
						} else {
							System.out.println("**************** ERROR: Karte " + card.getCardnumberString() + " nicht enthalten ************");
						}
					}
//					writeOutList(list);

//					logger.info("************ 20247_Czech_zum_importieren_20_08_14.csv **************");
//					file = new FileReader(path + "20247_Czech_zum_importieren_20_08_14.csv");
//					readDataFromFile(file, "20247");
//					writeOutList(list);
//					
//					logger.info("************ 20250_Polen_zum_importieren_20_08_14.csv **************");
//					file = new FileReader(path + "20250_Polen_zum_importieren_20_08_14.csv");
//					readDataFromFile(file, "20250");
//					writeOutList(list);
//
//					logger.info("************ 20249_UK_zum_importieren_20_08_14.csv **************");
//					file = new FileReader(path + "20249_UK_zum_importieren_20_08_14.csv");
//					readDataFromFile(file, "20249");
//					writeOutList(list);
//
//					logger.info("************ 20122_GSM-SIM_Karten_ThyssenKrupp_NL_Stuttgart_30.05.2013.csv **************");
//					file = new FileReader(path + "20122_GSM-SIM_Karten_ThyssenKrupp_NL_Stuttgart_30.05.2013.csv");
//					readDataFromFile(file, "20122");
//					writeOutList(list);
//
//					logger.info("************ 20158_GSM-SIM_Karten_ThyssenKrupp_NL_Reutlingen.csv **************");
//					file = new FileReader(path + "20158_GSM-SIM_Karten_ThyssenKrupp_NL_Reutlingen.csv");
//					readDataFromFile(file, "20158");
//					writeOutList(list);
//
//					logger.info("************ 20222_GSM_TKA_NL_Singen.csv **************");
//					file = new FileReader(path + "20222_GSM_TKA_NL_Singen.csv");
//					readDataFromFile(file, "20222");
//					writeOutList(list);
//
//					logger.info("************ 20232_HALBJAHR_GSM_TKA_NL_Saarbrücken.csv **************");
//					file = new FileReader(path + "20232_HALBJAHR_GSM_TKA_NL_Saarbrücken.csv");
//					readDataFromFile(file, "20232");
//					writeOutList(list);
//
//					logger.info("************ 20152_GSM-SIM_Karten_ThyssenKrupp_NL_Mainz.csv **************");
//					file = new FileReader(path + "20152_GSM-SIM_Karten_ThyssenKrupp_NL_Mainz.csv");
//					readDataFromFile(file, "20152");
//					writeOutList(list);
//
//					logger.info("************ 20226_GSM_TKA_NL_Kassel.csv **************");
//					file = new FileReader(path + "20226_GSM_TKA_NL_Kassel.csv");
//					readDataFromFile(file, "20226");
//					writeOutList(list);
//
//					logger.info("************ 20261_JAHR_GSM-Tel._MBV_Karlsruhe.csv **************");
//					file = new FileReader(path + "20261_JAHR_GSM-Tel._MBV_Karlsruhe.csv");
//					readDataFromFile(file, "20261");
//					writeOutList(list);
//
//					logger.info("************ 20185_GSM-Tel_Fa._TKA_NL_Bremen.csv **************");
//					file = new FileReader(path + "20185_GSM-Tel_Fa._TKA_NL_Bremen.csv");
//					readDataFromFile(file, "20185");
//					writeOutList(list);
//
//					logger.info("************ 20199_GSM-Tel_Fa._TKA_NL_Freiburg.csv **************");
//					file = new FileReader(path + "20199_GSM-Tel_Fa._TKA_NL_Freiburg.csv");
//					readDataFromFile(file, "20199");
//					writeOutList(list);
//
//					logger.info("************ 20233_GSM-SIM_Karten_ThyssenKrupp_NL_Bayreuth.csv **************");
//					file = new FileReader(path + "20233_GSM-SIM_Karten_ThyssenKrupp_NL_Bayreuth.csv");
//					readDataFromFile(file, "20233");
//					writeOutList(list);
//
//					                          
//					logger.info("************ 20091_GSM_Liste_FA._Tepper_mit_Vertragsnummern.csv **************");
//					file = new FileReader(path + "20091_GSM_Liste_FA._Tepper_mit_Vertragsnummern.csv");
//					readDataFromFile(file, "20091");
//					writeOutList(list);
//
//					logger.info("************ 20181_GSM-Tel._Fa._GMT_Aufzug-Service_GmbH.csv **************");
//					file = new FileReader(path + "20181_GSM-Tel._Fa._GMT_Aufzug-Service_GmbH.csv");
//					readDataFromFile(file, "20181");
//					writeOutList(list);
//
//					logger.info("************ 20047_GSM-Tel_Fa_Liftservice_u_Montage_GmbH_29.03.2012.csv **************");
//					file = new FileReader(path + "20047_GSM-Tel_Fa_Liftservice_u_Montage_GmbH_29.03.2012.csv");
//					readDataFromFile(file, "20047");
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
		
		
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card = extractCardNums(card, split[0]);
				cardsInCSV.add(card.getCardnumberString());
			}
		}
	}
	
	private void setCardValues(CardBean card, String[] split2,
			HashMap<String, Integer> spalten, Customer customer) {
		if (spalten.containsKey("Rufnummer")) {
			if (card.getCardNumberFirst().equals("78702559")) {
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
		if (spalten.containsKey("Anlagen Nr.")) {
			card.setFactoryNumber(split[spalten.get("Anlagen Nr.")]);
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
		if (spalten.containsKey("Fabrik Nummer")) {
			card.setFactoryNumber(""+split[spalten.get("Fabrik Nummer")]);
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
		if (spalten.containsKey("Lokation")) {
			card.setLokation(""+split[spalten.get("Lokation")]);
		}
		if (spalten.containsKey("Meinsatzort")) {
			card.setEinsatzort(""+split[spalten.get("Meinsatzort")]);
		}
		if (spalten.containsKey("Status")) {
			card.setStatus(""+split[spalten.get("Status")]);
		}
		if (spalten.containsKey("BA Nr.")) {
			card.setBaNummer(""+split[spalten.get("BA Nr.")]);
		}
		if (spalten.containsKey("BA - Nr.")) {
			card.setBaNummer(""+split[spalten.get("BA - Nr.")]);
		}

		String date = null;

		if (spalten.containsKey("Datum")) {
			date = split[spalten.get("Datum")];
		} else if (spalten.containsKey("Aktiv. Datum")) {
			date = split[spalten.get("Aktiv. Datum")];
		} else if (spalten.containsKey("aktiviert")) {
			date = split[spalten.get("aktiviert")];
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
		if (spalten.containsKey("Auftragsnr.")) {
			if (split[spalten.get("Auftragsnr.")] != null)
				card.setAuftragsNr(""+split[spalten.get("Auftragsnr.")]);
		}
		if (spalten.containsKey("SO-Nr.(Auftr.Nr.)")) {
			if (split[spalten.get("SO-Nr.(Auftr.Nr.)")] != null)
				card.setAuftragsNr(""+split[spalten.get("SO-Nr.(Auftr.Nr.)")]);
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
		if (spalten.containsKey("Vertrag")) {
			card.setVertrag(""+split[spalten.get("Vertrag")]);
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
		if (spalten.containsKey("Order Nr.")) {
			card.setBestellNummer(split[spalten.get("Order Nr.")]);
		}
		if (spalten.containsKey("Order ID")) {
			card.setBestellNummer(split[spalten.get("Order ID")]);
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
		if (spalten.containsKey("Equipment Nr.")) {
			card.setEquipmentNr(split[spalten.get("Equipment Nr.")]);
		}
		if (spalten.containsKey("Eqiupment ID")) {
			card.setEquipmentNr(split[spalten.get("Eqiupment ID")]);
		}
		if (spalten.containsKey("Equipment ID")) {
			card.setEquipmentNr(split[spalten.get("Equipment ID")]);
		}
		if (spalten.containsKey("Equipment Aufteilung")) {
			card.setEquipmentNr(split[spalten.get("Equipment Aufteilung")]);
		}
		if (spalten.containsKey("SIE IAO Equipment")) {
			card.setEquipmentNr(split[spalten.get("SIE IAO Equipment")]);
		}
		if (spalten.containsKey("Kst.")) {
			card.setKostenstelle(split[spalten.get("Kst.")]);
		}
		if (spalten.containsKey("Kostenstelle")) {
			card.setKostenstelle(split[spalten.get("Kostenstelle")]);
		}
		if (spalten.containsKey("WE. Nr")) {
			card.setWe(split[spalten.get("WE. Nr")]);
		}
		if (spalten.containsKey("WE Nr.")) {
			card.setWe(split[spalten.get("WE Nr.")]);
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