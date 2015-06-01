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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.lowagie.text.Anchor;
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
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.codec.BmpImage;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Voucher;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.util.DateUtils;

public class ReportGenerator_portrait implements IReportGenerator {

	private final static Logger LOGGER = Logger.getLogger(ReportGenerator_portrait.class .getName()); 
	
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

	
	public ReportGenerator_portrait() {
		LOGGER.info("Instantiate: ReportGenerator_portrait");
		loadBaseFonts();
	}
	
	public boolean generateReportDirect(List<DaoObject> customerCards, Customer customer, Calendar calcMonth, boolean flatrateCalc, boolean severalBills, int mapCount, Date calcDate, String reportNumber) {
		LOGGER.info("Method: generateReportDirect");
		writeToDB = false;
		return generateReport(customerCards, customer, calcMonth, flatrateCalc, severalBills, mapCount, calcDate, reportNumber);
	}

	public boolean generateReport(List<DaoObject> customerCards, Customer customer, Calendar calcMonth, boolean flatrateCalc, boolean severalBills, int mapCount, Date calcDate, String reportNumber) {
		LOGGER.info("Method: generateReport");
		String baseName = "de.abd.mda.locale.report";
		String reportLocale = customer.getCountry().getShortName();
		if (reportLocale.toLowerCase().equals("de")) {
			Locale.setDefault(new Locale("de", "DE"));
		} else if (reportLocale.toLowerCase().equals("at")) {
			Locale.setDefault(new Locale("de", "AT"));
		} else if (reportLocale.toLowerCase().equals("no")) {
			Locale.setDefault(new Locale("no"));
		} else {
			Locale.setDefault(new Locale("en"));
		}

		bundle = ResourceBundle.getBundle(baseName);

		MAX_ROW_FIRST_PAGE = new Integer(bundle.getString("Report.maxRowFirstPage"));
		
		try {
			long time1 = System.currentTimeMillis();
			Document document = new Document(PageSize.A4, 60, 25, 40, 40);
			String month = "";
			if ((calcMonth.get(Calendar.MONTH) + 1) > 9) {
				month = month + (calcMonth.get(Calendar.MONTH) + 1);
			} else {
				month = "0" + (calcMonth.get(Calendar.MONTH) + 1);
			}
			
			String year = "" + calcMonth.get(Calendar.YEAR);

			String flatrateString = "";
			if (flatrateCalc)
				flatrateString = "_flatrate";
			String mapCountString = "";
			if (severalBills)
				mapCountString = "_" + mapCount;
			
			String filename = customer.getCustomernumber() + "_" + calcMonth.get(Calendar.YEAR) + "-" + month + flatrateString + mapCountString + ".pdf";
			long time2 = System.currentTimeMillis();
			long diff1 = time2-time1;
			LOGGER.info("genRep Teil 1 = " + diff1);

			
			
//			File dir = new File("C:/Temp/report/" + customer.getCustomernumber());
//			File dir = new File("C:/Temp/report/" + year + "/" + month);
//			dir.mkdirs();
			long time3 = System.currentTimeMillis();
//			File dir = new File("C:/Temp/report/");
			File dir = new File("C:/Temp/report/" + year + "/" + month);
			dir.mkdirs();
			
			
			FileOutputStream fos = new FileOutputStream(
					dir + "/" + filename);
			
			LOGGER.info("Writing file: " + dir + "/" + filename);
			
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			LOGGER.info("Calculating month: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR));
			Bill bill = new Bill();
			bill.setCustomerNumber(new Integer(customer.getCustomernumber()));
			bill.setFilename(filename);
			bill.setYear(calcMonth.get(Calendar.YEAR));
			bill.setMonth(calcMonth.get(Calendar.MONTH));
			bill.setMapCount(mapCount);
			bill.setFlatrate(flatrateCalc);
			BillController bc = new BillController();
			long timefb = System.currentTimeMillis();
			Bill dbBill = bc.findBill(bill);
			long timefb2 = System.currentTimeMillis();
			long difffb = timefb2-timefb;
			LOGGER.info("findBill = " + difffb);
			
			long time4 = System.currentTimeMillis();
			long diff2 = time4-time3;
			LOGGER.info("genRep Teil 2 = " + diff2);

			long time5 = System.currentTimeMillis();
			int invoiceNumber = -1;
			boolean increaseBillNum = false;
			if (reportNumber == null || reportNumber.length() == 0) {
				if (dbBill != null) {
					invoiceNumber = dbBill.getBillNumber();
					LOGGER.info("Bill already in DB... Getting bill number... " + invoiceNumber);
				} else {
					invoiceNumber = (bc.getMaxBillNumber())+1;
					increaseBillNum = true;
					LOGGER.info("New bill in DB... bill number: " + invoiceNumber);
				}
			} else {
				LOGGER.warn("Report number specified for bill. Creating no new bill number!!");
				try {
					invoiceNumber = Integer.parseInt(reportNumber);
				} catch (NumberFormatException e) {
					LOGGER.error("NumberFormatException: " + e);
				}
			}
			
			// headers and footers must be added before the document is opened
			HeaderFooter header = generateHeader(writer);
			if (header != null) {
				document.setHeader(header);
			} else {
				return false;
			}
			long time6 = System.currentTimeMillis();
			long diff3 = time6-time5;
			LOGGER.info("genRep Teil 3 = " + diff3);

			long time7 = System.currentTimeMillis();
			HeaderFooter footer = generateFooter(writer);
			if (footer != null) {
				footer.setBorder(Rectangle.NO_BORDER);
				footer.setAlignment(Element.ALIGN_LEFT);
				document.setFooter(footer);
			} else {
				return false;
			}

			document.open();
			long time8 = System.currentTimeMillis();
			long diff4 = time8-time7;
			LOGGER.info("genRep Teil 4 = " + diff4);

			long time9 = System.currentTimeMillis();
			boolean generatedWithErrors = generateBody(writer, document, customerCards, customer, calcMonth, flatrateCalc, severalBills, invoiceNumber, calcDate, bill);
			if (generatedWithErrors) {
				LOGGER.warn("Error in creation of bill body!");
				return false;
			} else {
				LOGGER.info("Bill body generated without errors");
			}
			
			document.close();
			fos.flush();
			fos.close();
			long time10 = System.currentTimeMillis();
			long diff5 = time10-time9;
			LOGGER.info("genRep Teil 5 = " + diff5);

			
			LOGGER.info("File " + filename + " created successfully!");
//			Runtime.getRuntime().exec(
//					"rundll32 url.dll,FileProtocolHandler "
//							+ "C:/Temp/report/" + customer.getCustomernumber());

			long time11 = System.currentTimeMillis();
			File file = new File(dir, filename);
			
			RandomAccessFile ra = new RandomAccessFile(file, "rw");
			
			byte[] b = new byte[(int) file.length()];
			try {
				ra.read(b);
			} catch (Exception e) {
				LOGGER.error("Exception: " + e);
			}
			
			if (b != null && b.length > 0) {
				LOGGER.info("Byte[] Länge == " + b.length);
				bill.setFile(b);
				
					
				long time13 = System.currentTimeMillis();
				boolean createdNew = bc.createOrUpdateObject(bill);
				long time14 = System.currentTimeMillis();
				long diff7 = time14-time13;
				LOGGER.info("createOrUpdateObject = " + diff7);
				
				if (createdNew)
					LOGGER.info("Added new bill to database");
				else
					LOGGER.info("Bill updated in database");
			}
			long time12 = System.currentTimeMillis();
			long diff6 = time12-time11;
			LOGGER.info("genRep Teil 6 = " + diff6);

			
			
//			File myFile = new File("Test.pdf");
//			FileOutputStream fos2 = new FileOutputStream(myFile);
//			fos2.write(b);
//			fos2.flush();
//			fos2.close();
//
//			Runtime.getRuntime().exec(
//			"rundll32 url.dll,FileProtocolHandler "
//					+ "Test.pdf");

			
			return true;
		} catch (Exception ex) {
			LOGGER.error(ex);
			return false;
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

//			Image logo = Image.getInstance("images/SiwalTec_Kontaktdaten_Hochformat.wmf");
			Image logo = Image.getInstance("images/Briefpapier_Fußzeile_klein.jpg");
			logo.scalePercent(26);
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

	private boolean generateBody(PdfWriter writer, Document doc, List<DaoObject> customerCards, Customer customer, Calendar calcMonth, Boolean flatrateCalc, boolean severalBills, int invoiceNumber, Date calcDate, Bill bill) {
		LOGGER.info("Method: generateBody");

		if (customer.getCustomernumber().equals("20125")) {
			System.out.println("Jetzt");
		}
		
		try {

			PdfContentByte cb = writer.getDirectContent();


//			Image sender = Image.getInstance("images/SiwalTec_Absenderzeile2.wmf");
			Image sender = Image.getInstance("images/Briefpapier_Kopfzeile.jpg");
			sender.scalePercent(24);
			
			Chunk senderChunk = new Chunk(sender, 0, -80);
			Phrase headPhrase = new Phrase(senderChunk);
			doc.add(headPhrase);
			
			cb.beginText();
			cb.setColorFill(Color.BLACK);
			cb.setFontAndSize(bf_arial, 11);
			
			int x = 60;
			int y = 700;

			// Firma
			String companyString = "";
			y = y - 9;
			int d = 11;
			if (customer.getContactPerson().getGender() != null) {
				companyString = bundle.getString("Report." + customer.getContactPerson().getGender());;
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
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.postbox") + " "+ customer.getInvoiceAddress().getPostbox(), x, y, 0);
			}

			y = y - d;
			// Firmenort
			if (customer.getCountry().getShortName().equals("GB")) {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getCity() + " " + customer.getInvoiceAddress().getPostcode(), x,
						y, 0);
			} else {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.postCodeCountryPrefix") + customer.getInvoiceAddress().getPostcode() + " " + customer.getInvoiceAddress().getCity(), x,
						y, 0);
			}
			
			int dateY = 0;
			
			// Ländername, wenn nicht Deutschland und wenn kein Country Prefix in locale properties angegeben ist.
			if (!customer.getCountry().getShortName().equals("DE") && bundle.getString("Report.postCodeCountryPrefix").length() == 0) {
				y = y - d;
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getCountry().getReportLocaleName(), x, y, 0);
				dateY = 15;
			}
			
			// Date
//			y = y - 5 * d;
			
			if (calcDate == null) {
				calcDate = new Date();
			}
			
			// Temporär auf 14. Januar gesetzt
//			date.setDate(13);
//			date.setMonth(2);
			SimpleDateFormat df = new SimpleDateFormat(bundle.getString("Report.dateformat"));
			df.setTimeZone(TimeZone.getDefault());
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, df.format(calcDate), 425, 630-dateY, 0);
			Calendar cal = Calendar.getInstance();
			cal.setTime(calcDate);
			bill.setCalcDate(cal);
			
			// Rechnungsnummer
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.invoicenumber"), 425, 600-dateY, 0);
			
			String invoiceNumStr = "";
			if (invoiceNumber > 9999) {
				invoiceNumStr = "0";
			}
			invoiceNumStr += invoiceNumber;
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, invoiceNumStr, new Integer(bundle.getString("Report.invoicenumberpos")), 600-dateY, 0);
			
