package de.abd.mda.report;

import java.util.Calendar;
import java.util.List;

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;

public interface IReportGenerator {

	public boolean generateReport(List<DaoObject> customerCards, Customer customer, Calendar calcMonth);
	
}
