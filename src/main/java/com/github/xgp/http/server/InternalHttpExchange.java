package com.github.xgp.http.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class InternalHttpExchange extends HttpExchange {

  private final HttpExchange ex;
  private final Route route;
  private final Map<String, Transformer> transformers;

  InternalHttpExchange(HttpExchange ex, Route route, Map<String, Transformer> transformers) {
    this.ex = ex;
    this.route = route;
    this.transformers = transformers;
  }

  public Route getRoute() {
    return route;
  }

  /**
   * A convenience for getting a Transformer, with fallbacks, from: 1) the Transformer set on this
   * Route, 2) the Transformer set in defaults that maps to this Content-Type 3) the default String
   * Transformer
   */
  public Transformer getTransformer() {
    return route
        .getTransformer()
        .orElseGet(
            () -> {
              return transformerFor(getResponseHeaders().getFirst("Content-Type"))
                  .orElse(Transformers.string());
            });
  }

  public Optional<Transformer> transformerFor(String contentType) {
    return Optional.ofNullable(transformers.get(contentType));
  }

  @Override
  public Headers getRequestHeaders() {
    return ex.getRequestHeaders();
  }

  @Override
  public Headers getResponseHeaders() {
    return ex.getResponseHeaders();
  }

  @Override
  public URI getRequestURI() {
    return ex.getRequestURI();
  }

  @Override
  public String getRequestMethod() {
    return ex.getRequestMethod();
  }

  @Override
  public HttpContext getHttpContext() {
    return ex.getHttpContext();
  }

  @Override
  public void close() {
    ex.close();
  }

  @Override
  public InputStream getRequestBody() {
    return ex.getRequestBody();
  }

  @Override
  public OutputStream getResponseBody() {
    return ex.getResponseBody();
  }

  @Override
  public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    ex.sendResponseHeaders(rCode, responseLength);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return ex.getRemoteAddress();
  }

  @Override
  public int getResponseCode() {
    return ex.getResponseCode();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return ex.getLocalAddress();
  }

  @Override
  public String getProtocol() {
    return ex.getProtocol();
  }

  @Override
  public Object getAttribute(String name) {
    return ex.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    ex.setAttribute(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    ex.setStreams(i, o);
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return ex.getPrincipal();
  }
}
