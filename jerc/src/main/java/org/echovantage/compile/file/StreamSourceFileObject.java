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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class StreamSourceFileObject extends BaseFileObject {
	private final Supplier<InputStream> stream;

	public StreamSourceFileObject(final String name, final Supplier<InputStream> stream, final Charset charset) {
		super(name, Kind.SOURCE, charset);
		this.stream = stream;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return stream.get();
	}
}
