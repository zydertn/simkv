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
import java.util.Map;
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
	BaseFont bf_broadway = null;
	BaseFont bf_arial = null;
	static final Logger logger = Logger.getLogger(ReportGenerator_portrait.class);
//	private static int MAX_ROW_FIRST_PAGE = 25;
	private static int MAX_ROW_FIRST_PAGE = 25;
//	private static int FULL_PAGE_SIZE = 39;
	private static int FULL_PAGE_SIZE = 40;
	private int pos = 0;
	private static int sevBillInvNum = 35000;
	private boolean billContainsVoucher = false;
	private boolean writeToDB = true;

	
	public ReportGenerator_portrait() {
		loadBaseFonts();
	}
	
	public boolean generateReportDirect(List<DaoObject> customerCards, Customer customer, Calendar calcMonth, boolean flatrateCalc, boolean severalBills, int mapCount) {
		writeToDB = false;
		return generateReport(customerCards, customer, calcMonth, flatrateCalc, severalBills, mapCount);
	}

	public boolean generateReport(List<DaoObject> customerCards, Customer customer, Calendar calcMonth, boolean flatrateCalc, boolean severalBills, int mapCount) {
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
			System.out.println("genRep Teil 1 = " + diff1);

			
			
//			File dir = new File("C:/Temp/report/" + customer.getCustomernumber());
//			File dir = new File("C:/Temp/report/" + year + "/" + month);
//			dir.mkdirs();
			long time3 = System.currentTimeMillis();
			File dir = new File("C:/Temp/report/");
			
			
			FileOutputStream fos = new FileOutputStream(
					dir + "/" + filename);
			
			
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			logger.info("Berechne für Monat: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR));
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
			System.out.println("findBill = " + difffb);
			
			long time4 = System.currentTimeMillis();
			long diff2 = time4-time3;
			System.out.println("genRep Teil 2 = " + diff2);

			long time5 = System.currentTimeMillis();
			int invoiceNumber = -1;
			boolean increaseBillNum = false;
			if (dbBill != null) {
				System.out.println("Rechnung bereits in DB enthalten... Hole Rechnungsnummer...");
				invoiceNumber = dbBill.getBillNumber();
			} else {
				invoiceNumber = (bc.getMaxBillNumber())+1;
				increaseBillNum = true;
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
			System.out.println("genRep Teil 3 = " + diff3);

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
			System.out.println("genRep Teil 4 = " + diff4);

			long time9 = System.currentTimeMillis();
			boolean generatedWithErrors = generateBody(writer, document, customerCards, customer, calcMonth, flatrateCalc, severalBills, invoiceNumber);
			if (generatedWithErrors) {
				return false;
			}
			
			document.close();
			fos.flush();
			fos.close();
			long time10 = System.currentTimeMillis();
			long diff5 = time10-time9;
			System.out.println("genRep Teil 5 = " + diff5);

			
			logger.info("File " + filename + " created successfully!");
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
				// TODO: handle exception
				e.printStackTrace();
			}
			
			if (b != null && b.length > 0) {
				System.out.println("Byte[] Länge == " + b.length);
				bill.setFile(b);
				
					
				long time13 = System.currentTimeMillis();
				boolean createdNew = bc.createOrUpdateObject(bill);
				long time14 = System.currentTimeMillis();
				long diff7 = time14-time13;
				System.out.println("createOrUpdateObject = " + diff7);
				
				if (createdNew)
					System.out.println("Neue Rechnung in DB gespeichert");
				else
					System.out.println("Rechnung in DB aktualisiert");
			}
			long time12 = System.currentTimeMillis();
			long diff6 = time12-time11;
			System.out.println("genRep Teil 6 = " + diff6);

			
			
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
			logger.error(ex.getMessage());
			return false;
		}
	}

	private HeaderFooter generateHeader(PdfWriter writer) {
		HeaderFooter header = null;

		try {

			Image logo = Image.getInstance("images/SiwalTec_Logo.wmf");
//			Image sender = Image.getInstance("images/SiwalTec_Absenderzeile.wmf");
			
			
			Chunk logoChunk = new Chunk(logo, -25, -20);
			Phrase phrase = new Phrase(logoChunk);
//			Chunk senderChunk = new Chunk(sender, -307, -90);
//			phrase.add(senderChunk);
			
			header = new HeaderFooter(phrase, false);
			header.setAlignment(Element.ALIGN_RIGHT);
			header.setBorder(Rectangle.NO_BORDER);
			
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			return null;
		}

		return header;
	}

	private HeaderFooter generateFooter(PdfWriter writer) {
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
			ex.printStackTrace();
			return null;
		}

		return footer;
	}

	private boolean generateBody(PdfWriter writer, Document doc, List<DaoObject> customerCards, Customer customer, Calendar calcMonth, Boolean flatrateCalc, boolean severalBills, int invoiceNumber) {
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
			String companyString = "Firma";
			if (customer.getContactPerson().getGender() != null) {
				companyString = customer.getContactPerson().getGender();
			}
			y = y - 20;
			int d = 11;
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyString, x, y,
					0);

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
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getStreet() + " " + customer.getInvoiceAddress().getHousenumber(),
						x, y, 0);
			} else if (customer.getInvoiceAddress().getPostbox() != null && customer.getInvoiceAddress().getPostbox().length() > 0) {
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Postfach "+ customer.getInvoiceAddress().getPostbox(), x, y, 0);
			}

			y = y - d;
			// Firmenort
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getPostcode() + " " + customer.getInvoiceAddress().getCity(), x,
					y, 0);
