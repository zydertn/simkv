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

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

public class SearchCustomers {

	private String zeile;
	static final Logger logger = Logger.getLogger(SearchCustomers.class);
	private ArrayList list = new ArrayList();
	private String[] split = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CustomerController cc = new CustomerController();
		List<DaoObject> customerList = cc.listObjects();
		List<String> customerNumbers = new ArrayList<String>();
		
		if (customerList != null && customerList.size() > 0) {
			System.out.println(customerList.size() + " Kunden gefunden");
			
			if (customerList.size() > 1) {
				Iterator it = customerList.iterator();
				while (it.hasNext()) {
					String cNumber = ((Customer) it.next()).getCustomernumber();
					if (cNumber != null && cNumber.length() > 0) {
						customerNumbers.add(cNumber);
					}
				}
			}
		} else {
			System.out.println("Kein Customer gefunden");
		}	
		SearchCustomers sc = new SearchCustomers();
		sc.readData(customerNumbers);
	}
	
	public void readData(List<String> cusNumbers) {
		try {
				FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/29_10_12_�berarbeitet4.csv");
				readDataFromFile(file, cusNumbers);
		} catch (FileNotFoundException e) {
			logger.error("Datei nicht gefunden");
		} catch (IOException e) {
			logger.error("E/A-Fehler");
		}
		
		logger.info("Anzahl S�tze: " + list.size());
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
