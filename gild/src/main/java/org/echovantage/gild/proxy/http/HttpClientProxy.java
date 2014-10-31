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

import org.echovantage.gild.proxy.ServiceProxy;
import org.echovantage.util.ReadOnlyPath;

public class HttpClientProxy implements ServiceProxy {
	private ReadOnlyPath input;
	private String host;
	private int port;
	
	public HttpClientProxy(int port){
		this("localhost", port);
	}
	
	public HttpClientProxy(String host, int port){
		this.host = host;
		this.port = port;
	}

	@Override
   public void prepare(ReadOnlyPath input) throws Exception {
		this.input = input;
   }

	@Override
   public void preserve(Path output, ReadOnlyPath golden) throws Exception {
		List<ReadOnlyPath> paths = new ArrayList<>();
		try(DirectoryStream<ReadOnlyPath> stream = input.newDirectoryStream()){
			for(ReadOnlyPath test: stream){
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
}
