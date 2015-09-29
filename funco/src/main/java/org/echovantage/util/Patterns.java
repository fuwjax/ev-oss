package org.echovantage.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns {
	public static String replaceAll(final Pattern pattern, final String input, final Function<String, String> replacement) {
		final Matcher m = pattern.matcher(input);
		final StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, String.valueOf(replacement.apply(m.group(1))));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
