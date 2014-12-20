package org.echovantage.util.function;

public class UnsafeException extends RuntimeException {
	public UnsafeException(final String message, final Exception cause) {
		super(message, cause);
	}

	public <X extends Throwable> UnsafeException throwIf(Class<X> exceptionType) throws X{
		if(exceptionType.isInstance(getCause())){
			throw (X)getCause();
		}
		return this;
	}
}
