package org.seal.xacml.utils;

import java.text.DecimalFormat;
import java.util.Random;

public class MiscUtil {
	public static String roundNumberToTwoDecimalPlaces(double n) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		return df.format(n);
	}
	
	public static String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
