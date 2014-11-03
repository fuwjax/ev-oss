package org.echovantage.wonton.standard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.echovantage.wonton.Wonton;

public abstract class AbstractContainerWonton extends AbstractWonton {
	private static final Pattern KEY = Pattern.compile("^\\[?([^\\[.\\]]+)\\]?\\.?((?![\\].]).*)$");

	private static String joinKey(final String prefix, final String suffix) {
		return prefix + (suffix.startsWith("[") ? suffix : "." + suffix);
	}

	@Override
	public Wonton get(final String key) {
		final Matcher matcher = KEY.matcher(key);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("Unparsable key: " + key);
		}
		try {
			final Wonton elm = getShallow(matcher.group(1));
			return elm == null ? NULL : "".equals(matcher.group(2)) ? elm : elm.get(matcher.group(2));
		} catch(final RuntimeException e) {
			return NULL;
		}
	}

	protected abstract Wonton getShallow(String shallowKey);

	@Override
	public void accept(final Visitor visitor) {
		acceptShallow(new Visitor() {
			@Override
			public void visit(final String key, final Wonton value) {
				visitor.visit(key, value);
				value.accept((subKey, subValue) -> visitor.visit(joinKey(key, subKey), subValue));
			}
		});
	}

	protected abstract void acceptShallow(Visitor visitor);
}
