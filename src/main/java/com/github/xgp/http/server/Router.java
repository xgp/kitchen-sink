package com.github.xgp.http.server;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Router implements HttpHandler {

  private final List<Route> routes;
  private final Map<String, Transformer> transformers;

  public Router() {
    this.routes = new ArrayList<Route>();
    this.transformers = new HashMap<String, Transformer>();
  }

  public List<Route> getRoutes() {
    return routes;
  }

  public Map<String, Transformer> getTransformers() {
    return transformers;
  }

  public Router addTransformer(Transformer transformer) {
    transformers.put(transformer.contentType(), transformer);
    return this;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Optional<Route> route =
        getRouteFor(exchange.getRequestMethod(), exchange.getRequestURI());
    if (route.isPresent()) {
      try {
        route
            .get()
            .getHandler()
            .handle(new InternalHttpExchange(exchange, route.get(), transformers));
      } catch (Exception e) {
        e.printStackTrace();
        HttpExchanges.cannedRespond(
            exchange, HTTP_INTERNAL_ERROR, "500 Internal Server Error: " + e.getMessage());
      }
    } else {
      HttpExchanges.cannedRespond(
          exchange, HTTP_NOT_FOUND, "404 Not Found: " + exchange.getRequestURI());
    }
  }

  private Optional<Route> getRouteFor(String method, URI uri) {
    for (Route route : routes) {
      if (route.matches(method, uri.getPath())) {
        return Optional.of(route);
      }
    }
    return Optional.empty();
  }

  public Router addHandler(String method, String path, HttpHandler handler) {
    return addHandler(method, path, handler, null);
  }

  public Router addHandler(
      String method, String path, HttpHandler handler, Transformer transformer) {
    getRoutes().add(new Route(method, path, handler, Optional.ofNullable(transformer)));
    return this;
  }

  public Router HEAD(String path, Handler handler) {
    return addHandler("HEAD", path, handler);
  }

  public Router OPTIONS(String path, Handler handler) {
    return addHandler("OPTIONS", path, handler);
  }

  public Router TRACE(String path, Handler handler) {
    return addHandler("TRACE", path, handler);
  }

  public Router GET(String path, Handler handler) {
    return addHandler("GET", path, handler);
  }

  public Router GET(String path, Handler handler, Transformer transformer) {
    return addHandler("GET", path, handler, transformer);
  }

  public Router POST(String path, Handler handler) {
    return addHandler("POST", path, handler);
  }

  public Router POST(String path, Handler handler, Transformer transformer) {
    return addHandler("POST", path, handler, transformer);
  }

  public Router PUT(String path, Handler handler) {
    return addHandler("PUT", path, handler);
  }

  public Router PUT(String path, Handler handler, Transformer transformer) {
    return addHandler("PUT", path, handler, transformer);
  }

  public Router DELETE(String path, Handler handler) {
    return addHandler("DELETE", path, handler);
  }

  public Router DELETE(String path, Handler handler, Transformer transformer) {
    return addHandler("DELETE", path, handler, transformer);
  }

  public Router PATCH(String path, Handler handler) {
    return addHandler("PATCH", path, handler);
  }

  public Router PATCH(String path, Handler handler, Transformer transformer) {
    return addHandler("PATCH", path, handler, transformer);
  }
}
