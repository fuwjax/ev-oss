package org.echovantage.wonton.standard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.echovantage.wonton.StandardType;
import org.echovantage.wonton.Wonton;

public abstract class AbstractContainerWonton extends AbstractWonton {
	private static final Pattern KEY = Pattern.compile("^\\[?([^\\[.\\]]+)\\]?\\.?((?![\\].]).*)$");

	private static String joinKey(final String prefix, final String suffix) {
		return prefix + (suffix.startsWith("[") ? suffix : "." + suffix);
	}

	public AbstractContainerWonton(final Type type) {
		super(type);
	}

	@Override
	public Wonton get(final String key) {
		final Matcher matcher = matchKey(key);
		try {
			final Wonton elm = getShallow(matcher.group(1));
			return elm == null ? null : "".equals(matcher.group(2)) ? elm : elm.get(matcher.group(2));
		} catch(final RuntimeException e) {
			return null;
		}
	}

	protected static Matcher matchKey(final String key) {
		final Matcher matcher = KEY.matcher(key);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("Unparsable key: " + key);
		}
		return matcher;
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

	public void set(final String key, final Wonton value) {
		Matcher matcher = matchKey(key);
		String prefix = matcher.group(1);
		String suffix = matcher.group(2);
		if("".equals(suffix)) {
			setShallow(prefix, value);
		} else {
			Wonton child = getShallow(prefix);
			if(child == null) {
				if(suffix.startsWith("[0]")) {
					child = new ListWonton(StandardType.ARRAY);
				} else {
					child = new MapWonton(StandardType.OBJECT);
				}
				setShallow(matcher.group(1), child);
			}
			if(child instanceof Wonton.Mutable) {
				((Wonton.Mutable) child).set(suffix, value);
			} else {
				throw new IllegalArgumentException("Cannot set property " + suffix + " on an immutable wonton");
			}
		}
	}

	protected void setShallow(final String shallowKey, final Wonton value) {
		throw new UnsupportedOperationException();
	}

	public Wonton build() {
		return this;
	}
}
