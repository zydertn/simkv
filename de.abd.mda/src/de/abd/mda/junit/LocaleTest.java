package de.abd.mda.junit;

import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import de.abd.mda.util.DateUtils;

public class LocaleTest {

	public LocaleTest() {
	}
	
	public static void main(String args[]) {
		String baseName = "de.abd.mda.locale.report";
		ResourceBundle bundle = ResourceBundle.getBundle(baseName);
		
//		System.out.println(bundle.getString("LocaleTest.teststring1"));
//		System.out.println(bundle.getString("LocaleTest.teststring2"));
//		
//		Locale.setDefault(new Locale("de", "CH"));
//		bundle = ResourceBundle.getBundle(baseName);
//		System.out.println(bundle.getString("LocaleTest.teststring1"));
//		System.out.println(bundle.getString("LocaleTest.teststring2"));
//		
//		Locale.setDefault(new Locale("en"));
//		bundle = ResourceBundle.getBundle(baseName);
//		System.out.println(bundle.getString("LocaleTest.teststring1"));
//		System.out.println(bundle.getString("LocaleTest.teststring2"));
		
		Locale.setDefault(new Locale("at"));
		bundle = ResourceBundle.getBundle(baseName);
		
		Calendar calcMonth = Calendar.getInstance();
		calcMonth.set(Calendar.DATE, 1);
		calcMonth.set(Calendar.MONTH, 11);
		calcMonth.set(Calendar.YEAR, 2014);
		String month = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH));
		System.out.println(month);

		Locale.setDefault(new Locale("en"));
		bundle = ResourceBundle.getBundle(baseName);
		month = DateUtils.getMonthAsString(calcMonth.get(Calendar.MONTH));
		System.out.println(month);

	}
	
}
