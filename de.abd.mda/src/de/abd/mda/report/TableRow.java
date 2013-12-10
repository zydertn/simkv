package de.abd.mda.report;

public class TableRow {
	private Integer invoiceRows;
	private String[] invoiceRow;

	public TableRow(Integer rows, String[] rowStrings) {
		invoiceRows = rows;
		invoiceRow = rowStrings;
	}
	
	public Integer getInvoiceRows() {
		return invoiceRows;
	}
	public void setInvoiceRows(Integer invoiceRows) {
		this.invoiceRows = invoiceRows;
	}
	public String[] getInvoiceRow() {
		return invoiceRow;
	}
	public void setInvoiceRow(String[] invoiceRow) {
		this.invoiceRow = invoiceRow;
	}
	
}
