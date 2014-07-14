package de.abd.mda.controller;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class SearchCardController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(SearchCardController.class .getName()); 

	private String cardNumberFirst;
	private String cardNumberSecond;
	private String phoneNrFirst;
	private String phoneNrSecond;
	private CardBean card;

	public SearchCardController() {
		LOGGER.info("Instantiate: SearchCardController");
	}

	public CardBean getCard() {
		return card;
	}

	public void setCard(CardBean card) {
		this.card = card;
	}

	public String searchCardClearing() {
		LOGGER.info("Method: searchCardClearing");
		try {
			if (performSearch("clearing") != null)
				return "clearing";
		} catch (Exception e) {
			LOGGER.error("Exception: " + e);
			getRequest().setAttribute("message", "Es muss entweder die Kartennummer oder die Rufnummer eingegeben werden!");
		}
		return "noCardFound";
	}

	public String searchCardViewDetails() {
		LOGGER.info("Method: searchCardClearing");
		try {
			if (performSearch("details") != null)
				return "viewDetails";
		} catch (Exception e) {
			LOGGER.error("Exception: " + e);
			getRequest().setAttribute("message", "Es muss entweder die Kartennummer oder die Rufnummer eingegeben werden!");
		}
		return "noCardFound";
	}

	public String updateCard() {
		LOGGER.info("Method: updateCard");
		getRequest().setAttribute("updateCard", card);
		return "updateCardDirect";
	}
	
	public String searchCard() {
		LOGGER.info("Method: searchCard");
		try {
			if (performSearch("") != null)
				return "success";
		} catch (Exception e) {
			LOGGER.error("Exception: " + e);
			getRequest().setAttribute("message", "Es muss entweder die Kartennummer oder die Rufnummer eingegeben werden!");
		}
		return "noCardFound";
	}
	
	public CardBean performSearch(String searchCase) throws Exception {
		LOGGER.info("Method: performSearch; CardNumber Values: " + cardNumberFirst + "-" + cardNumberSecond);
		
		CardController cardController = new CardController();
		card = cardController.searchCard(cardNumberFirst, cardNumberSecond, phoneNrFirst, phoneNrSecond, searchCase);
		if (card != null) {
			LOGGER.info("Card found; Adding to request and session");
			getRequest().setAttribute("searchedCard", card);
			getSession().setAttribute("cardToUpdate", card);
			return card;
		} else {
			LOGGER.warn("No card found in database; Entries to be checked!!");
			getRequest().setAttribute("message", "Keine Karte in der Datenbank gefunden! Prüfen Sie die Eingabe!");
			return card;
		}
	}
	
	public List<CardBean> performSearch(Integer customerID) {
		LOGGER.info("Method: performSearch; CustomerID = " + customerID);
		CardController cardController = new CardController();
		List<CardBean> cards = cardController.searchCustomerCards(customerID);
		if (cards != null) {
			LOGGER.info(cards.size() + " cards found for customer + " + customerID);
		} else {
			LOGGER.warn("No cards found for customerID " + customerID);
		}
		return cards;
	}
	
	public String saveComment() {
		LOGGER.info("Method: saveComment; Card = " + card.getCardnumberString());
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean searchCard = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			if (card.getCardNumberFirst() != null
					&& card.getCardNumberFirst().length() > 0) {
				select += " where card.cardNumberFirst = '"
						+ card.getCardNumberFirst()
						+ "' and card.cardNumberSecond = '"
						+ card.getCardNumberSecond() + "'";
			}

			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
	
			if (list.size() > 0)
				searchCard = (CardBean) list.get(0);

				
		if (searchCard != null) {
			searchCard.setComment(card.getComment());
			tx.commit();
			LOGGER.info("Comment saved on card " + card.getCardnumberString());
			LOGGER.info("Comment: " + card.getComment());
			getRequest().setAttribute("message", "Kommentar wurde gespeichert!");
		} else {
			tx.rollback();
			getRequest().setAttribute("message", "Fehler!!! Kommentar wurde nicht gespeichert!");
			LOGGER.warn("Comment was not saved on card " + card.getCardnumberString());
		}
		
		} catch (Exception e) {
			LOGGER.error("Exception " + e);
			tx.rollback();
			getRequest().setAttribute("message", "Fehler!!! Kommentar konnte nicht gespeichert werden!");
			return "failure";
		}

		return "success";
	}
	
	public String getSearchFieldBgColor() {
		if (getNoCardFound()) {
			return "Yellow";
		} else {
			return "LightGrey";
		}
	}

	public String getEnterFieldBgColor() {
		if (getNoCardFound()) {
			return "LightGrey";
		} else {
			return "Yellow";
		}
	}
	
	public boolean getNoCardFound() {
		return (getRequest().getAttribute("searchedCard") == null);
	}

	public boolean getCardFound() {
		return (getRequest().getAttribute("searchedCard") != null);
	}

	public boolean getNotSubmitted() {
		return (getRequest().getAttribute("searchSubmitted") == null);
	}
	
	public String getCardNumberFirst() {
		if (getSearchedCard() != null)
			return getSearchedCard().getCardNumberFirst();
		return cardNumberFirst;
	}

	public void setCardNumberFirst(String cardNumberFirst) {
		this.cardNumberFirst = cardNumberFirst;
	}

	public String getCardNumberSecond() {
		if (getSearchedCard() != null)
			return getSearchedCard().getCardNumberSecond();
		return cardNumberSecond;
	}

	public void setCardNumberSecond(String cardNumberSecond) {
		this.cardNumberSecond = cardNumberSecond;
	}

	public String getPhoneNrFirst() {
		if (getSearchedCard() != null)
			return getSearchedCard().getPhoneNrFirst();
		return phoneNrFirst;
	}

	public void setPhoneNrFirst(String phoneNrFirst) {
		this.phoneNrFirst = phoneNrFirst;
	}

	public String getPhoneNrSecond() {
		if (getSearchedCard() != null)
			return getSearchedCard().getPhoneNrSecond();
		return phoneNrSecond;
	}

	public void setPhoneNrSecond(String phoneNrSecond) {
		this.phoneNrSecond = phoneNrSecond;
	}
	
	private CardBean getSearchedCard() {
		if (getRequest().getAttribute("searchedCard") != null)
			return (CardBean) getRequest().getAttribute("searchedCard");
		else return null;
	}

}