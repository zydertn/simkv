package de.abd.mda.util;

import java.util.Comparator;
import java.util.Date;

import de.abd.mda.persistence.dao.CardBean;
import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;

public class CardComparator implements Comparator<DaoObject> {

	@Override
	public int compare(DaoObject c1, DaoObject c2) {
		// TODO Auto-generated method stub

		Date d1 = ((CardBean) c1).getActivationDate();
		Date d2 = ((CardBean) c2).getActivationDate();
		
		if (d1 == null)
			return -1;
		if (d2 == null)
			return 1;
		
		return ((CardBean) c1).getActivationDate().compareTo(((CardBean) c2).getActivationDate());
	}

}
