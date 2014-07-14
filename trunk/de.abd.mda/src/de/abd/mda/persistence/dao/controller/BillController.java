package de.abd.mda.persistence.dao.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.controller.CardActionController;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Util;
import de.abd.mda.persistence.dao.controller.DaoController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class BillController extends DaoController {

	private final static Logger LOGGER = Logger.getLogger(BillController.class .getName()); 

	private int maxBillNumber;
	private Bill bill;

	public BillController() {
		LOGGER.info("Instatiate: BillController");
	}
	
	public Integer getMaxBillNumber() {
		LOGGER.info("Method: getMaxBillNumber");
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();

		List<Util> utils = session.createQuery("from Util as util").list();
//		tx.commit();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);
			LOGGER.info("MaxBillNumber = " + util.getMaxBillNumber());
			return util.getMaxBillNumber();
		}
		LOGGER.warn("No MaxBillNumber found");
		return null;
	}

	public boolean createOrUpdateObject(Bill bill) {
		LOGGER.info("Method: createOrUpdateObject");
		Bill dbBill = findBill(bill);
		if (dbBill == null) {
			LOGGER.warn("No bill found... creating new bill...");
			increaseBillNumber();
			bill.setBillNumber(getMaxBillNumber());
			createObject(bill);
			LOGGER.info("Bill created successfully with billNumber = " + bill.getBillNumber());
			return true;
		} else {
			dbBill.setFile(bill.getFile());
			LOGGER.info("Updated file for bill " + dbBill.getBillNumber());
			return false;
		}
	}
	
	public Bill findBill(Bill bill) {
		LOGGER.info("Method: findBill; bill.customerNumber = " + bill.getCustomerNumber() + ", bill.year = " + bill.getYear() + ", bill.month = " + bill.getMonth() + ", bill.mapCount = " + bill.getMapCount());
		String select = "select distinct bill from Bill bill";
		select += " where bill.customerNumber = '" + bill.getCustomerNumber() + "'";
		select += " and bill.year = '" + bill.getYear() + "'";
		select += " and bill.month = '" + bill.getMonth() +"'";
		select += " and bill.mapCount = '" + bill.getMapCount() + "'"; 
		
		List<Bill> list = createListQuery(select);
		Iterator it = list.iterator();
		Bill dbBill = null;
		if (list.size() > 0) {
			dbBill = list.get(0);
			LOGGER.info("Bill found: Bill number = " + dbBill.getBillNumber());
		}
		
		return dbBill;
	}
	
	public List<Bill> findCustomerBills(int customerNumber) {
		LOGGER.info("Method: findCustomerBills");
		String select = "select distinct bill from Bill bill";
		select += " where bill.customerNumber = '" + customerNumber + "'";
		List<Bill> bills = createListQuery(select);
		if (bills != null) {
			LOGGER.info(bills.size() + " bills found");
		}
		return bills;
	}

	public List<Bill> findMonthBills(int year, int month) {
		LOGGER.info("Method: findMonthBills");
		String select = "select distinct bill from Bill bill";
		select += " where bill.year = '" + year + "'";
		select += " and bill.month = '" + month + "'";
		List<Bill> bills = createListQuery(select);
		if (bills != null) {
			LOGGER.info(bills.size() + " bills found");
		}
		return bills;
	}
	
	private List<Bill> createListQuery(String select) {
		LOGGER.info("Method: createListQuery; Select = " + select);
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.getTransaction();
		
		if (!(tx != null && tx.isActive())) {
			tx = session.beginTransaction();
		}

		return session.createQuery(select).list();
	}
	
	public boolean increaseBillNumber() {
		LOGGER.info("Method: increaseBillNumber in utils");
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		
		List<Util> utils = session.createQuery("from Util as util").list();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);
			int mbn = util.getMaxBillNumber();
			mbn++;
			util.setMaxBillNumber(mbn);
			LOGGER.info("New maxBillNumber = " + mbn);
			return true;
		}
		
		return false;
	}

	
	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public void setMaxBillNumber(int maxBillNumber) {
		this.maxBillNumber = maxBillNumber;
	}

	

}
