package de.abd.mda.importExcel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class ExcelImporterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExcelImporterTest e = new ExcelImporterTest();
		e.readData();
	}

	public void readData() {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		tx = session.beginTransaction();
		session.createSQLQuery("SET foreign_key_checks=0").executeUpdate();
		session.createSQLQuery("truncate table mda.card").executeUpdate();
		session.createSQLQuery("truncate table mda.address").executeUpdate();
		session.createSQLQuery("truncate table mda.customer").executeUpdate();
		session.createSQLQuery("truncate table mda.invoiceconfiguration").executeUpdate();
		session.createSQLQuery("truncate table mda.person").executeUpdate();
		session.createSQLQuery("truncate table mda.sequence_number").executeUpdate();
		tx.commit();
		
		
		try {			
			FileReader file = new FileReader(
					"D:/Softwareentwicklung/Bestand_Simkarten/SIM_Karten_Test.csv");
			ExcelImporter eI = new ExcelImporter();
			eI.readDataFromFile(file);
		} catch (FileNotFoundException e) {
			System.out.println("Datei nicht gefunden");
		} catch (IOException e) {
			System.out.println("E/A-Fehler");
		}

	}

}
