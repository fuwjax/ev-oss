package org.echovantage.util.function;

import org.echovantage.util.RunWrapException;

public class UnsafeException extends RunWrapException {
	public UnsafeException(Exception cause, String pattern, Object... args) {
		super(cause, pattern, args);
	}
}
