package org.echovantage.gild.transform;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public class RecursiveTransformer implements Transformer {
	private final Transformer transformer;

	public RecursiveTransformer(final Transformer transformer) {
		this.transformer = transformer;
	}

	@Override
	public void transform(final ReadOnlyPath src, final Path dest) throws IOException {
		if(src.isDirectory()) {
			Files.createDirectories(dest);
			try(DirectoryStream<ReadOnlyPath> dir = src.newDirectoryStream()) {
				for(final ReadOnlyPath sub : dir) {
					transform(sub, dest.resolve(sub.getFileName()));
				}
			}
		} else if(src.exists()) {
			transformer.transform(src, dest);
		}
	}
}
