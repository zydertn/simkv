package de.abd.mda.persistence.dao.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class CardController extends DaoController implements IDaoController {

	private final static Logger LOGGER = Logger.getLogger(CardController.class .getName()); 

	public CardController() {
		LOGGER.info("Instantiate: CardController");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		LOGGER.info("Method: listObjects; select c from CARD as c");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<DaoObject> cards = null;
		try {
			tx = session.beginTransaction();
			cards = session.createQuery("select c from CARD as c").list();
			if (cards != null) {
				LOGGER.info(cards.size() + " cards found");
			}
			for (Iterator<DaoObject> iter = cards.iterator(); iter.hasNext();) {
				CardBean element = (CardBean) iter.next();
				System.out.println(element);
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
		
		return cards;
	}
	
	public CardBean searchCard(String cardNumFirst, String cardNumSecond, String phoneNrFirst, String phoneNrSecond, String searchCase) throws Exception {
		LOGGER.info("Method: searchCard");
		String select = "select distinct card from CardBean card";
		if (cardNumFirst != null && cardNumFirst.length() > 0) {
			select += " where card.cardNumberFirst = '" + cardNumFirst + "'";
			if (cardNumSecond != null && cardNumSecond.length() > 0) {
				 select += " and card.cardNumberSecond = '" + cardNumSecond +"'";
			}	
		} else if (phoneNrFirst != null && phoneNrFirst.length() > 0 && phoneNrSecond != null && phoneNrSecond.length() > 0) {
			select += " where card.phoneNrFirst = '" + phoneNrFirst + "' and card.phoneNrSecond = '" + phoneNrSecond +"'";
		} else {
			Exception e = new Exception("MandatoryInfoMissing");
			LOGGER.error("Exception: " + e);
			throw e;
		}

		return searchCard(select);
	}
	
	public CardBean searchCard(String select) {
		LOGGER.info("Method: searchCard; Select = " + select);
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
			
			if (list.size() > 0) {
				card = (CardBean) list.get(0);
				LOGGER.info("Card found: " + card.getCardnumberString());
			} else {
				LOGGER.warn("No card found!");
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
		return card;
	}

	public List<CardBean> searchCustomerCards(Integer customerID) {
		LOGGER.info("Method: searchCustomerCards; CustomerID = " + customerID);
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		List<CardBean> cards = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			if (customerID != null) {
				select += " where card.customer = '" + customerID + "'";
			} else {
				LOGGER.warn("CustomerID missing! No Cards found with this CustomerID");
				return null;
			}
			
			LOGGER.info("Select = " + select);
			cards = session.createQuery(select).list();
			if (cards != null) {
				LOGGER.info(cards.size() + " cards found");
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
		return cards;
	}
}