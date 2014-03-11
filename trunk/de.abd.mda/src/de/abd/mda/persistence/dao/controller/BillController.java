package de.abd.mda.persistence.dao.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.dao.Util;
import de.abd.mda.persistence.dao.controller.DaoController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class BillController extends DaoController {

	private int maxBillNumber;
	private Bill bill;
	
	public Integer getMaxBillNumber() {
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();

		List<Util> utils = session.createQuery("from Util as util").list();
//		tx.commit();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);
			return util.getMaxBillNumber();
		}
		return null;
	}

	public boolean createOrUpdateObject(Bill bill) {
		Bill dbBill = findBill(bill);
		if (dbBill == null) {
			System.out.println("No bill found... creating bill...");
			increaseBillNumber();
			bill.setBillNumber(getMaxBillNumber());
			createObject(bill);
			return true;
		} else {
			dbBill.setFile(bill.getFile());
			return false;
		}
	}
	
	public Bill findBill(Bill bill) {
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
		}
		
		return dbBill;
	}
	
	public List<Bill> findCustomerBills(int customerNumber) {
		String select = "select distinct bill from Bill bill";
		select += " where bill.customerNumber = '" + customerNumber + "'";
		return createListQuery(select);
	}

	public List<Bill> findMonthBills(int year, int month) {
		String select = "select distinct bill from Bill bill";
		select += " where bill.year = '" + year + "'";
		select += " and bill.month = '" + month + "'";
		return createListQuery(select);
	}
	
	private List<Bill> createListQuery(String select) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.getTransaction();
		
		if (!(tx != null && tx.isActive())) {
			tx = session.beginTransaction();
		}

		return session.createQuery(select).list();
	}
	
	public boolean increaseBillNumber() {
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		
		List<Util> utils = session.createQuery("from Util as util").list();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);
			int mbn = util.getMaxBillNumber();
			mbn++;
			util.setMaxBillNumber(mbn);
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
