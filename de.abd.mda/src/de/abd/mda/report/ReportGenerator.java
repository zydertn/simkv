package de.abd.mda.report;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.util.DateUtils;

public class ReportGenerator {
	BaseFont bf_broadway = null;
	BaseFont bf_arial = null;
	static final Logger logger = Logger.getLogger(ReportGenerator.class);
	private static int MAX_ROW_FIRST_PAGE = 25;
	private static int FULL_PAGE_SIZE = 39;
	private int pos = 0;
	private int extraLineBreaks = 0;


	
	public ReportGenerator() {
		loadBaseFonts();
	}

	public boolean generateReport(List<DaoObject> customerCards, Customer customer, Calendar calcMonth) {
		try {
			Document document = new Document(PageSize.A4.rotate(), 60, 25, 40, 40);
			String month = "";
			if ((calcMonth.get(Calendar.MONTH) + 1) > 9) {
				month = month + (calcMonth.get(Calendar.MONTH) + 1);
			} else {
				month = "0" + (calcMonth.get(Calendar.MONTH) + 1);
			}
			String filename = customer.getCustomernumber() + "_" + calcMonth.get(Calendar.YEAR) + "-" + month + ".pdf";
			File dir = new File("C:/Temp/report/" + customer.getCustomernumber());
			dir.mkdirs();
			
			FileOutputStream fos = new FileOutputStream(
					dir + "/" + filename);
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			logger.info("Berechne für Monat: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH) + 1) + " " + calcMonth.get(Calendar.YEAR));

			// headers and footers must be added before the document is opened
			HeaderFooter header = generateHeader(writer);
			if (header != null) {
				document.setHeader(header);
			} else {
				return false;
			}

			HeaderFooter footer = generateFooter(writer);
			if (footer != null) {
				footer.setBorder(Rectangle.NO_BORDER);
				footer.setAlignment(Element.ALIGN_LEFT);
				document.setFooter(footer);
			} else {
				return false;
			}

			document.open();

			boolean generatedWithErrors = generateBody(writer, document, customerCards, customer, calcMonth);
			if (generatedWithErrors) {
				return false;
			}
			
			document.close();

			logger.info("File " + filename + " created successfully!");
//			Runtime.getRuntime().exec(
//					"rundll32 url.dll,FileProtocolHandler "
//							+ "C:/Temp/header-footer.pdf");

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

	private boolean generateBody(PdfWriter writer, Document doc, List<DaoObject> customerCards, Customer customer, Calendar calcMonth) {
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
			cb.setFontAndSize(bf_arial, 10);
			String companyString = "Firma";
			y = y - 20;
			int d = 11;
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyString, x, y,
					0);

			// Firmenname
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getName(), x,
					y - d, 0);

			// Firmenstrasse
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getStreet() + " " + customer.getInvoiceAddress().getHousenumber(),
					x, y - 2 * d, 0);

			// Firmenort
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getInvoiceAddress().getPostcode() + " " + customer.getInvoiceAddress().getCity(), x,
					y - 4 * d, 0);
			cb.endText();

			// SiwalTec GmbH Absender
			x = 445;
			y = 680;
			cb.beginText();
			cb.setColorFill(Color.BLUE);
			cb.setFontAndSize(bf_broadway, 10);

			// Siwalstrasse Absender
			y = y - 2;
			cb.setColorFill(Color.BLACK);
			cb.setFontAndSize(bf_arial, 10);