			// Kundennr.
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.customernumber"), 425, 589-dateY, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "" + customer.getCustomernumber(), new Integer(bundle.getString("Report.customernumberpos")), 589-dateY, 0);

			int abzSuppNum = 0;
			if (customer.getSupplierNumber() != null && customer.getSupplierNumber().length() > 0) {
				// Lieferantennr.
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.suppliernumber"), 425, 578-dateY, 0);
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "" + customer.getSupplierNumber(), 510, 578-dateY, 0);
				abzSuppNum = 11;
			}
			
			cb.setFontAndSize(bf_arial, 8);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, bundle.getString("Report.info"), 425, 578-abzSuppNum-dateY, 0);

			cb.endText();

			x = 60;
			y = 715;

			Font invoiceFont = new Font(bf_arial, 12, Font.BOLD);
			String invoiceS = bundle.getString("Report.invoice");
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_QUARTERLY))
				invoiceS = bundle.getString("Report.invoice_quarterly");
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_HALFYEARLY))
				invoiceS = bundle.getString("Report.invoice_halfyear");
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY))
				invoiceS = bundle.getString("Report.invoice_year");
			Chunk invoice = new Chunk(addNewLines(15) + invoiceS, invoiceFont);

			Font timeframeFont = new Font(bf_arial, 11);
			Font timeframeFontBold = new Font(bf_arial, 11, Font.BOLD);

			String calcTimeString = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH));
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_QUARTERLY)) {
				int diff = calcMonth.get(Calendar.MONTH) % 3;
				calcTimeString = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH) - diff);
				calcTimeString += " - " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH) + 2 - diff);
			}
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_HALFYEARLY)) {
				int diff = calcMonth.get(Calendar.MONTH) % 6;
				calcTimeString = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH) - diff);
				calcTimeString += " - " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH) + 5 - diff);
			}
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY))
				calcTimeString = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " - " + DateUtils.getMonthAsString(Calendar.DECEMBER);
			calcTimeString += " " + calcMonth.get(Calendar.YEAR);
			Chunk timeframe = new Chunk(
					addNewLines(2)
							+ bundle.getString("Report.timeframe") + calcTimeString, timeframeFont);
			String commentSevBills = "";
			if (severalBills) {
				for (DaoObject dao : customerCards) {
					CardBean c = (CardBean) dao;
					if (c.getBestellNummer() != null && c.getBestellNummer().length() > 0) {
						commentSevBills += "SAP: " + c.getBestellNummer() + " - ";
						break;
					}
				}
			}
			Chunk commentCk = null;
			if (customer.getComment() != null && customer.getComment().length() > 0) {
				commentCk = new Chunk(addNewLines(1) + commentSevBills + customer.getComment() + addNewLines(1), timeframeFont);
			} else {
				commentCk = new Chunk(addNewLines(2), timeframeFont);
			}

			Phrase phrase = new Phrase();
			phrase.add(invoice);

			/*
			 * VAT-Nummer - nur bei Auslandskunden benötigt
			 */
			if (customer.getVatNumber() != null && customer.getVatNumber().length() > 0) {
				Chunk vatChunk = new Chunk(addNewLines(2) + bundle.getString("Report.vatString") + customer.getVatNumber(), invoiceFont);
				phrase.add(vatChunk);
			}

			phrase.add(timeframe);
			phrase.add(commentCk);
			
			// phrase.se
			doc.add(phrase);

			// Tabelle erstellen mit Default-Parameter
			PdfPTable table = new PdfPTable(new float[] { 1.5f, 2, 12.5f, 9, 4, 4 });
			table.setWidthPercentage(100f);
			table.getDefaultCell().setBackgroundColor(null);
			table.getDefaultCell().setBorder(0);
			
			ArrayList<TableRow> tableRowList = new ArrayList<TableRow>();
			Iterator<DaoObject> iter = customerCards.iterator();			
			ConfigurationController cc = new ConfigurationController();
			Map<Integer, Double> simPrices = cc.getSimPricesFromDB();
			Map<Integer, Double> dataOptionPrices = cc.getDataOptionPricesFromDB();

			BigDecimal calcSum = new BigDecimal("0.0");
			
			while (iter.hasNext()) {
				CardBean card = (CardBean) iter.next();
				if (card.getCardNumberFirst().equals("113399683") && card.getCardNumberSecond().equals("3")) {
					System.out.println("Jetzt");
				}
				BigDecimal simPrice = new BigDecimal("0.0");
				if (!card.getStandardPrice()) {
					simPrice = new BigDecimal(""+simPrices.get(card.getSimPrice()));
				} else {
					if (simPrices.get(customer.getInvoiceConfiguration().getSimPrice()) != null) {
						simPrice = new BigDecimal(""+simPrices.get(customer.getInvoiceConfiguration().getSimPrice()));
					} else {
						LOGGER.warn("SimPrice Key == " + customer.getInvoiceConfiguration().getSimPrice() + ", ==> Key gibt es nicht in SimPrice-Konfigurations-Map!");
					}
				}
				
				ArrayList<String> invoiceRowList = new ArrayList<String>();
				
				List<String> columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
//				if (columns.contains(Model.COLUMN_AMOUNT)) {
				int monthAmount = 1;
				Calendar periodMaxCalcDate = getPeriodMaxCalcDate(customer.getInvoiceConfiguration().getCreationFrequency(), calcMonth);
				if (customer.getCustomernumber().equals("20216")) {
					System.out.println("Jetzt");
				}
				monthAmount = getMonthAmount(customer.getInvoiceConfiguration().getCreationFrequency(), card.getStatus(), card.getActivationDate(), card.getDeactivationDate(), periodMaxCalcDate);
				calcSum = calcSum.add(simPrice.multiply(new BigDecimal(monthAmount)));

				BigDecimal dataOptionPrice = new BigDecimal("0.0");
				if (dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge()) != null) {
					dataOptionPrice.add(new BigDecimal(""+dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge())));
				} else {
					LOGGER.warn("DataOptionSurcharge Key == " + customer.getInvoiceConfiguration().getDataOptionSurcharge() + ", ==> Key gibt es nicht in DataOptionSurcharge-Konfigurations-Map!");
				}

				calcSum = calcSum.add(dataOptionPrice.multiply(new BigDecimal(monthAmount)));

				invoiceRowList.add("" + monthAmount);
				
				//				}
				if (columns.contains(Model.COLUMN_DESCRIPTION)) {
					invoiceRowList.add(card.getInstallAddress().getAddressString());
				}
				if (columns.contains(Model.COLUMN_CARD_NR)) {
					invoiceRowList.add(card.getCardnumberString());
				}
				if (columns.contains(Model.COLUMN_TEL_NR)) {
//					invoiceRowList.add(card.getPhoneStringInvoice());
					invoiceRowList.add(card.getPhoneString());
				}
				if (columns.contains(Model.COLUMN_INST_PLZ)) {
					invoiceRowList.add(card.getInstallAddress().getPostcode());
				}
				if (columns.contains(Model.COLUMN_EINSATZORT)) {
					invoiceRowList.add(card.getEinsatzort());
				}
				if (columns.contains(Model.COLUMN_INST_STREET)) {
					invoiceRowList.add(card.getInstallAddress().getStreet());
				}
				if (columns.contains(Model.COLUMN_PLANT_NUMBER)) {
					if (card.getFlatrateCard()) {
						invoiceRowList.add(bundle.getString("Report.flatratestring"));
					} else {
						invoiceRowList.add(card.getFactoryNumber());
					}
				}
				if (columns.contains(Model.COLUMN_EQUIP_NR)) {
					invoiceRowList.add(card.getEquipmentNr());
				}
				if (columns.contains(Model.COLUMN_AUFTRAGS_NR)) {
					invoiceRowList.add(card.getAuftragsNr());
				}
				if (columns.contains(Model.COLUMN_BESTELL_NR)) {
					invoiceRowList.add(card.getBestellNummer());
				}
				if (columns.contains(Model.COLUMN_VERTRAG_NR)) {
					invoiceRowList.add(card.getVertrag());
				}
				if (columns.contains(Model.COLUMN_BA_NR)) {
					invoiceRowList.add(card.getBaNummer());
				}
				if (columns.contains(Model.COLUMN_WE_NR)) {
					invoiceRowList.add(card.getWe());
				}
				if (columns.contains(Model.COLUMN_COST_CENTER)) {
					invoiceRowList.add(card.getKostenstelle());
				}
				
