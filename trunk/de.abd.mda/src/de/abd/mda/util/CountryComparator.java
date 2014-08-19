package de.abd.mda.util;

import java.util.Comparator;

import de.abd.mda.persistence.dao.Country;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;

public class CountryComparator implements Comparator<DaoObject> {

	@Override
	public int compare(DaoObject c1, DaoObject c2) {
		// TODO Auto-generated method stub
		return ((Country) c1).getName().toLowerCase().compareTo(((Country) c2).getName().toLowerCase());
	}

}
