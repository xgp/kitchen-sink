package com.github.xgp.http.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.HttpCookie;

public class Response {

  private final HttpExchange exchange;

  public Response(HttpExchange exchange) {
    this.exchange = exchange;
  }

  public HttpExchange exchange() {
    return this.exchange;
  }

  public Headers headers() {
    return exchange.getResponseHeaders();
  }

  public Response header(String key, String value) {
    headers().add(key, value);
    return this;
  }

  public Response cookie(HttpCookie cookie) {
    header("Set-Cookie", cookie.toString());
    return this;
  }

  private Object body;

  public Response body(Object body) {
    this.body = body;
    return this;
  }

  public Object body() {
    return this.body;
  }

  private Transformer transformer;

  public Response transformer(Transformer transformer) {
    this.transformer = transformer;
    return this;
  }

  public Transformer transformer() {
    return this.transformer;
  }

  public OutputStream stream() {
    return exchange.getResponseBody();
  }

  private int status;

  public Response status(int status) {
    this.status = status;
    return this;
  }

  public int status() {
    return this.status;
  }

  public Response contentType(String contentType) {
    header("Content-Type", contentType);
    return this;
  }

  public String contentType() {
    return headers().getFirst("Content-Type");
  }

  private String redirect;

  public Response redirect(String redirect) {
    this.redirect = redirect;
    return this;
  }

  public String redirect() {
    return this.redirect;
  }
}
