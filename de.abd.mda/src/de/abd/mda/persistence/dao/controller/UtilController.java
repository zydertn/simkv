package de.abd.mda.persistence.dao.controller;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.lowagie.text.Image;

import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Util;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.FacesUtil;


public class UtilController extends DaoController implements IDaoController {
	
	private final static Logger LOGGER = Logger.getLogger(UtilController.class .getName()); 

	public UtilController() {
		LOGGER.info("Instantiate CountryController");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DaoObject> listObjects() {
		return null;
	}
	
	public Util getUtil() {
		LOGGER.info("Method listObjects");
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Util util = null;
		try {
			tx = session.beginTransaction();
			String select = "select util from Util as util";

			LOGGER.info("Select = " + select);
			List<DaoObject> daoList = session.createQuery(select).list();
			if (daoList != null) {
				DaoObject dao = daoList.get(0);
				util = (Util) dao;
			}

			tx.commit();
		} catch (RuntimeException e) {
			LOGGER.error("RuntimeException: " + e);
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					LOGGER.error("HibernateException: Error rolling back transaction; " + e1);
				}
				// throw again the first exception
				throw e;
			}

		}
		
		return util;
	}

	public boolean increaseBillNumber() {
		LOGGER.info("Method: increaseBillNumber in utils");
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		
		List<Util> utils = session.createQuery("from Util as util").list();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);
			int mbn = util.getMaxBillNumber();
			mbn++;
			util.setMaxBillNumber(mbn);
			LOGGER.info("New maxBillNumber = " + mbn);
			return true;
		}
		
		return false;
	}


	public boolean saveImageToDB(String path, int uploadCase) {
		LOGGER.info("Method: saveLogoToDB in utils");
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		
		List<Util> utils = session.createQuery("from Util as util").list();
		if (utils != null && utils.size() > 0) {
			Util util = utils.get(0);

			//save image into database
	    	File file = new File(path);
	        byte[] bFile = new byte[(int) file.length()];
	        
	        try {
		     FileInputStream fileInputStream = new FileInputStream(file);
		     //convert file into array of bytes
		     fileInputStream.read(bFile);
		     fileInputStream.close();
	        } catch (Exception e) {
		     e.printStackTrace();
	        }
	        
	        switch (uploadCase) {
	        case 0: util.setHeader(bFile);
	        	break;
	        case 1: util.setAddress(bFile);
	        	break;
	        case 2: util.setFooter(bFile);
        		break;
	        }

			transaction.commit();
			return true;
		}
		transaction.rollback();
		return false;
	}

	


}