package org.fuwjax.oss.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public abstract class HttpHandler extends AbstractHandler{
	private ProxyConfig config;
	
	public abstract void init() throws ServletException;
	
    public HttpHandler(ProxyConfig config){
    	this.config = config;
    }
    
    @Override
    protected void doStart() throws Exception {
    	init();
    	super.doStart();
    }
	
	public ProxyConfig getServletConfig(){
		return config;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		service(request, response);
	}
	
    protected abstract void service(HttpServletRequest clientRequest, HttpServletResponse proxyResponse) throws ServletException, IOException;
}
