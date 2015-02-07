/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
package org.echovantage.gild.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public abstract class LineTransform implements StreamTransform {
	private final Charset charset;

	public LineTransform(final Charset charset) {
		this.charset = charset;
	}

	@Override
	public void transform(final InputStream input, final OutputStream output) throws IOException {
		try(final PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset));
				final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
			String line;
			while((line = reader.readLine()) != null) {
				writer.println(transform(line));
			}
		}
	}

	protected abstract String transform(final String line);
}
