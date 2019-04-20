package com.github.xgp.http.server;

import static java.net.HttpURLConnection.HTTP_OK;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface Handler extends HttpHandler {
  
  @SuppressWarnings("unchecked")
  default void handle(HttpExchange exchange) throws IOException {
    InternalHttpExchange ex = (InternalHttpExchange)exchange;
    Request request = new Request(ex);
    Response response = new Response(ex);
    handle(request, response);

    // send response
    // 1. if it's a redirect, send and ignore the rest
    if (response.redirect() != null) {
      HttpExchanges.sendRedirect(ex, request.uri().resolve(response.redirect()));
      return;
    }
    // 2. if there is a body string, transform it
    if (response.body() != null) {
      // a. set sensible defaults, if the user did not
      if (response.transformer() == null) {
        response.transformer(ex.getTransformer());
      }
      if (response.contentType() == null)
        response.contentType(response.transformer().contentType());
      if (response.status() == 0) response.status(HTTP_OK);
      // b. send the status and headers
      ex.sendResponseHeaders(response.status(), 0);
      // c. send the content
      response.transformer().render(response.body(), response);
    }
    // 3. close everything
    response.stream().close();
    ex.close();
  }

  void handle(Request request, Response response) throws IOException;
}
