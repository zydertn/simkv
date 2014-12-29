package de.abd.mda.report;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.persistence.Column;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.lowagie.text.Anchor;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.WebColors;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Voucher;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.util.DateUtils;

public class FriendlyReminderGenerator {

	private final static Logger LOGGER = Logger.getLogger(FriendlyReminderGenerator.class .getName()); 
	
	BaseFont bf_broadway = null;
	BaseFont bf_arial = null;
//	private static int MAX_ROW_FIRST_PAGE = 25;
	private int MAX_ROW_FIRST_PAGE = 25;
//	private static int FULL_PAGE_SIZE = 39;
	private int FULL_PAGE_SIZE = 40;
	private int pos = 0;
	private static int sevBillInvNum = 35000;
	private boolean billContainsVoucher = false;
	private boolean writeToDB = true;
	private ResourceBundle bundle = null;

	
	public FriendlyReminderGenerator() {
		LOGGER.info("Instantiate: FriendlyReminderGenerator");
		loadBaseFonts();
	}
	
	public HashMap<String, Object> generateReport(Bill bill, Customer customer) {
		LOGGER.info("Method: generateReport");
		String baseName = "de.abd.mda.locale.reminder";
		String reportLocale = customer.getCountry().getShortName();
		if (reportLocale.toLowerCase().equals("de") || reportLocale.toLowerCase().equals("at")) {
			Locale.setDefault(new Locale(reportLocale.toLowerCase()));
		} else {
			Locale.setDefault(new Locale("en"));
		}

		bundle = ResourceBundle.getBundle(baseName);

		try {
			Document document = new Document(PageSize.A4, 60, 25, 40, 40);
			Calendar calcMonth = bill.getCalcDate();
			if (calcMonth == null) {
				calcMonth = Calendar.getInstance();
			}
			String month = "";
			if ((calcMonth.get(Calendar.MONTH) + 1) > 9) {
				month = month + (calcMonth.get(Calendar.MONTH) + 1);
			} else {
				month = "0" + (calcMonth.get(Calendar.MONTH) + 1);
			}
			
			String filename = customer.getCustomernumber() + "_" + calcMonth.get(Calendar.YEAR) + "-" + month + "_r00.pdf";

			File dir = new File("C:/Temp/report/" + calcMonth.get(Calendar.YEAR) + "/" + month);
			dir.mkdirs();
			
			
			FileOutputStream fos = new FileOutputStream(
					dir + "/" + filename);
			
			LOGGER.info("Writing file: " + dir + "/" + filename);
			
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			LOGGER.info("Calculating month: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR));
			
			// headers and footers must be added before the document is opened
			HeaderFooter header = generateHeader(writer);
			if (header != null) {
				document.setHeader(header);
			} else {
				return null;
			}

			HeaderFooter footer = generateFooter(writer);
			if (footer != null) {
				footer.setBorder(Rectangle.NO_BORDER);
				footer.setAlignment(Element.ALIGN_LEFT);
				document.setFooter(footer);
			} else {
				return null;
			}

			document.open();

			boolean generatedWithErrors = generateBody(writer, document, customer, calcMonth, bill.getBillNumber(), bill.getCalcDate(), bill);
			if (generatedWithErrors) {
				LOGGER.warn("Error in creation of bill body!");
				return null;
			} else {
				LOGGER.info("Bill body generated without errors");
			}
			
			document.close();
			fos.flush();
			fos.close();
			
			LOGGER.info("File " + filename + " created successfully!");

			FileInputStream fin=new FileInputStream(dir + "/" + filename);
			  // Create a byte array
			byte[] file=new byte[fin.available()];
			  // Read data into the array
			fin.read(file);
			 
			 
			HashMap<String, Object> retVals = new HashMap<String, Object>();
			retVals.put("File", file);
			retVals.put("Filename", filename);
			return retVals;
		} catch (Exception ex) {
			LOGGER.error(ex);
			ex.printStackTrace();
			return null;
		}
	}

	private HeaderFooter generateHeader(PdfWriter writer) {
		HeaderFooter header = null;

		LOGGER.info("Method: generateHeader");
		try {

			Image logo = Image.getInstance("images/SiwalTec_Logo.wmf");
			
			
			Chunk logoChunk = new Chunk(logo, -25, -20);
			Phrase phrase = new Phrase(logoChunk);
			
			header = new HeaderFooter(phrase, false);
			header.setAlignment(Element.ALIGN_RIGHT);
			header.setBorder(Rectangle.NO_BORDER);
			
		} catch (Exception ex) {
			LOGGER.error("Exception: " + ex);
			return null;
		}

		return header;
	}

	private HeaderFooter generateFooter(PdfWriter writer) {
		LOGGER.info("Method: generateFooter");
		HeaderFooter footer = null;
		try {
			Font broadwayFont = new Font(bf_broadway);
			broadwayFont.setColor(Color.BLUE);
			broadwayFont.setSize(8);

			Font arialFont = new Font(bf_arial);
			arialFont.setColor(Color.BLACK);
			arialFont.setSize(8);

			Font arialFontBold = new Font(bf_arial);
			arialFontBold.setColor(Color.BLACK);
			arialFontBold.setSize(8);
			arialFontBold.setStyle(Font.BOLD);

			Image logo = Image.getInstance("images/SiwalTec_Kontaktdaten_Hochformat.wmf");
			Chunk logoChunk = new Chunk(logo, 0, -20);
			Phrase phrase = new Phrase(logoChunk);

			footer = new HeaderFooter(phrase, false);
			footer.setAlignment(Element.ALIGN_RIGHT);
			footer.setBorder(Rectangle.NO_BORDER);
		} catch (Exception ex) {
			LOGGER.error("Exception: " + ex);
			return null;
		}

		return footer;
	}

	private boolean generateBody(PdfWriter writer, Document doc, Customer customer, Calendar calcMonth, int invoiceNumber, Calendar calendar, Bill bill) {
		LOGGER.info("Method: generateBody");
		try {
			Image sender = Image.getInstance("images/SiwalTec_Absenderzeile.wmf");

			Chunk senderChunk = new Chunk(sender, 0, -80);
			Phrase headPhrase = new Phrase(senderChunk);
			doc.add(headPhrase);
			
			int x = 60;
			int y = 700;

			PdfContentByte cb = writer.getDirectContent();

			// Firma
			cb.beginText();
			cb.setColorFill(Color.BLACK);
			cb.setFontAndSize(bf_arial, 11);
			String companyString = "";
			y = y - 9;
			int d = 11;
			if (customer.getContactPerson().getGender() != null) {
				companyString = bundle.getString("Reminder." + customer.getContactPerson().getGender());;
				if (companyString != null && companyString.length() > 0) {
					y = y - d;
					cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyString, x, y,
							0);
				}
			}

			y = y - d;
			// Firmenname
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getName(), x,
					y, 0);

			if (customer.getBranch() != null && customer.getBranch().length() > 0) {
				y = y - d;
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getBranch(), x, y, 0);
			}

