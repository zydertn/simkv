package de.abd.mda.report;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

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

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
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

	public boolean generateReport(HashMap<String, List<CardBean>> cardMap, Customer customer, Calendar calcMonth) {
		try {
			Document document = new Document(PageSize.A4, 60, 25, 40, 40);
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

			boolean generatedWithErrors = generateBody(writer, document, cardMap, customer, calcMonth);
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
			Image sender = Image.getInstance("images/SiwalTec_Absenderzeile.wmf");
			
			
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

	private boolean generateBody(PdfWriter writer, Document doc, HashMap<String, List<CardBean>> cardMap, Customer customer, Calendar calcMonth) {
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
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getAddress().getStreet() + " " + customer.getAddress().getHousenumber(),
					x, y - 2 * d, 0);

			// Firmenort
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customer.getAddress().getPostcode() + " " + customer.getAddress().getCity(), x,
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
			Set<String> keySet = cardMap.keySet();
			Iterator<String> keyIt = keySet.iterator();
			while (keyIt.hasNext()) {
				String key = keyIt.next();
				List<CardBean> cardList = cardMap.get(key);

				int amount = cardList.size();
				
				Iterator<CardBean> iter = cardList.iterator();			
				CardBean card = null;
				String factoryNumbers = "";
				while (iter.hasNext()) {
					card = (CardBean) iter.next();
					factoryNumbers = factoryNumbers + card.getFactoryNumber() + ";";
				}
				String[] invoiceRow = { ""+ amount, card.getInstallAddress().getAddressString(), factoryNumbers, "" + (customer.getInvoiceConfiguration().getSimPrice() + customer.getInvoiceConfiguration().getDataOptionSurcharge()), "" + amount * (customer.getInvoiceConfiguration().getSimPrice() + customer.getInvoiceConfiguration().getDataOptionSurcharge()) };
				tableRowList.add(invoiceRow);
			}

			tableRowList = addCalculationRows(tableRowList, cardMap, customer);
			
//			tableRowList = addDummyRows(tableRowList);
			
			ArrayList<PdfPTable> tables = prepareTables(tableRowList);
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
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		
		return false;
	}
	
	private ArrayList<String[]> addCalculationRows(
			ArrayList<String[]> tableRowList,
			HashMap<String, List<CardBean>> cardMap, Customer customer) {

		double simPrice = customer.getInvoiceConfiguration().getSimPrice();
		double dataOptionSurcharge = customer.getInvoiceConfiguration().getDataOptionSurcharge();
		
		double price = simPrice + dataOptionSurcharge;
		
		Set<String> keys = cardMap.keySet();
		Iterator<String> keyIt = keys.iterator();
		int cardCount = 0;
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			cardCount = cardCount + cardMap.get(key).size();
		}
		double nettoSum = cardCount * price;
		DecimalFormat df = new DecimalFormat("#0.00");
		
		String[] cr1 = { "", "", "", "Netto Summe €", df.format(nettoSum) };
		String[] cr2 = { "", "", "", "19% MWST. €", df.format(nettoSum * 0.19) };
		String[] cr3 = { "", "", "", "", "" };
		String[] cr4 = { "", "", "", "Endbetrag €", df.format(nettoSum + nettoSum * 0.19) };

		tableRowList.add(cr1);
		tableRowList.add(cr2);
		tableRowList.add(cr3);
		tableRowList.add(cr4);
		
		return tableRowList;
	}

	private ArrayList<String[]> addDummyRows(ArrayList<String[]> tableRowList) {
		String[] r1 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r2 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r3 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r4 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r5 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r6 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r7 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r8 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r9 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r10 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r11 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r12 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r13 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r14 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r15 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r16 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r17 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r18 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r19 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r20 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r21 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r22 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209,210,211,212,213,214", "9,50 €", "9,50 €" };
		String[] r23 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r24 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r25 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r26 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r27 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r28 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r29 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r30 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r31 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r32 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r33 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r34 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r35 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r36 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r37 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r38 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r39 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r40 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r41 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r42 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209,210,211,212,213,214", "9,50 €", "9,50 €" };
		String[] r43 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r44 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r45 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r46 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r47 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r48 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r49 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r50 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r51 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r52 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r53 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r54 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r55 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r56 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r57 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r58 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r59 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r60 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r61 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r62 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209,210,211,212,213,214", "9,50 €", "9,50 €" };
		String[] r63 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r64 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r65 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r66 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r67 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r68 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r69 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r70 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r71 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r72 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r73 = { "1", "Obere Paulusstr. 126, 70197 Stuttgart", "14 078", "9,50 €", "9,50 €" };
		String[] r74 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
		String[] r75 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
		String[] r76 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
		String[] r77 = { "1", "Johannesstr. 65, 70176 Stuttgart", "22 480", "9,50 €", "9,50 €" };
		String[] r78 = { "1", "Silberburgstr. 144, 70176 Stuttgart", "13 28 214", "9,50 €", "9,50 €" };
		String[] r79 = { "1", "Alexanderstr. 87, 70182 Stuttgart", "13 25 778", "9,50 €", "9,50 €" };
		String[] r80 = { "1", "Onstmettinger Weg 17, 70567 Stuttgart-Möhringen", "4657", "9,50 €", "9,50 €" };
		String[] r81 = { "", "", "", "Netto Summe €", "218,50 €" };
		String[] r82 = { "", "", "", "19% MWST. €", "41,52 €" };
		String[] r83 = { "", "", "", "", "" };
		String[] r84 = { "", "", "", "Endbetrag €", "260,02 €" };
		
		
		tableRowList.add(r1);
		tableRowList.add(r2);
		tableRowList.add(r3);
		tableRowList.add(r4);
		tableRowList.add(r5);
		tableRowList.add(r6);
		tableRowList.add(r7);
		tableRowList.add(r8);
		tableRowList.add(r9);
		tableRowList.add(r10);
		tableRowList.add(r11);
		tableRowList.add(r12);
		tableRowList.add(r13);
		tableRowList.add(r14);
		tableRowList.add(r15);
		tableRowList.add(r16);
		tableRowList.add(r17);
		tableRowList.add(r18);
		tableRowList.add(r19);
		tableRowList.add(r20);
		tableRowList.add(r21);
		tableRowList.add(r22);
		tableRowList.add(r23);
		tableRowList.add(r24);
		tableRowList.add(r25);
		tableRowList.add(r26);
		tableRowList.add(r27);
		tableRowList.add(r28);
		tableRowList.add(r29);
		tableRowList.add(r30);
		tableRowList.add(r31);
		tableRowList.add(r32);
		tableRowList.add(r33);
		tableRowList.add(r34);
		tableRowList.add(r35);
		tableRowList.add(r36);
		tableRowList.add(r37);
		tableRowList.add(r38);
		tableRowList.add(r39);
		tableRowList.add(r40);
		tableRowList.add(r41);
		tableRowList.add(r42);
		tableRowList.add(r43);
		tableRowList.add(r44);
		tableRowList.add(r45);
		tableRowList.add(r46);
		tableRowList.add(r47);
		tableRowList.add(r48);
		tableRowList.add(r49);
		tableRowList.add(r50);
		tableRowList.add(r51);
		tableRowList.add(r52);
		tableRowList.add(r53);
		tableRowList.add(r54);
		tableRowList.add(r55);
		tableRowList.add(r56);
		tableRowList.add(r57);
		tableRowList.add(r58);
		tableRowList.add(r59);
//		tableRowList.add(r60);
//		tableRowList.add(r61);
//		tableRowList.add(r62);
//		tableRowList.add(r63);
//		tableRowList.add(r64);
//		tableRowList.add(r65);
//		tableRowList.add(r66);
//		tableRowList.add(r67);
//		tableRowList.add(r68);
//		tableRowList.add(r69);
//		tableRowList.add(r70);
//		tableRowList.add(r71);
//		tableRowList.add(r72);
//		tableRowList.add(r73);
//		tableRowList.add(r74);
//		tableRowList.add(r75);
//		tableRowList.add(r76);
//		tableRowList.add(r77);
//		tableRowList.add(r78);
//		tableRowList.add(r79);
//		tableRowList.add(r80);
		tableRowList.add(r81);
		tableRowList.add(r82);
		tableRowList.add(r83);
		tableRowList.add(r84);
		return tableRowList;
	}

	private ArrayList<PdfPTable> prepareTables(ArrayList<String[]> tableRowList) {
		ArrayList<PdfPTable> tableList = new ArrayList<PdfPTable>();
		
		// check, ob alles inkl. Tabellenende auf 1 Seite passt
		if (tableRowList.size() < MAX_ROW_FIRST_PAGE) {
			tableList.add(createTable(tableRowList, true));
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
		tableList.add(createTable(firstPageList, false));
		
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
			tableList.add(createTable(otherFullTableList, false));
		}
		
		// create last table
		ArrayList<String[]> lastTable = new ArrayList<String[]>();
		while (i < tableRowList.size()) {
			lastTable.add(tableRowList.get(i));
			i++;
		}
		tableList.add(createTable(lastTable, true));
		
		return tableList;
	}

	private PdfPTable createTable(ArrayList<String[]> currentRowList, boolean lastPage) {
		PdfPTable table = createTableHeader();
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

	private PdfPTable createTableHeader() {
		// Tabelle erstellen mit Default-Parameter
		PdfPTable tableHeader = new PdfPTable(new float[] { 1.5f, 2, 12.5f, 9, 4, 4 });
		tableHeader.setWidthPercentage(100f);
		tableHeader.getDefaultCell().setBackgroundColor(null);
		tableHeader.getDefaultCell().setBorder(0);

		// Table Header
		Font tableFont = new Font(bf_arial, 8);
		PdfPCell cell = new PdfPCell(new Phrase("Pos.", tableFont));
		cell.setPaddingTop(10);
		cell.setPaddingBottom(10);
		cell.setBorderWidthTop(0.5f);
		cell.setBorderWidthBottom(0.5f);
		cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
		tableHeader.addCell(cell);
		cell.setPhrase(new Phrase("Menge", tableFont));
		tableHeader.addCell(cell);
		cell.setPhrase(new Phrase("Bezeichnung", tableFont));
		tableHeader.addCell(cell);
		cell.setPhrase(new Phrase("Anlagen Nr.", tableFont));
		tableHeader.addCell(cell);
		cell.setPhrase(new Phrase("Einzelpreis", tableFont));
		tableHeader.addCell(cell);
		cell.setPhrase(new Phrase("Gesamtpreis", tableFont));
		tableHeader.addCell(cell);

		cell.setPaddingTop(1);
		cell.setPaddingBottom(5);
		cell.setBorder(Rectangle.NO_BORDER);

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
