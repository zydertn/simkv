package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.model.Country;
import de.abd.mda.model.Model;
import de.abd.mda.persistence.dao.Address;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Person;
import de.abd.mda.persistence.dao.SequenceNumber;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

public class ExcelCorrection_13_08_07_02 {

	/**
	 * @param args
	 */
	private String zeile;
	private ArrayList list = new ArrayList();
	private String[] split = null;
	static final Logger logger = Logger
			.getLogger(ExcelCorrection_13_08_07_02.class);

	public static void main(String[] args) {
		ExcelCorrection_13_08_07_02 c = new ExcelCorrection_13_08_07_02();
		try {
//			c.updateColumn(c.readData("D:/Softwareentwicklung/Excel-Lieferung-Sina/Datenbank_Korrekturen_01_08_2013_STATUS.csv"), "Status");
			c.updateColumn(c.readData("D:/Softwareentwicklung/Excel-Lieferung-Sina/Datenbank_Korrekturen_07_08_2013_02_DEL_SLIP_DATE.csv"), "delSlipDate");

		} catch (IOException e) {
			logger.error("E/A-Fehler");
		}
	}

	public FileReader readData(String path) {
		FileReader file = null;
		try {
			file = new FileReader(path);
		} catch (FileNotFoundException e) {
			logger.error("Datei nicht gefunden");
		}
				
		return file;
	}

	public void updateColumn(FileReader file, String fall) throws IOException {
		BufferedReader data = new BufferedReader(file);
		data.readLine();
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance()
					.getCurrentSession();
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card.setCardNumberFirst(split[0]);
			}
			if (split.length > 1) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[1] == " + split[1]);
				card.setCardNumberSecond(split[1]);
				
				try {
					String select = "select distinct card from CardBean card where card.cardNumberFirst = '" + card.getCardNumberFirst() + "'";
					if (card.getCardNumberSecond() != null && card.getCardNumberSecond().length() > 0 && !card.getCardNumberSecond().equals(" ")) {
						select = select + "	and card.cardNumberSecond = '" + card.getCardNumberSecond() +"'";
					}
					tx = session.beginTransaction();
					List<CardBean> list = session.createQuery(select).list();
					Iterator it = list.iterator();
					
					if (list.size() > 0) {
						if (list.size() > 1) {
							MdaLogger.warn(logger, "Mehr als eine Karte gefunden!");
							continue;
						} else {
							MdaLogger.info(logger, "Nur eine Karte gefunden!");
							existingCard = (CardBean) list.get(0);
						}
					} else {
						MdaLogger.info(logger, "Keine Karte gefunden!");
					}
				} catch (Exception e) {
					MdaLogger.error(logger, e);
				}

			}

			if (split.length > 2) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[2] == " + split[2]);

				Address instAdd = null;
				if (existingCard != null)
					instAdd = existingCard.getInstallAddress();
				else
					instAdd = card.getInstallAddress();
				if (fall.equals("status")) {
					if (existingCard != null)
						existingCard.setStatus(split[2]);
					else
						card.setStatus(split[2]);
				} else if (fall.equals("delSlipDate")) {
					String date = split[2];
					if (date != null && date.length() > 0) {
						Calendar c = new GregorianCalendar();
						c.set(new Integer(date.substring(6, 10)), new Integer(
								date.substring(3, 5)) - 1,
								new Integer(date.substring(0, 2)));
						if (existingCard != null) {
							existingCard.setActivationDate(c.getTime());
							existingCard.setStatus(Model.STATUS_ACTIVE);
						}
						else {
							card.setActivationDate(c.getTime());
							card.setStatus(Model.STATUS_ACTIVE);
						}
					} else if (fall.equals("facNumber")) {
						if (existingCard != null)
							existingCard.setFactoryNumber(split[2]);
						else
							card.setFactoryNumber(split[2]);
					} else if (fall.equals("instAddCity")) {
						instAdd.setCity(split[2]);
					} else if (fall.equals("instAddHNumber")) {
						instAdd.setHousenumber(split[2]);
					} else if (fall.equals("instAddPostcode")) {
						instAdd.setPostcode(split[2]);
					} else if (fall.equals("instAddStreet")) {
						instAdd.setStreet(split[2]);
					} else {
						logger.error("Irgendwas ging schief! Fall = " + fall);
					}

				}
			}
			tx.commit();
		}
	}


}