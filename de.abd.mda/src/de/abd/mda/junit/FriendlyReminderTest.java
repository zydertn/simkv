package de.abd.mda.junit;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.ReminderGenerator;

public class FriendlyReminderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReminderGenerator frg = new ReminderGenerator();
		Calendar calcMonth = Calendar.getInstance();
		CustomerController cc = new CustomerController();
		Customer customer = cc.findCustomer("20190");
//		frg.generateReport(null, customer, calcMonth, true, true, 0, null, null);
	}

}
