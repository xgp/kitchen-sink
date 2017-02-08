package com.github.xgp.http.server;

import com.sun.net.httpserver.HttpHandler;

/**
 * Makes injected HTTP handlers easier.
 */
public abstract class RegisteringHandler implements HttpHandler {

    public RegisteringHandler(Server http, String path) {
	http.createContext(path).setHandler(this);
    }

}
