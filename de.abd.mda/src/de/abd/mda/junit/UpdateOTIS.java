package de.abd.mda.junit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

public class UpdateOTIS {

	private String zeile;
	static final Logger logger = Logger.getLogger(UpdateOTIS.class);
	private ArrayList list = new ArrayList();
	private String[] split = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<CardBean> cards = null;
		tx = session.beginTransaction();
		String select = "select distinct card from CardBean card where card.customer = '3'";
		cards = session.createQuery(select).list();
		for (CardBean card : cards) {
			card.setPhoneNrSecond(card.getPhoneNrSecond().substring(1));
		}
		
		tx.commit();

	}
	
	public void readData(List<String> cusNumbers) {
		try {
				FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/29_10_12_Überarbeitet4.csv");
				readDataFromFile(file, cusNumbers);
		} catch (FileNotFoundException e) {
			logger.error("Datei nicht gefunden");
		} catch (IOException e) {
			logger.error("E/A-Fehler");
		}
		
		logger.info("Anzahl Sätze: " + list.size());
	}
	
	public void readDataFromFile(FileReader file, List<String> cusNumbers) throws IOException {
		BufferedReader data = new BufferedReader(file);
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			MdaLogger.debug(logger, zeile);
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			if (split.length > 15) {
				String customerNumber = split[15];
				if (customerNumber.length() > 0) {
					if (cusNumbers.contains(split[15])) {
						MdaLogger.info(logger, split[15] + " enthalten!");
					} else {
						MdaLogger.error(logger, split[15] + " NICHT enthalten!");
					}
				} else {
					MdaLogger.warn(logger, "split[15].length == 0");
				}
			} else {
				MdaLogger.warn(logger, "Keine Kundennummer enthalten!");
			}
		}
	}

}
