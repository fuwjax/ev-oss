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
import java.io.StringReader;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class StringSourceFileObject extends BaseFileObject {
	private final CharSequence source;

	/**
	 * Creates a new instance.
	 * @param name the name of the class
	 * @param source the java source code
	 */
	public StringSourceFileObject(final String name, final CharSequence source) {
		super(name, Kind.SOURCE, null);
		this.source = source;
	}

	@Override
	public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException {
		return source;
	}

	@Override
	public Reader openReader(final boolean ignoreEncodingErrors) throws IOException {
		return new StringReader(source.toString());
	}
}
