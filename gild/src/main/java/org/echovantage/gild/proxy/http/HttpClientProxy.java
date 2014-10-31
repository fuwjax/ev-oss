package org.echovantage.gild.proxy.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.echovantage.gild.proxy.AbstractServiceProxy;
import org.echovantage.util.ReadOnlyPath;

public class HttpClientProxy extends AbstractServiceProxy {
	private final String host;
	private final int port;

	public HttpClientProxy(final int port) {
		this("localhost", port);
	}

	public HttpClientProxy(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public void send() {
		configured();
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws Exception {
		Files.createDirectories(output);
		List<ReadOnlyPath> paths = new ArrayList<>();
		try(DirectoryStream<ReadOnlyPath> stream = input.newDirectoryStream()) {
			for(ReadOnlyPath test : stream) {
				paths.add(test);
			}
		}
		Collections.sort(paths);
		for(final ReadOnlyPath file : paths) {
			try(final Socket socket = new Socket(host, port);
					OutputStream out = socket.getOutputStream();
					InputStream in = socket.getInputStream();
					InputStream req = file.newInputStream();
					InputStream hin = new HttpResponseInputStream(in, req)) {
				file.copyTo(out);
				Files.copy(hin, output.resolve(file.getFileName()));
			}
		}
	}

	@Override
	protected boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception {
		// preserved in prepare
		return false;
	}
}
