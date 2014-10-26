package org.echovantage.gild.transform;

import static org.echovantage.gild.transform.StreamTransform.serial;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public interface Transformer {
	public static Transformer recurse(final Transformer transformer) {
		return new RecursiveTransformer(transformer);
	}

	public static Transformer of(final StreamTransform... transform) {
		return new StreamTransformer(serial(transform));
	}

	public static FilteredTransformer with(final Filter<ReadOnlyPath> filter, final StreamTransform... transform) {
		return new FilteredTransformer().with(filter, serial(transform));
	}

	void transform(ReadOnlyPath src, Path dest) throws IOException;
}
