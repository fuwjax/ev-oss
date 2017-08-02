package org.fuwjax.oss.util.pipe;

public interface PipeSink extends AutoCloseable{
	public void writeLine(String line);
	
	@Override
	default public void close() throws Exception {
		// do nothing by default
	}
}
