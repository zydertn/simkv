package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.CountryComparator;
import de.abd.mda.util.CustomerComparator;


public class CountryController extends DaoController implements IDaoController {
	
	private final static Logger LOGGER = Logger.getLogger(CountryController.class .getName()); 

	public CountryController() {
		LOGGER.info("Instantiate CountryController");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		LOGGER.info("Method listObjects");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> countries = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct country from Country as country where country.name != ''";
//			" inner join customer.address as address where customer.name != ''");

			LOGGER.info("Select = " + select);
			countries = session.createQuery(select).list();
			if (countries != null) {
				LOGGER.info(countries.size() + " countries found");
			} else {
				LOGGER.warn("No countries found!");
			}

			tx.commit();
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("HibernateException: Error rolling back transaction; " + e1);
				}
				// throw again the first exception
				throw e;
			}

		}
		
		return countries;
	}

	public List<Country> listCountries() {
		List<DaoObject> daoCountries = listObjects();
		List<Country> countries = new ArrayList<Country>();
		for (Iterator it=daoCountries.iterator();it.hasNext();) {
			Country country = (Country) it.next();
			countries.add(country);
		}

		Comparator<DaoObject> comparator = new CountryComparator(); 
		Collections.sort(countries, comparator);

		return countries;
	}
	
	public Country findCountry(String countryName) {
		LOGGER.info("Method findCountry; CountryName = " + countryName);
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> countries = null;
		Transaction tx = null;
		boolean transactionStarted = false;
		if (session.getTransaction() == null || !session.getTransaction().isActive()) {
			tx = session.beginTransaction();
			transactionStarted = true;
		} else {
			tx = session.getTransaction();
		}
		try {
			String whereClause = "";
			if (countryName != null && countryName.length() > 0) {
				whereClause = " where country.name = '" + countryName + "'";
			}

			LOGGER.info("Select = from Country as country" + whereClause);
			countries = session.createQuery("from Country as country" + whereClause).list();
			if (countries != null) {
				LOGGER.info(countries.size() + " countries found");
			} else {
				LOGGER.warn("No country found!");
			}
			if (transactionStarted)
				tx.commit();
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("HibernateException: Error rolling back transaction; " + e1);
				}
				// throw again the first exception
				throw e;
			}

		}
		Country country = (Country) countries.get(0); 
		if (country != null) {
			LOGGER.info("Country found: Name = " + country.getName() + ", shortName = " + country.getShortName());
		} else {
			LOGGER.warn("No country found!");
		}
		return country;
	}

}