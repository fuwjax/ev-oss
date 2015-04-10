package org.echovantage.util;

public interface Log {
	public static final Log SILENT = new Log() {
		@Override
		public Log bind(final Object subject) {
			return this;
		}

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

	public Log bind(final Object subject);

	public void abort(final Throwable cause, final String message, final Object... args);

	public void warn(final Throwable cause, final String message, final Object... args);

	public void warn(final String message, final Object... args);
}
