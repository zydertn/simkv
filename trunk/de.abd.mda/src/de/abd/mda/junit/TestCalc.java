package de.abd.mda.junit;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class TestCalc {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "Citotec GmbH & Co. KG";
		s = s.replaceAll("&", " ");
		for (int i = 0; i < s.length(); i++) {
			System.out.println(s.charAt(i) + "; " + Character.getNumericValue(s.charAt(i)));
		}
		
		System.out.println(s);
		
//		String d = "9.5";
//	
//		BigDecimal bd = new BigDecimal(d);
//		bd = bd.multiply(new BigDecimal("0.19"));
//		System.out.println(bd.setScale(2, RoundingMode.DOWN));
//		System.out.println(bd.setScale(2, RoundingMode.HALF_UP));
	}

}
