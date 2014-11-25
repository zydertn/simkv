package de.abd.mda.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
	
	public static String getMonthAsString(int month) {
		if (month > 12)
			month = 12;
	    return new DateFormatSymbols().getMonths()[month];
	}

	public static String getCalendarString(Calendar cal) {
		return (cal.get(Calendar.DATE) + ". " + getMonthAsString(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR));
	}

	public static String getCalendarExportString(Calendar cal) {
		return (cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DATE));
	}
	
}
