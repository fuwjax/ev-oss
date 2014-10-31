package org.echovantage.gild.proxy;

import static org.echovantage.util.Assert2.assertCompletes;

import java.nio.file.Files;
import java.nio.file.Path;

import org.echovantage.util.Files2;
import org.echovantage.util.ReadOnlyPath;

public class FileSystemProxy extends AbstractServiceProxy {
	private Path working;

	public FileSystemProxy() {
		// must call setWorkingDirectory
	}

	public FileSystemProxy(final Path working) {
		setWorkingDirectory(working);
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws Exception {
		Files2.delete(working);
		Files.createDirectories(working);
		input.copyTo(working);
	}

	@Override
	protected boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception {
		Files.createDirectories(output);
		Files2.copy(working, output);
		return true;
	}

	public void setWorkingDirectory(final Path working) {
		this.working = working;
		assertCompletes(this::configured);
	}
}
