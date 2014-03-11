package de.abd.mda.util;

import java.util.Comparator;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;

public class CardComparator implements Comparator<DaoObject> {

	@Override
	public int compare(DaoObject c1, DaoObject c2) {
		// TODO Auto-generated method stub
		return ((CardBean) c1).getActivationDate().compareTo(((CardBean) c2).getActivationDate());
	}

}
