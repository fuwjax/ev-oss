package org.fuwjax.oss.lumber;

public interface LumberAppender extends LumberStrategy {
	void append(CharSequence line) throws Exception;

	default void safeAppend(final CharSequence line) {
		try {
			append(line);
		} catch (final Exception e) {
			System.err.println("Could not append to " + getClass().getCanonicalName() + " with " + line);
			e.printStackTrace();
		}
	}

}