//			cb.endText();

//			// SiwalTec GmbH Absender
//			x = 445;
//			y = 480;
//			cb.beginText();
//			cb.setColorFill(Color.BLUE);
//			cb.setFontAndSize(bf_broadway, 10);
//
//			// Siwalstrasse Absender
//			y = y - 2;
//			cb.setColorFill(Color.BLACK);
//			cb.setFontAndSize(bf_arial, 10);

			// Date
//			y = y - 5 * d;
			Date date = new Date();
			// Temporär auf 14. Januar gesetzt
//			date.setDate(13);
//			date.setMonth(2);
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			df.setTimeZone(TimeZone.getDefault());
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, df.format(date), 425, 630, 0);

			// Rechnungsnummer
			String invoiceText = "Rechnung - Nr.";
			int invNum = 30001;
			if (calcMonth.get(Calendar.MONTH) == Calendar.NOVEMBER) {
				invNum = 30201;
			} else if (calcMonth.get(Calendar.MONTH) == Calendar.DECEMBER) {
				invNum = 30401;
			} else if (calcMonth.get(Calendar.MONTH) == Calendar.JANUARY) {
				invNum = 30601;
			}
/*			String invoiceNumber = "";
			String[] invNums = new String[] {"20074", "20190", "20208", "20206", "20216", "20166", "20120", 
					"20016", "20039", "20076", "20198", "20200", "20128", "20157", "20012", "20060", 
					"20105", "20197", "20224", "20066", "20069", "20094", "20107", "20112", "20209", 
					"20218", "20079", "20098", "20165", "20201", "20212", "20214", "20219", "20145",
					"20149", "20188", "20215", "20018", "20043", "20078", "20080", "20174", "20180",
					"20040", "20054", "20183", "20213", "20221", "20017", "20059", "20119", "20228",
					"20002", "20014", "20015", "20048", "20070", "20073", "20075", "20102", "20164",
					"20041", "20065", "20130", "20147", "20155", "20163", "20184", "20191", "20235",
					"20237", "20238", "20239", "20240", "20087", "20182", "20194", "20156", "20234",
					"20169", "20170", "20241", "20242", "20244", "20160", "20101", "20210", "20133",
					"20203", "20134", "20092", "20205", "20135", "20132", "20100", "20136", "20175",
					"20196", "20224", "20169", "20170", "20241", "20242", "20013", "20036", "20050",
					"20055", "20063", "20093", "20110", "20111", "20121", "20126", "20177", "20193",
					"20230", "20090", "20097", "20127", "20159", "20161", "20172", "20220", "20229",
					"20251", "20252", "20253", "20254", "20255", "20256", "20243", "20243_flatrate",
					"20245", "20217", "20038", "20257", "20258", "20003", "20259", "20006", "20260",
					"20004", "20005", "20057", "20020", "20021", "20022", "20008", "20023", "20007",
					"20010", "20009", "20263", "20123", "20058", "20045", "20047", "20052", "20091",
					"20095", "20114", "20115", "20124", "20131", "20146", "20150", "20167", "20176",
					"20181", "20185", "20199", "20233", "20072", "20011", "20051", "20067", "20109",
					"20113", "20117", "20118", "20125", "20152", "20226", "20261", "20042", "20099",
					"20103", "20108", "20122", "20158", "20222", "20232", "20056", "20137", "20026",
					"20027", "20028", "20029", "20030", "20031", "20032", "20033", "20035", "20138",
					"20139", "20140", "20141", "20142", "20143", "20144", "20034"};
			HashMap<String, Integer> invNumMap = new HashMap<String, Integer>();
			for (String s: invNums) {
				invNumMap.put(s, invNum);
				invNum++;
			}
*/		
			// Rechnungsnummer
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Rechnung - Nr.", 425, 600, 0);
/*			int invoiceNum = 0;
			try {
				invoiceNum = invNumMap.get(customer.getCustomernumber());
			} catch (NullPointerException e) {
				logger.error("Nullpointer bei " + customer.getCustomernumber());
			}
			// Sonderlocke
			if (flatrateCalc)
				invoiceNum = invNumMap.get("20243_flatrate");
			if (customer.getInvoiceConfiguration().getSeparateBilling() != null && customer.getInvoiceConfiguration().getSeparateBilling()) {
				invoiceNum = new Integer(this.sevBillInvNum);
				this.sevBillInvNum++;
			}
*/			
//			invoiceNum = 35593;
			
			
			
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "0" + invoiceNumber, 510, 600, 0);
//			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "07075", 516, 600, 0);
			
			// Kundennr.
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Kunden - Nr.     ", 425, 589, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "" + customer.getCustomernumber(), 516, 589, 0);

			int abzSuppNum = 0;
			if (customer.getSupplierNumber() != null && customer.getSupplierNumber().length() > 0) {
				// Lieferantennr.
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Lieferanten - Nr.     ", 425, 578, 0);
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "" + customer.getSupplierNumber(), 510, 578, 0);
				abzSuppNum = 11;
			}
			
			cb.setFontAndSize(bf_arial, 8);
			String info = "(Bitte bei Bezahlung immer angeben)";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, info, 425, 578-abzSuppNum, 0);

			cb.endText();

			x = 60;
			y = 715;

			Font invoiceFont = new Font(bf_arial, 12, Font.BOLD);
			String invoiceS = "Rechnung";
			if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_QUARTERLY))
				invoiceS = "Quartalsrechnung";
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_HALFYEARLY))
				invoiceS = "Halbjahresrechnung";
			else if (customer.getInvoiceConfiguration().getCreationFrequency().equals(Model.FREQUENCY_YEARLY))
				invoiceS = "Jahresrechnung";
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
							+ "Berechnungszeitraum für die Servicegebühr: " + calcTimeString, timeframeFont);
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
				BigDecimal simPrice = new BigDecimal("0.0");
				if (!card.getStandardPrice()) {
					simPrice = new BigDecimal(""+simPrices.get(card.getSimPrice()));
				} else {
					if (simPrices.get(customer.getInvoiceConfiguration().getSimPrice()) != null) {
						simPrice = new BigDecimal(""+simPrices.get(customer.getInvoiceConfiguration().getSimPrice()));
					} else {
						logger.warn("SimPrice Key == " + customer.getInvoiceConfiguration().getSimPrice() + ", ==> Key gibt es nicht in SimPrice-Konfigurations-Map!");
					}
				}
				
				ArrayList<String> invoiceRowList = new ArrayList<String>();
				
				List<String> columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
