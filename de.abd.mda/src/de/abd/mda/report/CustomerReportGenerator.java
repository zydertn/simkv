package de.abd.mda.report;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.swing.text.TabStop;

import org.apache.log4j.Logger;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.VerticalPositionMark;
import com.sun.org.apache.bcel.internal.generic.NEW;

import de.abd.mda.controller.CustomerActionController;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.util.DateUtils;

public class CustomerReportGenerator {

	private final static Logger LOGGER = Logger.getLogger(CustomerReportGenerator.class .getName()); 
	BaseFont bf_arial = null;
	Font arialFont;
	Font arialFontBoldLarge;
	
	public CustomerReportGenerator() {
		loadBaseFonts();
	}
	
	public void generateReport(List<Customer> customers, List<Customer> monthly, List<Customer> quarterly, List<Customer> halfyearly, List<Customer> yearly, List<Customer> directDebit) {
		arialFont = new Font(bf_arial, 8);
		arialFont.setColor(Color.BLACK);
		
		arialFontBoldLarge = new Font(bf_arial, 10, Font.BOLD);
		arialFontBoldLarge.setColor(Color.BLACK);
		

		Document document = new Document(PageSize.A4, 60, 25, 40, 40);
		
		Calendar cal = Calendar.getInstance();
		String filename = "customerList_" + DateUtils.getCalendarExportStringWithTime(cal) + ".pdf";

		File dir = new File("C:/Temp/report/");
		dir.mkdirs();
		
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(
					dir + "/" + filename);

			LOGGER.info("Writing file: " + dir + "/" + filename);
			
			PdfWriter writer = PdfWriter.getInstance(document, fos);
			
			document.open();

			if (customers != null) {
				Paragraph p = new Paragraph();
				p.add(new Chunk("Alle Kunden (" + customers.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(customers, document);
			} else {
				Paragraph p = new Paragraph();
				p.add(new Chunk("Kunden mit monatlicher Zahlung (" + monthly.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(monthly, document);
				document.newPage();
				
				p = new Paragraph();
				p.add(new Chunk("Kunden mit Quartalszahlung (" + quarterly.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(quarterly, document);
				document.newPage();
				
				p = new Paragraph();
				p.add(new Chunk("Kunden mit halbjährlicher Zahlung (" + halfyearly.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(halfyearly, document);
				document.newPage();
				
				p = new Paragraph();
				p.add(new Chunk("Kunden mit Jahreszahlung (" + yearly.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(yearly, document);
				document.newPage();
				
				p = new Paragraph();
				p.add(new Chunk("Kunden mit Einzugsermächtigung (" + directDebit.size() + ")", arialFontBoldLarge));
				document.add(p);
				document = writeLines(directDebit, document);
				document.newPage();
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

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			
			if (filename != null && filename.length() > 0) {
				externalContext.responseReset();
				externalContext.setResponseContentType("application/pdf");
				externalContext.setResponseHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");

				OutputStream output = externalContext.getResponseOutputStream();

				output.write(file);

				output.flush();
				output.close();

				facesContext.responseComplete();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Document writeLines(List<Customer> customers, Document document) throws DocumentException {
		// TODO Auto-generated method stub
		for (Customer customer: customers) {
			Paragraph p = new Paragraph();
			p.add(new Chunk(customer.getCustomernumber(), arialFont));
			p.add("           ");
			p.add(new Chunk(customer.getName(), arialFont));
			p.add(Chunk.NEWLINE);
			document.add(p);
		}
		return document;
	}

	private void loadBaseFonts() {
		LOGGER.info("Method: loadBaseFonts");
		try {
			bf_arial = BaseFont.createFont("C:/Windows/Fonts/Arial.TTF",
					"Cp1252", false);
		} catch (DocumentException e) {
			LOGGER.error("DocumentException: " + e);
		} catch (IOException e) {
			LOGGER.error("IOException: " + e);
		}
	}

}
