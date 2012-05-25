package de.abd.mda.report;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.print.Doc;

import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import de.abd.mda.persistence.dao.CardBean;

public class ReportGenerator {
	BaseFont bf_broadway = null;
	BaseFont bf_arial = null;

	public ReportGenerator() {
		loadBaseFonts();
	}

	public boolean generateReport(List<CardBean> cards, Calendar calcDate, Calendar startDate, Calendar endDate) {
		try {
			Document document = new Document(PageSize.A4, 60, 25, 40, 40);
			FileOutputStream fos = new FileOutputStream(
					"C:/Temp/header-footer.pdf");
			PdfWriter writer = PdfWriter.getInstance(document, fos);

			// headers and footers must be added before the document is opened
			HeaderFooter header = generateHeader(writer);
			document.setHeader(header);

			HeaderFooter footer = generateFooter(writer);
			footer.setBorder(Rectangle.NO_BORDER);
			footer.setAlignment(Element.ALIGN_LEFT);
			document.setFooter(footer);

			document.open();

			generateBody(writer, document);

			document.close();

			System.out
					.println("File is created successfully showing header and footer.");
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler "
							+ "C:/Temp/header-footer.pdf");

			return true;
		} catch (Exception ex) {
			System.out.println(ex);
			return false;
		}
	}

	private HeaderFooter generateHeader(PdfWriter writer) {
		HeaderFooter header = null;

		try {
			Font broadwayFont = new Font(bf_broadway);
			broadwayFont.setColor(Color.BLUE);
			broadwayFont.setSize(26);

			Phrase phrase = new Phrase("SiwalTec GmbH", broadwayFont);

			header = new HeaderFooter(phrase, false);
			header.setAlignment(Element.ALIGN_RIGHT);
			header.setBorder(Rectangle.NO_BORDER);

		} catch (Exception ex) {
			ex.printStackTrace();
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

			Chunk c11 = new Chunk(addNewLines(4)+"Postanschrift        ", arialFontBold);
			Chunk c12 = new Chunk("SiwalTec GmbH", broadwayFont);
			Chunk c13 = new Chunk(", Hanns-Klemm-Straße 5A\n", arialFont);

			Chunk c21 = new Chunk("Konto                   ", arialFontBold);
			Chunk c22 = new Chunk(
					"lautet auf SiwalTec, Vereinigte Volksbank AG Böblingen/Sindelfingen Konto Nr. 406 128 006, BLZ 603 900 00\n",
					arialFont);

			Chunk c31 = new Chunk("Handelsregister    ", arialFontBold);
			Chunk c32 = new Chunk(
					"Amtsgericht Stuttgart HRB 727261, Sitz der Gesellschaft Hildrizhausen\n",
					arialFont);

			Chunk c41 = new Chunk("Geschäftsführer    ", arialFontBold);
			Chunk c42 = new Chunk("Hans Zyder, Richard Schraml\n", arialFont);

			Chunk c51 = new Chunk("Ident-Nummer      ", arialFontBold);
			Chunk c52 = new Chunk(
					"Ust. - ID - Nr.: DE 263592009  Steuernummer : 56098/12758\n",
					arialFont);

			Phrase footerPhrase = new Phrase();
			footerPhrase.add(c11);
			footerPhrase.add(c12);
			footerPhrase.add(c13);

			footerPhrase.add(c21);
			footerPhrase.add(c22);

			footerPhrase.add(c31);
			footerPhrase.add(c32);

			footerPhrase.add(c41);
			footerPhrase.add(c42);

			footerPhrase.add(c51);
			footerPhrase.add(c52);

			footerPhrase.setLeading(9);

			footer = new HeaderFooter(footerPhrase, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return footer;
	}

	private void generateBody(PdfWriter writer, Document doc) {
		try {
			int x = 60;
			int y = 710;

			// SiwalTec GmbH mit Unterstrich links oben
			PdfContentByte cb = writer.getDirectContent();
			cb.beginText();
			cb.setColorFill(Color.BLUE);
			cb.setFontAndSize(bf_broadway, 8);
			String siwalString = "SiwalTec GmbH";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalString, x, y, 0);

			cb.setLineWidth(0.6F);
			cb.moveTo(x, y - 2);
			cb.lineTo(126, y - 2);
			cb.setColorStroke(Color.BLUE);
			cb.stroke();
			cb.endText();

			y = y - 10;
			// Hans-Klemm-Strasse mit Unterstrich links oben
			cb.beginText();
			cb.setColorFill(Color.BLACK);
			cb.setFontAndSize(bf_arial, 8);
			String siwalAddSmall = "Hanns-Klemm-Straße 5A, 71157 Hildrizhausen";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalAddSmall, x, y,
					0);

			cb.setLineWidth(0.6F);
			cb.moveTo(x, y - 2);
			cb.lineTo(223, y - 2);
			cb.setColorStroke(Color.BLACK);
			cb.stroke();
			cb.endText();

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
			String companyNameString = "afs Nachlinger GmbH";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyNameString, x,
					y - d, 0);

			// Firmenstrasse
			String companyStreetString = "Johannesstrasse 57 A";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyStreetString,
					x, y - 2 * d, 0);

			// Firmenort
			String companyCityString = "70176 Stuttgart";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, companyCityString, x,
					y - 4 * d, 0);
			cb.endText();

			// SiwalTec GmbH Absender
			x = 445;
			y = 680;
			cb.beginText();
			cb.setColorFill(Color.BLUE);
			cb.setFontAndSize(bf_broadway, 10);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalString, x, y, 0);

			// Siwalstrasse Absender
			y = y - 2;
			cb.setColorFill(Color.BLACK);
			cb.setFontAndSize(bf_arial, 10);
			String siwalStreet = "Hanns-Klemm-Straße 5 a";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalStreet, x,
					y - d, 0);

			// SiwalCity Absender
			String siwalCity = "71157 Hildrizhausen";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalCity, x, y - 2
					* d, 0);

			// SiwalPhone Absender
			String siwalPhone = "Telefon (07034) 25 39 422";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalPhone, x, y - 3
					* d, 0);

			// SiwalCity Absender
			String siwalFax = "Telefax (07034) 2792 - 317";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalFax, x, y - 4
					* d, 0);

			// SiwalCity email
			String siwalMail = "email Kontakt@Siwaltec.de";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalMail, x, y - 5
					* d, 0);

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
			String customerNumber = "Kunden - Nr.     20074";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, customerNumber, x, y
					- 5 * d, 0);

			// Rechnungsnummer
			cb.setFontAndSize(bf_arial, 8);
			String info = "(Bitte bei Bezahlung immer angeben)";
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, info, x, y - 7 * d, 0);

			cb.endText();

			x = 60;
			y = 715;

			// SiwalTec GmbH mit Unterstrich links oben
			// cb.beginText();
			// cb.setColorFill(Color.BLACK);
			// cb.setFontAndSize(bf_arial, 10);

			// String siwalString = "SiwalTec GmbH";
			// cb.showTextAligned(PdfContentByte.ALIGN_LEFT, siwalString, x, y,
			// 0);

			Font invoiceFont = new Font(bf_arial, 12, Font.BOLD);
			Chunk invoice = new Chunk(addNewLines(15) + "Rechnung", invoiceFont);
			Font timeframeFont = new Font(bf_arial, 11);
			Chunk timeframe = new Chunk(
					addNewLines(2)
							+ "Berechnungszeitraum für die Servicegebühr: Juli 2010\n\n",
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
			
			ArrayList<String[]> tableCellList = new ArrayList<String[]>();
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
			String[] r22 = { "1", "Oppenheimer Str., 70449 Stuttgart", "277 060 206, 207,208,209", "9,50 €", "9,50 €" };
			String[] r23 = { "1", "Danneker Str. 48 C, 70182 Stuttgart", "270 064 177", "9,50 €", "9,50 €" };
			String[] r24 = { "1", "Kirchstr. 3, 70839 Gerlingen", "85 / 2204", "9,50 €", "9,50 €" };
			String[] r25 = { "", "", "", "Netto Summe €", "218,50 €" };
			String[] r26 = { "", "", "", "19% MWST. €", "41,52 €" };
			String[] r27 = { "", "", "", "", "" };
			String[] r28 = { "", "", "", "", "Endbetrag €", "260,02 €" };
			
			
			tableCellList.add(r1);
			tableCellList.add(r2);
			tableCellList.add(r3);
			tableCellList.add(r4);
			tableCellList.add(r5);
			tableCellList.add(r6);
			tableCellList.add(r7);
			tableCellList.add(r8);
			tableCellList.add(r9);
			tableCellList.add(r10);
			tableCellList.add(r11);
			tableCellList.add(r12);
			tableCellList.add(r13);
			tableCellList.add(r14);
			tableCellList.add(r15);
			tableCellList.add(r16);
			tableCellList.add(r17);
			tableCellList.add(r18);
			tableCellList.add(r19);
			tableCellList.add(r20);
			tableCellList.add(r21);
			tableCellList.add(r22);
			tableCellList.add(r23);
			tableCellList.add(r24);
			tableCellList.add(r25);
			tableCellList.add(r26);
			tableCellList.add(r27);
						
			// Tabellen-Body erstellen
			Iterator<String[]> it = tableCellList.iterator();
			int pos = 0;
			
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

			Font tableFontBold = new Font(bf_arial, 8);
			tableFontBold.setStyle(Font.BOLD);
			for (int i = 0; i < r28.length; i++) {
				cell.setPhrase(new Phrase(r28[i], tableFontBold));
				table.addCell(cell);
			}

			doc.add(table);
		} catch (Exception ex) {
			ex.printStackTrace();
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