//				if (columns.contains(Model.COLUMN_AMOUNT)) {
				int monthAmount = 1;
				Calendar periodMaxCalcDate = getPeriodMaxCalcDate(customer.getInvoiceConfiguration().getCreationFrequency(), calcMonth);
				monthAmount = getMonthAmount(customer.getInvoiceConfiguration().getCreationFrequency(), card.getActivationDate(), periodMaxCalcDate);
				calcSum = calcSum.add(simPrice.multiply(new BigDecimal(monthAmount)));

				BigDecimal dataOptionPrice = new BigDecimal("0.0");
				if (dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge()) != null) {
					dataOptionPrice.add(new BigDecimal(""+dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge())));
				} else {
					logger.warn("DataOptionSurcharge Key == " + customer.getInvoiceConfiguration().getDataOptionSurcharge() + ", ==> Key gibt es nicht in DataOptionSurcharge-Konfigurations-Map!");
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
						invoiceRowList.add("GSM Datenflat für Leitstand Otisstraße Berlin");
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

			tableRowList = addCalculationRows(tableRowList, customerCards, customer, calcSum, calcMonth);
			
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
				Chunk debtOrder = new Chunk(addNewLines(2) + "Zahlung erfolgt per Lastschrift." + addNewLines(2), timeframeFont);
				Phrase debtOrderPhrase = new Phrase(debtOrder);
				doc.add(debtOrderPhrase);
			} else {
				Chunk paymentDueDate = new Chunk(addNewLines(2) + "Bitte überweisen Sie den Rechnungsbetrag rein netto innerhalb von 10 Tagen ab Rechnungsdatum ", timeframeFont);
				Chunk paymentDueDateEnd = new Chunk("unter Angabe der Rechnungsnummer." + addNewLines(2), timeframeFontBold);
				Phrase payPhrase = new Phrase(paymentDueDate);
				payPhrase.add(paymentDueDateEnd);
				doc.add(new Phrase(payPhrase));

				Chunk verzugsChunk = new Chunk("Wir weisen Sie darauf hin, dass Sie sich nach diesem Zeitraum bereits in Verzug befinden." + addNewLines(2), timeframeFont);
				doc.add(new Phrase(verzugsChunk));
			}

			Chunk rückfragenChunk = new Chunk("Bei Rückfragen können Sie sich gerne an folgende Kontaktdaten wenden:" + addNewLines(2), timeframeFont);
			doc.add(new Phrase(rückfragenChunk));

			Font mailFont = new Font(bf_arial, 12);
			mailFont.setColor(Color.BLUE);
			mailFont.setStyle(Font.UNDERLINE);
			
