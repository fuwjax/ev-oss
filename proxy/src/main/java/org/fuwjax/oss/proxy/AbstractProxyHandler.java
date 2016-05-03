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
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.HttpCookieStore;

import org.fuwjax.oss.util.Log;

/**
 * <p>Abstract base class for proxy servlets.</p>
 * <p>Forwards requests to another server either as a standard web reverse
 * proxy or as a transparent reverse proxy (as defined by RFC 7230).</p>
 * <p>To facilitate JMX monitoring, the {@link HttpClient} instance is set
 * as ServletContext attribute, prefixed with this servlet's name and
 * exposed by the mechanism provided by
 * {@link ServletContext#setAttribute(String, Object)}.</p>
 * <p>The following init parameters may be used to configure the servlet:</p>
 * <ul>
 * <li>preserveHost - the host header specified by the client is forwarded to the server</li>
 * <li>hostHeader - forces the host header to a particular value</li>
 * <li>viaHost - the name to use in the Via header: Via: http/1.1 &lt;viaHost&gt;</li>
 * <li>whiteList - comma-separated list of allowed proxy hosts</li>
 * <li>blackList - comma-separated list of forbidden proxy hosts</li>
 * </ul>
 * <p>In addition, see {@link #createHttpClient()} for init parameters
 * used to configure the {@link HttpClient} instance.</p>
 * <p>NOTE: By default the Host header sent to the server by this proxy
 * servlet is the server's host name. However, this breaks redirects.
 * Set {@code preserveHost} to {@code true} to make redirects working,
 * although this may break server's virtual host selection.</p>
 * <p>The default behavior of not preserving the Host header mimics
 * the default behavior of Apache httpd and Nginx, which both have
 * a way to be configured to preserve the Host header.</p>
 */
