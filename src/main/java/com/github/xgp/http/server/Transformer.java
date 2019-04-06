package com.github.xgp.http.server;

import java.io.IOException;

public interface Transformer {

  default String contentType() {
    return "application/octet-stream";
  }

  void render(Object object, Response response) throws IOException;
}