//				if (columns.contains(Model.COLUMN_TOTAL_PRICE)) {
				 	BigDecimal rowPrice = (simPrice.add(dataOptionPrice)).multiply(new BigDecimal(monthAmount));
					String price = ("" + rowPrice.setScale(2) + " €").replace(".", ",");
					invoiceRowList.add(price);
//				}
//				if (columns.contains(Model.COLUMN_TOTAL_PRICE)) {
//					invoiceRowList.add("" + (simPrice.add(dataOptionPrice)).setScale(2));
//				}

				if (!(invoiceRowList != null && invoiceRowList.size() > 0)) {
					throw new Exception("Keine Rechnungsspalten definiert!");
				}
				String[] invoiceRow = new String[invoiceRowList.size()];
				for (int i = 0; i < invoiceRowList.size(); i++) {
					invoiceRow[i] = invoiceRowList.get(i);
				}
				tableRowList.add(new TableRow(card.getInvoiceRows(), invoiceRow));
			}

			tableRowList = addCalculationRows(tableRowList, customerCards, customer, calcSum, calcMonth, bill);
			
//			tableRowList = addDummyRows(tableRowList);
			
			ArrayList<PdfPTable> tables = prepareTables(tableRowList, customer, flatrateCalc);
			int i = 0;
			while (i < tables.size()) {
				doc.add(tables.get(i));
//				if (i+1 < tables.size()){
//					String lineBreaks = "\n\n\n\n\n";
//					if (i+2 == tables.size()) {
//						for (int j = 0; j < extraLineBreaks; j++) {
//							lineBreaks += "\n";
//						}
//					}
//					doc.add(new Phrase(new Chunk(lineBreaks)));
//				}
				doc.newPage();
				doc.add(new Phrase(new Chunk("\n\n\n")));
				i++;
			}
			
			if (customer.getInvoiceConfiguration() != null && customer.getInvoiceConfiguration().getDebtOrder() != null && customer.getInvoiceConfiguration().getDebtOrder()) {
				Chunk debtOrder = new Chunk(addNewLines(2) + bundle.getString("Report.debitpayment") + addNewLines(2), timeframeFont);
				Phrase debtOrderPhrase = new Phrase(debtOrder);
				doc.add(debtOrderPhrase);
			} else {
				Chunk paymentDueDate = new Chunk(addNewLines(2) + bundle.getString("Report.paymenttime") + customer.getInvoiceConfiguration().getPaymentTarget() + " " + bundle.getString("Report.paymenttime2"), timeframeFont);
				Chunk paymentDueDateEnd = new Chunk(bundle.getString("Report.paymentinvoicenum"), timeframeFontBold);
				Chunk nlChunk = new Chunk(addNewLines(2));
				Chunk verzugsChunk = new Chunk(bundle.getString("Report.arrearsinfo") + addNewLines(2), timeframeFont);
				Phrase payPhrase = new Phrase(paymentDueDate);
				payPhrase.add(paymentDueDateEnd);
				String sn = customer.getCountry().getShortName().toLowerCase(); 
				if (!sn.equals("de") && !sn.equals("at")) {
					payPhrase.add(verzugsChunk);
					doc.add(payPhrase);
				} else {
					doc.add(payPhrase);
					doc.add(new Phrase(nlChunk));
					doc.add(new Phrase(verzugsChunk));
				}

			}

			Chunk rückfragenChunk = new Chunk(bundle.getString("Report.inquiry") + addNewLines(2), timeframeFont);
			doc.add(new Phrase(rückfragenChunk));

			Font mailFont = new Font(bf_arial, 12);
			mailFont.setColor(Color.BLUE);
			mailFont.setStyle(Font.UNDERLINE);
			
			if (bundle.getString("Report.email").length() > 0) {
				doc.add(new Phrase(new Chunk(bundle.getString("Report.email"))));
				doc.add(new Phrase(new Chunk(bundle.getString("Report.mailaddress"), mailFont).setAnchor("mailto:"+ bundle.getString("Report.mailaddress"))));
			}