//			Chunk mailChunk = new Chunk("E-Mail: " + mailAnchor + "<" + mailToAnchor + ">" + addNewLines(1));
			doc.add(new Phrase(new Chunk("E-Mail: ")));
			doc.add(new Phrase(new Chunk("Controlling@siwaltec.de", mailFont).setAnchor("mailto:Controlling@siwaltec.de")));
			
			Chunk phoneChunk = new Chunk(addNewLines(1) + "Tel.Nr.: 07031-9858-444");
			doc.add(new Phrase(phoneChunk));

			Chunk correctionChunk = new Chunk(addNewLines(3) + "Bitte prüfen Sie diese Rechnung. Ihnen ist eine Unstimmigkeit aufgefallen? Dann können Sie sich mit Ihrem Widerspruch sehr gerne innerhalb von 6 Wochen an unsere Service Abteilung wenden:" + addNewLines(1));
			doc.add(new Phrase(correctionChunk));
			
			Chunk mailChunk = new Chunk(addNewLines(1) + "            per Mail:           ", timeframeFontBold);
			Chunk mailAddChunk = new Chunk("kontakt@siwaltec.de", mailFont).setAnchor("mailto:kontakt@siwaltec.de");
			doc.add(mailChunk);
			doc.add(mailAddChunk);

			Chunk faxChunk = new Chunk(addNewLines(1) + "            per Fax:", timeframeFontBold);
			Chunk numberChunk = new Chunk("            07031 98 58 317", timeframeFont);
			doc.add(faxChunk);
			doc.add(numberChunk);
			

			Chunk postChunk = new Chunk(addNewLines(1) + "            per Post:", timeframeFontBold);
			Chunk siwalChunk = new Chunk("           SiwalTec GmbH", timeframeFont);
			doc.add(postChunk);
			doc.add(siwalChunk);
			
			Chunk rechnungsprüfungChunk = new Chunk(addNewLines(1) + "                                    -Rechnungsprüfung-", timeframeFont);
			doc.add(rechnungsprüfungChunk);

			Chunk streetChunk = new Chunk(addNewLines(1) + "                                     Max-Eyth-Straße 35", timeframeFont);
			doc.add(streetChunk);

			Chunk addressChunk = new Chunk(addNewLines(1) + "                                     71088 Holzgerlingen", timeframeFont);
			doc.add(addressChunk);

			Chunk appendixChunk = new Chunk(addNewLines(2) + "Nach diesem Zeitraum gilt die Rechnung als geprüft und von Ihnen anerkannt. Durch die Freischaltung der SiwalTec SimKarte erkennen Sie unsere AGBs an.");
			doc.add(appendixChunk);

			Chunk sepa1Chunk = new Chunk(addNewLines(6) + "SEPA Kontodaten SiwalTec GmbH:");
			Chunk sepa2Chunk = new Chunk(addNewLines(1) + "          IBAN:   DE72 6039 0000 0406 1280 06");
			Chunk sepa3Chunk = new Chunk(addNewLines(1) + "          BIC:     GENODES1BBV");
			doc.add(sepa1Chunk);
			doc.add(sepa2Chunk);
			doc.add(sepa3Chunk);
	
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		
		return false;
	}
	
	private ArrayList<TableRow> addCalculationRows(
			ArrayList<TableRow> tableRowList,
			List<DaoObject> customerCards, Customer customer, BigDecimal nettoSum, Calendar calcMonth) {

		DecimalFormat df = new DecimalFormat("#0.00");

		BigDecimal mwst = nettoSum.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);;
		
		int firstcols = customer.getInvoiceConfiguration().getColumns().length;
	
		
		String[] cr1 = createCalcRow(firstcols, "Netto Summe", df.format(nettoSum).replace(".", ",") + " €");;
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
					cr1_1[cr1.length - 1] = vs + " €";
					nettoSum = nettoSum.subtract(new BigDecimal(v.getTotalVoucher()));
					String[] cr1_2 = createCalcRow(firstcols, "", "" + df.format(nettoSum).replace(".", ",") + " €");
					mwst = nettoSum.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);;
					tableRowList.add(new TableRow(1, cr1_1));
					tableRowList.add(new TableRow(1, cr1_2));
				}
			}
		}
		String[] cr2 = createCalcRow(firstcols, "19% MWST.", "" + df.format(mwst).replace(".", ",") + " €");
		String[] cr3 = createCalcRow(firstcols, "", "");
		String[] cr4 = createCalcRow(firstcols, "Endbetrag", df.format(mwst.add(nettoSum)).replace(".", ",") + " €");
