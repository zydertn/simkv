package de.abd.mda.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;


import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.controller.BillController;

public class BillActionController extends ActionController {

	private final static Logger LOGGER = Logger.getLogger(BillActionController.class .getName());
	
	private HtmlInputHidden billNumberHidden;
	
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

	public void processPaymentAction(Bill bill) {
		// TODO Auto-generated method stub
		BillController bc = new BillController();
		Bill dbBill = bc.findBill(bill);
		dbBill.setPaymentStatus(true);
		dbBill.setPaymentDate(Calendar.getInstance());
		bc.updateObject(dbBill);
		getRequest().setAttribute("billListUpdate", true);
	}

	public void processReminderAction(Bill bill, int i) {
		// TODO Auto-generated method stub
		
	}

}
