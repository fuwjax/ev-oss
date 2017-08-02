package org.fuwjax.oss.util.pipe;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.fuwjax.oss.util.CompositeException;

public class Pipe implements PipeSource, PipeSink {
	private List<PipeSink> sinks = Collections.emptyList();
	private List<PipeSource> sources = Collections.emptyList();
	private AtomicBoolean closing = new AtomicBoolean();
	
	private void init() {
		if (!sources.isEmpty() && !sinks.isEmpty()) {
			readLines(this::writeLine);
		}
	}

	public Pipe teeTo(PipeSink... targets) {
		assert this.sinks.isEmpty();
		this.sinks = asList(targets);
		for(PipeSink sink: sinks) {
			if(sink instanceof Pipe) {
				((Pipe)sink).addSource(this);
			}
		}
		init();
		return this;
	}

	public Pipe to(PipeSink sink) {
		return teeTo(sink);
	}

	private void addSink(Pipe sink) {
		if(sinks.isEmpty()) {
			sinks = new ArrayList<>();
		}
		sinks.add(sink);
	}

	@Override
	public void writeLine(String line) {
		for (PipeSink sink : sinks) {
			sink.writeLine(line);
		}
	}

	private void addSource(Pipe source) {
		if(sources.isEmpty()) {
			sources = new ArrayList<>();
		}
		sources.add(source);
	}

	public Pipe from(PipeSource source) {
		return mergeFrom(source);
	}

	public Pipe mergeFrom(PipeSource... pubs) {
		assert this.sources.isEmpty();
		this.sources = Arrays.asList(pubs);
		for(PipeSource source: sources) {
			if(source instanceof Pipe) {
				((Pipe)source).addSink(this);
			}
		}
		init();
		return this;
	}

	@Override
	public void readLines(Consumer<String> handler) {
		for (PipeSource source : sources) {
			source.readLines(handler);
		}
	}

	@Override
	public void close() {
		if (closing.compareAndSet(false, true)) {
			CompositeException ex = new CompositeException("Could not close pipe");
			for (PipeSource source : sources) {
				ex.accept(AutoCloseable::close, source);
			}
			for (PipeSink sink : sinks) {
				ex.accept(AutoCloseable::close, sink);
			}
		}
	}
}
