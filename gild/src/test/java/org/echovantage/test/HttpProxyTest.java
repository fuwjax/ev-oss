/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.test;

import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.echovantage.gild.Gild;
import org.echovantage.gild.proxy.http.HttpClientProxy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class HttpProxyTest {
	private final HttpClientProxy http = new HttpClientProxy("localhost", 8080);
	@Rule
	public final Gild gild = new Gild().with("http", http);
	private static Undertow server;

	@BeforeClass
	public static void setupServer() {
		server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(new HttpHandler() {
			@Override
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				final StringBuilder responseBody = new StringBuilder();
				if (exchange.getQueryString().isEmpty()) {
					responseBody.append(exchange.getRequestURI()).append('\n');
				} else {
					responseBody.append(exchange.getRequestURI() + "?" + exchange.getQueryString()).append('\n');
				}
				exchange.getRequestHeaders().forEach((h) -> {
					responseBody.append(h.getHeaderName()).append(": ");
					responseBody.append(h.stream().collect(joining(", ")));
					responseBody.append('\n');
				});
				responseBody.append("----\n");
				exchange.startBlocking();
				final InputStream is = exchange.getInputStream();
				String charsetName = exchange.getRequestCharset();
				if (charsetName == null) {
					charsetName = "UTF-8";
				}
				final Reader r = new InputStreamReader(is, Charset.forName(charsetName));
				final CharBuffer cbuf = CharBuffer.allocate(4096);
				while (true) {
					final int numChars = r.read(cbuf);
					if (numChars < 0) {
						break;
					}
					cbuf.flip();
					responseBody.append(cbuf);
					cbuf.clear();
				}

				exchange.getResponseSender().send(responseBody.toString());
			}
		}).build();
		server.start();
	}

	@AfterClass
	public static void stopServer() {
		server.stop();
	}

	@Test
	public void request() {
		http.send();
	}
}
