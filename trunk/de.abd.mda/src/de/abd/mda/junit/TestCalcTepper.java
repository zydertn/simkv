package de.abd.mda.junit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CardController;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.report.ReportCalculator;

public class TestCalcTepper {

	private List<String> cardsInCSV;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestCalcTepper tct = new TestCalcTepper();
		tct.cardsInCSV = new ArrayList<String>();
		tct.readData();
		
		Session session = SessionFactoryUtil.getInstance().openSession();
		Transaction tx = null;
		tx = session.beginTransaction();
//		String select = "select distinct card from CardBean card where card.customer = '2597' and ((card.status = 'aktiv' and card.activationDate < '2015-01-01 00:00:00') or (card.status = 'Inaktiv' and (card.deactivationDate >= '2014-07-01 00:00:00' or card.deactivationDate < '2015-01-01 00:00:00')))";

		CustomerController cc = new CustomerController();
		Customer customer = cc.findCustomer("20091");
		
		Calendar calcMonth = Calendar.getInstance();
		calcMonth.set(Calendar.MILLISECOND, 0);
		calcMonth.set(Calendar.SECOND, 0);
		calcMonth.set(Calendar.MINUTE, 0);
		calcMonth.set(Calendar.HOUR, 0);
		calcMonth.set(Calendar.DATE, 1);
		calcMonth.set(Calendar.MONTH, Calendar.JANUARY);
		calcMonth.set(Calendar.YEAR, 2015);
		
		
		ReportCalculator rc = new ReportCalculator();
		List<DaoObject> objects = rc.searchCards(customer, calcMonth, tx, session, false, 1);
		
//		List<DaoObject> objects = null;
//		objects = (List<DaoObject>) session.createQuery(select).list();
		for (DaoObject d: objects) {
			CardBean card = (CardBean) d;
			if (tct.cardsInCSV.contains(card.getCardnumberString())) {
				System.out.println("Karte " + card.getCardnumberString() + " enthalten");
			} else {
				System.out.println("**************** ERROR: Karte " + card.getCardnumberString() + " nicht enthalten ************");
			}
		}
	}
	
	public void readData() {
		List<CardBean> cusCards = null;
		try {
				String hostname = java.net.InetAddress.getLocalHost().getHostName();
				System.out.println("Hostname" + hostname);
				
				String path = "D:/Softwareentwicklung/Reports/Tepper/";
				
				FileReader file = null;

//				logger.info("************ GSM **************");
				file = new FileReader(path + "GSM.csv");
				readDataFromFile(file, "20091");
//				CardController cc = new CardController();
//				cusCards = cc.searchCustomerActiveCards(437);
//				System.out.println("Anzahl Karten : " + cusCards);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void readDataFromFile(FileReader file, String customerNumber) throws IOException {
		BufferedReader data = new BufferedReader(file);
		
	
		String zeile = "";
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			String[] split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
//				logger.debug("---------------------------------------------------------------");
//				logger.debug("split[0] == " + split[0]);
				card = extractCardNums(card, split[1]);
				cardsInCSV.add(card.getCardnumberString());
			}
		}
	}

	private CardBean extractCardNums(CardBean card, String string) {
		string = string.replaceAll("\\s", "");
		card.setCardNumberFirst(string.substring(0, string.indexOf("-")));
		card.setCardNumberSecond(string.substring(string.indexOf("-")+1));
		return card;
	}

}
