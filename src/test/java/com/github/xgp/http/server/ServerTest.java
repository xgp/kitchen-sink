package com.github.xgp.http.server;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.github.xgp.http.client.HttpRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import org.junit.Test;

public class ServerTest {

  private static int getFreePort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
  }

  @Test
  public void notFound() throws Exception {
    int port = getFreePort();
    Server server = new Server(port);
    server.start();

    StringBuffer o = new StringBuffer();
    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/foo").receive(o);
    assertTrue(req.notFound());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(o.toString(), containsString("404"));

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

    StringBuffer o = new StringBuffer();
    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test").receive(o);
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(o.toString(), is("test"));

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

    StringBuffer o = new StringBuffer();
    HttpRequest req = HttpRequest.GET("http://localhost:" + port + "/test/1234").receive(o);
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(o.toString(), is("id: 1234"));

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
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), is("id: 1234"));

    req = HttpRequest.GET("http://localhost:" + port + "/test");
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), is("test"));

    req = HttpRequest.GET("http://localhost:" + port + "/test/some/1234");
    assertTrue(req.notFound());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), containsString("404"));

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
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), is("get"));

    req = HttpRequest.POST("http://localhost:" + port + "/test");
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), is("post"));

    req = HttpRequest.DELETE("http://localhost:" + port + "/test");
    assertTrue(req.notFound());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), containsString("404"));

    server.stop();
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
            }, transformer("bar"))
        .addTransformer(transformer("foo"));
    server.start();

    HttpRequest req = null;
    req = HttpRequest.GET("http://localhost:" + port + "/foo");
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/foo"));
    assertThat(req.body(), is("testfoo"));

    req = HttpRequest.GET("http://localhost:" + port + "/bar");
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/bar"));
    assertThat(req.body(), is("testbar"));

    req = HttpRequest.GET("http://localhost:" + port + "/test");
    assertTrue(req.ok());
    assertThat(req.contentType(), is("text/plain"));
    assertThat(req.body(), is("test"));
  }
  
  private static Transformer transformer(String s) {
    return new Transformer() {
      @Override
      public String contentType() {
        return "text/"+s;
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
