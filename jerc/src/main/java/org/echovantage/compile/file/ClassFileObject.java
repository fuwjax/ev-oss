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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A JavaFileObject for storing bytecode and source in memory.
 */
public class ClassFileObject extends BaseFileObject {
	private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

	/**
	 * Creates a new instance.
	 * @param name the class name
	 * @param kind the class kind
	 * @param charset the charset
	 */
	public ClassFileObject(final String name, final Kind kind, final Charset charset) {
		super(name, kind, charset);
	}

	public byte[] toBytes() {
		return bytes.toByteArray();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return bytes;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		if(bytes.size() == 0) {
			throw new FileNotFoundException();
		}
		return new ByteArrayInputStream(toBytes());
	}
}
