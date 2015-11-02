package org.fuwjax.oss.lumber;

public class SysoutAppender implements LumberAppender {
	@Override
	public void configure(final Object configData) throws Exception {
		// do nothing
	}

	@Override
	public void append(final CharSequence line) throws Exception {
		System.out.println(line);
	}

}
