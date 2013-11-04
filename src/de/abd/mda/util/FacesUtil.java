package de.abd.mda.util;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.type.descriptor.ValueBinder;

public class FacesUtil {

	/********* Weitere Methoden ****************/
	public HttpSession getSession() {
		if (FacesContext.getCurrentInstance() != null) {
			Object facesSession = FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			if (facesSession != null)
				return (HttpSession) facesSession;
		}
		return null;
	}

//	public HttpServletRequest getRequest() {
//		return ((HttpServletRequest) FacesContext.getCurrentInstance()
//				.getExternalContext().getRequest());
//	}

	public static Object getAttributeFromRequest(String attributeName) {
		if (FacesContext.getCurrentInstance() != null)
			return ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getAttribute(attributeName);
		return null;
	}
	
	public static void writeAttributeToRequest(String attributeName, String attributeValue) {
		if (FacesContext.getCurrentInstance() != null)
			((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute(attributeName, attributeValue);
		else 
			System.out.println("FacesContext nicht verfügbar! Attribut " + attributeName + " kann nicht in Request gespeichert werden!");
	}
	
	public static Object getManagedBean(String beanName) {
		return getValueBinding(getJsfEl(beanName)).getValue(FacesContext.getCurrentInstance());
	}

	private static Application getApplication() {
		ApplicationFactory appFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
		return appFactory.getApplication();
	}
	
	private static ValueBinding getValueBinding(String el) {
		return getApplication().createValueBinding(el);
	}
	
	private static String getJsfEl(String value) {
		return "#{" + value + "}";
	}
//	private void Applic
	//	appFactory.getApplication().createValueBinding()

	
}
