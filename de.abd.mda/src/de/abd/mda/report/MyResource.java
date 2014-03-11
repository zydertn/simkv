package de.abd.mda.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.icesoft.faces.context.Resource;

public class MyResource implements Resource, Serializable {

	private String customName;
	private String resourceName;
	private InputStream inputStream;
	private final Date lastModified;

	public MyResource(String resourceName) {
		this.customName = resourceName;
		this.resourceName = resourceName;
		this.lastModified = new Date();
	}
	
	public MyResource(String customName, String resourceName) {
		this.customName = customName;
		this.resourceName = resourceName;
		this.lastModified = new Date();
	}

	@Override
	public InputStream open() throws IOException {
		if (inputStream == null) {
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			InputStream stream = ec.getResourceAsStream(resourceName);
			byte[] byteArray = toByteArray(stream);
			inputStream = new ByteArrayInputStream(byteArray);
		} else {
			inputStream.reset();
		}
		return inputStream;
	}

	private byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len = 0;
		while ((len = input.read(buf)) > -1 ) output.write(buf, 0, len);
		return output.toByteArray();
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

}
