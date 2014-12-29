package de.abd.mda.util;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class HibernateUtil {

	public static Session getSession() {
		return SessionFactoryUtil.getInstance().getCurrentSession();
	}

	
}
