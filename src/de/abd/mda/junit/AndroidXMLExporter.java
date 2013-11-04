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
import org.xml.sax.SAXException;

import de.abd.mda.controller.SearchCardController;
import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.controller.CustomerController;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.DateUtils;
import de.abd.mda.util.MdaLogger;

public class AndroidXMLExporter {

	private String zeile;
	static final Logger logger = Logger.getLogger(AndroidXMLExporter.class);
	private ArrayList list = new ArrayList();
	Document document = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AndroidXMLExporter axe = new AndroidXMLExporter();
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			File f = new File("D:/Softwareentwicklung/Android/Deutsch-Spanisch.xml");
			Document doc = null;
			if (f.exists()) {
				doc = docBuilder.parse("D:/Softwareentwicklung/Android/Deutsch-Spanisch.xml");
				axe.setDocument(doc);
			}
			
			if (doc == null) {
				doc = docBuilder.newDocument();
				Element root = doc.createElement("Root");
				doc.appendChild(root);

				Element l1 = doc.createElement("Level1");
				root.appendChild(l1);

				Element l2 = doc.createElement("Level2");
				root.appendChild(l2);

				Element l3 = doc.createElement("Level3");
				root.appendChild(l3);

				Element l4 = doc.createElement("Level4");
				root.appendChild(l4);

				Element l5 = doc.createElement("Level5");
				root.appendChild(l5);
			}
				
			axe.setDocument(doc);
			axe.readData();
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("D:\\Softwareentwicklung\\Android\\Deutsch-Spanisch.xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document export(String german, String translation, String type, ArrayList<String> presente, ArrayList<String> futuro, ArrayList<String> preteritoIndefinido) {
		Document doc = getDocument();
		
		Node l3 = doc.getElementsByTagName("Level3").item(0);
		
		Element germanEl = doc.createElement("DE");
		germanEl.setAttribute("Wort", german);
		germanEl.setAttribute("Typ", type);
		germanEl.setAttribute("Übersetzung", translation);
		
		if (type != null && type.length() > 0) {
			Element presenteEl = doc.createElement("PresenteDeIndicativo");
			presenteEl.setAttribute("Zeit", "Presente De Indicativo");
			for (int i=0; i<6; i++) {
				presenteEl.appendChild(doc.createElement(presente.get(i)));
			}
			germanEl.appendChild(presenteEl);
			
			Element futuroEl = doc.createElement("FuturoImperfecto");
			futuroEl.setAttribute("Zeit", "Futuro Imperfecto");
			for (int i=0; i<6; i++) {
				futuroEl.appendChild(doc.createElement(futuro.get(i)));
			}
			germanEl.appendChild(futuroEl);
			
			Element preteritoEl = doc.createElement("PretéritoIndefinido");
			preteritoEl.setAttribute("Zeit", "Pretérito indefinido");
			for (int i=0; i<6; i++) {
				preteritoEl.appendChild(doc.createElement(preteritoIndefinido.get(i)));
			}
			germanEl.appendChild(preteritoEl);
		}
		
		l3.appendChild(germanEl);
		
		return doc;
	}
	
	public void readData() {
		try {
				FileReader file = new FileReader("D:/Softwareentwicklung/Android/Deutsch-Spanisch.csv");
				readDataFromFile(file);
		} catch (FileNotFoundException e) {
			System.out.println("Datei nicht gefunden");
		} catch (IOException e) {
			System.out.println("E/A-Fehler");
		}
		
	}
	
	public void readDataFromFile(FileReader file) throws IOException {
		ArrayList<String> allowedTypes = new ArrayList<String>();
		allowedTypes.add("vr");
		allowedTypes.add("vu");
		
		BufferedReader data = new BufferedReader(file);
		int i = 1;
		// skip header line
		data.readLine();
		
		while ((zeile = data.readLine()) != null) {
			i++;
			MdaLogger.debug(logger, zeile);
			String[] split = zeile.split(";");

			int column = 0;

			String german = setValue(split, column++, i);
			if (german == null) break;

			String translation = setValue(split, column++, i);
			if (translation == null) break;
			
			String type = setValue(split, column++, i);
			if (type != null) {
				if (!allowedTypes.contains(type)) {
					type = null;
					System.out.println("Zeile " + i + " enthält unbekannten Typ");
					break;
				}
			}

			ArrayList<String> presente = new ArrayList<String>();
			for (int f=0; f<6; f++ ) {
				String value = setValue(split, column++, i);
				presente.add(""+value);
			}

			ArrayList<String> futuro = new ArrayList<String>();
			for (int f=0; f<6; f++ ) {
				String value = setValue(split, column++, i);
				futuro.add(""+value);
			}

			ArrayList<String> preteritoIndefinido = new ArrayList<String>();
			for (int f=0; f<6; f++ ) {
				String value = setValue(split, column++, i);
				preteritoIndefinido.add(""+value);
			}
			
			export(german, translation, type, presente, futuro, preteritoIndefinido);
			
		}
			
	}

	private String setValue(String[] split, int column, int zeile) {
		String string = null;
		if (split.length > column && split[column] != null && split[column].length() > 0) {
			string = split[column];
		}		
		System.out.println(zeile + ": " + string);
		return string;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
}