			if (customer.getFao() != null && customer.getFao().length() > 0) {
				y = y - d;
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getFao(), x, y, 0);
			}

			y = y - d;
			// Firmenstrasse
			if (customer.getInvoiceAddress().getStreet() != null && customer.getInvoiceAddress().getStreet().length() > 0) {
				if (customer.getCountry().getShortName().equals("GB")) {
					cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getHousenumber() + " " + customer.getInvoiceAddress().getStreet(),
							x, y, 0);
				} else {
					cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getStreet() + " " + customer.getInvoiceAddress().getHousenumber(),
							x, y, 0);
				}
			} else if (customer.getInvoiceAddress().getPostbox() != null && customer.getInvoiceAddress().getPostbox().length() > 0) {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Reminder.postbox") + " "+ customer.getInvoiceAddress().getPostbox(), x, y, 0);
			}

			y = y - d;
			// Firmenort
			if (customer.getCountry().getShortName().equals("GB")) {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getCity() + " " + customer.getInvoiceAddress().getPostcode(), x,
						y, 0);
			} else {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Reminder.postCodeCountryPrefix") + customer.getInvoiceAddress().getPostcode() + " " + customer.getInvoiceAddress().getCity(), x,
						y, 0);
			}
			
			int dateY = 0;
			
			// Ländername, wenn nicht Deutschland und wenn kein Country Prefix in locale properties angegeben ist.
			if (!customer.getCountry().getShortName().equals("DE") && bundle.getString("Reminder.postCodeCountryPrefix").length() == 0) {
				y = y - d;
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getCountry().getReportLocaleName(), x, y, 0);
				dateY = 15;
			}
			
			// Date
