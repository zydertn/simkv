package de.abd.mda.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import de.abd.mda.controller.SearchCardController;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.DateUtils;
import de.abd.mda.util.MdaLogger;

public class CustomersAndCardToXMLExporter {

	private String zeile;
	static final Logger logger = Logger.getLogger(CustomersAndCardToXMLExporter.class);
	private ArrayList list = new ArrayList();
	private String[] split = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CustomerController cc = new CustomerController();
		List<DaoObject> customerList = cc.listObjects();
		List<String> customerNumbers = new ArrayList<String>();
		SearchCardController scc = new SearchCardController();
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement("Root");
			doc.appendChild(root);
				
			
			CustomersAndCardToXMLExporter exporter = new CustomersAndCardToXMLExporter();
			
			if (customerList != null && customerList.size() > 0) {
				System.out.println(customerList.size() + " Kunden gefunden");
				
				if (customerList.size() > 1) {
					Iterator it = customerList.iterator();
					while (it.hasNext()) {
						Customer cus = (Customer) it.next();
						String cNumber = cus.getCustomernumber();
						List<CardBean> cards = scc.performSearch(new Integer(cus.getId()));
						doc = exporter.export(doc, root, cus, cards);
					}
				}
			} else {
				System.out.println("Kein Customer gefunden");
			}	
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			String dfs = "yyyy-MM-dd_HH-mm-ss";
			StreamResult result = new StreamResult(new File("C:\\temp\\Export-"+ DateUtils.now(dfs) +".xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
//		sc.readData(customerNumbers);
	}
	
	public Document export(Document doc, Element root, Customer cus, List<CardBean> cards) {
		Element customer = doc.createElement("Kunde");
		root.appendChild(customer);
		
		Attr cusNum = doc.createAttribute("Kundennummer");
		cusNum.setValue(cus.getCustomernumber());
		customer.setAttributeNode(cusNum);
		
		Element cusName = doc.createElement("Name");
		cusName.appendChild(doc.createTextNode(cus.getName()));
		customer.appendChild(cusName);
		
		
		Iterator<CardBean> it = cards.iterator();
		while (it.hasNext()) {
			CardBean card = it.next();

			Element cusCard = doc.createElement("Karte");
			customer.appendChild(cusCard);

			Attr cardNum = doc.createAttribute("Kartennummer");
			cardNum.setValue(card.getCardNumber());
			cusCard.setAttributeNode(cardNum);
			
			Element telFirst = doc.createElement("Telefonnummer");
			telFirst.appendChild(doc.createTextNode(card.getPhoneString()));
			cusCard.appendChild(telFirst);
		}
		
		return doc;
	}
	
	public void readDataFromFile(FileReader file, List<String> cusNumbers) throws IOException {
		BufferedReader data = new BufferedReader(file);
		int i = 0;
		while ((zeile = data.readLine()) != null) {
			MdaLogger.debug(logger, zeile);
			Transaction tx = null;
			Session session = SessionFactoryUtil.getInstance().getCurrentSession();
			split = zeile.split(";");
			if (split.length > 15) {
				String customerNumber = split[15];
				if (customerNumber.length() > 0) {
					if (cusNumbers.contains(split[15])) {
						MdaLogger.info(logger, split[15] + " enthalten!");
					} else {
						MdaLogger.error(logger, split[15] + " NICHT enthalten!");
					}
				} else {
					MdaLogger.warn(logger, "split[15].length == 0");
				}
			} else {
				MdaLogger.warn(logger, "Keine Kundennummer enthalten!");
			}
		}
	}

}
