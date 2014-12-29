package de.abd.mda.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfReader;

import de.abd.mda.controller.BillActionController;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class BillParser {

	private String zeile;
	static final Logger logger = Logger.getLogger(BillParser.class);
	private ArrayList list = new ArrayList();
	private String[] split = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CustomerController cc = new CustomerController();
		BillController bc = new BillController();
		BillActionController bac = new BillActionController();
		BillParser bpt = new BillParser();
		List<DaoObject> customerList = cc.listObjects();
		Session session = HibernateUtil.getSession();
		Transaction transaction = session.beginTransaction();
		for (DaoObject customerDao: customerList) {
			Customer cus = (Customer) customerDao;
			List<Bill> bills = bc.findCustomerBills(session, transaction, Integer.parseInt(cus.getCustomernumber()));
			for (Bill bill: bills) {
				try {
					bpt.parsePdf(bill);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		transaction.commit();
	}
	
	private void parsePdf(Bill bill) throws IOException {
        PdfReader reader = new PdfReader(bill.getFile());
        // we can inspect the syntax of the imported page
        int num = reader.getNumberOfPages();
        int pos = 0;
        Calendar rechDat = Calendar.getInstance();
        String nettobetrag = "";
        String vat = "";
        String endbetrag = "";
        for (int i = 1; i < num; i++) {
            byte[] streamBytes = reader.getPageContent(i);
            PRTokeniser tokenizer = new PRTokeniser(streamBytes);
            while (tokenizer.nextToken()) {
            	if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
                	String token = tokenizer.getStringValue();
//                	System.out.println(token);
                	pos++;
                	if (pos > 20 && pos < 35) {
                		if (checkForDate(token)) {
                    		System.out.println(pos + ": " + token);
                    		int date = Integer.parseInt(token.substring(0, 2));
                    		int month = Integer.parseInt(token.substring(3, 5));
                    		int year = Integer.parseInt(token.substring(6, 10));
                    		rechDat.set(year, month, date);
                		}
                	}
                	if (token.equals("Netto Summe") ||  token.equals("Netto")) {
                		boolean nonString = true;
                		while (nonString) {
                    		tokenizer.nextToken();
                    		if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
                    			nonString = false;
                    		}
                		}
                		nettobetrag = tokenizer.getStringValue();
                		nettobetrag = nettobetrag.substring(0, nettobetrag.length()-2);
                		nettobetrag = nettobetrag.replace(",", ".");
                	}
                	if (token.equals("19% MWST.") ||  token.equals("19% Sales Tax") || token.equals("19% MwSt.") || token.equals("19%")) {
                		boolean nonString = true;
                		while (nonString) {
                    		tokenizer.nextToken();
                    		if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
                    			nonString = false;
                    		}
                		}
                		vat = tokenizer.getStringValue();
                		vat = vat.substring(0, vat.length()-2);
                		vat = vat.replace(",", ".");
                	}

                	if (token.equals("Endbetrag") ||  token.equals("Total Amount")) {
                		boolean nonString = true;
                		while (nonString) {
                    		tokenizer.nextToken();
                    		if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
                    			nonString = false;
                    		}
                		}
                		endbetrag = tokenizer.getStringValue();
                		endbetrag = endbetrag.substring(0, endbetrag.length()-2);
                		endbetrag = endbetrag.replace(",", ".");
                	}
//                	System.out.println(token + ", " + pos);
                }
            }
        }
        bill.setCalcDate(rechDat);
        bill.setBruttoPrice(new BigDecimal(endbetrag));
        bill.setNettoPrice(new BigDecimal(nettobetrag));
        bill.setVat(new BigDecimal(vat));
        bill.setPaymentStatus(true);
        Calendar paymentDate = Calendar.getInstance();
        paymentDate.set(rechDat.get(Calendar.YEAR), rechDat.get(Calendar.MONTH), rechDat.get(Calendar.DATE));
        paymentDate.add(Calendar.DATE, 10);
        bill.setPaymentDate(paymentDate);
        System.out.println(bill.getCustomerNumber() + " ; " + DateUtils.getCalendarExportString(rechDat) + " ; " + nettobetrag + " ; " + vat + " ; " + endbetrag);
        reader.close();
    }

	private boolean checkForDate(String token) {
		// TODO Auto-generated method stub
		String regex = "(\\d+)\\.(\\d+)\\.(\\d+)";
		if (token.matches(regex)) {
			return true;
		}
		return false;
	}


}
