package de.abd.mda.persistence.dao.controller;

import java.util.List;

import de.abd.mda.persistence.dao.DaoObject;

public interface IDaoController {

	public String createObject(DaoObject d);
	
	public void deleteObject(DaoObject d);
	
	public void updateObject(DaoObject d);
	
	public List<DaoObject> listObjects();
	
}