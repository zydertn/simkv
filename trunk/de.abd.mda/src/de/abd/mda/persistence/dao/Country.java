package de.abd.mda.persistence.dao;

import java.io.Serializable;


public class Country extends DaoObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7681723230865794429L;

	private int id;
	private String name;
	private String reportLocaleName;
	private String shortName;
	private String internationalAreaCode;

	public Country() {
		
	}
	
	public Country(String name, String reportLocaleName, String shortName, String intAreaCode) {
		this.name = name;
		this.reportLocaleName = reportLocaleName;
		this.shortName = shortName;
		this.internationalAreaCode = intAreaCode;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInternationalAreaCode() {
		return internationalAreaCode;
	}

	public void setInternationalAreaCode(String internationalAreaCode) {
		this.internationalAreaCode = internationalAreaCode;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReportLocaleName() {
		return reportLocaleName;
	}

	public void setReportLocaleName(String reportLocaleName) {
		this.reportLocaleName = reportLocaleName;
	}

}