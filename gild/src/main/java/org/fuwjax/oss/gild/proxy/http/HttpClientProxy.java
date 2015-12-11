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

import static java.nio.file.Files.newBufferedWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;

import org.fuwjax.oss.gild.proxy.AbstractServiceProxy;
import org.fuwjax.oss.util.io.ReadOnlyPath;

import jodd.http.HttpConnection;
import jodd.http.HttpException;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.net.SocketHttpConnection;
import jodd.http.net.SocketHttpConnectionProvider;
import jodd.http.net.SocketHttpSecureConnection;

public class HttpClientProxy extends AbstractServiceProxy {
	private static final DateFormat TIME_INSTANCE = new SimpleDateFormat("mm:ss.SSS");
	private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("(?<method>[\\S]+)\\s(?<path>[\\S]+)\\s(?<version>[\\S]+)");
	private SocketHttpConnectionProvider sockets;

	public HttpClientProxy(final int port) {
		this("localhost", port);
	}

	public HttpClientProxy(final String host, final int port) {
		sockets = new SocketHttpConnectionProvider(){
			@Override
			public HttpConnection createHttpConnection(HttpRequest httpRequest) throws IOException {
				SocketHttpConnection httpConnection;
				final boolean https = "https".equalsIgnoreCase(httpRequest.protocol());
				if (https) {
					SSLSocket sslSocket = createSSLSocket(host, port);
					httpConnection = new SocketHttpSecureConnection(sslSocket);
				} else {
					Socket socket = createSocket(host, port);
					httpConnection = new SocketHttpConnection(socket);
				}
				httpConnection.setTimeout(httpRequest.timeout());
				try {
					httpConnection.init();
					return httpConnection;
				}
				catch (Throwable thex) {
					httpConnection.close();
					throw new HttpException(thex);
				}
			}
		};
	}

	public void send() {
		configured();
	}

	@Override
	protected boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws Exception {
		configured();
		return false;
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws Exception {
		Files.createDirectories(output);
		final List<ReadOnlyPath> paths = new ArrayList<>();
		try (DirectoryStream<ReadOnlyPath> stream = input.newDirectoryStream()) {
			for (final ReadOnlyPath test : stream) {
				paths.add(test);
			}
		}
		Collections.sort(paths);
		for (final ReadOnlyPath file : paths) {
			final HttpRequest request = buildRequest(file);
			request.open(sockets);
			long start = System.currentTimeMillis();
			final HttpResponse response = request.send();
			System.out.println(file.getFileName()+"["+request.path()+"]: "+TIME_INSTANCE.format(System.currentTimeMillis() - start));
			persistResponse(response, output.resolve(file.getFileName()));
		}
	}

	private void persistResponse(final HttpResponse response, final Path output) throws IOException {
		try (BufferedWriter w = newBufferedWriter(output, StandardCharsets.UTF_8)) {
			w.append(response.httpVersion()).append(' ').append(Integer.toString(response.statusCode())).append(' ').append(response.statusPhrase()).append('\n');
			final Map<String, List<String>> sortedHeaders = new TreeMap<>();
			response.headers().forEach(e -> sortedHeaders.computeIfAbsent(e.getKey().toLowerCase(), k -> new ArrayList<>()).add(e.getValue()));
			for (final Map.Entry<String, List<String>> entry : sortedHeaders.entrySet()) {
				final String headerName = entry.getKey();
				if (headerName.equals("date")) {
					w.append("date: ${DATE}\n");
				} else {
					for (final String value : entry.getValue()) {
						w.append(headerName).append(": ").append(value).append('\n');
					}
				}
			}
			w.append('\n');
			final String body = response.bodyText();
			if (body != null) {
				w.append(body);
			}
		}
	}

	private HttpRequest buildRequest(final ReadOnlyPath file) throws IOException {
		try (InputStream is = file.newInputStream(); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(r)) {
			final HttpRequest request = new HttpRequest();
			request.removeHeader("Connection");
			String line = br.readLine();
			parseRequestLine(line, request);
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					break;
				}
				parseHeader(line, request);
			}
			appendBody(br, request);
			return request;
		}
	}

	private void appendBody(final BufferedReader br, final HttpRequest request) throws IOException {
		final StringBuilder body = new StringBuilder();
		final char[] cbuf = new char[4096];
		int numChars;
		while (true) {
			numChars = br.read(cbuf);
			if (numChars > -1) {
				body.append(cbuf, 0, numChars);
			} else {
				break;
			}
		}
		request.body(body.toString());
	}

	private void parseHeader(final String line, final HttpRequest request) {
		final String[] header = line.split(":", 2);
		request.header(header[0], header[1]);
	}

	private void parseRequestLine(final String line, final HttpRequest request) {
		final Matcher m = REQUEST_LINE_PATTERN.matcher(line);
		if (!m.matches()) {
			throw new IllegalArgumentException("Request line must be formatted as 'METHOD URI VERSION'. was '" + line + "'");
		}
		request.method(m.group("method"));
		request.path(m.group("path"));
		if(m.group("version").startsWith("HTTPS")){
			request.httpVersion(m.group("version").replaceFirst("(?i)HTTPS","HTTP"));
			request.protocol("HTTPS");
		}else{
			request.httpVersion(m.group("version"));
		}
	}
}
