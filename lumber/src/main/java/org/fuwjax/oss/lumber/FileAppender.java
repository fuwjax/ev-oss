package org.fuwjax.oss.lumber;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileAppender implements LumberAppender, AutoCloseable {
	private BufferedWriter writer;

	@Override
	public void configure(final Object configData) throws Exception {
		writer = Files.newBufferedWriter(Paths.get((String) configData), UTF_8);
	}

	@Override
	public void append(final CharSequence line) throws Exception {
		writer.append(line).append('\n');
		writer.flush();
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
}
