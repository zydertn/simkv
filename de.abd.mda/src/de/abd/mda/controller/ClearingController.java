package de.abd.mda.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class ClearingController extends ActionController {

	private CardBean card;

	public ClearingController() {
		card = new CardBean();
	}

	public String clearCard() {
		CardBean updateCard = null;
		if (getSession().getAttribute("cardToUpdate") != null) {
			updateCard = (CardBean) getSession().getAttribute("cardToUpdate");
			getSession().removeAttribute("cardToUpdate");
		}

		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean searchCard = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			if (updateCard.getCardNumberFirst() != null
					&& updateCard.getCardNumberFirst().length() > 0) {
				select += " where card.cardNumberFirst = '"
						+ updateCard.getCardNumberFirst()
						+ "' and card.cardNumberSecond = '"
						+ updateCard.getCardNumberSecond() + "'";
			}

			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
	
			if (list.size() > 0)
				searchCard = (CardBean) list.get(0);

				
		if (searchCard != null) {
			searchCard.setContactPerson(card.getContactPerson());
			searchCard.setInstallAddress(card.getInstallAddress());
			searchCard.setFactoryNumber(card.getFactoryNumber());
			searchCard.setVpnProfile(card.getVpnProfile());
		}
		
		if (!searchCard.getStatus().equals(Model.STATUS_ACTIVE)) {
			searchCard.setStatus(Model.STATUS_ACTIVE);
			searchCard.setActivationDate(new Date());
		}
		
		tx.commit();
		
		String message = "Karte " + card.getCardnumberString() + " wurde aktiviert!";
		getRequest().setAttribute("message", message);

		} catch (Exception e) {
			e.printStackTrace();
			return "failure";
		}
		
		return "success";
	}

	public CardBean getCard() {
		return card;
	}

	public void setCard(CardBean card) {
		this.card = card;
	}

}