package de.abd.mda.persistence.dao.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.controller.DaoController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class ConfigurationController extends DaoController {

	private String simPriceKey;
	private String dataOptionKey;
	private Double simPrice;
	private Double dataOptionPrice;
	
	public void updateConfiguration(Map<Integer, Double> simPriceMap, Map<Integer, Double> dataOptionMap) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();
		String select = "from Configuration";

		@SuppressWarnings("unchecked")
		List<Configuration> list = session.createQuery(select).list();
		Configuration c = null;
		if (list.size() > 0) {
			c = new Configuration();	
		} else {
			c = list.get(0);
		}
		
		if (simPriceMap != null) {
			c.setSimPrices(simPriceMap);
		}
		

		if (dataOptionMap != null) {
			c.setDataOptionPrices(dataOptionMap);
			
		}

		tx.commit();
	}
	
	public Map<Integer, Double> getSimPricesFromDB() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();

		List<Configuration> configs = session.createQuery("from Configuration as configuration").list();
		if (configs != null && configs.size() > 0) {
			Configuration con = configs.get(0);
			return con.getSimPrices();
		} else {
			return new HashMap<Integer, Double>();
		}
	}

	public Map<Integer, Double> getDataOptionPricesFromDB() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();

		List<Configuration> configs = session.createQuery("from Configuration as configuration").list();
		if (configs != null && configs.size() > 0) {
			Configuration con = configs.get(0);
			return con.getDataOptionPrices();
		} else {
			return new HashMap<Integer, Double>();
		}
	}

	public String getSimPriceKey() {
		return simPriceKey;
	}

	public void setSimPriceKey(String simPriceKey) {
		this.simPriceKey = simPriceKey;
	}

	public String getDataOptionKey() {
		return dataOptionKey;
	}

	public void setDataOptionKey(String dataOptionKey) {
		this.dataOptionKey = dataOptionKey;
	}

	public Double getSimPrice() {
		return simPrice;
	}

	public void setSimPrice(Double simPrice) {
		this.simPrice = simPrice;
	}

	public Double getDataOptionPrice() {
		return dataOptionPrice;
	}

	public void setDataOptionPrice(Double dataOptionPrice) {
		this.dataOptionPrice = dataOptionPrice;
	}

}
