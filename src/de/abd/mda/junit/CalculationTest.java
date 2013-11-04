package de.abd.mda.junit;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.util.DateUtils;

public class CalculationTest {

	public CalculationTest() {
		ReportCalculator rp = new ReportCalculator();
		rp.calculate();
	}
	
	
	public static void main(String args[]) {
		Calendar now = Calendar.getInstance();
		now.set(2012, Calendar.JANUARY, 1);
		System.out.println(DateUtils.getMonthAsString(now.get(Calendar.MONTH)));
		now.set(2012, Calendar.DECEMBER, 1);
		System.out.println(DateUtils.getMonthAsString(now.get(Calendar.MONTH)));
		
		new CalculationTest();
	}
	
}