//			y = y - 5 * d;
			
			String dateString = "Holzgerlingen, den ";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, dateString + DateUtils.getCalendarNumString(Calendar.getInstance()), 395, 630-dateY, 0);
 
			x = 60;
			y = 715;

			Font invoiceFont = new Font(bf_arial, 12, Font.BOLD);
			String invoiceS = bundle.getString("Reminder.reminder");
			Chunk invoice = new Chunk(addNewLines(15) + invoiceS, invoiceFont);
			Phrase phrase = new Phrase();
			phrase.add(invoice);
			doc.add(phrase);
			
			for (int i=1; i < 7; i++) {
				Chunk c = new Chunk(addNewLines(2) + bundle.getString("Reminder.string" + i));
				Phrase p = new Phrase();
				p.add(c);
				doc.add(p);
			}

			
			Chunk cu = new Chunk(addNewLines(2));
			Phrase ph = new Phrase();
			ph.add(cu);
			doc.add(ph);

			Font tableFontBold = new Font(bf_arial, 9);
			tableFontBold.setStyle(Font.BOLD);

			// Tabellenheader erzeugen
			PdfPTable tableHeader = new PdfPTable(3);
			tableHeader.setWidthPercentage(100f);
			tableHeader.getDefaultCell().setBorder(1);
			Color headerColor = new Color(146, 205, 220);
			Color bodyColor = new Color(218, 238, 243);
			
			PdfPCell cell = new PdfPCell();
			cell.setBackgroundColor(headerColor);
			cell.setPhrase(new Phrase(bundle.getString("Reminder.billnumber"), tableFontBold));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			tableHeader.addCell(cell);

			cell.setPhrase(new Phrase(bundle.getString("Reminder.billdate"), tableFontBold));
			tableHeader.addCell(cell);

			cell.setPhrase(new Phrase(bundle.getString("Reminder.billamount"), tableFontBold));
			tableHeader.addCell(cell);


			PdfPCell bodyCell = new PdfPCell();
			bodyCell.setBackgroundColor(bodyColor);
			bodyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			bodyCell.setPhrase(new Phrase(bundle.getString("Reminder.billnumber"), tableFontBold));
			bodyCell.setPhrase(new Phrase(""+bill.getBillNumber(), tableFontBold));
			tableHeader.addCell(bodyCell);
			bodyCell.setPhrase(new Phrase(DateUtils.getCalendarNumString(bill.getCalcDate()), tableFontBold));
			tableHeader.addCell(bodyCell);
			bodyCell.setPhrase(new Phrase("" + bill.getBruttoPrice() + " €", tableFontBold));
			tableHeader.addCell(bodyCell);
			
			doc.add(tableHeader);

			for (int i=7; i < 10; i++) {
				Chunk c = new Chunk(addNewLines(2) + bundle.getString("Reminder.string" + i));
				Phrase p = new Phrase();
				p.add(c);
				doc.add(p);
			}

			cb.endText();
	
		} catch (Exception ex) {
			LOGGER.error("Exception: "+ ex);
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return true;
		}
		return false;
	}
	
	private String addSpaces(int i) {
		String s = "";
		if (i > 0) {
			for (int j = 0; j < i; j++) {
				s += " ";
			}
		}
		return s;
	}

	private String addNewLines(int x) {
		String s = "";
		for (int i = 0; i < x; i++) {
			s = s + "\n";
		}
		return s;
	}

	private void loadBaseFonts() {
		LOGGER.info("Method: loadBaseFonts");
		try {
			bf_broadway = BaseFont.createFont("C:/Windows/Fonts/BROADW.TTF",
					"Cp1252", false);
			bf_arial = BaseFont.createFont("C:/Windows/Fonts/Arial.TTF",
					"Cp1252", false);
		} catch (DocumentException e) {
			LOGGER.error("DocumentException: " + e);
		} catch (IOException e) {
			LOGGER.error("IOException: " + e);
		}
	}
	
}
