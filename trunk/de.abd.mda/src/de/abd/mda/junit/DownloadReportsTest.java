package de.abd.mda.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Bill;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.BillController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.IReportGenerator;
import de.abd.mda.report.ReportCalculator;
import de.abd.mda.report.ReportGenerator_portrait;
import de.abd.mda.report.ReportGenerator_landscape;
import de.abd.mda.report.ReportCalculator.DateComparator;
import de.abd.mda.util.HibernateUtil;

public class DownloadReportsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		DownloadReportsTest rg = new DownloadReportsTest();
		rg.testCustomerQuery(20243);
//		rg.testMonthQuery(2014, 1);
		
	}

	private void testMonthQuery(int year, int month) {
		BillController bc = new BillController();
		List<Bill> bills = bc.findMonthBills(year, month);
		
		if (bills != null) {

			System.out.println("Gefundene Rechnungen: " + bills.size());

			String testFilename = null;
			if (bills.size() > 3) {
				testFilename = bills.get(3).getFilename(); 
			}
			
			for (Bill bill: bills) {
				File file = new File(bill.getFilename());
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
//					ZipOutputStream zos = new ZipOutputStream(fos);
//					ZipEntry ze = new ZipEntry(bill.getFilename());
//					ze.
//					zos.putNextEntry(ze);
					fos.write(bill.getFile());
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
			File zipFile = new File("C:/Temp/report/Test.zip");
				FileOutputStream zfos;
				try {
					zfos = new FileOutputStream(zipFile);
					ZipOutputStream zos = new ZipOutputStream(zfos);
					byte[] buffer = new byte[1024];
					for (Bill bill: bills) {
						ZipEntry ze = new ZipEntry(bill.getFilename());
						zos.putNextEntry(ze);

						File f = new File(bill.getFilename());
						FileInputStream fis = new FileInputStream(f);
						int length;
						while ((length = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, length);
						}
						
//						zos.write(bill.getFile());
						zos.closeEntry();
						fis.close();
					}
					zos.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
//			try {
//				Runtime.getRuntime().exec(
//				"rundll32 url.dll,FileProtocolHandler " + testFilename);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
	}

	
	private void testCustomerQuery(int customerNumber) {
		BillController bc = new BillController();
		Session session = HibernateUtil.getSession();
		Transaction transaction = session.getTransaction();
		List<Bill> bills = bc.findCustomerBills(session, transaction, customerNumber);
		
		if (bills != null) {

			System.out.println("Gefundene Rechnungen: " + bills.size());

			String testFilename = null;
			if (bills.size() > 3) {
				testFilename = bills.get(3).getFilename(); 
			}
			
			for (Bill bill: bills) {
				File file = new File(bill.getFilename());
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
					fos.write(bill.getFile());
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			File zipFile = new File("C:/Temp/report/Test.zip");
			FileOutputStream zfos;
			try {
				zfos = new FileOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(zfos);
				byte[] buffer = new byte[1024];
				for (Bill bill: bills) {
					ZipEntry ze = new ZipEntry(bill.getFilename());
					zos.putNextEntry(ze);

					File f = new File(bill.getFilename());
					FileInputStream fis = new FileInputStream(f);
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					
//					zos.write(bill.getFile());
					zos.closeEntry();
					fis.close();
				}
				zos.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
//			try {
//				Runtime.getRuntime().exec(
//				"rundll32 url.dll,FileProtocolHandler " + testFilename);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
	}
}
