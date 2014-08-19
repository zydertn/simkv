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

import de.abd.mda.controller.CustomerActionController;
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.InvoiceConfiguration;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CountryController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

import jxl.demo.CSV;

public class CountryModelCreator {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(CountryModelCreator.class);
			
		public static void main(String[] args) {
			CountryModelCreator c = new CountryModelCreator();
			List<Country> countries = c.getCountriesToCreate();
			c.createCountriesInDB(countries);
		}
		
	private List<Country> getCountriesToCreate() {
			List<Country> countries = new ArrayList<Country>();
			countries.add(new Country("Deutschland", "Deutschland", "DE", "+49"));
			countries.add(new Country("D‰nemark", "Denmark", "DK", "+45"));
			countries.add(new Country("Irland", "Ireland","IE", "+353"));
			countries.add(new Country("Groﬂbritannien", "Great Britain", "GB", "+44"));
			countries.add(new Country("÷sterreich", "÷sterreich", "AT", "+43"));
			countries.add(new Country("Polen", "Poland", "PL", "+48"));
			countries.add(new Country("Rum‰nien", "Romania", "RO", "+40"));
			countries.add(new Country("Tschechien", "Czech Republic","CZ", "+420"));
			return countries;
		}

	public void createCountriesInDB(List<Country> countries) {
		CountryController cc = new CountryController();
		for (Country country : countries) {
			cc.createObject(country);
		}
	}
	

}