public abstract class AbstractProxyHandler extends HttpHandler
{
    protected static final Set<String> HOP_HEADERS;
    static
    {
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

    private final Set<String> _whiteList = new HashSet<>();
    private final Set<String> _blackList = new HashSet<>();
    protected Log _log;
    private boolean _preserveHost;
    private String _hostHeader;
    private String _viaHost;
    private HttpClient _client;
    private long _timeout;
    
    public AbstractProxyHandler(ProxyConfig config){
    	super(config);
    }

    @Override
    public void init() throws ServletException
    {
        _log = createLogger();

        ProxyConfig config = getServletConfig();

        _preserveHost = Boolean.parseBoolean(config.getInitParameter("preserveHost"));

        _hostHeader = config.getInitParameter("hostHeader");

        _viaHost = config.getInitParameter("viaHost");
        if (_viaHost == null)
            _viaHost = viaHost();

        try
        {
            _client = createHttpClient();

            String whiteList = config.getInitParameter("whiteList");
            if (whiteList != null)
                getWhiteListHosts().addAll(parseList(whiteList));

            String blackList = config.getInitParameter("blackList");
            if (blackList != null)
                getBlackListHosts().addAll(parseList(blackList));
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy()
    {
        try
        {
            _client.stop();
        }
        catch (Exception x)
        {
            _log.warning(x, "client failed to stop");
        }
    }

    public String getHostHeader()
    {
        return _hostHeader;
    }

    public String getViaHost()
    {
        return _viaHost;
    }

    private static String viaHost()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException x)
        {
            return "localhost";
        }
    }

    public long getTimeout()
    {
        return _timeout;
    }

    public void setTimeout(long timeout)
    {
        this._timeout = timeout;
    }

    public Set<String> getWhiteListHosts()
    {
        return _whiteList;
    }

    public Set<String> getBlackListHosts()
    {
        return _blackList;
    }

    /**
     * @return a logger instance with a name derived from this servlet's name.
     */
    protected Log createLogger()
    {
    	return getServletConfig().getLog();
    }

    /**
     * <p>Creates a {@link HttpClient} instance, configured with init parameters of this servlet.</p>
     * <p>The init parameters used to configure the {@link HttpClient} instance are:</p>
     * <table>
     * <caption>Init Parameters</caption>
     * <thead>
     * <tr>
     * <th>init-param</th>
     * <th>default</th>
     * <th>description</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>maxThreads</td>
     * <td>256</td>
     * <td>The max number of threads of HttpClient's Executor.  If not set, or set to the value of "-", then the
     * Jetty server thread pool will be used.</td>
     * </tr>
     * <tr>
     * <td>maxConnections</td>
     * <td>32768</td>
     * <td>The max number of connections per destination, see {@link HttpClient#setMaxConnectionsPerDestination(int)}</td>
     * </tr>
     * <tr>
     * <td>idleTimeout</td>
     * <td>30000</td>
     * <td>The idle timeout in milliseconds, see {@link HttpClient#setIdleTimeout(long)}</td>
     * </tr>
     * <tr>
     * <td>timeout</td>
     * <td>60000</td>
     * <td>The total timeout in milliseconds, see {@link Request#timeout(long, java.util.concurrent.TimeUnit)}</td>
     * </tr>
     * <tr>
     * <td>requestBufferSize</td>
     * <td>HttpClient's default</td>
     * <td>The request buffer size, see {@link HttpClient#setRequestBufferSize(int)}</td>
     * </tr>
     * <tr>
     * <td>responseBufferSize</td>
     * <td>HttpClient's default</td>
     * <td>The response buffer size, see {@link HttpClient#setResponseBufferSize(int)}</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @return a {@link HttpClient} configured from the {@link #getServletConfig() servlet configuration}
     * @throws ServletException if the {@link HttpClient} cannot be created
     */
    protected HttpClient createHttpClient() throws ServletException
    {
    	ProxyConfig config = getServletConfig();

        HttpClient client = newHttpClient();

        // Redirects must be proxied as is, not followed.
        client.setFollowRedirects(false);

        // Must not store cookies, otherwise cookies of different clients will mix.
        client.setCookieStore(new HttpCookieStore.Empty());

        Executor executor = config.getExecutor();

        client.setExecutor(executor);

        String value = config.getInitParameter("maxConnections");
        if (value == null)
            value = "256";
        client.setMaxConnectionsPerDestination(Integer.parseInt(value));

        value = config.getInitParameter("idleTimeout");
        if (value == null)
            value = "30000";
        client.setIdleTimeout(Long.parseLong(value));

        value = config.getInitParameter("timeout");
        if (value == null)
            value = "60000";
        _timeout = Long.parseLong(value);

        value = config.getInitParameter("requestBufferSize");
        if (value != null)
            client.setRequestBufferSize(Integer.parseInt(value));

        value = config.getInitParameter("responseBufferSize");
        if (value != null)
            client.setResponseBufferSize(Integer.parseInt(value));

        try
        {
            client.start();

            // Content must not be decoded, otherwise the client gets confused.
            client.getContentDecoderFactories().clear();

            // No protocol handlers, pass everything to the client.
            client.getProtocolHandlers().clear();

            return client;
        }
        catch (Exception x)
        {
            throw new ServletException(x);
        }
    }

    /**
     * @return a new HttpClient instance
     */
    protected HttpClient newHttpClient()
    {
        return new HttpClient();
    }

    protected HttpClient getHttpClient()
    {
        return _client;
    }

    private Set<String> parseList(String list)
    {
        Set<String> result = new HashSet<>();
        String[] hosts = list.split(",");
        for (String host : hosts)
        {
            host = host.trim();
            if (host.length() == 0)
                continue;
            result.add(host);
        }
        return result;
    }

    /**
     * Checks the given {@code host} and {@code port} against whitelist and blacklist.
     *
     * @param host the host to check
     * @param port the port to check
     * @return true if it is allowed to be proxy to the given host and port
     */
    public boolean validateDestination(String host, int port)
    {
        String hostPort = host + ":" + port;
        if (!_whiteList.isEmpty())
        {
            if (!_whiteList.contains(hostPort))
            {
                _log.debug("Host {}:{} not whitelisted", host, port);
                return false;
            }
        }
        if (!_blackList.isEmpty())
        {
            if (_blackList.contains(hostPort))
            {
                _log.debug("Host {}:{} blacklisted", host, port);
                return false;
            }
        }
        return true;
    }

    protected String rewriteTarget(HttpServletRequest clientRequest)
    {
        if (!validateDestination(clientRequest.getServerName(), clientRequest.getServerPort()))
            return null;

        StringBuffer target = clientRequest.getRequestURL();
        String query = clientRequest.getQueryString();
        if (query != null)
            target.append("?").append(query);
        return target.toString();
    }

    /**
     * <p>Callback method invoked when the URI rewrite performed
     * in {@link #rewriteTarget(HttpServletRequest)} returns null
     * indicating that no rewrite can be performed.</p>
     * <p>It is possible to use blocking API in this method,
     * like {@link HttpServletResponse#sendError(int)}.</p>
     *
     * @param clientRequest the client request
     * @param proxyResponse the client response
     */
    protected void onProxyRewriteFailed(HttpServletRequest clientRequest, HttpServletResponse proxyResponse)
    {
        sendProxyResponseError(clientRequest, proxyResponse, HttpStatus.FORBIDDEN_403);
    }

    protected boolean hasContent(HttpServletRequest clientRequest)
    {
        return clientRequest.getContentLength() > 0 ||
                clientRequest.getContentType() != null ||
                clientRequest.getHeader(HttpHeader.TRANSFER_ENCODING.asString()) != null;
    }

    protected void copyRequestHeaders(HttpServletRequest clientRequest, Request proxyRequest)
    {
        // First clear possibly existing headers, as we are going to copy those from the client request.
        proxyRequest.getHeaders().clear();

        Set<String> headersToRemove = findConnectionHeaders(clientRequest);

        for (Enumeration<String> headerNames = clientRequest.getHeaderNames(); headerNames.hasMoreElements();)
        {
            String headerName = headerNames.nextElement();
            String lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH);

            if (HttpHeader.HOST.is(headerName) && !_preserveHost)
                continue;

            // Remove hop-by-hop headers.
            if (HOP_HEADERS.contains(lowerHeaderName))
                continue;
            if (headersToRemove != null && headersToRemove.contains(lowerHeaderName))
                continue;

            for (Enumeration<String> headerValues = clientRequest.getHeaders(headerName); headerValues.hasMoreElements();)
            {
                String headerValue = headerValues.nextElement();
                if (headerValue != null)
                    proxyRequest.header(headerName, headerValue);
            }
        }

        // Force the Host header if configured
        if (_hostHeader != null)
            proxyRequest.header(HttpHeader.HOST, _hostHeader);
    }

