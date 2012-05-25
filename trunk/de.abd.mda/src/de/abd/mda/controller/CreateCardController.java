package de.abd.mda.controller;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class CreateCardController extends ActionController {

	public CardBean ccCardBean;

	public CardBean getCcCardBean() {
		return ccCardBean;
	}

	public void setCcCardBean(CardBean ccCardBean) {
		this.ccCardBean = ccCardBean;
	}

	public void createCard() {
		CardController cardController = new CardController();

		String message = "";
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
//			String select = "select distinct card from CardBean card";
			if (ccCardBean.getCardNumberFirst() != null
					&& ccCardBean.getCardNumberFirst().length() > 0
					&& ccCardBean.getCardNumberSecond() != null
					&& ccCardBean.getCardNumberSecond().length() > 0) {
//				select += " where card.cardNumberFirst = '"
//						+ ccCardBean.getCardNumberFirst()
//						+ "' and card.cardNumberSecond = '"
//						+ ccCardBean.getCardNumberSecond() + "'";
			} else {
				message = "Beide Kartennummern-Teile müssen befüllt werden! Karte wurde nicht in DB angelegt!";
				getRequest().setAttribute("message", message);
				return;
			}
//			List<CardBean> list = session.createQuery(select).list();
//			Iterator it = list.iterator();
//
//			if (list.size() > 0)
//				card = (CardBean) list.get(0);
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

//		if (card != null) {
//			tx.commit();
//			message = "Karte existiert bereits in der Datenbank!";
//		} else {
//			tx.commit();
//
//			tx = session.beginTransaction();
			String select = "select sequenceNumber from SequenceNumber sequenceNumber";
			List<SequenceNumber> list = session.createQuery(select).list();
			Iterator it = list.iterator();

			int currentSequenceNumber = -1;
			SequenceNumber sn = null;
			if (list.size() > 0) {
				sn = (SequenceNumber) list.get(0);
				currentSequenceNumber = sn.getSequenceNumber();
				currentSequenceNumber++;
				sn.setSequenceNumber(currentSequenceNumber);
			} else {
				currentSequenceNumber = 0;
				sn = new SequenceNumber();
				sn.setSequenceNumber(currentSequenceNumber);
				cardController.createObject(sn);
			}
						
			ccCardBean.setSequenceNumber(currentSequenceNumber);
			String retMessage = cardController.createObject(ccCardBean);
			if (retMessage != null && retMessage.length() == 0) {
//				tx.commit();			
				
				message = "Neue Karte wurde erfolgreich angelegt! Kartennummer:"
						+ ccCardBean.getCardNumberFirst() + " - "
						+ ccCardBean.getCardNumberSecond() + "; Telefonnummer: "
						+ ccCardBean.getPhoneString();

				getRequest().setAttribute("message", message);
			} else
				getRequest().setAttribute("message", retMessage);
//				tx.rollback();
//		}


		ccCardBean = new CardBean();
	}
	
	public String updateCard() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean card = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			select += " where card.cardNumberFirst = '" + ccCardBean.getCardNumberFirst() + "' and card.cardNumberSecond = '" + ccCardBean.getCardNumberSecond() +"'";
	
			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
			
			if (list.size() > 0) {
				card = (CardBean) list.get(0);
				card.setPhoneNrFirst(ccCardBean.getPhoneNrFirst());
				card.setPhoneNrSecond(ccCardBean.getPhoneNrSecond());
				card.setSupplier(ccCardBean.getSupplier());
				card.setStatus(ccCardBean.getStatus());
			} else
				getRequest().setAttribute("message", "Keine Karte in der Datenbank gefunden!");
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
		getRequest().setAttribute("message", "Änderung gespeichert!");
		
		return "";
	}

	public String createCardSearch() {
		SearchCardController scc = new SearchCardController();
		scc.setCardNumberFirst(ccCardBean.getCardNumberFirst());
		scc.setCardNumberSecond(ccCardBean.getCardNumberSecond());
		scc.setPhoneNrFirst(ccCardBean.getPhoneNrFirst());
		scc.setPhoneNrSecond(ccCardBean.getPhoneNrSecond());
		try {
			ccCardBean = scc.performSearch("createCard");
		} catch (Exception e) {
			getRequest().setAttribute("message", "Eine vollständige Kartennummer muss für die Suche eingegeben werden!");
			return "noCardFound";
		}
		if (ccCardBean != null) {
			return "cardFound";
		} else {
			ccCardBean = new CardBean();
			ccCardBean.setCardNumberFirst(scc.getCardNumberFirst());
			ccCardBean.setCardNumberSecond(scc.getCardNumberSecond());
			return "noCardFound";
		}
	}

	public String createCardNext() {
		createCard();
		ccCardBean = new CardBean();
		return null;
	}

	public String createCardFinish() {
		createCard();
		ccCardBean = new CardBean();
		return "finish";
	}

	public CardBean getCardBean() {
		if (getRequest().getAttribute("clearCard") != null && (Boolean) getRequest().getAttribute("clearCard")) {
			ccCardBean = new CardBean();
			getRequest().removeAttribute("clearCard");
		}
		return ccCardBean;
	}

	public void setCardBean(CardBean cardBean) {
		this.ccCardBean = cardBean;
	}

}