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
//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.fuwjax.oss.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.DeferredContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.CountingCallback;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.IteratingCallback;
import org.fuwjax.oss.proxy.Dialog.Headers;
import org.fuwjax.oss.util.Log;

/**
 * <p>Servlet 3.1 asynchronous proxy servlet with capability to intercept and
 * modify request/response content.</p> <p>Both the request processing and the
 * I/O are asynchronous.</p>
 *
 */
public class AsyncMiddleManHandler extends AbstractHandler {
	private static final String PROXY_REQUEST_COMMITTED = AsyncMiddleManHandler.class.getName() + ".proxyRequestCommitted";
	private static final String CLIENT_TRANSFORMER = AsyncMiddleManHandler.class.getName() + ".clientTransformer";
	private static final String SERVER_TRANSFORMER = AsyncMiddleManHandler.class.getName() + ".serverTransformer";
	private static final String WRITE_LISTENER_ATTRIBUTE = AsyncMiddleManHandler.class.getName() + ".writeListener";

	protected Log _log;
	private String _hostHeader;
	private String _viaHost;
	private HttpClient _client;
	private long _timeout;

	public AsyncMiddleManHandler(ProxyConfig config) {
		_log = config.getLog();
		_hostHeader = config.hostHeader();
		_timeout = config.timeout(60000L);
		_viaHost = config.viaHost(viaHost());
		_client = config.createClient();
	}