//		String[] cr4 = createCalcRow(firstcols, "Endbetrag", df.format(mwst.add(netSumAfterVoucher)).replace(".", ",") + " €");

		tableRowList.add(new TableRow(1, cr2));
		tableRowList.add(new TableRow(1, cr3));
		tableRowList.add(new TableRow(1, cr4));
		
		return tableRowList;
	}

	private String[] createCalcRow(int firstcols, String text, String value) {
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
		List<String> columns = null;
		ArrayList<String> cols = new ArrayList<String>();
		if (customer != null) {
			columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
			for (String s: columns) {
				if (s != null)
					cols.add(s);
			}
		} else {
			logger.error("Customer is NULL!");
			throw new Exception("Customer is NULL");
		}

		if (columns.size() < 1) {
			logger.warn("Customer " + customer.getCustomernumber() + " hat keine Rechnungsspalten konfiguriert!");
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
			PdfPCell cell = new PdfPCell(new Phrase("Pos.", tableFont));
			cell.setPaddingTop(10);
			cell.setPaddingBottom(10);
			cell.setBorderWidthTop(0.5f);
			cell.setBorderWidthBottom(0.5f);
			cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			tableHeader.addCell(cell);
//			if (columns.contains(Model.COLUMN_AMOUNT)) {
				cell.setPhrase(new Phrase(Model.COLUMN_AMOUNT, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				tableHeader.addCell(cell);
//			}
			if (cols.contains(Model.COLUMN_DESCRIPTION)) {
				cell.setPhrase(new Phrase("Bezeichnung", tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_CARD_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_CARD_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_TEL_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_TEL_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_INST_PLZ)) {
				cell.setPhrase(new Phrase(Model.COLUMN_INST_PLZ, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_EINSATZORT)) {
				cell.setPhrase(new Phrase(Model.COLUMN_EINSATZORT, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_INST_STREET)) {
				cell.setPhrase(new Phrase(Model.COLUMN_INST_STREET, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_PLANT_NUMBER)) {
				if (flatrateCalc && customer.getCustomernumber().equals("20243")) {
					cell.setPhrase(new Phrase("Beschreibung", tableFont));
				} else {
					cell.setPhrase(new Phrase(Model.COLUMN_PLANT_NUMBER, tableFont));
				}
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_EQUIP_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_EQUIP_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_AUFTRAGS_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_AUFTRAGS_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_BESTELL_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_BESTELL_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_VERTRAG_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_VERTRAG_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_BA_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_BA_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_WE_NR)) {
				cell.setPhrase(new Phrase(Model.COLUMN_WE_NR, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
			if (cols.contains(Model.COLUMN_COST_CENTER)) {
				cell.setPhrase(new Phrase(Model.COLUMN_COST_CENTER, tableFont));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				tableHeader.addCell(cell);
			}
//			if (cols.contains(Model.COLUMN_TOTAL_PRICE)) {
				cell.setPhrase(new Phrase(Model.COLUMN_TOTAL_PRICE, tableFont));
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

			return tableHeader;
		} else {
			logger.warn("Bei Kunde " + customer.getCustomernumber() + " sind keine Rechnungsspalten selektiert!");
			throw new Exception("Bei Kunde " + customer.getCustomernumber() + " sind keine Rechnungsspalten selektiert!");
		}
	}
	
	private String addNewLines(int x) {
		String s = "";
		for (int i = 0; i < x; i++) {
			s = s + "\n";
		}
		return s;
	}

	private void loadBaseFonts() {
		try {
			bf_broadway = BaseFont.createFont("C:/Windows/Fonts/BROADW.TTF",
					"Cp1252", false);
			bf_arial = BaseFont.createFont("C:/Windows/Fonts/Arial.TTF",
					"Cp1252", false);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Calendar getPeriodMaxCalcDate(String creationFrequency, Calendar calcMonth) {
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
				periodMaxCalcDate.set(Calendar.MONTH, Calendar.JULY);
			}
		} else {
			// Jährliche Rechnung - darf aktuell auch erst abgerechnet werden, wenn das Jahr zuende ist
			periodMaxCalcDate.add(Calendar.YEAR, 1);
			periodMaxCalcDate.set(Calendar.MONTH, Calendar.JANUARY);
		}
		
		return periodMaxCalcDate;
	}

	private int getMonthAmount(String creationFrequency, Date activationDate, Calendar periodMaxCalcDate) {
		int monthAmount = 0;
		Calendar activationCal = Calendar.getInstance();
		activationCal.setTime(activationDate);
		if (creationFrequency.equals(Model.FREQUENCY_MONTHLY))
			return 1;
		else if (creationFrequency.equals(Model.FREQUENCY_QUARTERLY)) {
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
		} else if (creationFrequency.equals(Model.FREQUENCY_HALFYEARLY)) {
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
