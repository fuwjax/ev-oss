package org.fuwjax.parser;

import java.io.IOException;

import org.fuwjax.oss.util.io.IntReader;

public interface Grammar {
	Object parse(IntReader reader) throws IOException;
}
