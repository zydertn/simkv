package de.abd.mda.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;


import com.icesoft.faces.context.Resource;

import de.abd.mda.model.Model;

public class MyResource implements Resource, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9078176615991635089L;
	private String customName;
	private String resourceName;
	private String type;
	private InputStream inputStream;
	private final Date lastModified;

	public MyResource(String resourceName, String type) {
		this.customName = resourceName;
		this.resourceName = resourceName;
		this.lastModified = new Date();
		this.type = type;
	}
	

	@Override
	public InputStream open() throws IOException {
		if (inputStream == null) {
			Model model = new Model();
			model.createModel();
			String path = "";
			if (type.equals("zip")) {
				path = model.getZipPath();
			} else if (type.equals("pdf")) {
				path = model.getPdfPath();
			}
			File f = new File(path + resourceName);
			byte[] byteArray = org.apache.commons.io.FileUtils.readFileToByteArray(f);

			inputStream = new ByteArrayInputStream(byteArray);
		} else {
			inputStream.reset();
		}
		return inputStream;
	}


	@Override
	public String calculateDigest() {
		return customName;
	}

	@Override
	public Date lastModified() {
		return lastModified;
	}


	@Override
	public void withOptions(Options arg0) throws IOException {
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

}