//			Chunk mailChunk = new Chunk("E-Mail: " + mailAnchor + "<" + mailToAnchor + ">" + addNewLines(1));
			
			if (bundle.getString("Report.telnum").length() > 0) {
				Chunk phoneChunk = new Chunk(addNewLines(1) + bundle.getString("Report.telnum"));
				doc.add(new Phrase(phoneChunk));
			}

			if (bundle.getString("Report.check_for_dissonance").length() > 0) {
				Chunk correctionChunk = new Chunk(addNewLines(3) + bundle.getString("Report.check_for_dissonance") + addNewLines(1));
				doc.add(new Phrase(correctionChunk));
			}
			
			Chunk mailChunk = new Chunk(addNewLines(1) + "            " + bundle.getString("Report.per_mail"), timeframeFontBold);
			Chunk mailAddChunk = new Chunk(bundle.getString("Report.contactmailaddress"), mailFont).setAnchor("mailto:" + bundle.getString("Report.contactmailaddress"));
			doc.add(mailChunk);
			doc.add(mailAddChunk);

			Chunk faxChunk = new Chunk(addNewLines(1) + "            " + bundle.getString("Report.per_fax"), timeframeFontBold);
			Chunk numberChunk = new Chunk("            " + bundle.getString("Report.faxNum"), timeframeFont);
			doc.add(faxChunk);
			doc.add(numberChunk);
			

			Chunk postChunk = new Chunk(addNewLines(1) + "            " + bundle.getString("Report.per_post"), timeframeFontBold);
			Chunk siwalChunk = new Chunk("           " + bundle.getString("Report.postString"), timeframeFont);
			doc.add(postChunk);
			doc.add(siwalChunk);
			
			Chunk rechnungsprüfungChunk = new Chunk(addNewLines(1) + addSpaces(new Integer(bundle.getString("Report.invoice_address_spaces"))) + bundle.getString("Report.invoice_verification"), timeframeFont);
			doc.add(rechnungsprüfungChunk);

			Chunk streetChunk = new Chunk(addNewLines(1) + addSpaces(new Integer(bundle.getString("Report.invoice_address_spaces"))) + bundle.getString("Report.postStreet"), timeframeFont);
			doc.add(streetChunk);

			Chunk addressChunk = new Chunk(addNewLines(1) + addSpaces(new Integer(bundle.getString("Report.invoice_address_spaces"))) + bundle.getString("Report.postCity"), timeframeFont);
			doc.add(addressChunk);

			Chunk appendixChunk = new Chunk(addNewLines(2) + bundle.getString("Report.invoiceApproval"));
			doc.add(appendixChunk);

			String reverseCharge = bundle.getString("Report.reverseCharge");
			if (reverseCharge.length() > 0) {
				Chunk reverseChargeChunk = new Chunk(addNewLines(2) + bundle.getString("Report.reverseCharge"));
				doc.add(reverseChargeChunk);
			}
			
			Chunk sepa1Chunk = new Chunk(addNewLines(4) + bundle.getString("Report.sepaString"));
			Chunk sepa2Chunk = new Chunk(addNewLines(1) + "          " + bundle.getString("Report.iban"));
			Chunk sepa3Chunk = new Chunk(addNewLines(1) + "          " + bundle.getString("Report.bic"));
			doc.add(sepa1Chunk);
			doc.add(sepa2Chunk);
			doc.add(sepa3Chunk);
	
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

	private ArrayList<TableRow> addCalculationRows(
			ArrayList<TableRow> tableRowList,
			List<DaoObject> customerCards, Customer customer, BigDecimal nettoSum, Calendar calcMonth, Bill bill) {

		LOGGER.info("Method: addCalculationRows");
		DecimalFormat df = new DecimalFormat("#0.00");

		BigDecimal mwst = nettoSum.multiply(new BigDecimal(bundle.getString("Report.value_added_tax"))).setScale(2, RoundingMode.HALF_UP);;
		
		int firstcols = customer.getInvoiceConfiguration().getColumns().length;
	
		
		String[] cr1 = createCalcRow(firstcols, bundle.getString("Report.net_sum"), df.format(nettoSum).replace(".", ",") + " " + bundle.getString("Report.euro_sign"));;
		tableRowList.add(new TableRow(1, cr1));

		if (customer.getVouchers() != null && customer.getVouchers().size() > 0) {
			for (Voucher v : customer.getVouchers()) {
				if (calcMonth.get(Calendar.YEAR) == v.getYear() && calcMonth.get(Calendar.MONTH) == v.getMonth()) {
					billContainsVoucher = true;
					String[] cr1_1 = new String[cr1.length];
					cr1_1[0] = "";
					cr1_1[1] = "" + v.getCardAmount();
					cr1_1[2] = "Gutschrift für " + DateUtils.getMonthAsString(v.getMonth()) + " " + v.getYear();
					int cri = 3;
					int diff = cr1.length - 4;
					for (int i = 0; i < diff; i++) {
						cr1_1[cri + i] = "";
					}
//					cr1_1[3] = "";
					String vs = ("-" + v.getTotalVoucher()).replace(".", ",");
					if (vs.indexOf(",") == (vs.length() - 2))
						vs += "0";
					cr1_1[cr1.length - 1] = vs + " " + bundle.getString("Report.euro_sign");
					nettoSum = nettoSum.subtract(new BigDecimal(v.getTotalVoucher()));
					String[] cr1_2 = createCalcRow(firstcols, "", "" + df.format(nettoSum).replace(".", ",") + " " + bundle.getString("Report.euro_sign"));
					mwst = nettoSum.multiply(new BigDecimal(bundle.getString("Report.value_added_tax"))).setScale(2, RoundingMode.HALF_UP);;
					tableRowList.add(new TableRow(1, cr1_1));
					tableRowList.add(new TableRow(1, cr1_2));
				}
			}
		}
		String[] cr2 = createCalcRow(firstcols, bundle.getString("Report.vat"), "" + df.format(mwst).replace(".", ",") + " " + bundle.getString("Report.euro_sign"));
		String[] cr3 = createCalcRow(firstcols, "", "");
		BigDecimal finalAmount = mwst.add(nettoSum);
		String[] cr4 = createCalcRow(firstcols, bundle.getString("Report.final_amount"), df.format(finalAmount).replace(".", ",") + " " + bundle.getString("Report.euro_sign"));
//		String[] cr4 = createCalcRow(firstcols, "Endbetrag", df.format(mwst.add(netSumAfterVoucher)).replace(".", ",") + " €");

		bill.setNettoPrice(nettoSum);
		bill.setVat(mwst);
		bill.setBruttoPrice(finalAmount);
		
		tableRowList.add(new TableRow(1, cr2));
		tableRowList.add(new TableRow(1, cr3));
		tableRowList.add(new TableRow(1, cr4));
		
		return tableRowList;
	}

	private String[] createCalcRow(int firstcols, String text, String value) {
		LOGGER.info("Method: createCalcRow");
		String[] row = new String[firstcols+3];
		int i = 0;
		while (i < firstcols+1) {
			row[i] = "";
			i++;
		}
		row[i] = text;
		i++;
		row[i] = value;
		return row;
	}

	private ArrayList<PdfPTable> prepareTables(ArrayList<TableRow> tableRowList, Customer customer, Boolean flatrateCalc) throws Exception {
		LOGGER.info("Method: prepareTables");
		ArrayList<PdfPTable> tableList = new ArrayList<PdfPTable>();
		
		// check, ob alles inkl. Tabellenende auf 1 Seite passt
		if (tableRowList.size() < MAX_ROW_FIRST_PAGE) {
			tableList.add(createTable(tableRowList, true, customer, flatrateCalc));
			return tableList;
		}
		
		/* Erstellung aller inkl. erster Tabelle (erste Seite nur 26 Zeilen, danach 35,
		 * außer bei letzter Seite, da hier noch das Tabellenende mit drauf muss) 
		 */
		int fullTableCount = new Double((tableRowList.size() - MAX_ROW_FIRST_PAGE) / FULL_PAGE_SIZE).intValue();

		// create table for first page
		ArrayList<TableRow> firstPageList = new ArrayList<TableRow>();
		int i = 0;
		int tablePos = 0;
		while (tablePos < MAX_ROW_FIRST_PAGE) {
			if (tablePos+6 < tableRowList.size()) {
				firstPageList.add(tableRowList.get(i));
				i++;
				tablePos += tableRowList.get(i).getInvoiceRows();
			} else {
				break;
			}
		}
		tableList.add(createTable(firstPageList, false, customer, flatrateCalc));
		
		// create tables for other full pages
		for (int j=1; j<fullTableCount+1; j++) {
			ArrayList<TableRow> otherFullTableList = new ArrayList<TableRow>();
			while (tablePos < (MAX_ROW_FIRST_PAGE + j*FULL_PAGE_SIZE)) {
				if (tablePos+6 < tableRowList.size()) {
					otherFullTableList.add(tableRowList.get(i));
					i++;
					tablePos += tableRowList.get(i).getInvoiceRows();
				} else {
					break;
				}
			}
			tableList.add(createTable(otherFullTableList, false, customer, flatrateCalc));
		}
		
		// create last table
		ArrayList<TableRow> lastTable = new ArrayList<TableRow>();
		while (i < tableRowList.size()) {
			lastTable.add(tableRowList.get(i));
			i++;
		}
		tableList.add(createTable(lastTable, true, customer, flatrateCalc));
		
		return tableList;
	}

	private PdfPTable createTable(ArrayList<TableRow> currentRowList, boolean lastPage, Customer customer, Boolean flatrateCalc) throws Exception {
		LOGGER.info("createTable");
		PdfPTable table = createTableHeader(customer, flatrateCalc);
		if (table != null) {
			if (lastPage) {
				ArrayList<TableRow> bodyList = new ArrayList<TableRow>();
				ArrayList<TableRow> endList = new ArrayList<TableRow>();
				int lastRows = 4;
				if (billContainsVoucher) {
					lastRows = 6;
				}
				for (int i = 0; i < (currentRowList.size() - lastRows); i++) {
					bodyList.add(currentRowList.get(i));
				}
//				for (int i = (currentRowList.size() - 6); i < currentRowList.size(); i++) {
				for (int i = (currentRowList.size() - lastRows); i < currentRowList.size(); i++) {
					endList.add(currentRowList.get(i));
				}
				table = createTableBody(table, bodyList, flatrateCalc);
				table = createTableEnd(table, endList);
			} else {
				table = createTableBody(table, currentRowList, flatrateCalc);
				
			}
		} else {
			return null;
		}
		return table;
	}
	
	private PdfPTable createTableEnd(PdfPTable table,
			ArrayList<TableRow> tableEndList) {
		LOGGER.info("Method: createTableEnd");
		Font tableFontBold = new Font(bf_arial, 9);
		tableFontBold.setStyle(Font.BOLD);
		Font tableFont = new Font(bf_arial, 9);
		Iterator<TableRow> it = tableEndList.iterator();
		while (it.hasNext()) {
			TableRow tr = it.next();
			PdfPCell cell = new PdfPCell();
			cell.setPaddingTop(1);
			cell.setPaddingBottom(5);
			cell.setBorder(Rectangle.NO_BORDER);
			String[] cellS = tr.getInvoiceRow();
//			cell.setPhrase(new Phrase("", tableFontBold));
//			table.addCell(cell);
			for (int i = 0; i < cellS.length; i++) {
				if (i == 1) {
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				}
				if (i == 2)
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				else if (i + 1 == cellS.length)
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				if (i > 2) {
					cell.setPhrase(new Phrase(cellS[i], tableFontBold));
				} else {
					cell.setPhrase(new Phrase(cellS[i], tableFont));
				}
				if (tr.getInvoiceRows() == 1)
					cell.setFixedHeight(14);
				table.addCell(cell);
			}
		}
		return table;
	}

	private PdfPTable createTableBody(PdfPTable table, ArrayList<TableRow> tableRowList, Boolean flatrateCalc) {
		LOGGER.info("Method: createTableBody");
		Iterator<TableRow> it = tableRowList.iterator();
		Font tableFont = null;
		if (tableRowList.size() > 5) {
			
			tableFont = new Font(bf_arial, 8);
//			MAX_ROW_FIRST_PAGE = 25;
//			FULL_PAGE_SIZE = 40;
		} else 
			tableFont = new Font(bf_arial, 9);

		
		while (it.hasNext()) {
			pos++;
			PdfPCell cell = new PdfPCell();
			cell.setPaddingTop(1);
			cell.setPaddingBottom(5);
			cell.setBorder(Rectangle.NO_BORDER);
			TableRow tr = it.next();
			String[] cellS = tr.getInvoiceRow();
			if (tr.getInvoiceRows() == 1)
				cell.setFixedHeight(14);
			cell.setPhrase(new Phrase(""+pos, tableFont));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			for (int i = 0; i < cellS.length; i++) {
				if (i == 0)
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				else if (i+1 == cellS.length)
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				else
					cell.setHorizontalAlignment(Element.ALIGN_LEFT);

				cell.setPhrase(new Phrase(cellS[i], tableFont));
				table.addCell(cell);
			}
		}

		return table;
	}

	private PdfPTable createTableHeader(Customer customer, Boolean flatrateCalc) throws Exception {
		LOGGER.info("Method: createTableHeader");
		List<String> columns = null;
		ArrayList<String> cols = new ArrayList<String>();
		if (customer != null) {
			columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
			for (String s: columns) {
				if (s != null)
					cols.add(s);
			}
		} else {
			LOGGER.error("Customer is NULL!");
			throw new Exception("Customer is NULL");
		}

		if (columns.size() < 1) {
			LOGGER.warn("Customer " + customer.getCustomernumber() + " hat keine Rechnungsspalten konfiguriert!");
			throw new Exception("Customer " + customer.getCustomernumber() + " hat keine Rechnungsspalten konfiguriert!");
		}
		
		int anzahl_mandatory_spalten = 3;
		
		// Tabelle erstellen mit Default-Parameter ; +1 wegen Pos-Spalte
		float[] colSizes = new float[cols.size() + anzahl_mandatory_spalten];
		
		Model model = new Model();
		model.createModel();

		HashMap<String, Float> columnSizes = model.getColumnSize();
		try {
			colSizes[0] = columnSizes.get(Model.COLUMN_POS);
			colSizes[1] = columnSizes.get(Model.COLUMN_AMOUNT);

			for (int i = 0; i < cols.size(); i++) {
				if (flatrateCalc && cols.get(i).equals(Model.COLUMN_PLANT_NUMBER)) {
					colSizes[i+2] = 8f;
				} else {
					colSizes[i+2] = columnSizes.get(cols.get(i));
				}
			}
			colSizes[2+cols.size()] = columnSizes.get(Model.COLUMN_TOTAL_PRICE);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		
		PdfPTable tableHeader = new PdfPTable(colSizes);
		tableHeader.setWidthPercentage(100f);
		tableHeader.getDefaultCell().setBackgroundColor(null);
		tableHeader.getDefaultCell().setBorder(0);

		
		if (cols != null) {
			// Table Header
			Font tableFont = null;
			if (cols.size() > 2)
				tableFont = new Font(bf_arial, 8);
			else 
				tableFont = new Font(bf_arial, 9);
			PdfPCell cell = new PdfPCell(new Phrase(bundle.getString("Report.pos"), tableFont));
			cell.setPaddingTop(10);
			cell.setPaddingBottom(10);
			cell.setBorderWidthTop(0.5f);
			cell.setBorderWidthBottom(0.5f);
			cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			tableHeader.addCell(cell);
//			if (columns.contains(Model.COLUMN_AMOUNT)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_months"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				tableHeader.addCell(cell);
//			}
			if (cols.contains(Model.COLUMN_DESCRIPTION)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_description"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_CARD_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_card_num"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_TEL_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_tel_num"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_INST_PLZ)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_inst_plz"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_EINSATZORT)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_inst_city"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_INST_STREET)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_inst_street"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_PLANT_NUMBER)) {
				if (flatrateCalc && customer.getCustomernumber().equals("20243")) {
					cell.setPhrase(new Phrase(bundle.getString("Report.column_beschr"), tableFont));
				} else {
					cell.setPhrase(new Phrase(bundle.getString("Report.column_plant_number"), tableFont));
				}
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_EQUIP_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_equip_number"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_AUFTRAGS_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_auftrags_num"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_BESTELL_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_bestell_nummer"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_VERTRAG_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_vertrag_nr"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_BA_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_ba_num"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_WE_NR)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_we_num"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_COST_CENTER)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_cost_center"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
//			if (cols.contains(Model.COLUMN_TOTAL_PRICE)) {
				cell.setPhrase(new Phrase(bundle.getString("Report.column_total_price"), tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				tableHeader.addCell(cell);
//			}
//			if (cols.contains(Model.COLUMN_TOTAL_PRICE)) {
//				cell.setPhrase(new Phrase("Gesamtpreis", tableFont));
//				tableHeader.addCell(cell);
//			}

			cell.setPaddingTop(1);
			cell.setPaddingBottom(5);
			cell.setBorder(Rectangle.NO_BORDER);

		}
		return tableHeader;
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
	
	private Calendar getPeriodMaxCalcDate(String creationFrequency, Calendar calcMonth) {
		LOGGER.info("Method: getPeriodMaxCalcDate");
		Calendar periodMaxCalcDate = Calendar.getInstance();
		periodMaxCalcDate.set(calcMonth.get(Calendar.YEAR), calcMonth.get(Calendar.MONTH), 1, 0, 0, 0);
		periodMaxCalcDate.set(Calendar.MILLISECOND, 0);
		
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY)) {
			// Nothing to be done - Date set to first day of current month
		} else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY)) {
			if (calcMonth.get(Calendar.MONTH) < Calendar.APRIL)
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.APRIL);
			else if (calcMonth.get(Calendar.MONTH) < Calendar.JULY)
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.JULY);
			else if (calcMonth.get(Calendar.MONTH) < Calendar.OCTOBER)
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.OCTOBER);
			else {
				periodMaxCalcDate.add(Calendar.YEAR, 1);
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
			}
		} else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY)) {
			if (calcMonth.get(Calendar.MONTH) < Calendar.JULY)
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.JULY);
			else {
				periodMaxCalcDate.add(Calendar.YEAR, 1);
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
			}
		} else {
			// Jährliche Rechnung - darf aktuell auch erst abgerechnet werden, wenn das Jahr zuende ist
			periodMaxCalcDate.add(Calendar.YEAR, 1);
			periodMaxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
		}
		
		return periodMaxCalcDate;
	}

	private int getMonthAmount(String creationFrequency, String status, Date activationDate, Date deactivationDate, Calendar periodMaxCalcDate) {
		LOGGER.info("Method: getMonthAmount");
		int monthAmount = 0;
		Calendar activationCal = null;
		if (activationDate != null) {
			activationCal = Calendar.getInstance();
			activationCal.setTime(activationDate);
		}
		Calendar deactivationCal = null;
		if (deactivationDate != null) {
			deactivationCal = Calendar.getInstance();
			deactivationCal.setTime(deactivationDate);
		}
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY))
			return 1;
		else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY)) {
			if (status.equalsIgnoreCase(Model.STATUS_ACTIVE)) {
				if (activationCal.get(Calendar.YEAR) == periodMaxCalcDate.get(Calendar.YEAR)) {
					monthAmount = periodMaxCalcDate.get(Calendar.MONTH) - activationCal.get(Calendar.MONTH);
					if (monthAmount > 3)
						return 3;
				} else if (activationCal.get(Calendar.YEAR)+1 == periodMaxCalcDate.get(Calendar.YEAR)) {
					monthAmount = 12 + periodMaxCalcDate.get(Calendar.MONTH) - activationCal.get(Calendar.MONTH);
					if (monthAmount > 3)
						return 3;
				} else {
					return 3;			}
			} else if (status.equalsIgnoreCase(Model.STATUS_INACTIVE)) {
				if (activationCal != null) {
					// Daten sind alle 0-basiert
					// QMAX = Maximale Anzahl Monate im Quartal
					int QMAX = 3;
					int MAX = periodMaxCalcDate.get(Calendar.MONTH);
					// Wenn MAX < QMAX ist, dann gibt es einen Jahreswechsel
					if (MAX < QMAX)
						MAX = 12;
					int DEACT = deactivationCal.get(Calendar.MONTH);
					int ACT = 0;
					if (activationCal.get(Calendar.YEAR) == deactivationCal.get(Calendar.YEAR)) {
						ACT = activationCal.get(Calendar.MONTH);
					}
					int DEACTRES = QMAX - (MAX-DEACT) + 1;
					int ACTDIFF = MAX-ACT;
					int ACTRES = QMAX-ACTDIFF;
					if (ACTRES < 0) ACTRES = 0;
					return DEACTRES-ACTRES;
				}
			}
		} else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY)) {
			if (status.equalsIgnoreCase(Model.STATUS_ACTIVE)) {
				if (activationCal.get(Calendar.YEAR) == periodMaxCalcDate.get(Calendar.YEAR)) {
					monthAmount = periodMaxCalcDate.get(Calendar.MONTH) - activationCal.get(Calendar.MONTH);
					if (monthAmount > 6)
						return 6;
				} else if (activationCal.get(Calendar.YEAR)+1 == periodMaxCalcDate.get(Calendar.YEAR)) {
					monthAmount = 12 + periodMaxCalcDate.get(Calendar.MONTH) - activationCal.get(Calendar.MONTH);
					if (monthAmount > 6)
						return 6;
				} else {
					return 6;
				}
			} else if (status.equalsIgnoreCase(Model.STATUS_INACTIVE)) {
				if (activationCal != null) {
					// Daten sind alle 0-basiert
					// QMAX = Maximale Anzahl Monate im Quartal
					int QMAX = 6;
					int MAX = periodMaxCalcDate.get(Calendar.MONTH);
					// Wenn MAX < QMAX ist, dann gibt es einen Jahreswechsel
					if (MAX < QMAX)
						MAX = 12;
					int DEACT = deactivationCal.get(Calendar.MONTH);
					int ACT = 0;
					if (activationCal.get(Calendar.YEAR) == deactivationCal.get(Calendar.YEAR)) {
						ACT = activationCal.get(Calendar.MONTH);
					}
					int DEACTRES = QMAX - (MAX-DEACT) + 1;
					int ACTDIFF = MAX-ACT;
					int ACTRES = QMAX-ACTDIFF;
					if (ACTRES < 0) ACTRES = 0;
					return DEACTRES-ACTRES;
				}
			}
		} else if (creationFrequency.equals(Model.FREQUENCY_YEARLY)) {
			if (activationCal.get(Calendar.YEAR) + 1 == periodMaxCalcDate.get(Calendar.YEAR))
				monthAmount = 12 - activationCal.get(Calendar.MONTH);
			else 
				monthAmount = 12;
		}
		return monthAmount;
	}
	
	/*
	 * Methode für Jahreskarten, um den Anfangsmonat für den Berechnungszeitraum zu ermitteln
	 * Die Karten, die hier untersucht werden, sind alle in diesem Jahr oder davor aktiviert worden.
	 */
	private String getStartMonthFromCards(int year, List<DaoObject> customerCards) {
		LOGGER.info("Method: getStartMonthFromCards");
		String startMonth = null;
		// Initialisierung mit spätestem Zeitpunkt im Berechnungsjahr
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		for (DaoObject cardDao : customerCards) {
			Calendar cardCal = Calendar.getInstance();
			cardCal.setTime(((CardBean) cardDao).getActivationDate());
			if (cardCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
				if (cardCal.get(Calendar.MONTH) < cal.get(Calendar.MONTH)) {
					cal.set(Calendar.MONTH, cardCal.get(Calendar.MONTH));
				}
			} else if (cardCal.get(Calendar.YEAR) < cal.get(Calendar.YEAR)) {
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				break;
			}
		}
		
		startMonth = DateUtils.getMonthAsString(cal.get(Calendar.MONTH));
		
		return startMonth;
	}

}
