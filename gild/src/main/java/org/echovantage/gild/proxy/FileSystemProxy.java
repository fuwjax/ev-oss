package org.echovantage.gild.proxy;

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
		this.working = working;
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input) throws Exception {
		Files.createDirectories(working);
		Files2.delete(working);
		input.copyTo(working);
	}

	@Override
	protected void preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception {
		Files.createDirectories(output);
		Files2.copy(working, output);
	}

	@Override
	protected boolean isReady() {
		return working != null;
	}

	public void setWorkingDirectory(final Path working) {
		checkNotReady();
		this.working = working;
		checkReady();
	}
}
