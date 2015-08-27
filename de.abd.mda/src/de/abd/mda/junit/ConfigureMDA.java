package de.abd.mda.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.dao.controller.DaoController;
import de.abd.mda.persistence.dao.controller.IDaoController;
import de.abd.mda.persistence.dao.controller.UtilController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CustomerComparator;

public class ConfigureMDA extends DaoController implements IDaoController {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigureMDA cmda = new ConfigureMDA();
//		cmda.configurePrices();
		cmda.uploadReportImagesToDB();


	}

	private void uploadReportImagesToDB() {
		UtilController uc = new UtilController();
		String path = "C:\\temp\\images\\SiwalTec_Logo.wmf";
		uc.saveImageToDB(path, 0);
		path = "images/Briefpapier_Kopfzeile.jpg";
		uc.saveImageToDB(path, 1);
		path = "images/Briefpapier_FuﬂzeileGimpLinear.jpg";
		uc.saveImageToDB(path, 2);
	}
	
	private void configurePrices() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();
		String select = "from Configuration";

		List<Configuration> list = session.createQuery(select).list();
		Configuration c = null;
		if (list.size() > 0) {
			c = list.get(0);
		} else {
			c = new Configuration();
		}
			
		Map<Integer, Double> simPriceMap = new HashMap<Integer, Double>();
		simPriceMap.put(1, 9.5);
		simPriceMap.put(2, 6.5);
		c.setSimPrices(simPriceMap);
		

//		Configuration c = new Configuration();
		Map<Integer, Double> dataOptionMap = new HashMap<Integer, Double>();
		dataOptionMap.put(1, 0.0);
		dataOptionMap.put(2, 4.5);
		c.setDataOptionPrices(dataOptionMap);
		c.setReportProgress(10);
		if (list.size() == 0) {
			createObject(c);
		}

		
		tx.commit();
		
		session = SessionFactoryUtil.getInstance().getCurrentSession();
//		CardBean card = null;
		tx = session.beginTransaction();
		
		List<Configuration> configs = session.createQuery("from Configuration as configuration").list();
		if (configs != null && configs.size() > 0) {
			Configuration con = configs.get(0);
			Map<Integer, Double> simPrices = con.getSimPrices();
			Iterator<Integer> i = simPrices.keySet().iterator();
			System.out.println("SIM Prices:");
			while (i.hasNext()) {
				Integer key = i.next();
				System.out.println(key + ", " + simPrices.get(key));
			}

			Map<Integer, Double> dataOptionPrices = con.getDataOptionPrices();
			Iterator<Integer> it = dataOptionPrices.keySet().iterator();
			System.out.println("Data Option Prices:");
			while (it.hasNext()) {
				Integer key = it.next();
				System.out.println(key + ", " + dataOptionPrices.get(key));
			}
		
		}
		reconfigureCustomerInvoiceConf();
	}
	
	private static void reconfigureCustomerInvoiceConf() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		try {
			tx = session.beginTransaction();
			String select = "select distinct customer from Customer as customer where customer.name != ''";

			List<Customer> list = session.createQuery(select).list();
			for (Iterator it=list.iterator();it.hasNext();) {
				Customer customer = (Customer) it.next();
				customer.getInvoiceConfiguration().setSimPrice(1);
				customer.getInvoiceConfiguration().setDataOptionSurcharge(1);
			}

			tx.commit();
			
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}

		}
	}

}
