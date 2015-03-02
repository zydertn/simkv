package de.abd.mda.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
	
	public static String getMonthAsString(int month) {
		if (month > 12)
			month = 12;
	    return new DateFormatSymbols(new Locale("de", "DE")).getMonths()[month];
	}

	public static String getCalendarString(Calendar cal) {
		return (cal.get(Calendar.DATE) + ". " + getMonthAsString(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR));
	}

	public static String getCalendarNumString(Calendar cal) {
		if (cal == null)
			return "";

		String calString = "";

		if (cal.get(Calendar.DATE) < 10) {
			calString = "0";
		}
		calString += cal.get(Calendar.DATE) + ".";
		if (cal.get(Calendar.MONTH) + 1 < 10) {
			calString += "0";
		}
		calString += cal.get(Calendar.MONTH) + 1 + "." + cal.get(Calendar.YEAR);
		
		return calString;
	}

	public static String getCalendarExportString(Calendar cal) {
		return (cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + 1 + "-" + cal.get(Calendar.DATE));
	}

	public static String getCalendarExportStringWithTime(Calendar cal) {
		return (cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + 1 + "-" + cal.get(Calendar.DATE) + "_" + cal.get(Calendar.HOUR) + "." + cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND)) ;
	}

}
