package de.abd.mda.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.DateUtils;

public class ClearingController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(ClearingController.class .getName());
	
	private CardBean card;

	public ClearingController() {
		LOGGER.info("Instantiate: ClearingController");
		card = new CardBean();
		card.setContactPerson(new Person());
		card.setInstallAddress(new Address());
	}

	public String unclearCard() {
		LOGGER.info("Method: unclearCard; Card: " + card.getCardnumberString());
		return changeActivationStatus(false);
	}

	
	public String clearCard() {
		LOGGER.info("Method: clearCard; Card: " + card.getCardnumberString());
		return changeActivationStatus(true);
	}
	
	public String changeActivationStatus(boolean activate) {
		LOGGER.info("Method: changeActivationStatus - activate = " + activate);
		CardBean screenCard = null;
		if (getSession().getAttribute("cardToUpdate") != null) {
			screenCard = (CardBean) getSession().getAttribute("cardToUpdate");
		}

		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		CardBean dbCard = null;
		try {
			tx = session.beginTransaction();
			String select = "select distinct card from CardBean card";
			if (screenCard.getCardNumberFirst() != null
					&& screenCard.getCardNumberFirst().length() > 0) {
				select += " where card.cardNumberFirst = '"
						+ screenCard.getCardNumberFirst()
						+ "' and card.cardNumberSecond = '"
						+ screenCard.getCardNumberSecond() + "'";
		}

			List<CardBean> list = session.createQuery(select).list();
			Iterator it = list.iterator();
	
			if (list.size() > 0)
				dbCard = (CardBean) list.get(0);

		String message = "";
				
		if (screenCard != null && dbCard != null) {
			if (activate) {
				// Aktivierung wurde geklickt
				if (screenCard.getActivationDate() != null) {
					if (dbCard.getActivationDate() == null) {
						// Erstmalige Aktivierung
						dbCard = activateCard(dbCard);
					} else {
						// Karte wurde schon einmal aktiviert
						if (dbCard.getDeactivationDate() == null) {
							// Karte wurde bisher nur aktiviert
							dbCard = activateCard(dbCard);
						} else {
							// Karte wurde bereits einmal deaktiviert
							if (screenCard.getActivationDate().after(dbCard.getDeactivationDate())) {
								dbCard = activateCard(dbCard);
							} else {
								message = "Aktivierung fehlgeschlagen! Aktivierungsdatum muss zeitlich nach Deaktivierungsdatum liegen!";
								LOGGER.warn(message);
								getRequest().setAttribute("message", message);
								return "";
							}
						}
					}
				} else {
					message = "Kein Aktivierungsdatum angegeben! Bitte prüfen Sie Ihre Eingabe!";
					LOGGER.warn(message);
					getRequest().setAttribute("message", message);
					return "";
				}
			} else {
				// Deaktivierung wurde geklickt
				if (screenCard.getDeactivationDate() != null) {
					if (dbCard.getStatus().equals(Model.STATUS_ACTIVE)) {
						if (screenCard.getDeactivationDate().after(dbCard.getActivationDate())) {
							// Deaktivierung ist ok
							dbCard = deactivate(dbCard);
						} else  {
							message = "Aktivierung fehlgeschlagen! Deaktivierungsdatum muss zeitlich nach Aktivierungsdatum liegen!";
							LOGGER.warn(message);
							getRequest().setAttribute("message", message);
							return "";
						}
					} else {
						if (dbCard.getStatus().equals(Model.STATUS_INACTIVE)) {
							if (screenCard.getDeactivationDate().after(dbCard.getActivationDate())) {
								// Deaktivierung ist ok
								dbCard = deactivate(dbCard);
							} else {
								message = "Aktivierung fehlgeschlagen! Deaktivierungsdatum muss zeitlich nach Aktivierungsdatum liegen!";
								LOGGER.warn(message);
								getRequest().setAttribute("message", message);
								return "";
							}							
						} else {
							message = "Karte ist Dummy! Keine Deaktivierung möglich!";
							LOGGER.warn(message);
							getRequest().setAttribute("message", message);
							return "";
						}
					}
				} else {
					message = "Kein Aktivierungsdatum angegeben! Bitte prüfen Sie Ihre Eingabe!";
					LOGGER.warn(message);
					getRequest().setAttribute("message", message);
					return "";
				}
			}
			
			//			searchCard.setContactPerson(card.getContactPerson());
			Person scp = dbCard.getContactPerson();
			Person cp = screenCard.getContactPerson();
			scp.setEmail(cp.getEmail());
			scp.setFirstname(cp.getFirstname());
			scp.setGender(cp.getGender());
			scp.setName(cp.getName());
			scp.setPhoneNrFirst(cp.getPhoneNrFirst());
			scp.setPhoneNrSecond(cp.getPhoneNrSecond());
			
//			searchCard.setInstallAddress(card.getInstallAddress());
			Address scia = dbCard.getInstallAddress();
			Address cia = screenCard.getInstallAddress();
			scia.setCity(cia.getCity());
			scia.setHousenumber(cia.getHousenumber());
			scia.setPostcode(cia.getPostcode());
			scia.setStreet(cia.getStreet());
			
			dbCard.setFactoryNumber(screenCard.getFactoryNumber());
			dbCard.setVpnProfile(screenCard.getVpnProfile());
//			dbCard.setOrderNumber(screenCard.getOrderNumber());
			dbCard.setProject(screenCard.getProject());
			dbCard.setComment(screenCard.getComment());
			
//			dbCard.setDeactivationDate(new Date());
		} else {
			message = "Karte für Aktivierung wurde nicht gefunden!";
			LOGGER.warn(message);
			getRequest().setAttribute("message", message);
			return "";
		}
		
		
		tx.commit();
		
//		getRequest().setAttribute("message", message);

		} catch (Exception e) {
			LOGGER.error("Exception: " + e);
			e.printStackTrace();
			return "";
		}
		
		return "success";
	}

	
	private CardBean deactivate(CardBean dbCard) {
		LOGGER.info("Method: deactivate; Card: " + dbCard.getCardnumberString());
		dbCard.setStatus(Model.STATUS_INACTIVE);
		dbCard.setDeactivationDate(card.getDeactivationDate());
		String message = "Karte " + card.getCardnumberString() + " wurde deaktiviert!";
		getRequest().setAttribute("message", message);
		getSession().removeAttribute("cardToUpdate");
		return dbCard;
	}

	private CardBean activateCard(CardBean dbCard) {
		LOGGER.info("Method: activateCard; Card: " + dbCard.getCardnumberString());
		dbCard.setStatus(Model.STATUS_ACTIVE);
		dbCard.setActivationDate(card.getActivationDate());
		String message = "Karte " + card.getCardnumberString() + " wurde aktiviert!";
		getRequest().setAttribute("message", message);
		getSession().removeAttribute("cardToUpdate");
		return dbCard;
	}

	public CardBean getCard() {
		if (getRequest().getAttribute("searchedCard") != null) {
			card = (CardBean) getRequest().getAttribute("searchedCard");
			if (card.getContactPerson() == null) {
				card.setContactPerson(new Person());
			}
			if (card.getInstallAddress() == null) {
				card.setInstallAddress(new Address());
			}
		}
		return card;
	}

	public void setCard(CardBean card) {
		this.card = card;
	}

	public Date getActivationDate() {
		if (getRequest().getAttribute("newActivationDateSet") == null && card != null) {
			if (card.getActivationDate() == null) {
				return null;
			}
		}

		return card.getActivationDate();
	}

	public void setActivationDate(Date activationDate) {
		card.setActivationDate(activationDate);
		getRequest().setAttribute("newActivationDateSet", true);
	}

	public Date getDeactivationDate() {
		if (getRequest().getAttribute("newDeactivationDateSet") == null && card != null) {
			if (card.getDeactivationDate() == null) {
				return null;
			}
		}

		return card.getDeactivationDate();
	}

	public void setDeactivationDate(Date deactivationDate) {
		card.setDeactivationDate(deactivationDate);
		getRequest().setAttribute("newDeactivationDateSet", true);
	}

	
}