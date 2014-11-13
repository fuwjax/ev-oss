package org.echovantage.wonton.standard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.echovantage.wonton.Wonton;

abstract class AbstractContainerWonton extends AbstractWonton {
	private static final Pattern KEY = Pattern.compile("^\\[?([^\\[.\\]]+)\\]?\\.?((?![\\].]).*)$");

	static String joinKey(final String prefix, final String suffix) {
		return prefix + (suffix.startsWith("[") ? suffix : "." + suffix);
	}

	private static Matcher matchKey(final String key) {
		final Matcher matcher = KEY.matcher(key);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("Unparsable key: " + key);
		}
		return matcher;
	}

	@Override
	public final Wonton get(final String key) {
		assert key != null;
		final Matcher matcher = matchKey(key);
		final String prefix = matcher.group(1);
		final String suffix = matcher.group(2);
		final Wonton elm = getShallow(prefix);
		if(elm == null) {
			throw new NoSuchKeyException();
		}
		return "".equals(suffix) ? elm : elm.get(suffix);
	}

	protected abstract Wonton getShallow(String shallowKey);

	@Override
	public final void accept(final Visitor visitor) {
		assert visitor != null;
		acceptShallow(new Visitor() {
			@Override
			public void visit(final String key, final Wonton value) {
				assert key != null;
				assert value != null;
				visitor.visit(key, value);
				value.accept((subKey, subValue) -> visitor.visit(joinKey(key, subKey), subValue));
			}
		});
	}

	protected abstract void acceptShallow(Visitor visitor);

	protected void set(final String key, final Wonton value) {
		assert key != null;
		assert value != null;
		final Matcher matcher = matchKey(key);
		final String prefix = matcher.group(1);
		final String suffix = matcher.group(2);
		if("".equals(suffix)) {
			setShallow(prefix, value);
		} else {
			Wonton child = getShallow(prefix);
			if(child == null) {
				if(suffix.startsWith("[0]")) {
					child = new ListWonton();
				} else {
					child = new MapWonton();
				}
				setShallow(prefix, child);
			}
			if(child instanceof MutableStruct) {
				((MutableStruct)child).set(suffix, value);
			} else {
				throw new IllegalArgumentException("Cannot set property " + suffix + " on an immutable wonton");
			}
		}
	}

	protected void setShallow(final String shallowKey, final Wonton value) {
		throw new UnsupportedOperationException();
	}

	protected Wonton build() {
		return this;
	}
}
