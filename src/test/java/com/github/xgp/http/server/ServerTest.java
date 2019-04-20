package com.github.xgp.http.server;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.github.xgp.http.client.HttpRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import org.hamcrest.Matcher;
import org.junit.Test;

public class ServerTest {

  private static int getFreePort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
  }

  private void checkResponse(HttpRequest req, int status, String contentType, Matcher matcher) {
    StringBuffer o = new StringBuffer();
    req = req.receive(o);
    assertThat(req.code(), is(status));
    assertThat(req.contentType(), is(contentType));
    assertThat(o.toString(), matcher);
  }

  @Test
  public void notFound() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server.start();

    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/foo");
    checkResponse(req, HTTP_NOT_FOUND, "text/plain", containsString("404"));

    server.stop();
  }

  @Test
  public void simpleTextResponse() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test",
            (request, response) -> {
              response.body("test");
            });
    server.start();

    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("test"));

    server.stop();
  }

  @Test
  public void parameterTextResponse() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test/{id}",
            (request, response) -> {
              response.body("id: " + request.param("id"));
            });
    server.start();

    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test/1234");
    checkResponse(req, HTTP_OK, "text/plain", is("id: 1234"));

    server.stop();
  }

  @Test
  public void queryTextResponse() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test",
            (request, response) -> {
              response.body("id: " + request.query("id").get(0));
            });
    server.start();

    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test?id=1234");
    checkResponse(req, HTTP_OK, "text/plain", is("id: 1234"));

    server.stop();
  }

  @Test
  public void multipleHandlers() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test/{id}",
            (request, response) -> {
              response.body("id: " + request.param("id"));
            })
        .GET(
            "/test",
            (request, response) -> {
              response.body("test");
            });
    server.start();

    HttpRequest req = null;
    req = HttpRequest.GET("http://localhost:" + port + "/test/1234");
    checkResponse(req, HTTP_OK, "text/plain", is("id: 1234"));

    req = HttpRequest.GET("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("test"));

    req = HttpRequest.GET("http://localhost:" + port + "/test/some/1234");
    checkResponse(req, HTTP_NOT_FOUND, "text/plain", containsString("404"));

    server.stop();
  }

  @Test
  public void mixedMethods() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test",
            (request, response) -> {
              response.body("get");
            })
        .POST(
            "/test",
            (request, response) -> {
              response.body("post");
            });
    server.start();

    HttpRequest req = null;
    req = HttpRequest.GET("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("get"));

    req = HttpRequest.POST("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("post"));

    req = HttpRequest.DELETE("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_NOT_FOUND, "text/plain", containsString("404"));

    server.stop();
  }

  @Test
  public void handlerMethodReference() throws Exception {
    int port = getFreePort();
    TestHandler handler = new TestHandler();
    Server server = new Server(port);
    server.router().GET("/test", handler::test);
    server.start();

    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("test"));

    server.stop();
  }

  public class TestHandler {
    public void test(Request request, Response response) {
      response.body("test");
    }
  }

  @Test
  public void defaultTransformers() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server
        .router()
        .GET(
            "/test",
            (request, response) -> {
              response.body("test");
              response.contentType("text/plain");
            })
        .GET(
            "/foo",
            (request, response) -> {
              response.body("test");
              response.contentType("text/foo");
            })
        .GET(
            "/bar",
            (request, response) -> {
              response.body("test");
              response.contentType("text/bar");
            },
            transformer("bar"))
        .addTransformer(transformer("foo"));
    server.start();

    HttpRequest req = null;
    req = HttpRequest.GET("http://localhost:" + port + "/foo");
    checkResponse(req, HTTP_OK, "text/foo", is("testfoo"));

    req = HttpRequest.GET("http://localhost:" + port + "/bar");
    checkResponse(req, HTTP_OK, "text/bar", is("testbar"));

    req = HttpRequest.GET("http://localhost:" + port + "/test");
    checkResponse(req, HTTP_OK, "text/plain", is("test"));
  }

  private static Transformer transformer(String s) {
    return new Transformer() {
      @Override
      public String contentType() {
        return "text/" + s;
      }

      @Override
      public void render(Object object, Response response) throws IOException {
        OutputStream output = response.stream();
        output.write(object.toString().getBytes());
        output.write(s.getBytes());
        output.flush();
      }
    };
  }
}
