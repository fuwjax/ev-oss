package org.echovantage.gild.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public class StreamTransformer implements Transformer {
	private final StreamTransform transform;

	public StreamTransformer(final StreamTransform transform) {
		this.transform = transform;
	}

	@Override
	public void transform(final ReadOnlyPath src, final Path dest) throws IOException {
		try(InputStream in = src.open();
				OutputStream out = Files.newOutputStream(dest)) {
			transform.transform(in, out);
		}
	}
}
