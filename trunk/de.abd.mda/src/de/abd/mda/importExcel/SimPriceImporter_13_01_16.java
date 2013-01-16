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

public class SimPriceImporter_13_01_16 {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(SimPriceImporter_13_01_16.class);
			
		public static void main(String[] args) {
			SimPriceImporter_13_01_16 c = new SimPriceImporter_13_01_16();
			c.readData();
		}
		
		public void readData() {
			try {
					FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/Preiszuordnung.csv");
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
			
			if (split.length > 5) {
				logger.debug("split[5] == " + split[5]);
				if (split[5].length() > 0) {
					List<DaoObject> cusList = this.searchCustomer(split[5], null);
					if (cusList != null && cusList.size() > 0) {
						Customer c = (Customer) cusList.get(0);
						logger.info("Kunde " + c.getCustomernumber() + " existiert!");
					} else {
						logger.warn("Kunde " + split[5] + " gibt es nicht in der DB!");
					}
				}
			}
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