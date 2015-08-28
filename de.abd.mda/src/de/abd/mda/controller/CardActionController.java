package de.abd.mda.controller;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class CardActionController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(CardActionController.class .getName()); 
	
	public CardBean ccCardBean;
	private HtmlSelectOneMenu invoiceconfigSimpriceBinding;
	private HtmlInputHidden cardPriceHidden;
	private HtmlInputHidden cardTypeAutHidden;
	private HtmlInputHidden cardTypeDeHidden;	
	private HtmlInputHidden cardActivatedAsHidden;
	private boolean selected = true;
	private String firstName = "";

	public CardActionController() {
		LOGGER.info("Instatiate: CardActionController");
	}
	

	
	public void createCard() {
		LOGGER.info("Method: createCard");
		CardController cardController = new CardController();

		String message = "";
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		try {
			tx = session.beginTransaction();
			if (ccCardBean.getCardNumberFirst() != null
					&& ccCardBean.getCardNumberFirst().length() > 0
					&& ccCardBean.getCardNumberSecond() != null
					&& ccCardBean.getCardNumberSecond().length() > 0) {
			} else {
				message = "Beide Kartennummern-Teile müssen befüllt werden! Karte wurde nicht in DB angelegt!";
				LOGGER.warn(message);
				getRequest().setAttribute("message", message);
				return;
			}
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("HibernateException: Error rolling back transaction; "+ e);
				}
				// throw again the first exception
				throw e;
			}

		}

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
			try {
				if (!ccCardBean.setIPAdressString()) {
					message = "Inkorrekte IP-Adresse! Karte wurde nicht angelegt.";
					getRequest().setAttribute("message", message);
					return;
				}
			} catch (NumberFormatException nfe) {
				message = "Inkorrekte IP-Adresse! Karte wurde nicht angelegt.";
				getRequest().setAttribute("message", message);
				return;
			}
			updateTypeInfo();
			String retMessage = cardController.createObject(ccCardBean);
			if (retMessage != null && retMessage.length() == 0) {
				
				message = "Neue Karte wurde erfolgreich angelegt! Kartennummer:"
						+ ccCardBean.getCardNumberFirst() + " - "
						+ ccCardBean.getCardNumberSecond() + "; Telefonnummer: "
						+ ccCardBean.getPhoneString();

				getRequest().setAttribute("message", message);
				LOGGER.info(message);
			} else {
				getRequest().setAttribute("message", retMessage);
				LOGGER.warn(retMessage);
			}
			tx.commit();
			ccCardBean = new CardBean();
	}
	
	public String updateCard() {
		LOGGER.info("Method: updateCard");
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
				card.setIpFirst(ccCardBean.getIpFirst());
				card.setIpSecond(ccCardBean.getIpSecond());
				card.setIpThird(ccCardBean.getIpThird());
				card.setIpFourth(ccCardBean.getIpFourth());
				try {
					if (!card.setIPAdressString()) {
						String message = "Inkorrekte IP-Adresse! Karte wurde nicht angelegt.";
						getRequest().setAttribute("message", message);
						return "";
					}
				} catch (NumberFormatException nfe) {
					String message = "Inkorrekte IP-Adresse! Karte wurde nicht angelegt.";
					getRequest().setAttribute("message", message);
					return "";
				}
				card.setSupplier(ccCardBean.getSupplier());
				card.setStatus(ccCardBean.getStatus());
				card.setStandardPrice(ccCardBean.getStandardPrice());
				card.setFlatrateCard(ccCardBean.getFlatrateCard());
				card.setSimPrice(ccCardBean.getSimPrice());
				card.setInvoiceRows(ccCardBean.getInvoiceRows());
				card.setRelation(ccCardBean.getRelation());
				card.setAnlagenNr(ccCardBean.getAnlagenNr());
				card.setAuftragsNr(ccCardBean.getAuftragsNr());
				card.setEinsatzort(ccCardBean.getEinsatzort());
				card.setEquipmentNr(ccCardBean.getEquipmentNr());
				card.setFactoryNumber(ccCardBean.getFactoryNumber());
				card.setKostenstelle(ccCardBean.getKostenstelle());
				card.setLeitstand(ccCardBean.getLeitstand());
				card.setLokation(ccCardBean.getLokation());
				card.setNotrufNr(ccCardBean.getNotrufNr());
//				card.setOrderNumber(ccCardBean.getOrderNumber());
				card.setSachkonto(ccCardBean.getSachkonto());
				card.setSoNr(ccCardBean.getSoNr());
				card.setVertrag(ccCardBean.getVertrag());
				card.setWe(ccCardBean.getWe());
				card.setBestellNummer(ccCardBean.getBestellNummer());
				card.setPin(ccCardBean.getPin());
				updateTypeInfo();
				card.setCardAutActivatedAs(ccCardBean.getCardAutActivatedAs());
				card.setCardAutType(ccCardBean.getCardAutType());
				card.setCardDeType(ccCardBean.getCardDeType());
				card.setBaNummer(ccCardBean.getBaNummer());
				LOGGER.info("Karte aktualisiert: " + card.getCardnumberString());
				getRequest().setAttribute("message", "Änderung gespeichert!");
			} else {
				String message = "Keine Karte in der Datenbank gefunden!";
				getRequest().setAttribute("message", message + " " + ccCardBean.getCardnumberString());
				LOGGER.warn(message);
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
		return "";
	}

	private void updateTypeInfo() {
		LOGGER.info("Method: updateTypeInfo; Karte: " + ccCardBean.getCardnumberString());
		String supp = ccCardBean.getSupplier();
		if (supp != null && supp.equals(Model.SUPPLIER_TELEKOM)) {
			ccCardBean.setCardAutType("");
			ccCardBean.setCardAutActivatedAs("");
		} else if (supp != null && supp.equals(Model.SUPPLIER_TELEKOM_AUSTRIA)) {
			ccCardBean.setCardDeType("");
		}
		String cardAutType = ccCardBean.getCardAutType();
		// Prüfung, ob der Typ einer Austria Karte == EU oder NTS ist; wenn nicht, dann muss ActivatedAs auf leer gesetzt werden
		if (cardAutType != null && !(cardAutType.equals(getModel().getCardAut4()) || (cardAutType.equals(getModel().getCardAut5())))) {
			ccCardBean.setCardAutActivatedAs("");
		}
	}

	public String deleteCard() {
		LOGGER.info("Method: deleteCard; Karte: " + ccCardBean.getCardnumberString());
		CardController controller = new CardController();
		controller.deleteObject(ccCardBean);
		getRequest().setAttribute("message", "Karte wurde erfolgreich gelöscht!");
		return "";
	}
	
	public String createCardSearch() {
		LOGGER.info("Method: createCardSearch; Karte: " + ccCardBean.getCardnumberString() + "; " + ccCardBean.getPhoneString());
		SearchCardController scc = new SearchCardController();
		scc.setCardNumberFirst(ccCardBean.getCardNumberFirst());
		scc.setCardNumberSecond(ccCardBean.getCardNumberSecond());
		scc.setPhoneNrFirst(ccCardBean.getPhoneNrFirst());
		scc.setPhoneNrSecond(ccCardBean.getPhoneNrSecond());
		try {
			ccCardBean = scc.performSearch("createCard");
		} catch (Exception e) {
			String message = "Eine vollständige Kartennummer muss für die Suche eingegeben werden!";
			getRequest().setAttribute("message", message);
			LOGGER.warn("Exception: " + message + "; " + e);
			return "noCardFound";
		}
		if (ccCardBean != null) {
			ccCardBean.extractIPSegments();
			return "cardFound";
		} else {
			ccCardBean = new CardBean();
			ccCardBean.setCardNumberFirst(scc.getCardNumberFirst());
			ccCardBean.setCardNumberSecond(scc.getCardNumberSecond());
			return "noCardFound";
		}
	}




	public String createCardNext() {
		LOGGER.info("Method: createCardNext");
		createCard();
		ccCardBean = new CardBean();
		return null;
	}

	public String createCardFinish() {
		LOGGER.info("Method: createCardFinish");
		createCard();
		ccCardBean = new CardBean();
		return "finish";
	}

	public CardBean getCardBean() {
		if (getRequest().getAttribute("clearCard") != null && (Boolean) getRequest().getAttribute("clearCard")) {
			ccCardBean = new CardBean();
			getRequest().removeAttribute("clearCard");
		}
		if (getRequest().getAttribute("updateCard") != null) {
			System.out.println("getCardBean updateCard im Request");
			CardBean card = (CardBean) getRequest().getAttribute("updateCard");
			return card;
		}
			
		return ccCardBean;
	}

	public void setCardBean(CardBean cardBean) {
		this.ccCardBean = cardBean;
	}

	public HtmlSelectOneMenu getInvoiceconfigSimpriceBinding() {
		return invoiceconfigSimpriceBinding;
	}

	public void setInvoiceconfigSimpriceBinding(
			HtmlSelectOneMenu invoiceconfigSimpriceBinding) {
		this.invoiceconfigSimpriceBinding = invoiceconfigSimpriceBinding;
	}
	
	public void priceCheckboxAction(ValueChangeEvent evt) {
		invoiceconfigSimpriceBinding.setDisabled(false);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public String getBoxValueDescription() {
		if (ccCardBean.getStandardPrice())
			return "selected";
		else
			return "unselected";
	}
	
	public void processClickEvent(AjaxBehaviorEvent e) {
		Boolean b = (Boolean) e.getComponent().getAttributes().get("standardPriceCheckvalue");
		invoiceconfigSimpriceBinding.setDisabled(b);
		ccCardBean.setStandardPrice(false);
		invoiceconfigSimpriceBinding.setRendered(false);
	}

	public HtmlInputHidden getCardPriceHidden() {
		return cardPriceHidden;
	}

	public void setCardPriceHidden(HtmlInputHidden cardPriceHidden) {
		this.cardPriceHidden = cardPriceHidden;
	}

	public HtmlInputHidden getCardTypeAutHidden() {
		return cardTypeAutHidden;
	}

	public void setCardTypeAutHidden(HtmlInputHidden cardTypeAutHidden) {
		this.cardTypeAutHidden = cardTypeAutHidden;
	}

	public HtmlInputHidden getCardActivatedAsHidden() {
		return cardActivatedAsHidden;
	}

	public void setCardActivatedAsHidden(HtmlInputHidden cardActivatedAsHidden) {
		this.cardActivatedAsHidden = cardActivatedAsHidden;
	}

	public HtmlInputHidden getCardTypeDeHidden() {
		return cardTypeDeHidden;
	}

	public void setCardTypeDeHidden(HtmlInputHidden cardTypeDeHidden) {
		this.cardTypeDeHidden = cardTypeDeHidden;
	}
	
	public CardBean getCcCardBean() {
		return ccCardBean;
	}

	public void setCcCardBean(CardBean ccCardBean) {
		this.ccCardBean = ccCardBean;
	}



	public String getFirstName() {
		return firstName;
	}



	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

}