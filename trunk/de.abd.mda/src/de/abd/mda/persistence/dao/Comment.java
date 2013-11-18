package de.abd.mda.persistence.dao;

public class Comment extends DaoObject {
/**
	 * 
	 */
	private static final long serialVersionUID = -4714665994959608905L;
	private int id;
	private String date;
	private String commentString;

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getCommentString() {
		return commentString;
	}
	public void setCommentString(String commentString) {
		this.commentString = commentString;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
