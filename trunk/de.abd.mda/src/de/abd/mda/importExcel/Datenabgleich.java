package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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

public class Datenabgleich {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList<CardBean> list = new ArrayList<CardBean>();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(Datenabgleich.class);
		private ArrayList<String[]> cardsInCSV = new ArrayList<String[]>();
			
		public static void main(String[] args) {
			Datenabgleich c = new Datenabgleich();
			c.readData();
		}
		
		public void readData() {
			try {
					String hostname = java.net.InetAddress.getLocalHost().getHostName();
					System.out.println("Hostname" + hostname);
					
					String path = "D:/Softwareentwicklung/Datenabgleich/";
					
					FileReader file = null;
					FileWriter writer = new FileWriter("D:\\Softwareentwicklung\\Datenabgleich\\output.csv");
					
					logger.info("************ SiwalTec-Komplettreport-14-12-17 **************");
					file = new FileReader(path + "SiwalTec-Komplettreport-14-12-17.csv");
					readDataFromFile(file);
					CardController cc = new CardController();
					for (String[] s: cardsInCSV) {
						CardBean card = cc.searchCard(s[0], s[1], null, null, null);
						writer.append(s[0]);
						writer.append("-");
						writer.append(s[1]);
						writer.append(";");
						if (card != null) {
							writer.append("1");
						} else {
							writer.append("0");
						}
						writer.append("\n");
					}
					writer.flush();
					writer.close();

			} catch (FileNotFoundException e) {
				logger.error("Datei nicht gefunden");
			} catch (IOException e) {
				logger.error("E/A-Fehler");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

		
	public void readDataFromFile(FileReader file) throws IOException {
		BufferedReader data = new BufferedReader(file);
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		
		while ((zeile = data.readLine()) != null) {
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card = extractCardNums(card, split[0]);
				cardsInCSV.add(new String[] {card.getCardNumberFirst(), card.getCardNumberSecond()});
				
			}
		}
	}
	

	private CardBean extractCardNums(CardBean card, String string) {
		string = string.replaceAll("\\s", "");
		card.setCardNumberFirst(string.substring(0, string.indexOf("-")));
		card.setCardNumberSecond(string.substring(string.indexOf("-")+1));
		return card;
	}

}