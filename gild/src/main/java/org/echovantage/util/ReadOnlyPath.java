package org.echovantage.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ReadOnlyPath implements Comparable<ReadOnlyPath> {
	public static ReadOnlyPath readOnly(Path path){
		return path == null ? null : new ReadOnlyPath(path);
	}
	
	private final Path path;

	private ReadOnlyPath(final Path path) {
		this.path = path;
	}

	public InputStream newInputStream() throws IOException {
		return Files.newInputStream(path);
	}

	public boolean exists() {
		return Files.exists(path);
	}

	public boolean isDirectory() {
		return Files.isDirectory(path);
	}

	public DirectoryStream<ReadOnlyPath> newDirectoryStream() throws IOException {
		return new DirectoryStream<ReadOnlyPath>() {
			private final DirectoryStream<Path> stream = Files.newDirectoryStream(path);

			@Override
			public void close() throws IOException {
				stream.close();
			}

			@Override
			public Iterator<ReadOnlyPath> iterator() {
				return new Iterator<ReadOnlyPath>() {
					private final Iterator<Path> iter = stream.iterator();

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public ReadOnlyPath next() {
						return new ReadOnlyPath(iter.next());
					}
				};
			}
		};
	}

	public String getFileName() {
		return path.getFileName().toString();
	}

	public ReadOnlyPath resolve(final String relative) {
		return new ReadOnlyPath(path.resolve(relative));
	}

	public void copyTo(final Path dest) throws IOException {
		Files2.copy(path, dest);
	}

	public ReadOnlyPath getParent() {
		return new ReadOnlyPath(path.getParent());
	}
	
	@Override
	public String toString() {
	   return getFileName();
	}

	@Override
   public int compareTo(ReadOnlyPath o) {
	   return path.compareTo(o.path);
   }

	public void copyTo(OutputStream out) throws IOException {
		Files.copy(path, out);
   }
}
