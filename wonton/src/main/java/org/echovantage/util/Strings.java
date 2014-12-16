package org.echovantage.util;

public class Strings {
	public static boolean nullOrEmpty(final String s) {
		return s == null || "".equals(s);
	}

	public static String ellipse(final String string, final int length) {
		return string.length() <= length ? string : string.substring(0, length - 1) + "\u2026";
	}
}
