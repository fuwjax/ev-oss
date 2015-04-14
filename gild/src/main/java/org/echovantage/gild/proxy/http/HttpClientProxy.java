package org.echovantage.gild.proxy.http;

import static java.nio.file.Files.newBufferedWriter;
import static org.echovantage.util.Charsets.ISO_8859_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jodd.http.HttpConnection;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.net.SocketHttpConnection;

import org.echovantage.gild.proxy.AbstractServiceProxy;
import org.echovantage.util.ReadOnlyPath;

public class HttpClientProxy extends AbstractServiceProxy {
    private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^(?<method>[\\S]+)\\s(?<path>[\\S]+)\\s(?<version>[\\S]+)");

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
    protected boolean preserveImpl(Path output, ReadOnlyPath golden) throws Exception {
        // preserved in prepare
        return false;
    }

    @Override
    protected void prepareImpl(ReadOnlyPath input, Path output) throws Exception {
        Files.createDirectories(output);
        List<ReadOnlyPath> paths = new ArrayList<>();
        try (DirectoryStream<ReadOnlyPath> stream = input.newDirectoryStream()) {
            for (ReadOnlyPath test : stream) {
                paths.add(test);
            }
        }
        Collections.sort(paths);
        for (final ReadOnlyPath file : paths) {
            HttpRequest request = buildRequest(file);
            HttpConnection connection = new SocketHttpConnection(new Socket(host, port));
            request.open(connection);
            HttpResponse response = request.send();
            persistResponse(response, output.resolve(file.getFileName()));
        }
    }

    private void persistResponse(HttpResponse response, Path output) throws IOException {
        try (BufferedWriter w = newBufferedWriter(output, ISO_8859_1)) {
            w.append(response.httpVersion()).append(' ').append(Integer.toString(response.statusCode())).append(' ').append(response.statusPhrase())
                    .append('\n');

            TreeMap<String, String[]> sortedHeaders = new TreeMap<>(response.headers());
            for (Map.Entry<String, String[]> entry : sortedHeaders.entrySet()) {
                String headerName = entry.getKey().toLowerCase();
                if (headerName.equals("date")) {
                    w.append("date: ${DATE}\n");
                } else {
                    for (String value : entry.getValue()) {
                        w.append(headerName).append(": ").append(value).append('\n');
                    }
                }
            }

            w.append('\n');
            w.append(response.body());
        }
    }

    private HttpRequest buildRequest(ReadOnlyPath file) throws IOException {
        try (InputStream is = file.newInputStream(); Reader r = new InputStreamReader(is); BufferedReader br = new BufferedReader(r)) {
            HttpRequest request = new HttpRequest();
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

    private void appendBody(BufferedReader br, HttpRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] cbuf = new char[4096];
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

    private void parseHeader(String line, HttpRequest request) {
        String[] header = line.split(":");
        request.header(header[0], header[1]);
    }

    private void parseRequestLine(String line, HttpRequest request) {
        Matcher m = REQUEST_LINE_PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException("Request line must be formatted as 'METHOD URI VERSION'. was '" + line + "'");
        }
        request.method(m.group("method"));
        request.path(m.group("path"));
        request.httpVersion(m.group("version"));
    }

}
