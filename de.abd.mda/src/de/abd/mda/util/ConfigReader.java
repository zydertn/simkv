package de.abd.mda.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigReader {
	
	static final Logger logger = Logger.getLogger(ConfigReader.class);

	public static Properties readGeneralConfig() {
		Properties props = new Properties();
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream("E:\\general.properties"));
			props.load(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		
		return props;
	}
}
