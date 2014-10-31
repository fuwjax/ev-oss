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