			// Date
			y = y - 5 * d;
			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
			df.setTimeZone(TimeZone.getDefault());
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, df.format(date), x, y
					- 2 * d, 0);

			// Rechnungsnummer
			String invoiceNumber = "Rechnung - Nr.   02357";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, invoiceNumber, x, y
					- 4 * d, 0);

			// Kundennr.
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Kunden - Nr.     " + customer.getCustomernumber(), x, y
					- 5 * d, 0);

			// Rechnungsnummer
			cb.setFontAndSize(bf_arial, 8);
			String info = "(Bitte bei Bezahlung immer angeben)";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, info, x, y - 7 * d, 0);

			cb.endText();

			x = 60;
			y = 715;

			Font invoiceFont = new Font(bf_arial, 12, Font.BOLD);
			Chunk invoice = new Chunk(addNewLines(15) + "Rechnung", invoiceFont);
			Font timeframeFont = new Font(bf_arial, 11);
			Chunk timeframe = new Chunk(
					addNewLines(2)
							+ "Berechnungszeitraum für die Servicegebühr: " + DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR) + "\n\n",
					timeframeFont);
			Phrase phrase = new Phrase();
			phrase.add(invoice);
			phrase.add(timeframe);
			// phrase.se
			doc.add(phrase);

			// Tabelle erstellen mit Default-Parameter
			PdfPTable table = new PdfPTable(new float[] { 1.5f, 2, 12.5f, 9, 4, 4 });
			table.setWidthPercentage(100f);
			table.getDefaultCell().setBackgroundColor(null);
			table.getDefaultCell().setBorder(0);

			// Table Header
			Font tableFont = new Font(bf_arial, 8);
			PdfPCell cell = new PdfPCell(new Phrase("Pos.", tableFont));
			cell.setPaddingTop(10);
			cell.setPaddingBottom(10);
			cell.setBorderWidthTop(0.5f);
			cell.setBorderWidthBottom(0.5f);
			cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
			table.addCell(cell);
			cell.setPhrase(new Phrase("Menge", tableFont));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Bezeichnung", tableFont));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Anlagen Nr.", tableFont));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Einzelpreis", tableFont));
			table.addCell(cell);
			cell.setPhrase(new Phrase("Gesamtpreis", tableFont));
			table.addCell(cell);

			cell.setPaddingTop(1);
			cell.setPaddingBottom(5);
			cell.setBorder(Rectangle.NO_BORDER);
			
			ArrayList<String[]> tableRowList = new ArrayList<String[]>();
			Iterator<DaoObject> iter = customerCards.iterator();			
			ConfigurationController cc = new ConfigurationController();
			Map<Integer, Double> simPrices = cc.getSimPricesFromDB();
			Map<Integer, Double> dataOptionPrices = cc.getDataOptionPricesFromDB();

			BigDecimal calcSum = new BigDecimal(0.0);
			
			while (iter.hasNext()) {
				CardBean card = (CardBean) iter.next();
				BigDecimal simPrice = new BigDecimal(0.0);
				if (!card.getStandardPrice()) {
					simPrice = new BigDecimal(card.getSimPrice());
				} else {
					if (simPrices.get(customer.getInvoiceConfiguration().getSimPrice()) != null) {
						simPrice = new BigDecimal(simPrices.get(customer.getInvoiceConfiguration().getSimPrice()));
					} else {
						logger.warn("SimPrice Key == " + customer.getInvoiceConfiguration().getSimPrice() + ", ==> Key gibt es nicht in SimPrice-Konfigurations-Map!");
					}
				}

				calcSum = calcSum.add(simPrice);
				
				Double dataOptionPrice = 0.0;
				if (dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge()) != null) {
					dataOptionPrice += dataOptionPrices.get(customer.getInvoiceConfiguration().getDataOptionSurcharge());
				} else {
					logger.warn("DataOptionSurcharge Key == " + customer.getInvoiceConfiguration().getDataOptionSurcharge() + ", ==> Key gibt es nicht in DataOptionSurcharge-Konfigurations-Map!");
				}

				calcSum = calcSum.add(new BigDecimal(dataOptionPrice));
				
				if (customer.getCustomernumber().equals("20074")) {
					if (calcMonth.get(Calendar.YEAR) == 2013 && calcMonth.get(Calendar.MONTH) == 2) {
						if (card.getCardNumberFirst().equals("72264689") && card.getCardNumberSecond().equals("5")) {
							System.out.println(DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH)) + " " + calcMonth.get(Calendar.YEAR));
						}
					}
				}

				
				
				System.out.println("Jetzt: Kunde " + customer.getCustomernumber());
				
				ArrayList<String> invoiceRowList = new ArrayList<String>();
	
				if (customer.getCustomernumber().equals("20074")) {
					System.out.println("Kunde 20074");
				}
				
				List<String> columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
				if (columns.contains(Model.COLUMN_AMOUNT)) {
					invoiceRowList.add("1");
				}
				if (columns.contains(Model.COLUMN_DESCRIPTION)) {
					invoiceRowList.add(card.getInstallAddress().getAddressString());
				}
				if (columns.contains(Model.COLUMN_PLANT_NUMBER)) {
					invoiceRowList.add(card.getFactoryNumber());
				}
				if (columns.contains(Model.COLUMN_SINGLE_PRICE)) {
					invoiceRowList.add("" + (simPrice.add(new BigDecimal(dataOptionPrice))).setScale(2));
				}
				if (columns.contains(Model.COLUMN_TOTAL_PRICE)) {
					invoiceRowList.add("" + (simPrice.add(new BigDecimal(dataOptionPrice))).setScale(2));
				}

				if (!(invoiceRowList != null && invoiceRowList.size() > 0)) {
					throw new Exception("Keine Rechnungsspalten definiert!");
				}
				String[] invoiceRow = new String[invoiceRowList.size()];
				for (int i = 0; i < invoiceRowList.size(); i++) {
					invoiceRow[i] = invoiceRowList.get(i);
				}
				tableRowList.add(invoiceRow);
			}

			tableRowList = addCalculationRows(tableRowList, customerCards, customer, calcSum);
			
