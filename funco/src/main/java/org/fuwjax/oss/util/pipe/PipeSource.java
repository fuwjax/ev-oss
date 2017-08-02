package org.fuwjax.oss.util.pipe;

import java.util.function.Consumer;

public interface PipeSource extends AutoCloseable {
	public void readLines(Consumer<String> handler);
	
	@Override
	default public void close() throws Exception {
		// do nothing by default
	}
}
