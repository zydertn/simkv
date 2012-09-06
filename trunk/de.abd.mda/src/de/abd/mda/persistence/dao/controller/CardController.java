package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class CardController extends DaoController implements IDaoController {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> cards = null;
		try {
			tx = session.beginTransaction();
			cards = session.createQuery("select c from CARD as c").list();
			for (Iterator<DaoObject> iter = cards.iterator(); iter.hasNext();) {
				CardBean element = (CardBean) iter.next();
				System.out.println(element);
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}

		}
		
		return cards;
	}
	
	public CardBean searchCard(String cardNumFirst, String cardNumSecond, String phoneNrFirst, String phoneNrSecond, String searchCase) throws Exception {
		String select = "select distinct card from CardBean card";
		if (cardNumFirst != null && cardNumFirst.length() > 0 && cardNumSecond != null && cardNumSecond.length() > 0) {
			select += " where card.cardNumberFirst = '" + cardNumFirst + "' and card.cardNumberSecond = '" + cardNumSecond +"'";
		} else if (phoneNrFirst != null && phoneNrFirst.length() > 0 && phoneNrSecond != null && phoneNrSecond.length() > 0) {
			select += " where card.phoneNrFirst = '" + phoneNrFirst + "' and card.phoneNrSecond = '" + phoneNrSecond +"'";
		} else {
			Exception e = new Exception("MandatoryInfoMissing");
			throw e;
		}
		return searchCard(select);
	}
	
	public CardBean searchCard(String select) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
			
			if (list.size() > 0)
				card = (CardBean) list.get(0);
			else
				System.out.println("No card found");
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}

		}
		return card;
	}
}