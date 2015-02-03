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

	private final static Logger LOGGER = Logger.getLogger(ReminderGenerator.class .getName()); 
	BaseFont bf_arial = null;
	
	public CustomerReportGenerator() {
		loadBaseFonts();
	}
	
	public void generateReport(List<Customer> customers) {
		Font arialFont = new Font(bf_arial);
		arialFont.setColor(Color.BLACK);
		arialFont.setSize(8);

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
			
			for (Customer customer: customers) {
				Paragraph p = new Paragraph();
				p.add(new Chunk(customer.getCustomernumber()));
				p.add("           ");
				p.add(new Chunk(customer.getName()));
				p.add(Chunk.NEWLINE);
				document.add(p);
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
