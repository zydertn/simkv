package de.abd.mda.junit;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class TestCalc {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction tx = null;
		tx = session.beginTransaction();
		String select = "select distinct card from CardBean card where card.customer = '2597' and ((card.status = 'aktiv' and card.activationDate < '2015-01-01 00:00:00') or (card.status = 'Inaktiv' and (card.deactivationDate >= '2014-07-01 00:00:00' or card.deactivationDate < '2015-01-01 00:00:00')))";
		List<DaoObject> objects = null;
		objects = (List<DaoObject>) session.createQuery(select).list();
		for (DaoObject d: objects) {
			CardBean c = (CardBean) d;
			if (c.getCardNumberFirst().equals("113052950")) {
				System.out.println("Jetzt");
			}
		}
	}

}
