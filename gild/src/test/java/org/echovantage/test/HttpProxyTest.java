package org.echovantage.test;

import static java.util.stream.Collectors.joining;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

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

public class HttpProxyTest {
    private final HttpClientProxy http = new HttpClientProxy("localhost", 8080);
    @Rule public final Gild gild = new Gild().with("http", http);
    private static Undertow server;
    
    @BeforeClass
    public static void setupServer() {
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        StringBuilder responseBody = new StringBuilder();
                        exchange.getRequestHeaders().forEach((h) -> {
                            responseBody.append(h.getHeaderName()).append(": ");
                            responseBody.append(h.stream().collect(joining(", ")));
                            responseBody.append('\n');
                        });
                        responseBody.append("----\n");
                        exchange.startBlocking();
                        InputStream is = exchange.getInputStream();
                        String charsetName = exchange.getRequestCharset();
                        if(charsetName == null) {
                            charsetName = "UTF-8";
                        }
                        Reader r = new InputStreamReader(is, Charset.forName(charsetName));
                        CharBuffer cbuf = CharBuffer.allocate(4096);
                        while(true) {
                            int numChars = r.read(cbuf);
                            if(numChars < 0) {
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