	@Override
	public void doStart() throws ServletException {
		// Redirects must be proxied as is, not followed.
        _client.setFollowRedirects(false);
        // Must not store cookies, otherwise cookies of different clients will mix.
        _client.setCookieStore(new HttpCookieStore.Empty());

		try {
			_client.start();
			// Content must not be decoded, otherwise the client gets confused.
			_client.getContentDecoderFactories().clear();
			// No protocol handlers, pass everything to the client.
			_client.getProtocolHandlers().clear();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		try {
			_client.stop();
		} catch (Exception x) {
			_log.warning(x, "client failed to stop");
		}
	}

	private static String viaHost() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException x) {
			return "localhost";
		}
	}
	
	public interface DialogStrategy{
		String target();
		
		ContentTransformer requestTransform();
		
		ContentTransformer responseTransform();
		
		void modifyRequestHeaders(Headers headers);
		
		void modifyResponseHeaders(Headers headers);
	}

	protected DialogStrategy strategy(Dialog dialog){
		return null;
	}
	
	@Override
	public void handle(String requestTarget, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest clientRequest, HttpServletResponse proxyResponse)
			throws ServletException, IOException {
		Dialog dialog = new Dialog(_log, clientRequest, proxyResponse);
		DialogStrategy strategy = strategy(dialog);
		if(strategy == null || !dialog.createServerRequest(strategy.target(), _client, _hostHeader, _viaHost, _timeout)){
			return;
		}
		strategy.modifyRequestHeaders(dialog.proxyRequestHeaders());

		// If there is content, the send of the proxy request
		// is delayed and performed when the content arrives,
		// to allow optimization of the Content-Length header.
		if (dialog.hasRequestContent()){
			DeferredContentProvider content = new DeferredContentProvider() {
				@Override
				public boolean offer(ByteBuffer buffer, Callback callback) {
					_log.debug("{} proxying content to upstream: {} bytes", dialog.id(), buffer.remaining());
					return super.offer(buffer, callback);
				}
			};
			dialog.transferRequestContent(new ProxyReader(dialog, strategy, content), content);
		}else{
			dialog.send(new ProxyResponseListener(dialog, strategy));
		}
	}

	private static void write(OutputStream output, ByteBuffer content) throws IOException {
		int length = content.remaining();
		int offset = 0;
		byte[] buffer;
		if (content.hasArray()) {
			offset = content.arrayOffset();
			buffer = content.array();
		} else {
			buffer = new byte[length];
			content.get(buffer);
		}
		output.write(buffer, offset, length);
	}

	private class ProxyReader extends IteratingCallback implements ReadListener {
		private final byte[] buffer = new byte[_client.getRequestBufferSize()];
		private final List<ByteBuffer> buffers = new ArrayList<>();
		private final int contentLength;
		private int length;
		DeferredContentProvider provider;
		private Dialog dialog;
		private DialogStrategy strategy;
		
		protected ProxyReader(Dialog dialog, DialogStrategy strategy, DeferredContentProvider provider) {
			this.dialog = dialog;
			this.strategy = strategy;
			this.provider = provider;
			this.contentLength = dialog.getRequestContentLength();
		}
		
		@Override
		public void onDataAvailable() throws IOException {
			iterate();
		}

		@Override
		public void onAllDataRead() throws IOException {
			// succeeded?
			if (!provider.isClosed()) {
				process(BufferUtil.EMPTY_BUFFER, new Callback(){
					@Override
					public void failed(Throwable x) {
						dialog.abort(x);
					}
				}, true);
			}

			_log.debug("{} proxying content to upstream completed", dialog.id());
			super.close();
		}

		@Override
		public void onError(Throwable t) {
			dialog.abort(t);
		}
		
		@Override
		protected Action process() throws Exception {
			ServletInputStream input = dialog.requestInputStream();
			while (input.isReady() && !input.isFinished()) {
				int read = input.read(buffer);

				_log.debug("{} asynchronous read {} bytes on {}", dialog.id(), read, input);

				if (read < 0)
					return Action.SUCCEEDED;

				if (contentLength > 0 && read > 0)
					length += read;

				ByteBuffer content = read > 0 ? ByteBuffer.wrap(buffer, 0, read) : BufferUtil.EMPTY_BUFFER;
				boolean finished = length == contentLength;
				process(content, this, finished);

				if (read > 0)
					return Action.SCHEDULED;
			}

			if (input.isFinished()) {
				_log.debug("{} asynchronous read complete on {}", dialog.id(), input);
				return Action.SUCCEEDED;
			} else {
				_log.debug("{} asynchronous read pending on {}", dialog.id(), input);
				return Action.IDLE;
			}
		}

		private void process(ByteBuffer content, Callback callback, boolean finished) throws IOException {
			ContentTransformer transformer = dialog.attribute(CLIENT_TRANSFORMER, strategy::requestTransform);

			boolean committed = dialog.hasAttribute(PROXY_REQUEST_COMMITTED);

			int contentBytes = content.remaining();

			// Skip transformation for empty non-last buffers.
			if (contentBytes == 0 && !finished) {
				callback.succeeded();
				return;
			}

			try {
				transformer.transform(content, finished, buffers);
			} catch (Throwable x) {
				_log.info("Exception while transforming " + transformer, x);
				throw x;
			}

			int newContentBytes = 0;
			int size = buffers.size();
			if (size > 0) {
				CountingCallback counter = new CountingCallback(callback, size);
				for (int i = 0; i < size; ++i) {
					ByteBuffer buffer = buffers.get(i);
					newContentBytes += buffer.remaining();
					provider.offer(buffer, counter);
				}
				buffers.clear();
			}

			if (finished)
				provider.close();

			_log.debug("{} upstream content transformation {} -> {} bytes", dialog.id(), contentBytes, newContentBytes);

			if (!committed && (size > 0 || finished)) {
				dialog.requestHeader(HttpHeader.CONTENT_LENGTH, null);
				dialog.attribute(PROXY_REQUEST_COMMITTED, () -> true);
				dialog.send(new ProxyResponseListener(dialog, strategy));
			}

			if (size == 0)
				callback.succeeded();
		}

		@Override
		protected void onCompleteFailure(Throwable x) {
			dialog.abort(x);
		}
	}
	
	private class ProxyResponseCallback implements Callback {
			private Dialog dialog;

			public ProxyResponseCallback(Dialog dialog) {
				this.dialog = dialog;
			}

			@Override
			public void succeeded() {
				dialog.succeeded();
			}

			@Override
			public void failed(Throwable failure) {
				dialog.failed(failure);
			}
	}

	private class ProxyResponseListener extends Response.Listener.Adapter {
		private final List<ByteBuffer> buffers = new ArrayList<>();
		private boolean hasContent;
		private long length;
		private final Callback complete;
		private DialogStrategy strategy;
		private Dialog dialog;

		protected ProxyResponseListener(Dialog dialog, DialogStrategy strategy) {
			this.dialog = dialog;
			this.strategy = strategy;
			this.complete = new CountingCallback(new ProxyResponseCallback(dialog), 2);
		}

		@Override
		public void onBegin(Response serverResponse) {
			dialog.setResponse(serverResponse);
		}

		@Override
		public void onHeaders(Response serverResponse) {
			dialog.initResponseHeaders();
			strategy.modifyResponseHeaders(dialog.proxyResponseHeaders());
		}

		@Override
		public void onContent(final Response serverResponse, ByteBuffer content, final Callback callback) {
			try {
				int contentBytes = content.remaining();
				_log.debug("{} received server content: {} bytes", dialog.id(), contentBytes);

				hasContent = true;

				boolean committed = dialog.hasAttribute(WRITE_LISTENER_ATTRIBUTE);
				length += contentBytes;
				boolean finished = length == dialog.getResponseContentLength();

				ProxyWriter proxyWriter = write(content, finished, callback);
				
				if (finished)
					proxyWriter.offer(BufferUtil.EMPTY_BUFFER, complete);

				if (committed) {
					proxyWriter.onWritePossible();
				} else {
					dialog.write(proxyWriter);
				}
			} catch (Throwable x) {
				callback.failed(x);
			}
		}

		private ProxyWriter write(ByteBuffer content, boolean finished, Callback callback) throws Throwable {
			int contentBytes = content.remaining();
			ProxyWriter proxyWriter = dialog.attribute(WRITE_LISTENER_ATTRIBUTE, () -> new ProxyWriter(dialog));				
			ContentTransformer transformer = dialog.attribute(SERVER_TRANSFORMER, strategy::responseTransform);
			try {
				transformer.transform(content, finished, buffers);
			} catch (IOException x) {
				_log.info("Exception while transforming " + transformer, x);
				throw x;
			}

			int newContentBytes = 0;
			int size = buffers.size();
			if (size > 0) {
				Callback counter = size == 1 ? callback : new CountingCallback(callback, size);
				for (int i = 0; i < size; ++i) {
					ByteBuffer buffer = buffers.get(i);
					newContentBytes += buffer.remaining();
					proxyWriter.offer(buffer, counter);
				}
				buffers.clear();
			} else {
				proxyWriter.offer(BufferUtil.EMPTY_BUFFER, callback);
			}
			
			_log.debug("{} downstream content transformation {} -> {} bytes", dialog.id(), contentBytes, newContentBytes);

			return proxyWriter;
		}

		@Override
		public void onSuccess(final Response serverResponse) {
			try {
				if (hasContent) {
					// If we had unknown length content, we need to call the
					// transformer to signal that the content is finished.
					if (dialog.getResponseContentLength() < 0) {
						ProxyWriter proxyWriter = write(BufferUtil.EMPTY_BUFFER, true, complete);
						proxyWriter.onWritePossible();
					}
				} else {
					complete.succeeded();
				}
			} catch (Throwable x) {
				complete.failed(x);
			}
		}

		@Override
		public void onComplete(Result result) {
			if (result.isSucceeded())
				complete.succeeded();
			else
				complete.failed(result.getFailure());
		}
	}

	private class ProxyWriter implements WriteListener {
		private final Queue<DeferredContentProvider.Chunk> chunks = new ArrayDeque<>();
		private DeferredContentProvider.Chunk chunk;
		private boolean writePending;
		private Dialog dialog;

		protected ProxyWriter(Dialog dialog) {
			this.dialog = dialog;
		}

		public boolean offer(ByteBuffer content, Callback callback) {
			_log.debug("{} proxying content to downstream: {} bytes {}", dialog.id(), content.remaining(), callback);
			return chunks.offer(new DeferredContentProvider.Chunk(content, callback));
		}

		// Succeeding the callback may cause to reenter in onWritePossible()
		// because typically the callback is the one that controls whether the
		// content received from the server has been consumed, so succeeding
		// the callback causes more content to be received from the server,
		// and hence more to be written to the client by onWritePossible().
		// A reentrant call to onWritePossible() performs another write,
		// which may remain pending, which means that the reentrant call
		// to onWritePossible() returns all the way back to just after the
		// succeed of the callback. There, we cannot just loop attempting
		// write, but we need to check whether we are write pending.
		@Override
		public void onWritePossible() throws IOException {
			ServletOutputStream output = dialog.responseOutputStream();

			// If we had a pending write, let's succeed it.
			if (writePending) {
				_log.debug("{} pending async write complete of {} on {}", dialog.id(), chunk, output);
				writePending = false;
				chunk.callback.succeeded();
			}

			int length = 0;
			DeferredContentProvider.Chunk chunk = null;
			while (output.isReady()) {
				if (chunk != null) {
					_log.debug("{} async write complete of {} ({} bytes) on {}", dialog.id(), chunk, length, output);
					chunk.callback.succeeded();
					if(writePending)
						return;
				}

				this.chunk = chunk = chunks.poll();
				if (chunk == null)
					return;

				length = chunk.buffer.remaining();
				if (length > 0)
					write(output, chunk.buffer);
			}

			_log.debug("{} async write pending of {} ({} bytes) on {}", dialog.id(), chunk, length, output);
			writePending = true;
		}

		@Override
		public void onError(Throwable failure) {
			if (chunk != null)
				chunk.callback.failed(failure);
			else
				dialog.abort(failure);
		}
	}
}
