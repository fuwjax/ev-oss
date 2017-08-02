package org.fuwjax.oss.util.pipe;

import static org.fuwjax.oss.util.function.Unsafe.unsafe;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class StandardPipes {
	private StandardPipes() {
		// utility
	}

	public static final Consumer<AutoCloseable> CLOSER = unsafe(AutoCloseable::close);
	public static final Consumer<AutoCloseable> STDIO = stdio -> {
		/* don't close stdio */};

	private static final InputStream REALIN = System.in;
	private static final PrintStream REALOUT = System.out;
	private static final PrintStream REALERR = System.err;

	public static final PipeSource STDIN = sourceStream(() -> REALIN, STDIO);
	public static final PipeSink STDOUT = sink(() -> REALOUT, STDIO);
	public static final PipeSink STDERR = sink(() -> REALERR, STDIO);
	public static final PipeSink DEVNULL = sinkStream(() -> new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// do nothing
		}
	});

	public static PipeSink captureStdIn() {
		InputOutputStream bridge = new InputOutputStream();
		System.setIn(bridge.input());
		return sinkStream(() -> bridge.output(), unsafe(StandardPipes::closeIn));
	}
	
	public static PipeSource captureStdOut() {
		InputOutputStream bridge = new InputOutputStream();
		System.setOut(new PrintStream(bridge.output()));
		return sourceStream(() -> bridge.input(), unsafe(StandardPipes::closeOut));
	}

	public static PipeSource captureStdErr() {
		InputOutputStream bridge = new InputOutputStream();
		System.setErr(new PrintStream(bridge.output()));
		return sourceStream(() -> bridge.input(), unsafe(StandardPipes::closeErr));
	}

	private static void closeIn(Closeable capin) throws IOException {
		try {
			capin.close();
		} finally {
			System.setIn(REALIN);
		}
	}

	private static void closeOut(Closeable capout) throws IOException {
		try {
			capout.close();
		} finally {
			System.setOut(REALOUT);
		}
	}

	private static void closeErr(Closeable caperr) throws IOException {
		try {
			caperr.close();
		} finally {
			System.setErr(REALERR);
		}
	}

	public static Pipe pipe() {
		return new Pipe();
	}

	public static PipeSink sink(Path sink) {
		return sinkStream(unsafe(() -> Files.newOutputStream(sink)));
	}

	public static PipeSink sinkStream(Supplier<? extends OutputStream> sink) {
		return sink(() -> new PrintStream(sink.get()));
	}

	public static PipeSink sinkStream(Supplier<? extends OutputStream> sink, Consumer<? super PrintStream> closer) {
		return sink(() -> new PrintStream(sink.get()), closer);
	}

	public static PipeSink sink(Supplier<? extends PrintStream> sink) {
		return new PrintStreamSink(sink);
	}

	public static PipeSink sink(Supplier<? extends PrintStream> sink, Consumer<? super PrintStream> closer) {
		return new PrintStreamSink(sink, closer);
	}

	public static PipeSource source(Path source) {
		return source(unsafe(() -> Files.newBufferedReader(source)));
	}

	public static PipeSource source(Supplier<? extends BufferedReader> source) {
		return new BufferedReaderSource(source);
	}

	public static PipeSource source(Supplier<? extends BufferedReader> source,
			Consumer<? super BufferedReader> closer) {
		return new BufferedReaderSource(source, closer);
	}

	public static PipeSource sourceStream(Supplier<? extends InputStream> source) {
		return source(() -> new BufferedReader(new InputStreamReader(source.get())));
	}

	public static PipeSource sourceStream(Supplier<? extends InputStream> source,
			Consumer<? super BufferedReader> closer) {
		return source(() -> new BufferedReader(new InputStreamReader(source.get())), closer);
	}

	public static PipeSink transform(Function<String, String> transform, PipeSink sink) {
		return line -> sink.writeLine(transform.apply(line));
	}

	public static PipeSink split(Predicate<String> predicate, PipeSink trueSink, PipeSink falseSink) {
		return line -> {
			if (predicate.test(line)) {
				trueSink.writeLine(line);
			} else {
				falseSink.writeLine(line);
			}
		};
	}
}
