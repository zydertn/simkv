package de.abd.mda.importExcel;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.controller.SearchCardController;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class UpdateTimestamp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String select = "select distinct card from CardBean card where card.cardNumberFirst != null";
		
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
			
			System.out.println("List size == " + list.size());
			Thread.sleep(1000);
			if (list.size() > 0)
				while (it.hasNext()) {
					card = (CardBean) it.next();
					System.out.println(card.getCardnumberString());
					if (card.getDeliverySlipDate() != null) {
						card.getDeliverySlipDate().setHours(2);
					}
					if (card.getActivationDate() != null) {
						System.out.println("Activation Date: " + card.getCardnumberString() + ", " + card.getActivationDate());
						card.getActivationDate().setHours(2);
					}
//					if (card.getLastCalculationDate() != null) {
//						System.out.println("CalculationDate: " + card.getCardnumberString() + ", " + card.getLastCalculationDate());
//						card.getLastCalculationDate().setHours(2);
//					}
				}
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

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}

}
