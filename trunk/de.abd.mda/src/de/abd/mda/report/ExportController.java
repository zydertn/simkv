package de.abd.mda.report;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.icesoft.faces.component.outputresource.OutputResource;
import com.icesoft.faces.context.Resource;

import de.abd.mda.controller.ActionController;
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.util.DateUtils;

public class ExportController extends ActionController {

	private final static Logger LOGGER = Logger
			.getLogger(ExportController.class.getName());

	private HtmlSelectOneMenu monthBinding;
	private HtmlSelectOneMenu yearBinding;
	private HtmlSelectOneMenu formatBinding;
	private int month;
	private int year;
	private int format;

	private OutputResource outputLinkExportBinding;

	public ExportController() {
	}

	public String startExport() {
		List<String> csvLines = generateCsvLines(this.month, this.year);
		download(csvLines, this.month, this.year);
		return "";
	}

	private static List<String> generateCsvLines(int month, int year) {
		BillController bc = new BillController();
		List<Bill> bills = bc.findMonthBills(year, month);
		List<Bill> taxableBills = new ArrayList<Bill>();
		List<Bill> nonTaxableBills = new ArrayList<Bill>();
		
		CustomerController cc = new CustomerController();
		
		for (Bill b : bills) {
			Customer cus = cc.findCustomer(""+b.getCustomerNumber());
			if (cus.getCountry().getShortName().equals(Model.COUNTRY_GERMANY)) {
				taxableBills.add(b);
			} else {
				nonTaxableBills.add(b);
			}
		}
		
		List<String> csvLines = new ArrayList<String>();
		csvLines.add("BILL_NUMBER;BILL_CUS_NUM;BILL_CALC_DATE;BILL_BRUTTO_PRICE;BILL_TAXABLE\n");
		for (Bill b: taxableBills) {
			csvLines.add(""+b.getBillNumber()+";" +b.getCustomerNumber()+";" + DateUtils.getCalendarExportString(b.getCalcDate())+";" + b.getBruttoPrice()+";" + "Inland\n");
		}
		for (Bill b: nonTaxableBills) {
			csvLines.add(""+b.getBillNumber()+";" +b.getCustomerNumber()+";" + DateUtils.getCalendarExportString(b.getCalcDate())+";" + b.getBruttoPrice()+";" + "Ausland\n");
		}

		return csvLines;
	}

	private void download(List<String> csvLines, int month, int year) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();

		String filename = "Rechnungen_"+year+"_"+(month+1)+".csv";

		externalContext.responseReset();
		externalContext.setResponseContentType("text/comma-separated-values");
		externalContext.setResponseHeader("Content-Disposition",
				"attachment; filename=\"" + filename + "\"");

		try {
			OutputStream output = externalContext.getResponseOutputStream();

			for (String s : csvLines) {
				output.write(s.getBytes());
			}

			output.flush();
			output.close();

			facesContext.responseComplete();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public HtmlSelectOneMenu getMonthBinding() {
		return monthBinding;
	}

	public void setMonthBinding(HtmlSelectOneMenu monthBinding) {
		this.monthBinding = monthBinding;
	}

	public HtmlSelectOneMenu getYearBinding() {
		return yearBinding;
	}

	public void setYearBinding(HtmlSelectOneMenu yearBinding) {
		this.yearBinding = yearBinding;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public HtmlSelectOneMenu getFormatBinding() {
		return formatBinding;
	}

	public void setFormatBinding(HtmlSelectOneMenu formatBinding) {
		this.formatBinding = formatBinding;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

}
