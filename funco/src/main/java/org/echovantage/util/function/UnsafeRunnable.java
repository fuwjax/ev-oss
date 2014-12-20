package org.echovantage.util.function;

public interface UnsafeRunnable extends Runnable {
	void runUnsafe() throws Exception;

	@Override
	default void run() {
		try {
			runUnsafe();
		} catch(final RuntimeException e) {
			throw e;
		} catch(final Exception e) {
			throw new UnsafeException("runnable did not run safely", e);
		}
	}
}
