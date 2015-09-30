/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

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

		@Override
		public void info(final String message, final Object... args) {
		}

		@Override
		public void info(final BooleanSupplier ifTrue, final String message, final Object... args) {
		}
	};
	public static final Log SYSTEM = new Log() {
		@Override
		public void abort(final Throwable cause, final String message, final Object... args) {
			System.err.println("ABORT: " + format(message, args));
			cause.printStackTrace();
		}

		@Override
		public void warn(final Throwable cause, final String message, final Object... args) {
			warn(message, args);
			cause.printStackTrace();
		}

		@Override
		public void warn(final String message, final Object... args) {
			System.err.println("WARN : " + format(message, args));
		}

		@Override
		public void info(final String message, final Object... args) {
			System.out.println("INFO : " + format(message, args));
		}
	};

	public static String format(final String message, final Object... args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i] instanceof Supplier) {
				args[i] = String.valueOf(((Supplier<?>) args[i]).get());
			}
		}
		return String.format(message, args);
	}

	public void abort(final Throwable cause, final String message, final Object... args);

	public void warn(final Throwable cause, final String message, final Object... args);

	public void warn(final String message, final Object... args);

	public void info(final String message, final Object... args);

	public default void info(final BooleanSupplier ifTrue, final String message, final Object... args) {
		if(ifTrue.getAsBoolean()) {
			info(message, args);
		}
	}
}
