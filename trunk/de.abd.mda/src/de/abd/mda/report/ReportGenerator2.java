package de.abd.mda.report;

import java.awt.Color;
import java.io.FileOutputStream;

import javax.swing.GroupLayout.Alignment;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
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
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import de.abd.mda.persistence.dao.CardBean;

//public class ReportGenerator extends ActionController {
public class ReportGenerator2 {

	public boolean generatePDF(CardBean card) {

//	      try{
//              Document document=new Document();
//              FileOutputStream fos=new FileOutputStream("C:/Temp/header-footer.pdf");
//              PdfWriter writer = PdfWriter.getInstance(document, fos);
//              document.open();
//              Image footerImage = Image.getInstance("D:/Softwareentwicklung/Images/siwalfooter.png");
//              footerImage.scalePercent(40);
//              
//              footerImage.setAbsolutePosition(0, 0);
//
//              PdfContentByte footerByte = writer.getDirectContent();
//              PdfTemplate tp2 = footerByte.createTemplate(600, 100);
//              tp2.addImage(footerImage);
//
//              footerByte.addTemplate(tp2, 50, 20);
//                            
//              Phrase footerPhrase = new Phrase(footerByte + "", FontFactory.getFont(FontFactory.TIMES_ROMAN, 7, Font.NORMAL));
//
////              HeaderFooter header = new HeaderFooter(phrase1, true);
//              HeaderFooter footer = new HeaderFooter(footerPhrase, true);
////              document.setHeader(header);
//              document.setFooter(footer);
//              document.close();
//              System.out.println("File is created successfully showing header and footer.");
//              Runtime.getRuntime().exec(
//  					"rundll32 url.dll,FileProtocolHandler "
//  							+ "C:/Temp/header-footer.pdf");
//              }
//              catch (Exception ex){
//                  System.out.println(ex);
//
//              }
//
//	return false;
		
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		try {
			// creation of the different writers
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream("C:/Temp/iTextExample.pdf"));

			// various fonts
			BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA,
					"Cp1252", false);
			BaseFont bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN,
					"Cp1252", false);
			BaseFont bf_courier = BaseFont.createFont(BaseFont.COURIER,
					"Cp1252", false);
//			BaseFont bf_symbol1 = BaseFont.createFont(BaseFont.SYMBOL,
//					"Cp1252", false);
			BaseFont bf_arial = BaseFont.createFont(
					"C:/Windows/Fonts/Arial.TTF", "Cp1252", false);
			BaseFont bf_broadway = BaseFont.createFont(
					"C:/Windows/Fonts/BROADW.TTF", "Cp1252", false);

			Font broadwayFontHeader = new Font(bf_broadway);
			broadwayFontHeader.setColor(Color.BLUE);
			broadwayFontHeader.setSize(30);

			Font arialFontFooter = new Font(bf_arial);
			arialFontFooter.setSize(10);
			arialFontFooter.setStyle(Font.BOLD);
			
			// headers and footers must be added before the document is opened
			HeaderFooter footer = new HeaderFooter(new Phrase("Postanschrift: \nBla ",
					arialFontFooter), new Phrase("Juhu", broadwayFontHeader));
			footer.setBorder(Rectangle.NO_BORDER);
			footer.setAlignment(Element.ALIGN_LEFT);
			document.setFooter(footer);

			HeaderFooter header = new HeaderFooter(new Phrase("SiwalTec GmbH",
					new Font(broadwayFontHeader)), false);
			header.setAlignment(Element.ALIGN_RIGHT);
			header.setBorder(Rectangle.NO_BORDER);
			document.setHeader(header);

			document.open();

			int y_line1 = 650;
			int y_line2 = y_line1 - 50;
			int y_line3 = y_line2 - 50;

			// draw a few lines ...
			PdfContentByte cb = writer.getDirectContent();
			cb.setLineWidth(0f);
			// cb.moveTo(250, y_line3 - 100);
			// cb.lineTo(250, y_line1 + 100);
			// cb.moveTo(50, y_line1);
			// cb.lineTo(400, y_line1);
			// cb.moveTo(50, y_line2);
			// cb.lineTo(400, y_line2);
			// cb.moveTo(50, y_line3);
			// cb.lineTo(400, y_line3);
			cb.stroke();
			// ... and some text that is aligned in various ways
			cb.beginText();
			cb.setFontAndSize(bf_helv, 12);
			String text = "Sample text for alignment";
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, text + " Center",
					250, y_line1, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, text + " Right",
					250, y_line2, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text + " Left", 250,
					y_line3, 0);
			cb.endText();

			// start second page
			document.newPage();

			// add text in three paragraphs from top to bottom with various font
			// styles
			Paragraph par = new Paragraph("bold paragraph");
			par.getFont().setStyle(Font.BOLD);
			document.add(par);
			par = new Paragraph("italic paragraph");
			par.getFont().setStyle(Font.ITALIC);
			document.add(par);
			par = new Paragraph("underlined and strike-through paragraph");
			par.getFont().setStyle(Font.UNDERLINE | Font.STRIKETHRU);
			document.add(par);

			// demonstrate some table features
			Table table = new Table(3);
			// 2 pixel wide blue border
			table.setBorderWidth(2);
			table.setBorderColor(new Color(0, 0, 255));
			table.setPadding(5);
			table.setSpacing(5);
			Cell c = new Cell("header");
			c.setHeader(true);
			c.setColspan(3);
			table.addCell(c);
			table.endHeaders();
			c = new Cell("example cell with rowspan 2 and red border");
			c.setRowspan(2);
			c.setBorderColor(new Color(255, 0, 0));
			table.addCell(c);
			table.addCell("1.1");
			table.addCell("2.1");
			table.addCell("1.2");
			table.addCell("2.2");
			c = new Cell("align center");
			c.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c);
			c = new Cell("big cell");
			c.setRowspan(2);
			c.setColspan(2);
			table.addCell(c);
			c = new Cell("align right");
			c.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(c);
			document.add(table);

			// add text at an absolute position
			cb.beginText();
			cb.setFontAndSize(bf_times, 14);
			cb.setTextMatrix(100, 300);
			cb.showText("Text at position 100, 300.");
			cb.endText();

			// rotated text at an absolute position
			PdfTemplate template = cb.createTemplate(300, 300);
			template.beginText();
			template.setFontAndSize(bf_times, 14);
			template.showText("Rotated text at position 400, 200.");
			template.endText();

			float rotate = 90;
			float x = 400;
			float y = 200;
			float angle = (float) (-rotate * (Math.PI / 180));
			float xScale = (float) Math.cos(angle);
			float yScale = (float) Math.cos(angle);
			float xRot = (float) -Math.sin(angle);
			float yRot = (float) Math.sin(angle);

			cb.addTemplate(template, xScale, xRot, yRot, yScale, x, y);

			// we're done!
			document.close();
			System.out.println("Done");

			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler "
							+ "C:/Temp/iTextExample.pdf");
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}

		return false;
	}

}
