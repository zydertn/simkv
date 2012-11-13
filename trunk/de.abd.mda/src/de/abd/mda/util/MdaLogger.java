package de.abd.mda.util;

import org.apache.log4j.Logger;

public final class MdaLogger {

	public static void debug(Logger logger, Exception e) {
		logger.debug(e.getMessage() + ", cause: " + e.getCause());
	}
	
	public static void debug(Logger logger, String e) {
		logger.debug(e);
	}

	public static void info(Logger logger, Exception e) {
		logger.info(e.getMessage() + ", cause: " + e.getCause());
	}
	
	public static void info(Logger logger, String e) {
		logger.info(e);
	}
	
	public static void warn(Logger logger, Exception e) {
		logger.warn(e.getMessage() + ", cause: " + e.getCause());
	}
	
	public static void warn(Logger logger, String e) {
		logger.warn(e);
	}

	public static void error(Logger logger, Exception e) {
		logger.error(e.getMessage() + ", cause: " + e.getCause());
	}
	
	public static void error(Logger logger, String e) {
		logger.error(e);
	}
}
