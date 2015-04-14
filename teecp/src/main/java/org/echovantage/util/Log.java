package org.echovantage.util;

public interface Log {
	public static final Log SILENT = new Log() {
		@Override
		public void abort(final Throwable cause, final String message, final Object... args) {
		}

		@Override
		public void warn(final Throwable cause, final String message, final Object... args) {
		}

		@Override
		public void warn(final String message, final Object... args) {
		}
	};
	public static final Log SYSTEM = new Log() {
		@Override
		public void abort(final Throwable cause, final String message, final Object... args) {
			System.err.println("ABORT: " + String.format(message, args));
			cause.printStackTrace();
		}

		@Override
		public void warn(final Throwable cause, final String message, final Object... args) {
			warn(message, args);
			// System.err.println(cause.getClass().getCanonicalName() + ": " +
			// cause.getMessage());
			cause.printStackTrace();
		}

		@Override
		public void warn(final String message, final Object... args) {
			System.err.println("WARN : " + String.format(message, args));
		}
	};

	public void abort(final Throwable cause, final String message, final Object... args);

	public void warn(final Throwable cause, final String message, final Object... args);

	public void warn(final String message, final Object... args);
}
