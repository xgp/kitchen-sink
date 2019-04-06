package com.github.xgp.http.server;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Router implements HttpHandler {

  private final List<Route> routes;

  public Router() {
    this.routes = new ArrayList<Route>();
  }

  public List<Route> getRoutes() {
    return this.routes;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Optional<Route> route =
        getRouteFor(exchange.getRequestMethod(), exchange.getRequestURI().toString());
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

  public Router addHandler(String method, String path, HttpHandler handler) {
    getRoutes().add(new Route(method, path, handler));
    return this;
  }

  public Router GET(String path, Handler handler) {
    return addHandler("GET", path, handler);
  }

  public Router POST(String path, Handler handler) {
    return addHandler("POST", path, handler);
  }

  public Router HEAD(String path, Handler handler) {
    return addHandler("HEAD", path, handler);
  }

  public Router OPTIONS(String path, Handler handler) {
    return addHandler("OPTIONS", path, handler);
  }

  public Router PUT(String path, Handler handler) {
    return addHandler("PUT", path, handler);
  }

  public Router DELETE(String path, Handler handler) {
    return addHandler("DELETE", path, handler);
  }

  public Router TRACE(String path, Handler handler) {
    return addHandler("TRACE", path, handler);
  }
}
