package org.fuwjax.oss.util.pipe;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PrintStreamSink implements PipeSink {
	private PrintStream writer;
	private Supplier<? extends PrintStream> factory;
	private Consumer<? super PrintStream> closer;

	public PrintStreamSink(Supplier<? extends PrintStream> factory, Consumer<? super PrintStream> closer) {
		this.factory = factory;
		this.closer = closer;
	}

	public PrintStreamSink(Supplier<? extends PrintStream> factory) {
		this(factory, PrintStream::close);
	}

	@Override
	public void writeLine(String line) {
		assert line != null;
		if(writer == null) {
			writer = factory.get();
		}
		writer.println(line);
	}

	@Override
	public void close() {
		if(writer != null) {
			closer.accept(writer);
			writer = null;
		}
	}
}
