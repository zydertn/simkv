package de.abd.mda.report;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;

public interface IReportGenerator {

	public boolean generateReport(List<DaoObject> customerCards, Customer customer, Calendar calcMonth, boolean flatrateCalc, boolean severalBills, int mapCount, Date calcDate);
	
}
