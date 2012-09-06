package de.abd.mda.persistence.dao.controller;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;

public class DaoController implements IDaoController {

	@Override
	public String createObject(DaoObject d) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		String message = "";
		try {
			tx = session.beginTransaction();
			session.save(d);
			tx.commit();
			return "";
		} catch (NonUniqueObjectException e) {
			System.out.println("NonUniqueObjectException");
			System.out.println(e.getStackTrace());
			message = "Fehler!! Objekt existiert bereits und wurde nicht angelegt!";
			return message;
		} catch (ConstraintViolationException e) {
			if (e.getSQLException().getMessage().contains("Duplicate entry")) {
				message = "Fehler!! Objekt existiert bereits und wurde nicht angelegt!";
				return message;
			}
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
					return e.getMessage();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
		return message;
	}

	@Override
	public void deleteObject(DaoObject d) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		try {
			tx = session.beginTransaction();
			session.delete(d);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	@Override
	public void updateObject(DaoObject d) {
		Transaction tx = null;
		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
		try {
			tx = session.beginTransaction();
			session.save(d);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					tx.rollback();
				} catch (HibernateException e1) {
					System.out.println("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	protected HttpServletRequest getRequest() {
		if (FacesContext.getCurrentInstance().getExternalContext() != null) {
			return ((HttpServletRequest) FacesContext.getCurrentInstance()
					.getExternalContext().getRequest());
		} else {
			return null;
		}
	}
	
	@Override
	public List<DaoObject> listObjects() {
		// TODO Auto-generated method stub
		return null;
	}

}