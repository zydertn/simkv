package de.abd.mda.persistence.dao;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.abd.mda.model.Country;
import de.abd.mda.model.Model;

public class Address extends DaoObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5526520073319031205L;
	private int addressId;
	private String street;
	private String housenumber;
	private String postcode;
	private String city;
	private Country country;
	private String postbox;
	
	public Address() {
		this.street = "";
		this.housenumber = "";
		this.postcode = "";
		this.city = "";
		this.postbox = "";
	}
	
	public String getAddressString() {
		String nameS = "";
		if (country != null) {
			nameS += country.getShortName() + ", ";
		}
		if (postcode != null && postcode.length() > 0 && city != null && city.length() > 0) {
			if (street != null && street.length() > 0 && housenumber != null && housenumber.length() > 0)
				return nameS + street + " " + housenumber + ", " + postcode + " " + city;
			else if (postbox != null && postbox.length() > 0)
				return nameS + "Postfach " + postbox + ", " + postcode + " " + city;
		}
		return "";
	}
	
	public String getStreet() {
		return street;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public String getHousenumber() {
		return housenumber;
	}
	
	public void setHousenumber(String housenumber) {
		this.housenumber = housenumber;
	}
	
	public String getPostcode() {
		return postcode;
	}
	
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}

	public Country getCountry() {
		if (country != null)
			return country;
		else {
			Model model = (Model) ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getAttribute("model");
			return model.getCountryByShortName("D"); 
		}
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public String getPostbox() {
		return postbox;
	}

	public void setPostbox(String postbox) {
		this.postbox = postbox;
	}
	
}