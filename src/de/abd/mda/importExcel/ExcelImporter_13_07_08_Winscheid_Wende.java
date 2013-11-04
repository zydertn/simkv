package de.abd.mda.importExcel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
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
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.MdaLogger;

import jxl.demo.CSV;

public class ExcelImporter_13_07_08_Winscheid_Wende {

	/**
	 * @param args
	 */
		private String zeile;
		private ArrayList list = new ArrayList();
		private String[] split = null;
		static final Logger logger = Logger.getLogger(ExcelImporter_13_07_08_Winscheid_Wende.class);
			
		public static void main(String[] args) {
			ExcelImporter_13_07_08_Winscheid_Wende c = new ExcelImporter_13_07_08_Winscheid_Wende();
			c.readData();
		}
		
		public void readData() {
			try {
					FileReader file = new FileReader("D:/Softwareentwicklung/Excel-Lieferung-Sina/2013_07_08_Windscheid_Wendel.csv");
					readDataFromFile(file);
			} catch (FileNotFoundException e) {
				logger.error("Datei nicht gefunden");
			} catch (IOException e) {
				logger.error("E/A-Fehler");
			}
			
			logger.info("Anzahl Sätze: " + list.size());
	}

		
	public void readDataFromFile(FileReader file) throws IOException {
		BufferedReader data = new BufferedReader(file);
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			CardBean card = new CardBean();
			CardBean existingCard = null;
			if (split.length > 0) {
				logger.debug("---------------------------------------------------------------");
				logger.debug("split[0] == " + split[0]);
				card.setCardNumberFirst(split[0]);
			}
			if (split[0].equals("67959200")) {
				System.out.println("bla");
			}
			
			if (split.length > 1) {
				logger.debug("split[1] == " + split[1]);
				card.setCardNumberSecond(split[1]);
				CardController controller = new CardController();
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
				logger.debug("split[2] == " + split[2]);
//				Country country = new Country("Österreich", "AT", "+43");
				Country country = new Country("Deutschland", "DE", "+49");
				if (split[2].length() > 0) {
					if (existingCard != null) {
						existingCard.setPhoneNrFirst(split[2]);
						existingCard.setSupplier(Model.SUPPLIER_TELEKOM);
						existingCard.setCountryDates(country);
					} else {
						card.setPhoneNrFirst(split[2]);
						card.setSupplier(Model.SUPPLIER_TELEKOM);
						card.setCountryDates(country);
					}
				}
			}
			if (split.length > 3) {
				logger.debug("split[3] == " + split[3]);
				if (split[3].length() > 0) {
					if (existingCard != null) {
						existingCard.setPhoneNrSecond(split[3]);
					} else {
						card.setPhoneNrSecond(split[3]);
					}
				}
			}
			Address instAdd = null;
			if (split.length > 4) {
				logger.debug("split[4] == " + split[4]);
				if (split[4].length() > 0) {
					if (existingCard != null) {
						if (existingCard.getInstallAddress() != null) {
							existingCard.getInstallAddress().setPostcode(split[4]);
						} else {
							instAdd = new Address();
							instAdd.setPostcode(split[4]);
							existingCard.setInstallAddress(instAdd);
						}
					} else {
						instAdd = new Address();
						instAdd.setPostcode(split[4]);
						card.setInstallAddress(instAdd);
					}
				}
			}
			if (split.length > 5) {
				logger.debug("split[5] == " + split[5]);
				if (split[5].length() > 0) {
					if (existingCard != null) {
						if (existingCard.getInstallAddress() != null) {
							existingCard.getInstallAddress().setCity(split[5]);
						} else {
							Address a = new Address();
							a.setCity(split[5]);
							existingCard.setInstallAddress(a);
						}
					} else {
						if (instAdd == null) {
							instAdd = new Address();
						}
						instAdd.setCity(split[5]);
						card.setInstallAddress(instAdd);
					}
				}
			}
			if (split.length > 6) {
				logger.debug("split[6] == " + split[6]);
				if (split[6].length() > 0) {
					if (existingCard != null) {
						existingCard.getInstallAddress().setStreet(split[6]);
					} else {
						if (instAdd == null) {
							instAdd = new Address();
						}
						instAdd.setStreet(split[6]);
						card.setInstallAddress(instAdd);
					}
				}
			}
			if (split.length > 7) {
				logger.debug("split[7] == " + split[7]);
				if (split[7].length() > 0) {
					if (existingCard != null) {
						existingCard.getInstallAddress().setHousenumber(split[7]);
					} else {
						if (instAdd == null) {
							instAdd = new Address();
						}
						instAdd.setHousenumber(split[7]);
						card.setInstallAddress(instAdd);
					}
				}
			}
			if (split.length > 8) {
				logger.debug("split[8] == " + split[8]);
				if (split[8].length() > 0) {
					if (existingCard != null) {
						existingCard.setFactoryNumber(split[8]);
					} else {
						card.setFactoryNumber(split[8]);
					}
				}
			}
			if (split.length > 9) {
				logger.debug("split[9] == " + split[9]);
				if (split[9].length() > 0) {
					if (existingCard != null) {
						existingCard.setOrderNumber(split[9]);
					} else {
						card.setOrderNumber(split[9]);
					}
				}
			}
			if (split.length > 10) {
				// HIER GAB ES EINEN FEHLER ==> WIRD ZUR KORREKTUR WIEDER AUF "" GESETZT
				logger.debug("split[10] == " + split[10]);
				String customerOrderNumber = "";
					if (existingCard != null) {
						existingCard.setCustomerOrderNumber("");
					} else {
						card.setCustomerOrderNumber("");
					}
			}
			if (split.length > 10) {
				logger.debug("split[10] == " + split[10]);
				String date = split[10];
				if (date != null && date.length() > 0) {
					Calendar c = new GregorianCalendar();
					c.set(new Integer(date.substring(6, 10)), new Integer(date.substring(3, 5)) - 1, new Integer(date.substring(0, 2)));
					if (existingCard != null) {
						existingCard.setActivationDate(c.getTime());
						existingCard.setStatus(Model.STATUS_ACTIVE);
					} else {
						card.setActivationDate(c.getTime());
						card.setStatus(Model.STATUS_ACTIVE);
					}
				}
			}
			Person contactPerson = null;
			if (split.length > 11) {
				logger.debug("split[11] == " + split[11]);
				if (split[11].length() > 0) {
					if (existingCard != null) {
						if (existingCard.getContactPerson() != null) {
							existingCard.getContactPerson().setGender(split[11]);
						} else {
							contactPerson = new Person();
							contactPerson.setGender(split[11]);
							existingCard.setContactPerson(contactPerson);
						}
					} else {
						contactPerson = new Person();
						contactPerson.setGender(split[11]);
						card.setContactPerson(contactPerson);
					}
				}
			}
			if (split.length > 12) {
				logger.debug("split[12] == " + split[12]);
				if (split[12].length() > 0) {
					if (existingCard != null) {
						existingCard.getContactPerson().setFirstname(split[12]);
					} else {
						if (contactPerson == null) {
							contactPerson = new Person();
						}
						contactPerson.setFirstname(split[12]);
						card.setContactPerson(contactPerson);
					}
				}
			}
			if (split.length > 13) {
				logger.debug("split[13] == " + split[13]);
				if (split[13].length() > 0) {
					if (existingCard != null) {
						existingCard.getContactPerson().setName(split[13]);
					} else {
						if (contactPerson == null) {
							contactPerson = new Person();
						}
						contactPerson.setName(split[13]);
						card.setContactPerson(contactPerson);
					}
				}
			}
			if (split.length > 14) {
				logger.debug("split[14] == " + split[14]);
				if (split[14].length() > 0) {
					if (existingCard != null) {
						existingCard.getContactPerson().setPhoneNrFirst(split[14]);
					} else {
						if (contactPerson == null) {
							contactPerson = new Person();
						}
						contactPerson.setPhoneNrFirst(split[14]);					
						card.setContactPerson(contactPerson);
					}
				}
			}
			if (split.length > 15) {
				logger.debug("split[15] == " + split[15]);
				// HIER GAB ES EINEN FEHLER ==> phoneNumber wird wieder auf leer gesetzt
				if (true) {
					if (existingCard != null) {
						existingCard.getContactPerson().setPhoneNrSecond(split[15]);
					} else {
						if (contactPerson == null) {
							contactPerson = new Person();
						}
						contactPerson.setPhoneNrSecond(split[15]);
						card.setContactPerson(contactPerson);
					}
				}
			}
			
			if (split.length > 16) {
				logger.debug("split[16] == " + split[16]);
				if (split[16].length() > 0) {
					if (existingCard != null) {
						if (existingCard.getCustomer() != null) {
							if (existingCard.getCustomer().getCustomernumber().equals(split[16])) {
								MdaLogger.info(logger, "001 Kunde bereits bei Karte angelegt! Tue nichts!");
							} else {
								MdaLogger.warn(logger, "001 Customer Number UNGLEICH!!!: split[16] == " + split[16] + ", existingCard.Customer.Customernumber == " + existingCard.getCustomer().getCustomernumber());
								List<DaoObject> cusList = this.searchCustomer(split[16], null);
								if (cusList.size() > 0) {
									Customer c = (Customer) cusList.get(0);
									existingCard.setCustomer(c);
									MdaLogger.info(logger, "Kunde mit Kundennummer " + split[16] + " auf Karte " + existingCard.getCardNumberFirst() + ", " + existingCard.getCardNumberSecond() + " gesetzt.");
								} else {
									// Dieser Fall kann eigentlich nicht eintreten
									Customer c = new Customer();
									c.setCustomernumber(split[16]);
									existingCard.setCustomer(c);
									MdaLogger.info(logger, "Es existiert kein Kunde mit Kundennummer " + split[16] + " in der DB. Der Kunde wird angelegt und auf der Karte " + existingCard.getCardNumberFirst() + ", " + existingCard.getCardNumberSecond() + " gesetzt.");
								}
							}
						} else {
							List<DaoObject> cusList = this.searchCustomer(split[16], null);
							if (cusList.size() > 0) {
								Customer c = (Customer) cusList.get(0);
								existingCard.setCustomer(c);
								MdaLogger.info(logger, "002 Für die existierende Karte war noch kein Kunde vergeben! Kunde existiert in DB und wird vergeben!");
							} else {
								Customer c = new Customer();
								c.setCustomernumber(split[16]);
								existingCard.setCustomer(c);
								MdaLogger.warn(logger, "001 Für die existierende Karte war noch kein Kunde vergeben! Kunde existiert nicht in DB! Neuer Kunde wird angelegt.");
							}
						}
					} else {
						List<DaoObject> cusList = this.searchCustomer(split[16], null);
						if (cusList.size() > 0) {
							Customer c = (Customer) cusList.get(0);
							card.setCustomer(c);
							MdaLogger.info(logger, "003 Karte ist neu in DB. Kunde existiert in DB und wird vergeben!");
						} else {
							Customer c = new Customer();
							c.setId(new Integer(split[16]));
							card.setCustomer(c);
							MdaLogger.warn(logger, "003 Es handelt sich um eine neue Karte und einen neuen Kunde. Kunde wird vergeben!");
						}
					}
				}
			}
			
			
			
			CardController cardController = new CardController();

			String select = "select sequenceNumber from SequenceNumber sequenceNumber";
			try {
				tx.commit();				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			if (existingCard == null) {
				tx = null;
				session = SessionFactoryUtil.getInstance().getCurrentSession();
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
					String message = cardController.createObject(sn);
					MdaLogger.info(logger, message);
				}
				
				card.setSequenceNumber(currentSequenceNumber);
				String message = cardController.createObject(card);
				MdaLogger.info(logger, message);				
				MdaLogger.info(logger, "NEW CARD!!!");
			}

			i++;
		}
	}
	
	private List<DaoObject> searchCustomer (String customerNumber, String customerName) {
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			List<DaoObject> customers = null;
			tx = session.beginTransaction();
			String whereClause = "";
			if (customerNumber != null && customerNumber.length() > 0) {
				whereClause = " where customer.customernumber = '" + customerNumber + "'";
				if (customerName != null && customerName.length() > 0) {
					whereClause += " && customer.name = '" + customerName + "'";
				}
			} else if (customerName != null && customerName.length() > 0) {
				whereClause += " where customer.name = '" + customerName + "'";
			}
			
			customers = session.createQuery("from Customer as customer" + whereClause).list();

			return customers;
	}

}