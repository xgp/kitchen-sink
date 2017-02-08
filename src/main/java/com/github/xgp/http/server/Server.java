package com.github.xgp.http.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple web framework server for the JDK built-in {@link com.sun.net.httpserver.HttpServer}
 */
public class Server {

    private final int port;
    private final AtomicBoolean started;
    private final HttpServer server;
    private final RouterHandler router;

    public Server(int port) {
        this.port = port;
	this.started = new AtomicBoolean(false);
	try {
	    this.server = HttpServer.create(new InetSocketAddress(port), 0);
	    this.router = new RouterHandler(this.server);
	    this.server.createContext("/", this.router);
	} catch (IOException e) {
	    throw new IllegalStateException("Unable to start HTTP server on port "+port, e);
	}
    }

    /**
     * Exposes the underlying HttpServer's createContext method. Using
     * this may cause conflicts with the RouterHandler, which can handle normal
     * paths, as well as paths with parameters. Use @see #addHandler() instead.
     */
    public HttpContext createContext(String path) {
	if (!started.get()) {
	    return server.createContext(path);
	} else {
	    throw new IllegalStateException("Cannot create context after server is started");
	}
    }

    /**
     * Exposes the underlying HttpServer.
     */
    public HttpServer getHttpServer() {
	return this.server;
    }

    public boolean isStarted() {
	return started.get();
    }

    public void start() throws Exception {
	if (started.get()) {
	    throw new IllegalStateException("Server is already started");
	} else {
	    server.start();
	    started.set(true);
	}
    }

    public void stop() {
	if (started.get()) {
	    server.stop(0);
	    started.set(false);
	}
    }

    public Server addHandler(String method, String path, HttpHandler handler) {
	router.getRoutes().add(new Route(method, path, handler));
	return this;
    }

    public Server GET(String path, Handler handler) {
	return addHandler("GET", path, handler);
    }

    public Server POST(String path, Handler handler) {
	return addHandler("POST", path, handler);
    }
    
    public Server HEAD(String path, Handler handler) {
	return addHandler("HEAD", path, handler);
    }
    
    public Server OPTIONS(String path, Handler handler) {
	return addHandler("OPTIONS", path, handler);
    }
    
    public Server PUT(String path, Handler handler) {
	return addHandler("PUT", path, handler);
    }
    
    public Server DELETE(String path, Handler handler) {
	return addHandler("DELETE", path, handler);
    }
    
    public Server TRACE(String path, Handler handler) {
	return addHandler("TRACE", path, handler);
    }

}
