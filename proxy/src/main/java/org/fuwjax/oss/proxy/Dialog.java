package org.fuwjax.oss.proxy;

import static java.lang.System.identityHashCode;
import static java.util.Arrays.asList;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toCollection;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.util.component.Destroyable;
import org.fuwjax.oss.util.Log;

public class Dialog {
	private static final Set<String> HOP_HEADERS;

	static {
		Set<String> hopHeaders = new HashSet<>();
		hopHeaders.add("connection");
		hopHeaders.add("keep-alive");
		hopHeaders.add("proxy-authorization");
		hopHeaders.add("proxy-authenticate");
		hopHeaders.add("proxy-connection");
		hopHeaders.add("transfer-encoding");
		hopHeaders.add("te");
		hopHeaders.add("trailer");
		hopHeaders.add("upgrade");
		HOP_HEADERS = Collections.unmodifiableSet(hopHeaders);
	}

	private HttpServletRequest clientRequest;
	private HttpServletResponse proxyResponse;
	private Request proxyRequest;
	private Response serverResponse;
	private Log log;
	private long responseContentLength;
	private Set<String> attributes = new HashSet<>();

	public Dialog(Log log, HttpServletRequest clientRequest, HttpServletResponse proxyResponse) {
		this.log = log;
		this.clientRequest = clientRequest;
		this.proxyResponse = proxyResponse;
	}

	public String target() {
		StringBuffer target = clientRequest.getRequestURL();
		String query = clientRequest.getQueryString();
		if (query != null)
			target.append("?").append(query);
		return target.toString();
	}

	private void sendProxyResponseError(int status) {
		proxyResponse.setStatus(status);
		proxyResponse.setHeader(HttpHeader.CONNECTION.asString(), HttpHeaderValue.CLOSE.asString());
		if (clientRequest.isAsyncStarted())
			clientRequest.getAsyncContext().complete();
	}

	public boolean createServerRequest(String rewrittenTarget, HttpClient client, String hostHeader, String viaName, long timeout) {
		log.debug("{} rewriting: {} -> {}", id(), Log.defer(this::target), rewrittenTarget);
		if (rewrittenTarget == null) {
			sendProxyResponseError(HttpStatus.FORBIDDEN_403);
			return false;
		}
		proxyRequest = client.newRequest(rewrittenTarget).method(clientRequest.getMethod()).version(HttpVersion.fromString(clientRequest.getProtocol()));
		initRequestHeaders(hostHeader, viaName);
		
		final AsyncContext asyncContext = clientRequest.startAsync();
		// We do not timeout the continuation, but the proxy request.
		asyncContext.setTimeout(0);
		proxyRequest.timeout(timeout, TimeUnit.MILLISECONDS);

		return true;
	}

	protected void initRequestHeaders(String hostHeader, String viaName) {
		// First clear possibly existing headers, as we are going to copy those from the client request.
		proxyRequest.getHeaders().clear();

		Set<String> ignore = list(clientRequest.getHeaders(HttpHeader.CONNECTION.asString())).stream()
				.flatMap(v -> asList(v.trim().split("\\s*,\\s*")).stream().map(s -> s.toLowerCase(Locale.ENGLISH)))
				.collect(toCollection(HashSet::new));
		ignore.addAll(HOP_HEADERS);
		if(hostHeader != null){
			ignore.add(HttpHeader.HOST.asString());
		}
		list(clientRequest.getHeaderNames()).stream().filter(v -> !ignore.contains(v.toLowerCase(Locale.ENGLISH)))
			.forEach(name -> list(clientRequest.getHeaders(name)).stream().filter(v -> v != null).forEach(value -> proxyRequest.header(name, value)));

		// Force the Host header if configured
		if (hostHeader != null)
			proxyRequest.header(HttpHeader.HOST, hostHeader);
		
		// add proxy headers
		proxyRequest.header(HttpHeader.VIA, "http/1.1 " + viaName);
		proxyRequest.header(HttpHeader.X_FORWARDED_FOR, clientRequest.getRemoteAddr());
		proxyRequest.header(HttpHeader.X_FORWARDED_PROTO, clientRequest.getScheme());
		proxyRequest.header(HttpHeader.X_FORWARDED_HOST, clientRequest.getHeader(HttpHeader.HOST.asString()));
		proxyRequest.header(HttpHeader.X_FORWARDED_SERVER, clientRequest.getLocalName());
	}