    protected Set<String> findConnectionHeaders(HttpServletRequest clientRequest)
    {
        // Any header listed by the Connection header must be removed:
        // http://tools.ietf.org/html/rfc7230#section-6.1.
        Set<String> hopHeaders = null;
        Enumeration<String> connectionHeaders = clientRequest.getHeaders(HttpHeader.CONNECTION.asString());
        while (connectionHeaders.hasMoreElements())
        {
            String value = connectionHeaders.nextElement();
            String[] values = value.split(",");
            for (String name : values)
            {
                name = name.trim().toLowerCase(Locale.ENGLISH);
                if (hopHeaders == null)
                    hopHeaders = new HashSet<>();
                hopHeaders.add(name);
            }
        }
        return hopHeaders;
    }

    protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest)
    {
        addViaHeader(proxyRequest);
        addXForwardedHeaders(clientRequest, proxyRequest);
    }

    protected void addViaHeader(Request proxyRequest)
    {
        proxyRequest.header(HttpHeader.VIA, "http/1.1 " + getViaHost());
    }

    protected void addXForwardedHeaders(HttpServletRequest clientRequest, Request proxyRequest)
    {
        proxyRequest.header(HttpHeader.X_FORWARDED_FOR, clientRequest.getRemoteAddr());
        proxyRequest.header(HttpHeader.X_FORWARDED_PROTO, clientRequest.getScheme());
        proxyRequest.header(HttpHeader.X_FORWARDED_HOST, clientRequest.getHeader(HttpHeader.HOST.asString()));
        proxyRequest.header(HttpHeader.X_FORWARDED_SERVER, clientRequest.getLocalName());
    }

    protected void sendProxyRequest(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Request proxyRequest)
    {
            _log.debug("{} proxying to upstream:{}{}{}{}",
                    getRequestId(clientRequest),
                    System.lineSeparator(),
                    Log.defer(() -> {
                        StringBuilder builder = new StringBuilder(clientRequest.getMethod());
                        builder.append(" ").append(clientRequest.getRequestURI());
                        String query = clientRequest.getQueryString();
                        if (query != null)
                            builder.append("?").append(query);
                        builder.append(" ").append(clientRequest.getProtocol()).append(System.lineSeparator());
                        for (Enumeration<String> headerNames = clientRequest.getHeaderNames(); headerNames.hasMoreElements();)
                        {
                            String headerName = headerNames.nextElement();
                            builder.append(headerName).append(": ");
                            for (Enumeration<String> headerValues = clientRequest.getHeaders(headerName); headerValues.hasMoreElements();)
                            {
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
                    }),
                    proxyRequest,
                    System.lineSeparator(),
                    proxyRequest.getHeaders().toString().trim());

        proxyRequest.send(newProxyResponseListener(clientRequest, proxyResponse));
    }

    protected abstract Response.CompleteListener newProxyResponseListener(HttpServletRequest clientRequest, HttpServletResponse proxyResponse);

    protected void onClientRequestFailure(HttpServletRequest clientRequest, Request proxyRequest, HttpServletResponse proxyResponse, Throwable failure)
    {
        boolean aborted = proxyRequest.abort(failure);
        if (!aborted)
        {
            int status = failure instanceof TimeoutException ?
                    HttpStatus.REQUEST_TIMEOUT_408 :
                    HttpStatus.INTERNAL_SERVER_ERROR_500;
            sendProxyResponseError(clientRequest, proxyResponse, status);
        }
    }

    protected void onServerResponseHeaders(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse)
    {
        for (HttpField field : serverResponse.getHeaders())
        {
            String headerName = field.getName();
            String lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH);
            if (HOP_HEADERS.contains(lowerHeaderName))
                continue;

            String newHeaderValue = filterServerResponseHeader(clientRequest, serverResponse, headerName, field.getValue());
            if (newHeaderValue == null || newHeaderValue.trim().length() == 0)
                continue;

            proxyResponse.addHeader(headerName, newHeaderValue);
        }

            _log.debug("{} proxying to downstream:{}{}{}{}{}",
                    getRequestId(clientRequest),
                    System.lineSeparator(),
                    serverResponse,
                    System.lineSeparator(),
                    serverResponse.getHeaders().toString().trim(),
                    System.lineSeparator(),
                    Log.defer(() -> {
                        StringBuilder builder = new StringBuilder(System.lineSeparator());
                        builder.append(clientRequest.getProtocol()).append(" ").append(proxyResponse.getStatus())
                                .append(" ").append(serverResponse.getReason()).append(System.lineSeparator());
                        for (String headerName : proxyResponse.getHeaderNames())
                        {
                            builder.append(headerName).append(": ");
                            for (Iterator<String> headerValues = proxyResponse.getHeaders(headerName).iterator(); headerValues.hasNext(); )
                            {
                                String headerValue = headerValues.next();
                                if (headerValue != null)
                                    builder.append(headerValue);
                                if (headerValues.hasNext())
                                    builder.append(",");
                            }
                            builder.append(System.lineSeparator());
                        }
                        return builder;
                    }));
    }

    protected String filterServerResponseHeader(HttpServletRequest clientRequest, Response serverResponse, String headerName, String headerValue)
    {
        return headerValue;
    }

    protected void onProxyResponseSuccess(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse)
    {
            _log.debug("{} proxying successful", getRequestId(clientRequest));

        AsyncContext asyncContext = clientRequest.getAsyncContext();
        asyncContext.complete();
    }

    protected void onProxyResponseFailure(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse, Throwable failure)
    {
            _log.debug(getRequestId(clientRequest) + " proxying failed", failure);

        if (proxyResponse.isCommitted())
        {
            try
            {
                // Use Jetty specific behavior to close connection.
                proxyResponse.sendError(-1);
                AsyncContext asyncContext = clientRequest.getAsyncContext();
                asyncContext.complete();
            }
            catch (IOException x)
            {
                    _log.debug(getRequestId(clientRequest) + " could not close the connection", failure);
            }
        }
        else
        {
            proxyResponse.resetBuffer();
            int status = failure instanceof TimeoutException ?
                    HttpStatus.GATEWAY_TIMEOUT_504 :
                    HttpStatus.BAD_GATEWAY_502;
            sendProxyResponseError(clientRequest, proxyResponse, status);
        }
    }

    protected int getRequestId(HttpServletRequest clientRequest)
    {
        return System.identityHashCode(clientRequest);
    }

    protected void sendProxyResponseError(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, int status)
    {
        proxyResponse.setStatus(status);
        proxyResponse.setHeader(HttpHeader.CONNECTION.asString(), HttpHeaderValue.CLOSE.asString());
        if (clientRequest.isAsyncStarted())
            clientRequest.getAsyncContext().complete();
    }

    /**
     * <p>Utility class that implement transparent proxy functionalities.</p>
     * <p>Configuration parameters:</p>
     * <ul>
     * <li>{@code proxyTo} - a mandatory URI like http://host:80/context to which the request is proxied.</li>
     * <li>{@code prefix} - an optional URI prefix that is stripped from the start of the forwarded URI.</li>
     * </ul>
     * <p>For example, if a request is received at "/foo/bar", the {@code proxyTo} parameter is
     * "http://host:80/context" and the {@code prefix} parameter is "/foo", then the request would
     * be proxied to "http://host:80/context/bar".
     */
    protected static class TransparentDelegate
    {
        private final AbstractProxyHandler proxyServlet;
        private String _proxyTo;
        private String _prefix;

        protected TransparentDelegate(AbstractProxyHandler proxyServlet)
        {
            this.proxyServlet = proxyServlet;
        }

        protected void init(ProxyConfig config) throws ServletException
        {
            _proxyTo = config.getInitParameter("proxyTo");
            if (_proxyTo == null)
                throw new UnavailableException("Init parameter 'proxyTo' is required.");

            String prefix = config.getInitParameter("prefix");
            if (prefix == null){
            	_prefix = "";
            }
            {
                if (!prefix.startsWith("/"))
                    throw new UnavailableException("Init parameter 'prefix' must start with a '/'.");
                _prefix = prefix;
            }

                proxyServlet._log.debug("redirect " + _prefix + " to " + _proxyTo);
        }

        protected String rewriteTarget(HttpServletRequest request)
        {
            String path = request.getRequestURI();
            if (!path.startsWith(_prefix))
                return null;

            StringBuilder uri = new StringBuilder(_proxyTo);
            if (_proxyTo.endsWith("/"))
                uri.setLength(uri.length() - 1);
            String rest = path.substring(_prefix.length());
            if (!rest.isEmpty())
            {
                if (!rest.startsWith("/"))
                    uri.append("/");
                uri.append(rest);
            }

            String query = request.getQueryString();
            if (query != null)
            {
                // Is there at least one path segment ?
                String separator = "://";
                if (uri.indexOf("/", uri.indexOf(separator) + separator.length()) < 0)
                    uri.append("/");
                uri.append("?").append(query);
            }
            URI rewrittenURI = URI.create(uri.toString()).normalize();

            if (!proxyServlet.validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
                return null;

            return rewrittenURI.toString();
        }
    }
}
