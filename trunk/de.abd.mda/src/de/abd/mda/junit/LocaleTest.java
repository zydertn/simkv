package de.abd.mda.junit;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleTest {

	public LocaleTest() {
	}
	
	public static void main(String args[]) {
		String baseName = "de.abd.mda.locale.localetest";
		ResourceBundle bundle = ResourceBundle.getBundle(baseName);
		
		System.out.println(bundle.getString("LocaleTest.teststring1"));
		System.out.println(bundle.getString("LocaleTest.teststring2"));
		
		Locale.setDefault(new Locale("de", "CH"));
		bundle = ResourceBundle.getBundle(baseName);
		System.out.println(bundle.getString("LocaleTest.teststring1"));
		System.out.println(bundle.getString("LocaleTest.teststring2"));
		
		Locale.setDefault(new Locale("en"));
		bundle = ResourceBundle.getBundle(baseName);
		System.out.println(bundle.getString("LocaleTest.teststring1"));
		System.out.println(bundle.getString("LocaleTest.teststring2"));
		
	}
	
}
