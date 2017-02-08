package com.github.xgp.http.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class RouterHandler implements HttpHandler {

    private final HttpServer http;
    private final List<Route> routes;

    public RouterHandler(HttpServer http) {
	this.http = http;
	this.routes = new ArrayList<Route>();
    }

    public List<Route> getRoutes() {
	return this.routes;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
	Optional<Route> route = getRouteFor(exchange.getRequestMethod(), exchange.getRequestURI().toString());
	if (route.isPresent()) {
	    route.get().addAttributes(exchange);
	    route.get().getHandler().handle(exchange);
	} else {
	    HttpExchanges.cannedRespond(exchange, HTTP_NOT_FOUND, "404: Not Found.");
	}
    }

    private Optional<Route> getRouteFor(String method, String uri) {
        for (Route route : routes) {
            if (route.matches(method, uri)) {
                return Optional.of(route);
            }
        }
        return Optional.empty();
    }

}
