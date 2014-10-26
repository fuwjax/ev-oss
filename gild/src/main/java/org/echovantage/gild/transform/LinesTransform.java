package org.echovantage.gild.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class LinesTransform implements StreamTransform {
	private final Charset charset;

	public LinesTransform(final Charset charset) {
		this.charset = charset;
	}

	@Override
	public void transform(final InputStream input, final OutputStream output) throws IOException {
		final List<String> lines = readLines(input, charset);
		writeLines(output, charset, transform(lines));
	}

	protected abstract List<String> transform(List<String> lines);

	private static void writeLines(final OutputStream output, final Charset charset, final List<String> lines) {
		try(final PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset))) {
			for(final String line : lines) {
				writer.println(line);
			}
		}
	}

	private static List<String> readLines(final InputStream input, final Charset charset) throws IOException {
		try(final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
			String line;
			final List<String> lines = new ArrayList<>();
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}
}
