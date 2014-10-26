package org.echovantage.gild.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.function.Function;

public interface StreamTransform {
	public static LineTransform line(final Charset charset, final Function<String, String> transform) {
		return new LineTransform(charset) {
			@Override
			protected String transform(final String line) {
				return transform.apply(line);
			}
		};
	}

	public static StreamTransform sort(final Charset charset) {
		return new SortTransform(charset);
	}

	public static StreamTransform sort(final Charset charset, final Comparator<String> comparator) {
		return new SortTransform(charset, comparator);
	}

	public static StreamTransform serial(final StreamTransform... transforms) {
		if(transforms.length == 1) {
			return transforms[0];
		}
		return new CompositeTransform(transforms);
	}

	public void transform(InputStream input, OutputStream output) throws IOException;
}