//			tableRowList = addDummyRows(tableRowList);
			
			ArrayList<PdfPTable> tables = prepareTables(tableRowList, customer);
			int i = 0;
			while (i < tables.size()) {
				doc.add(tables.get(i));
				if (i+1 < tables.size()){
					String lineBreaks = "\n\n\n\n\n";
					if (i+2 == tables.size()) {
						for (int j = 0; j < extraLineBreaks; j++) {
							lineBreaks += "\n";
						}
					}
					doc.add(new Phrase(new Chunk(lineBreaks)));
				}
				i++;
			}
			
			Chunk paymentDueDate = new Chunk(addNewLines(2) + "Bitte überweisen Sie den oben stehenden Betrag innerhalb von 14 Tagen nach Rechnungsdatum.", timeframeFont);
			doc.add(new Phrase(paymentDueDate));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		
		return false;
	}
	
	private ArrayList<String[]> addCalculationRows(
			ArrayList<String[]> tableRowList,
			List<DaoObject> customerCards, Customer customer, BigDecimal nettoSum) {

		DecimalFormat df = new DecimalFormat("#0.00");

		BigDecimal mwst = nettoSum.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);;
	
		int firstcols = customer.getInvoiceConfiguration().getColumns().length - 2;
	
		
		String[] cr1 = createCalcRow(firstcols, "Netto Summe €", df.format(nettoSum));;
		String[] cr2 = createCalcRow(firstcols, "19% MWST. €", "" + mwst);
		String[] cr3 = createCalcRow(firstcols, "", "");
		String[] cr4 = createCalcRow(firstcols, "Endbetrag €", df.format(mwst.add(nettoSum)));

		tableRowList.add(cr1);
		tableRowList.add(cr2);
		tableRowList.add(cr3);
		tableRowList.add(cr4);
		
		return tableRowList;
	}

	private String[] createCalcRow(int firstcols, String text, String value) {
		String[] row = new String[firstcols+2];
		int i = 0;
		while (i < firstcols) {
			row[i] = "";
			i++;
		}
		row[i] = text;
		i++;
		row[i] = value;
		return row;
	}

	private ArrayList<PdfPTable> prepareTables(ArrayList<String[]> tableRowList, Customer customer) throws Exception {
		ArrayList<PdfPTable> tableList = new ArrayList<PdfPTable>();
		
		// check, ob alles inkl. Tabellenende auf 1 Seite passt
		if (tableRowList.size() < MAX_ROW_FIRST_PAGE) {
			tableList.add(createTable(tableRowList, true, customer));
			return tableList;
		}
		
		/* Erstellung aller inkl. erster Tabelle (erste Seite nur 26 Zeilen, danach 35,
		 * außer bei letzter Seite, da hier noch das Tabellenende mit drauf muss) 
		 */
		int fullTableCount = new Double((tableRowList.size() - MAX_ROW_FIRST_PAGE) / FULL_PAGE_SIZE).intValue();

		// create table for first page
		ArrayList<String[]> firstPageList = new ArrayList<String[]>();
		int i = 0;
		while (i < MAX_ROW_FIRST_PAGE) {
			if (i+6 < tableRowList.size()) {
				firstPageList.add(tableRowList.get(i));
				i++;
			} else {
				extraLineBreaks = (MAX_ROW_FIRST_PAGE) - i;
				break;
			}

		}
		tableList.add(createTable(firstPageList, false, customer));
		
		// create tables for other full pages
		for (int j=1; j<fullTableCount+1; j++) {
			ArrayList<String[]> otherFullTableList = new ArrayList<String[]>();
			while (i < (MAX_ROW_FIRST_PAGE + j*FULL_PAGE_SIZE)) {
				if (i+6 < tableRowList.size()) {
					otherFullTableList.add(tableRowList.get(i));
					i++;
				} else {
					extraLineBreaks = (MAX_ROW_FIRST_PAGE + j*FULL_PAGE_SIZE) - i;
					break;
				}
			}
			tableList.add(createTable(otherFullTableList, false, customer));
		}
		
		// create last table
		ArrayList<String[]> lastTable = new ArrayList<String[]>();
		while (i < tableRowList.size()) {
			lastTable.add(tableRowList.get(i));
			i++;
		}
		tableList.add(createTable(lastTable, true, customer));
		
		return tableList;
	}

	private PdfPTable createTable(ArrayList<String[]> currentRowList, boolean lastPage, Customer customer) throws Exception {
		PdfPTable table = createTableHeader(customer);
		if (table != null) {
			if (lastPage) {
				ArrayList<String[]> bodyList = new ArrayList<String[]>();
				ArrayList<String[]> endList = new ArrayList<String[]>();
				for (int i = 0; i < (currentRowList.size() - 4); i++) {
					bodyList.add(currentRowList.get(i));
				}
				for (int i = (currentRowList.size() - 4); i < currentRowList.size(); i++) {
					endList.add(currentRowList.get(i));
				}
				table = createTableBody(table, bodyList);
				table = createTableEnd(table, endList);
			} else {
				table = createTableBody(table, currentRowList);
				
			}
		} else {
			return null;
		}
		return table;
	}
	
	private PdfPTable createTableEnd(PdfPTable table,
			ArrayList<String[]> tableEndList) {
		Font tableFontBold = new Font(bf_arial, 8);
		tableFontBold.setStyle(Font.BOLD);
		PdfPCell cell = new PdfPCell();
		cell.setPaddingTop(1);
		cell.setPaddingBottom(5);
		cell.setBorder(Rectangle.NO_BORDER);
		Iterator<String[]> it = tableEndList.iterator();
		while (it.hasNext()) {
			String[] cellS = it.next();
			cell.setPhrase(new Phrase("", tableFontBold));
			table.addCell(cell);
			for (int i = 0; i < cellS.length; i++) {
				cell.setPhrase(new Phrase(cellS[i], tableFontBold));
				cell.setFixedHeight(14);
				table.addCell(cell);
			}
		}
		return table;
	}

	private PdfPTable createTableBody(PdfPTable table, ArrayList<String[]> tableRowList) {
		Iterator<String[]> it = tableRowList.iterator();
		Font tableFont = new Font(bf_arial, 8);
		PdfPCell cell = new PdfPCell();
		cell.setFixedHeight(14);
		cell.setPaddingTop(1);
		cell.setPaddingBottom(5);
		cell.setBorder(Rectangle.NO_BORDER);

		
		while (it.hasNext()) {
			pos++;
			String[] cellS = it.next();
			cell.setPhrase(new Phrase(""+pos, tableFont));
			table.addCell(cell);
			for (int i = 0; i < cellS.length; i++) {
				cell.setPhrase(new Phrase(cellS[i], tableFont));
				table.addCell(cell);
			}
		}

		return table;
	}

	private PdfPTable createTableHeader(Customer customer) throws Exception {
		List<String> columns = null;
		if (customer != null) {
			columns = Arrays.asList(customer.getInvoiceConfiguration().getColumns());
		} else {
			logger.error("Customer is NULL!");
			throw new Exception("Customer is NULL");
		}

		if (columns.size() < 1) {
			logger.warn("Customer " + customer.getCustomernumber() + " hat keine Rechnungsspalten konfiguriert!");
			throw new Exception("Customer " + customer.getCustomernumber() + " hat keine Rechnungsspalten konfiguriert!");
		}
		
		int anzahl_mandatory_spalten = 1;
		
		// Tabelle erstellen mit Default-Parameter ; +1 wegen Pos-Spalte
		float[] colSizes = new float[columns.size() + anzahl_mandatory_spalten];
		
		Model model = new Model();
		model.createModel();
		HashMap<String, Float> columnSizes = model.getColumnSize();
		colSizes[0] = columnSizes.get(Model.COLUMN_POS);
		for (int i = 0; i < columns.size(); i++) {
			colSizes[i+anzahl_mandatory_spalten] = columnSizes.get(columns.get(i));
		}
		
		
		PdfPTable tableHeader = new PdfPTable(colSizes);
		tableHeader.setWidthPercentage(100f);
		tableHeader.getDefaultCell().setBackgroundColor(null);
		tableHeader.getDefaultCell().setBorder(0);

		
		if (columns != null) {
			// Table Header
			Font tableFont = new Font(bf_arial, 8);
			PdfPCell cell = new PdfPCell(new Phrase("Pos.", tableFont));
			cell.setPaddingTop(10);
			cell.setPaddingBottom(10);
			cell.setBorderWidthTop(0.5f);
			cell.setBorderWidthBottom(0.5f);
			cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
			tableHeader.addCell(cell);
			if (columns.contains(Model.COLUMN_AMOUNT)) {
				cell.setPhrase(new Phrase("Menge", tableFont));
				tableHeader.addCell(cell);
			}
			if (columns.contains(Model.COLUMN_DESCRIPTION)) {
				cell.setPhrase(new Phrase("Bezeichnung", tableFont));
				tableHeader.addCell(cell);
			}
			if (columns.contains(Model.COLUMN_PLANT_NUMBER)) {
				cell.setPhrase(new Phrase("Anlagen Nr.", tableFont));
				tableHeader.addCell(cell);
			}
			if (columns.contains(Model.COLUMN_SINGLE_PRICE)) {
				cell.setPhrase(new Phrase("Einzelpreis", tableFont));
				tableHeader.addCell(cell);
			}
			if (columns.contains(Model.COLUMN_TOTAL_PRICE)) {
				cell.setPhrase(new Phrase("Gesamtpreis", tableFont));
				tableHeader.addCell(cell);
			}

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
}
