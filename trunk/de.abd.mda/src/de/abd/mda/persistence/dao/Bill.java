package de.abd.mda.persistence.dao;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.icesoft.faces.component.ext.HtmlSelectOneMenu;


import de.abd.mda.controller.BillActionController;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.util.DateUtils;

public class Bill extends DaoObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6907539712172216071L;

	private final static Logger LOGGER = Logger.getLogger(Bill.class .getName());
	
	private int billId;
	private int billNumber;
	private int customerNumber;
	private int year;
	private int month;
	private byte[] file;
	private String filename;
	private int mapCount;
	private boolean flatrate;
	private boolean finalized;
	private BigDecimal bruttoPrice;
	private BigDecimal nettoPrice;
	private BigDecimal vat;
	private Calendar calcDate;
	private Timestamp updateTime;
	private boolean paymentStatus;
	private Calendar paymentDate;
	private String statusString;
	private int reminderStatus;
	private String action;
	private int actionHidden;
	private Calendar friendlyReminderDate;
	private Calendar firstReminderDate;
	
	
	public Bill() {
		paymentStatus = false;
		reminderStatus = -1;
		actionHidden = 1;
	}
	
	public String processStatusAction() {
		BillActionController bac = new BillActionController();
		if (action.equals("Action_empty")) {
			LOGGER.warn("BillController.processAction clicked with empty action!");
		} else if (action.equals("Action_payment")) {
			LOGGER.info("BillController.processAction payment.");
			if (paymentStatus == false)
				bac.processPaymentAction(this);
			else
				((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("message", "Diese Rechnung wurde bereits bezahlt!");
		} else if (action.equals("Action_FriendlyReminder")) {//			LOGGER.info("BillController.processAction FriendlyReminder.");
//			bac.processReminderAction(this, customerNumber, 0);
//		} else if (action.equals("Action_1stReminder")) {
//			LOGGER.info("BillController.processAction 1stReminder.");
//			bac.processReminderAction(this, customerNumber, 1);
//		} else if (action.equals("Action_2ndReminder")) {
//			LOGGER.info("BillController.processAction 2ndReminder.");
//			bac.processReminderAction(this, customerNumber, 2);
		}
		return "";
	}
	
	public void actionChanged(ValueChangeEvent event) {
		String value = ""+ event.getNewValue();
		actionHidden = 2;
	}

	public String getBillMonthString() {
		return DateUtils.getMonthAsString(month) + " " + year;
	}
	
	public String getCalcDateString() {
		if (calcDate != null) {
			return DateUtils.getCalendarString(calcDate);
		} else return "";
	}

	public String getPaymentDateString() {
		if (paymentDate != null) {
			return DateUtils.getCalendarString(paymentDate);
		} else return "";
	}
	
	public String getReminderStatusColor() {
		String style="";
		switch (reminderStatus) {
			case -1: break;
			case 0:	style = "background-color:yellow";
					break;
			case 1: style = "background-color:#E55B3C";
					break;
			case 2: style = "background-color:red";
					break;
		}
		return style;
	}
	
	public String getStatusImage() {
		if (paymentStatus) {
			statusString = "Bezahlt";
			return "images/haken_transp.png";
		} else {
			statusString = "Offen";
			return "images/cancel_transp.png";
		}
	}
	
	public String getReminderStatusString() {
		String reminderString = "";
		switch (reminderStatus) {
		case -1: 	reminderString = "-";
					break;
		case 0:		reminderString = "Freundliche Erinnerung";
					break;
		case 1:		reminderString = "1. Mahnung";
					break;
		case 2: 	reminderString = "2. Mahnung";
					break;
		}
		return reminderString;
	}
	
	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}

	public int getBillId() {
		return billId;
	}

	public void setBillId(int billId) {
		this.billId = billId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public int getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(int customerNumber) {
		this.customerNumber = customerNumber;
	}

	public int getMapCount() {
		return mapCount;
	}

	public void setMapCount(int mapCount) {
		this.mapCount = mapCount;
	}

	public boolean isFlatrate() {
		return flatrate;
	}

	public void setFlatrate(boolean flatrate) {
		this.flatrate = flatrate;
	}

	public BigDecimal getBruttoPrice() {
		return bruttoPrice;
	}

	public void setBruttoPrice(BigDecimal bruttoPrice) {
		this.bruttoPrice = bruttoPrice;
	}

	public BigDecimal getNettoPrice() {
		return nettoPrice;
	}

	public void setNettoPrice(BigDecimal nettoPrice) {
		this.nettoPrice = nettoPrice;
	}

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public Calendar getCalcDate() {
		return calcDate;
	}

	public void setCalcDate(Calendar calcDate) {
		this.calcDate = calcDate;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Calendar getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Calendar paymentDate) {
		this.paymentDate = paymentDate;
	}

	public boolean getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(boolean paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public int getReminderStatus() {
		return reminderStatus;
	}

	public void setReminderStatus(int reminderStatus) {
		this.reminderStatus = reminderStatus;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getActionColumnStyle() {
		String style = "";
		if (BillController.rowCountNum == 0) {
			style = "background-color: #FFFFFF";
		} else {
			style = "background-color: #F7F7F7";
		}
		return style;
	}

	public String getActionSelectStyle() {
		String style = "";
		if (BillController.rowCountNum == 0) {
			style = "border: none; background-color: #FFFFFF";
			BillController.rowCountNum = 1;
		} else {
			style = "border: none; background-color: #F7F7F7";
			BillController.rowCountNum = 0;
		}
		return style;

		
	}

	public int getActionHidden() {
		return actionHidden;
	}

	public void setActionHidden(int actionHidden) {
		this.actionHidden = actionHidden;
	}

	public Calendar getFriendlyReminderDate() {
		return friendlyReminderDate;
	}

	public void setFriendlyReminderDate(Calendar friendlyReminderDate) {
		this.friendlyReminderDate = friendlyReminderDate;
	}

	public Calendar getFirstReminderDate() {
		return firstReminderDate;
	}

	public void setFirstReminderDate(Calendar firstReminderDate) {
		this.firstReminderDate = firstReminderDate;
	}

}
