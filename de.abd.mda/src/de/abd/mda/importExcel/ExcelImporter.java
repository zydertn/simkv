package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

import jxl.demo.CSV;

public class ExcelImporter {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
			
		public static void main(String[] args) {
			ExcelImporter c = new ExcelImporter();
			c.readData();
		}
		
		public void readData() {
			try {
					FileReader file = new FileReader("D:/Softwareentwicklung/Bestand_Simkarten/Bestand_SIM_Karten_Teil_1_24.01.2012.csv");
					FileReader file2 = new FileReader("D:/Softwareentwicklung/Bestand_Simkarten/Bestand_SIM_Karten_Teil_2_24.01.2012.csv");
					FileReader file3 = new FileReader("D:/Softwareentwicklung/Bestand_Simkarten/Bestand_SIM_Karten_Teil_3_24.01.2012.csv");
					readDataFromFile(file);
					readDataFromFile(file2);
					readDataFromFile(file3);
			} catch (FileNotFoundException e) {
				System.out.println("Datei nicht gefunden");
			} catch (IOException e) {
				System.out.println("E/A-Fehler");
			}
			
			System.out.println("Anzahl Sätze: " + list.size());
	}

		
	public void readDataFromFile(FileReader file) throws IOException {
		BufferedReader data = new BufferedReader(file);
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			split = zeile.split(";");
			CardBean card = new CardBean();
			if (split.length > 0) {
				System.out.println("split[0] == " + split[0]);
				card.setCardNumberFirst(split[0]);
			}
			if (split.length > 1) {
				System.out.println("split[1] == " + split[1]);
				card.setCardNumberSecond(split[1]);
			}
			if (split.length > 2) {
				System.out.println("split[2] == " + split[2]);
				card.setPhoneNrFirst(split[2]);
			}
			if (split.length > 3) {
				System.out.println("split[3] == " + split[3]);
				card.setPhoneNrSecond(split[3]);
			}

			CardController cardController = new CardController();

			String select = "select sequenceNumber from SequenceNumber sequenceNumber";

			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			tx = session.beginTransaction();
			List<SequenceNumber> list = session.createQuery(select).list();
			Iterator it = list.iterator();

			int currentSequenceNumber = -1;
			SequenceNumber sn = null;
			if (list.size() > 0) {
				sn = (SequenceNumber) list.get(0);
				currentSequenceNumber = sn.getSequenceNumber();
				currentSequenceNumber++;
				sn.setSequenceNumber(currentSequenceNumber);
				tx.commit();
			} else {
				currentSequenceNumber = 0;
				sn = new SequenceNumber();
				sn.setSequenceNumber(currentSequenceNumber);
				cardController.createObject(sn);
			}
			
			card.setSequenceNumber(currentSequenceNumber);
			cardController.createObject(card);
			System.out.println(i);
			i++;
		}
	}

}