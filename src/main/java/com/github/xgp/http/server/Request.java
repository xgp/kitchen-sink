package com.github.xgp.http.server;

import com.github.xgp.io.Streams;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {

  private final InternalHttpExchange exchange;
  private final Map<String, List<String>> queryMap;
  private final Map<String, String> paramMap;

  public Request(InternalHttpExchange exchange) {
    this.exchange = exchange;
    this.queryMap = HttpExchanges.parseQueryParameters(exchange, StandardCharsets.UTF_8);
    this.paramMap =
        exchange.getRoute().getPathParametersEncoded(exchange.getRequestURI().getPath());
  }

  public HttpExchange exchange() {
    return this.exchange;
  }

  public Object attribute(String name) {
    return exchange.getAttribute(name);
  }

  public InetSocketAddress localAddress() {
    return exchange.getLocalAddress();
  }

  public HttpPrincipal principal() {
    return exchange.getPrincipal();
  }

  public String protocol() {
    return exchange.getProtocol();
  }

  public InetSocketAddress remoteAddress() {
    return exchange.getRemoteAddress();
  }

  public Headers headers() {
    return exchange.getRequestHeaders();
  }

  public List<String> headers(String name) {
    return headers().get(name);
  }

  public String header(String name) {
    return headers().getFirst(name);
  }

  public List<HttpCookie> cookies() {
    return headers("Cookie").stream()
        .flatMap(h -> HttpCookie.parse(h).stream())
        .collect(Collectors.toList());
  }

  public URI uri() {
    return exchange.getRequestURI();
  }

  public String method() {
    return exchange.getRequestMethod();
  }

  public InputStream stream() {
    return exchange.getRequestBody();
  }

  public String body() {
    try {
      return Streams.readInputStreamToString(exchange.getRequestBody(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      return null;
    }
  }

  public Map<String, List<String>> queryMap() {
    return queryMap;
  }

  public List<String> query(String name) {
    return queryMap.get(name);
  }

  public Map<String, String> paramMap() {
    return paramMap;
  }

  public String param(String name) {
    return paramMap.get(name);
  }
}
