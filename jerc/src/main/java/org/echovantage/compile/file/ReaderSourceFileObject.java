/*******************************************************************************
 * Copyright (c) 2010 Michael Doberenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Doberenz - initial API and implementation
 ******************************************************************************/
package org.echovantage.compile.file;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Supplier;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class ReaderSourceFileObject extends BaseFileObject {
	private final Supplier<Reader> reader;

	public ReaderSourceFileObject(final String name, final Supplier<Reader> reader) {
		super(name, Kind.SOURCE, null);
		this.reader = reader;
	}

	@Override
	public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
		return reader.get();
	}
}
