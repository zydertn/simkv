package de.abd.mda.controller;

import de.abd.mda.persistence.dao.Voucher;
import de.abd.mda.persistence.dao.controller.VoucherController;

public class VoucherActionController extends ActionController {

	public Voucher voucher;
	
	public void createVoucher() {
		VoucherController vc = new VoucherController();
		vc.createObject(voucher);
	}

	public Voucher getVoucher() {
		return voucher;
	}

	public void setVoucher(Voucher voucher) {
		this.voucher = voucher;
	}
	
}
