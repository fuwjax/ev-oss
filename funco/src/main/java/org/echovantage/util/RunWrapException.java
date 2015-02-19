/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util;

import java.util.function.Supplier;

/**
 * Created by fuwjax on 12/20/14.
 */
public class RunWrapException extends RuntimeException {
	private final Supplier<String> message;

	public RunWrapException(final Throwable cause) {
		this(cause, "Wrapped %s: %s", cause.getClass().getCanonicalName(), cause.getMessage());
	}

	public RunWrapException(final Throwable cause, final String pattern, final Object... args) {
		this(cause, () -> String.format(pattern, args));
	}

	public RunWrapException(final Throwable cause, final String message) {
		this(cause, () -> message);
	}

	public RunWrapException(final Throwable cause, final Supplier<String> message) {
		super(cause);
		this.message = message;
	}

	protected Supplier<String> message() {
		return message;
	}

	@Override
	public String getMessage() {
		return message.get();
	}

	public <X extends Throwable> RunWrapException throwIf(final Class<X> exceptionType) throws X {
		if(exceptionType.isInstance(getCause())) {
			throw exceptionType.cast(getCause());
		}
		return this;
	}
}