	protected boolean hasRequestContent() {
		return clientRequest.getContentLength() > 0 || clientRequest.getContentType() != null || clientRequest.getHeader(HttpHeader.TRANSFER_ENCODING.asString()) != null;
	}

	private String requestString() {
		StringBuilder builder = new StringBuilder(clientRequest.getMethod());
		builder.append(" ").append(clientRequest.getRequestURI());
		String query = clientRequest.getQueryString();
		if (query != null)
			builder.append("?").append(query);
		builder.append(" ").append(clientRequest.getProtocol()).append(System.lineSeparator());
		for (Enumeration<String> headerNames = clientRequest.getHeaderNames(); headerNames.hasMoreElements();) {
			String headerName = headerNames.nextElement();
			builder.append(headerName).append(": ");
			for (Enumeration<String> headerValues = clientRequest.getHeaders(headerName); headerValues.hasMoreElements();) {
				String headerValue = headerValues.nextElement();
				if (headerValue != null)
					builder.append(headerValue);
				if (headerValues.hasMoreElements())
					builder.append(",");
			}
			builder.append(System.lineSeparator());
		}
		builder.append(System.lineSeparator());
		return builder.toString();
	}
	
	private String responseString(){
		StringBuilder builder = new StringBuilder(System.lineSeparator());
		builder.append(clientRequest.getProtocol()).append(" ").append(proxyResponse.getStatus()).append(" ").append(serverResponse.getReason())
				.append(System.lineSeparator());
		for (String headerName : proxyResponse.getHeaderNames()) {
			builder.append(headerName).append(": ");
			for (Iterator<String> headerValues = proxyResponse.getHeaders(headerName).iterator(); headerValues.hasNext();) {
				String headerValue = headerValues.next();
				if (headerValue != null)
					builder.append(headerValue);
				if (headerValues.hasNext())
					builder.append(",");
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}
	
	private void cleanup(HttpServletRequest clientRequest) {
		attributes.forEach(name -> {
			Object value = clientRequest.getAttribute(name);
			if (value instanceof Destroyable)
				((Destroyable) value).destroy();
			else if(value instanceof AutoCloseable){
				try {
					((AutoCloseable)value).close();
				} catch (Exception e) {
					log.warning(e, "Error while closing %s: %s", value.getClass().getCanonicalName(), value);
				}
			}
		});
	}
	
	public void abort(Throwable t){
		cleanup(clientRequest);
		boolean aborted = proxyRequest.abort(t);
		if (!aborted) {
			int status = t instanceof TimeoutException ? HttpStatus.REQUEST_TIMEOUT_408 : HttpStatus.INTERNAL_SERVER_ERROR_500;
			sendProxyResponseError(status);
		}
	}

	public int getRequestContentLength() {
		return clientRequest.getContentLength();
	}
	
	public int id(){
		return identityHashCode(clientRequest);
	}

	public ServletInputStream requestInputStream() throws IOException {
		return clientRequest.getInputStream();
	}

	public <T> T attribute(String name, Supplier<T> factory){
		T value = (T)clientRequest.getAttribute(name);
		if(value == null){
			value = factory.get();
			clientRequest.setAttribute(name, value);
			attributes.add(name);
		}
		return value;
	}
	
	public void initResponseHeaders() {
		responseContentLength = serverResponse.getHeaders().getLongField(HttpHeader.CONTENT_LENGTH.asString());
		for (HttpField field : serverResponse.getHeaders()) {
			String headerName = field.getName();
			String lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH);
			if (HOP_HEADERS.contains(lowerHeaderName))
				continue;
		
			String newHeaderValue = field.getValue();
			if (newHeaderValue == null || newHeaderValue.trim().length() == 0)
				continue;
		
			proxyResponse.addHeader(headerName, newHeaderValue);
		}
		
		log.debug("{} proxying to downstream:{}{}{}{}{}", id(), System.lineSeparator(), serverResponse, System.lineSeparator(),
				serverResponse.getHeaders().toString().trim(), System.lineSeparator(), Log.defer(this::responseString));
		
	}
	
	public void setResponse(Response serverResponse) {
		this.serverResponse = serverResponse;
		proxyResponse.setStatus(serverResponse.getStatus());
	}

	public void succeeded() {
		cleanup(clientRequest);
		log.debug("{} proxying successful", id());
		
		AsyncContext asyncContext = clientRequest.getAsyncContext();
		asyncContext.complete();		
	}

	public void failed(Throwable failure) {
		cleanup(clientRequest);
		log.debug(id() + " proxying failed", failure);
		
		if (proxyResponse.isCommitted()) {
			try {
				// Use Jetty specific behavior to close connection.
				proxyResponse.sendError(-1);
				AsyncContext asyncContext = clientRequest.getAsyncContext();
				asyncContext.complete();
			} catch (IOException x) {
				log.debug(id() + " could not close the connection", failure);
			}
		} else {
			proxyResponse.resetBuffer();
			int status = failure instanceof TimeoutException ? HttpStatus.GATEWAY_TIMEOUT_504 : HttpStatus.BAD_GATEWAY_502;
			sendProxyResponseError(status);
		}		
	}

	public void write(WriteListener proxyWriter) throws IOException {
		if (responseContentLength >= 0)
			proxyResponse.setContentLength(-1);

		// Setting the WriteListener triggers an invocation to
		// onWritePossible(), possibly on a different thread.
		// We cannot succeed the callback from here, otherwise
		// we run into a race where the different thread calls
		// onWritePossible() and succeeding the callback causes
		// this method to be called again, which also may call
		// onWritePossible().
		proxyResponse.getOutputStream().setWriteListener(proxyWriter);		
	}

	public ServletOutputStream responseOutputStream() throws IOException {
		return proxyResponse.getOutputStream();
	}

	public boolean hasAttribute(String name) {
		return clientRequest.getAttribute(name) != null;
	}

	public void transferRequestContent(ReadListener reader, ContentProvider content) throws IOException {
		clientRequest.getInputStream().setReadListener(reader);
		proxyRequest.content(content);
		
	}

	public void send(CompleteListener listener) {
		log.debug("{} proxying to upstream:{}{}{}{}", id(), System.lineSeparator(), Log.defer(this::requestString), proxyRequest, System.lineSeparator(), proxyRequest.getHeaders().toString().trim());
		proxyRequest.send(listener);		
	}

	public void requestHeader(HttpHeader name, String value) {
		proxyRequest.header(name, value);
	}

	public long getResponseContentLength() {
		return responseContentLength;
	}

	public String method() {
		return clientRequest.getMethod();
	}

	public interface Headers{
		String get(String name);
		Collection<String> getAll(String name);
		Collection<String> names();
		void set(String name, String value);
		void add(String name, String value);
		void clear(String name);
	}
	
	public Headers clientRequestHeaders() {
		return new Headers(){
			@Override
			public String get(String name) {
				return clientRequest.getHeader(name);
			}

			@Override
			public List<String> getAll(String name) {
				return list(clientRequest.getHeaders(name));
			}

			@Override
			public Collection<String> names() {
				return list(clientRequest.getHeaderNames());
			}

			@Override
			public void set(String name, String value) {
				throw new UnsupportedOperationException("Cannot modify client request headers");
			}

			@Override
			public void add(String name, String value) {
				throw new UnsupportedOperationException("Cannot modify client request headers");
			}

			@Override
			public void clear(String name) {
				throw new UnsupportedOperationException("Cannot modify client request headers");
			}
			
		};
	}

	public Headers proxyRequestHeaders() {
		return new Headers(){
			@Override
			public String get(String name) {
				return proxyRequest.getHeaders().get(name);
			}

			@Override
			public List<String> getAll(String name) {
				return proxyRequest.getHeaders().getValuesList(name);
			}

			@Override
			public Set<String> names() {
				return proxyRequest.getHeaders().getFieldNamesCollection();
			}

			@Override
			public void set(String name, String value) {
				proxyRequest.getHeaders().put(name, value);
			}
			
			@Override
			public void add(String name, String value) {
				proxyRequest.getHeaders().add(name, value);
			}
			
			@Override
			public void clear(String name) {
				proxyRequest.getHeaders().remove(name);
			}
		};
	}

	public Headers proxyResponseHeaders() {
		return new Headers(){
			@Override
			public String get(String name) {
				return proxyResponse.getHeader(name);
			}

			@Override
			public Collection<String> getAll(String name) {
				return proxyResponse.getHeaders(name);
			}

			@Override
			public Collection<String> names() {
				return proxyResponse.getHeaderNames();
			}

			@Override
			public void set(String name, String value) {
				proxyResponse.setHeader(name, value);
			}
			
			@Override
			public void add(String name, String value) {
				proxyResponse.addHeader(name, value);
			}
			
			@Override
			public void clear(String name) {
				proxyResponse.setHeader(name, null);
			}
		};
	}
}
