package de.abd.mda.model;

public class Country {
	private String name;
	private String shortName;
	private String internationalAreaCode;

	public Country(String name, String shortName, String intAreaCode) {
		this.name = name;
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

}