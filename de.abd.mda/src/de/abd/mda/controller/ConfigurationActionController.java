package de.abd.mda.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;
import org.icefaces.ace.component.column.Column;
import org.icefaces.ace.component.datatable.DataTable;

import de.abd.mda.persistence.dao.controller.ConfigurationController;
import de.abd.mda.persistence.dao.controller.DaoController;

public class ConfigurationActionController extends DaoController {

	private final static Logger LOGGER = Logger.getLogger(ConfigurationActionController.class .getName());

	private String simPriceKey;
	private String dataOptionKey;
	private Double simPrice;
	private Double dataOptionPrice;
	private Map<Integer, Double> simPriceMap;
	private Map<Integer, Double> dataOptionMap;
	
	public ConfigurationActionController() {
		LOGGER.info("Instantiate: ConfigurationActionController");
		ConfigurationController cc = new ConfigurationController();
		simPriceMap = cc.getSimPricesFromDB();
		if (simPriceMap != null && simPriceMap.containsKey(1)) {
			simPrice = simPriceMap.get(1);
		}
		
		dataOptionMap = cc.getDataOptionPricesFromDB();
		if (dataOptionMap != null && dataOptionMap.containsKey(1)) {
			dataOptionPrice = dataOptionMap.get(1);
		}
		
	}
	
	public void changeSimKeyValue(ValueChangeEvent evt) {
		LOGGER.info("Method: changeSimKeyValue: " + evt.getNewValue());
		int key = new Integer("" + evt.getNewValue());
		setSimPrice(simPriceMap.get(key));
		getRequest().setAttribute("simPrice", simPriceMap.get(key));
	}

	public void changeDataOptionKeyValue(ValueChangeEvent evt) {
		LOGGER.info("Method: changeDataOptionKeyValue: " + evt.getNewValue());
		int key = new Integer("" + evt.getNewValue());
		setDataOptionPrice(dataOptionMap.get(key));
		getRequest().setAttribute("dataOptionPrice", dataOptionMap.get(key));
	}

	
	public String updateConfiguration() {
		LOGGER.info("Method: updateConfiguration");
		ConfigurationController cc = new ConfigurationController();
		simPriceMap = cc.getSimPricesFromDB();
		if (simPriceKey != null && simPriceKey.length() > 0) {
				Integer simKey = new Integer(simPriceKey);
				simPriceMap.put(simKey, simPrice);
		}
		dataOptionMap = cc.getDataOptionPricesFromDB();
		if (dataOptionKey != null && dataOptionKey.length() > 0) {
				dataOptionMap.put(new Integer(dataOptionKey), dataOptionPrice);
		}
		
		cc.updateConfiguration(simPriceMap, dataOptionMap);
		
		getRequest().setAttribute("message", "Konfiguration wurde aktualisiert!");
		return "success";
	}
	
	public String addSimPriceProfile() {
		LOGGER.info("Method: addSimPriceProfile");
		ConfigurationController cc = new ConfigurationController();
		Map<Integer, Double> simPriceMap = cc.getSimPricesFromDB();
		simPriceMap.put(simPriceMap.size()+1, 0.0);
		cc.updateConfiguration(simPriceMap, null);
		getRequest().setAttribute("updateSimPrices", true);
		return "refresh";
	}

	public String addDataOptionProfile() {
		LOGGER.info("Method: addDataOptionProfile");
		ConfigurationController cc = new ConfigurationController();
		Map<Integer, Double> dataOptionMap = cc.getDataOptionPricesFromDB();
		dataOptionMap.put(dataOptionMap.size()+1, 0.0);
		cc.updateConfiguration(null, dataOptionMap);
		getRequest().setAttribute("updateDataOptions", true);
		return "refresh";
	}
	
	public String getSimPriceKey() {
		return simPriceKey;
	}

	public void setSimPriceKey(String simPriceKey) {
		this.simPriceKey = simPriceKey;
	}

	public String getDataOptionKey() {
		return dataOptionKey;
	}

	public void setDataOptionKey(String dataOptionKey) {
		this.dataOptionKey = dataOptionKey;
	}

	public Double getSimPrice() {
		if (getRequest().getAttribute("simPrice") != null) {
			return (Double) getRequest().getAttribute("simPrice");
		}
		return simPrice;
	}

	public void setSimPrice(Double simPrice) {
		this.simPrice = simPrice;
	}

	public Double getDataOptionPrice() {
		if (getRequest().getAttribute("dataOptionPrice") != null) {
			return (Double) getRequest().getAttribute("dataOptionPrice");
		}
		return dataOptionPrice;
	}

	public void setDataOptionPrice(Double dataOptionPrice) {
		this.dataOptionPrice = dataOptionPrice;
	}

	public Map<Integer, Double> getSimPriceMap() {
		return simPriceMap;
	}

	public void setSimPriceMap(Map<Integer, Double> simPriceMap) {
		this.simPriceMap = simPriceMap;
	}

	public Map<Integer, Double> getDataOptionMap() {
		return dataOptionMap;
	}

	public void setDataOptionMap(Map<Integer, Double> dataOptionMap) {
		this.dataOptionMap = dataOptionMap;
	}

}
