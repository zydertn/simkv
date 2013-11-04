package de.abd.mda.persistence.dao;

public class Person extends DaoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8754205128095926999L;
	private int id;
	private String name;
	private String firstname;
	private String gender;
	private Address address;
	private String phoneNrFirst;
	private String phoneNrSecond;
	private String email;

	public Person() {
		this.name = "";
		this.firstname = "";
		this.gender = "Herr";
		this.address = new Address();
		this.phoneNrFirst = "";
		this.phoneNrSecond = "";
		this.email = "";
	}

	public String getNameString() {
		if (gender != null && gender.length() > 0 && name != null && name.length() > 0 && firstname != null
				&& firstname.length() > 0)
			return gender + " " + firstname + " " + name;
		return "";
	}

	public String getPhoneNr() {
		if (phoneNrFirst != null && phoneNrFirst.length() > 0 && phoneNrSecond != null && phoneNrSecond.length() > 0)
			if (phoneNrFirst.startsWith("0")) {
				return phoneNrFirst + phoneNrSecond;
			} else {
				return "0" + phoneNrFirst + phoneNrSecond;
		}
		else return "";
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Address getAddress() {
		if (address != null)
			return address;
		else
			return new Address();
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPhoneNrFirst() {
		return phoneNrFirst;
	}

	public void setPhoneNrFirst(String phoneNrFirst) {
		this.phoneNrFirst = phoneNrFirst;
	}

	public String getPhoneNrSecond() {
		return phoneNrSecond;
	}

	public void setPhoneNrSecond(String phoneNrSecond) {
		this.phoneNrSecond = phoneNrSecond;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}