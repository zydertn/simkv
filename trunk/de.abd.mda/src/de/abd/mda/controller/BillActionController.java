package de.abd.mda.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfReader;


import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.report.ReminderGenerator;
import de.abd.mda.util.DateUtils;

public class BillActionController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(BillActionController.class .getName());
	
	private HtmlInputHidden billNumberHidden;
	private HtmlInputHidden actionHidden;
	
	public BillActionController() {
	}
	
	public String downloadPdf() {
		Integer billNumber = Integer.parseInt(""+billNumberHidden.getValue());
//		Integer billNumber = 37333;
		BillController bc = new BillController();
		Bill dbBill = bc.findBill(billNumber);
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();


		try {
			String filename = dbBill.getFilename();
			if (filename != null && filename.length() > 0) {
				externalContext.responseReset();
				externalContext.setResponseContentType("application/pdf");
				externalContext.setResponseHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");

				OutputStream output = externalContext.getResponseOutputStream();

				output.write(dbBill.getFile());

				output.flush();
				output.close();

				facesContext.responseComplete();
			} else {
				return "";
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return "";
	}

	public HtmlInputHidden getBillNumberHidden() {
		return billNumberHidden;
	}

	public void setBillNumberHidden(HtmlInputHidden billNumberHidden) {
		this.billNumberHidden = billNumberHidden;
	}

//	public String processReminderAction(Bill bill, int customerNumber, int i) {
	public String processAction() {
		Integer billNumber = Integer.parseInt(""+billNumberHidden.getValue());
		String actionString = ""+actionHidden.getValue();
		if (!actionString.contains(""+billNumber)) {
			// Action-Auswahl und Buttonauswahl stimmen nicht überein.
			return "";
		}

		if (actionString.contains("Action_empty")) {
			// Keine Aktion ausgewählt
			return "";
		}
		
		BillController bc = new BillController();
		Bill bill = bc.findBill(billNumber);
		
		if (actionString.contains("Action_payment")) {
			processPaymentAction(bill);
		} else {
			processReminderAction(bill);
		}

		return "";		
	}

	public void processPaymentAction(Bill bill) {
		// TODO Auto-generated method stub
		BillController bc = new BillController();
		Bill dbBill = bc.findBill(bill);
		dbBill.setPaymentStatus(true);
		dbBill.setPaymentDate(Calendar.getInstance());
		bc.updateObject(dbBill);
		getRequest().setAttribute("billListUpdate", true);
	}

	private void processReminderAction(Bill bill) {
	// TODO Auto-generated method stub
		BillController bc = new BillController();
		Bill dbBill = bc.findBill(bill);
		CustomerController cc = new CustomerController();
		Customer customer = cc.findCustomer("" + dbBill.getCustomerNumber());
		ReminderGenerator frg = new ReminderGenerator();
		HashMap<String, Object> retVals = frg.generateReport(bill, customer);
		if (retVals != null) {
			if (bill.getReminderStatus() == -1) {
				dbBill.setReminderStatus(0);
				dbBill.setFriendlyReminderDate(Calendar.getInstance());
			} else if (bill.getReminderStatus() == 0) {
				dbBill.setReminderStatus(1);
				dbBill.setFirstReminderDate(Calendar.getInstance());
			} else {
				dbBill.setReminderStatus(2);
			}


			bc.updateObject(dbBill);
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();


		try {
			String filename = (String) retVals.get("Filename");
			if (filename != null && filename.length() > 0) {
				externalContext.responseReset();
				externalContext.setResponseContentType("application/pdf");
				externalContext.setResponseHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");

				OutputStream output = externalContext.getResponseOutputStream();

				output.write((byte[]) retVals.get("File"));

				output.flush();
				output.close();

				getRequest().setAttribute("billListUpdate", true);

				facesContext.responseComplete();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public HtmlInputHidden getActionHidden() {
		return actionHidden;
	}

	public void setActionHidden(HtmlInputHidden actionHidden) {
		this.actionHidden = actionHidden;
	}

}
