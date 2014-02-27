package de.abd.mda.util;

import java.util.Comparator;

import de.abd.mda.persistence.dao.Customer;
import de.abd.mda.persistence.dao.DaoObject;
import de.abd.mda.persistence.dao.Voucher;

public class VoucherComparator implements Comparator<Voucher> {

	@Override
	public int compare(Voucher v1, Voucher v2) {
		// TODO Auto-generated method stub
		String nullStringV1 = "";
		String nullStringV2 = "";
		if (v1.getMonth() < 10)
			nullStringV1 = "0";
		if (v2.getMonth() < 10)
			nullStringV2 = "0";
		
		String v1s = ""+ v1.getYear() + "," + nullStringV1 + v1.getMonth();
		String v2s = ""+ v2.getYear() + "," + nullStringV2 + v2.getMonth();
		return v1s.compareTo(v2s);
	}

}
