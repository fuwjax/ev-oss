package org.echovantage.gild.proxy;

import static org.echovantage.util.Assert2.assertCompletes;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;

import org.echovantage.util.ReadOnlyPath;

public abstract class AbstractServiceProxy implements ServiceProxy {
	private ReadOnlyPath bufferedInput;

	@Override
	public final void prepare(final ReadOnlyPath input) throws Exception {
		if(isReady()) {
			prepareImpl(input);
		} else {
			assertNull("Proxy has not been made ready", bufferedInput);
			bufferedInput = input;
		}
	}

	@Override
	public final void preserve(final Path output, final ReadOnlyPath golden) throws Exception {
		assertNull("Proxy has not been made ready", bufferedInput);
		preserveImpl(output, golden);
	}

	protected abstract void preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception;

	protected abstract boolean isReady();

	protected final void checkReady() {
		if(bufferedInput != null && isReady()) {
			final ReadOnlyPath input = bufferedInput;
			bufferedInput = null;
			assertCompletes(() -> prepareImpl(input));
		}
	}

	protected final void checkNotReady() {
		assertFalse("Cannot change properties after proxy is made ready", bufferedInput != null && isReady());
	}

	protected abstract void prepareImpl(ReadOnlyPath input) throws Exception;
}
