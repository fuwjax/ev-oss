/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.gild.proxy.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fuwjax.oss.gild.proxy.AbstractServiceProxy;
import org.fuwjax.oss.util.io.ReadOnlyPath;

public class SocketHttpProxy extends AbstractServiceProxy {
	private final String host;
	private final int port;

	public SocketHttpProxy(final int port) {
		this("localhost", port);
	}

	public SocketHttpProxy(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public void send() {
		configured();
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws Exception {
		Files.createDirectories(output);
		final List<ReadOnlyPath> paths = new ArrayList<>();
		try(DirectoryStream<ReadOnlyPath> stream = input.newDirectoryStream()) {
			for(final ReadOnlyPath test : stream) {
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
