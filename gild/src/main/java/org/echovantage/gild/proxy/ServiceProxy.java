package org.echovantage.gild.proxy;

import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public interface ServiceProxy {
	void prepare(ReadOnlyPath input, Path output);

	void preserve(Path output, ReadOnlyPath golden);
}
