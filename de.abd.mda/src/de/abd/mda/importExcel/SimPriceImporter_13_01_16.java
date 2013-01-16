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
import de.abd.mda.persistence.dao.InvoiceConfiguration;
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
			
			if (split.length > 7) {
				logger.debug("split[7] == " + split[7]);
				if (split[7].length() > 0) {
					List<DaoObject> cusList = this.searchCustomer(split[7], null);
					if (cusList != null && cusList.size() > 0) {
						Customer c = (Customer) cusList.get(0);
						logger.info("Kunde " + c.getCustomernumber() + " existiert!");
					} else {
						logger.warn("Kunde " + split[7] + " gibt es nicht in der DB!");
						Customer c = new Customer();
						c.setCustomernumber(split[7]);
						c.setName(split[0]);
						Address a = new Address();
						if (split[2] != null && split[2].length() > 0) {
							a.setStreet(split[2]);
						}
						if (split[3] != null && split[3].length() > 0) {
							a.setHousenumber(split[3]);
						}
						if (split[4] != null && split[4].length() > 0) {
							a.setPostcode(split[4]);
						}
						if (split[5] != null && split[5].length() > 0) {
							a.setCity(split[5]);
						}
						c.setAddress(a);
						if (split[6] != null && split[6].length() > 0) {
							InvoiceConfiguration ic = new InvoiceConfiguration();
							ic.setSimPrice(new Integer(split[6]));
							c.setInvoiceConfiguration(ic);
						}

						CustomerController cc = new CustomerController();
						cc.createObject(c);

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