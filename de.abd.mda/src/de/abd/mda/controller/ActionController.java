package de.abd.mda.controller;

import java.util.List;

import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.report.ReportRunnable;

public class ActionController implements ActionListener {

	private final static Logger LOGGER = Logger.getLogger(ActionController.class .getName());
	
	// Model related variables
	private Model model;
	protected CardBean cardBean;

	// JavaServerFaces related variables
	protected UIOutput uiMessage;

	// Constructor
	public ActionController() {
		model = new Model();
		model.createModel();
		CustomerController customerController = new CustomerController();
		List customerList = customerController.listObjects();
		HttpSession session = null;
		if (getSession() != null) {
			session = getSession();
			session.setAttribute("model", model);
			session.setAttribute("customerList", customerList);
		}
	}
	
	// ***************************** Aktionen.xhtml actions (navigation)
	// **********************
	public String openConfigDialog() {
		return "openConfigureDialog";
	}
	
	public String openCreateNewCardDialog() {
		cardBean = new CardBean();
		return "createCard";
	}

	public String openSearchDialog() {
		return "searchCard";
	}

	public String openShowCardsDialog() {
		return "showCards";
	}
	
	public String openUpdateCardDialog() {
		cardBean = new CardBean();
		return "updateCard";
	}

	public String openClearingDialog() {
		return "openClearingDialog";
	}

	public String openDeliveryBillDialog() {
		return "openDeliveryBillDialog";
	}

	public String generateReport() {
		return "Report";
	}

	public String openInvoicesStatus() {
		return "InvoiceStatus";
	}

	public String openAddCustomerDialog() {
		cardBean = new CardBean();
		return "openAddCustomerDialog";
	}

	public String openShowCustomersDialog() {
		return "openShowCustomersDialog";
	}

	public String openShowCustomerPaymentModaltiesDialog() {
		return "openShowCustomerPaymentModaltiesDialog";
	}
	
	
	public String openUpdateCustomerDialog() {
		getRequest().setAttribute("newCustomer", true);
		getRequest().setAttribute("componentDisabled", true);
		cardBean = new CardBean();
		return "openUpdateCustomerDialog";
	}

	// ***************** Aktionen.xhtml actions end **************************

	// ********* weitere Actions ****************

	public String cancel() {
		uiMessage.setRendered(true);
		uiMessage.setValue("Aktion abgebrochen! Neue Karte wurde NICHT angelegt!");
		return "failure";
	}

	public void calendarPartialSubmit(ValueChangeEvent event) {
		getRequest().setAttribute("calendarPartialSubmit", true);
	}

	/***************** ActionListener **************/
	public void processAction(ActionEvent event)
			throws AbortProcessingException {
		// TODO Auto-generated method stub
		getRequest().setAttribute("clearCard", true);
		getRequest().setAttribute("newCustomer", true);
		LOGGER.info("ID = " + event.getComponent().getId());
		if (uiMessage != null)
			uiMessage.setRendered(false);
	}

	/********* Weitere Methoden ****************/
	protected HttpSession getSession() {
		if (FacesContext.getCurrentInstance() != null) {
			return (HttpSession) FacesContext.getCurrentInstance()
					.getExternalContext().getSession(false);
		} else {
			return null;
		}
	}

	protected HttpServletRequest getRequest() {
		return ((HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest());
	}

	public String getRequestMessage() {
		if (getRequest().getAttribute("message") != null) {
			String message = "" + getRequest().getAttribute("message"); 
			return message;
		}
		return "";
	}

	// ********** Getter / Setter ************

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public UIOutput getUiMessage() {
		return uiMessage;
	}

	public void setUiMessage(UIOutput uiMessage) {
		this.uiMessage = uiMessage;
	}

	public CardBean getCardBean() {
		return cardBean;
	}

	public void setCardBean(CardBean cardBean) {
		this.cardBean = cardBean;
	}
	
}