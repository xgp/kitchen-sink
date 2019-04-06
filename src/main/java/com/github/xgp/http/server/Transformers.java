package com.github.xgp.http.server;

import java.io.IOException;
import java.io.OutputStream;

public class Transformers {

  public static Transformer string() {
    return new Transformer() {
      @Override
      public String contentType() {
        return "text/plain";
      }

      @Override
      public void render(Object object, Response response) throws IOException {
        OutputStream output = response.stream();
        output.write(object.toString().getBytes());
        output.flush();
      }
    };
  }
}
