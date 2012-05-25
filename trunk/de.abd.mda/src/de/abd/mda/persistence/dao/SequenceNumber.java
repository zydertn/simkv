package de.abd.mda.persistence.dao;

public class SequenceNumber extends DaoObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8521842705413566224L;
	private int id;
	private int sequenceNumber;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	
	
}
