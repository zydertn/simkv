package de.abd.mda.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
	
}
