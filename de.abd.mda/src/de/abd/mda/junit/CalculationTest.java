package de.abd.mda.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.controller.CustomerActionController;
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.util.DateUtils;

public class CalculationTest {

	public CalculationTest() {
		ReportCalculator rp = new ReportCalculator();
		int month = 6;
		int year = 2014;
		int customerNumber = 20246;
//		rp.calculate();
		CustomerController cc = new CustomerController();
		Customer customer = cc.findCustomer("" + customerNumber);
		int calc = rp.monthCalc(customer, 0, month, year);
		if (calc > 0) {
			showCustomerMonthBills(customerNumber, month, year);
		}
	}
	
	
	public static void main(String args[]) {
		new CalculationTest();
	}
	
	private void showCustomerMonthBills(int customerNumber, int month, int year) {
		BillController bc = new BillController();
		List<Bill> bills = bc.findCustomerMonthBills(customerNumber, month, year);
		
		if (bills != null) {

			System.out.println("Gefundene Rechnungen: " + bills.size());

			for (Bill bill: bills) {
				File file = new File(bill.getFilename());
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
					fos.write(bill.getFile());
					fos.flush();
					fos.close();
					Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler " + bill.getFilename());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
