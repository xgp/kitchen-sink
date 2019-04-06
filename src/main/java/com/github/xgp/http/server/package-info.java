/**
 * A simple web framework for the JDK built-in {@link com.sun.net.httpserver.HttpServer}
 * <pre>
 * {@code
 * Server server = new Server(8000).router()
 *   .GET("/test/{id}", (request, response) -> {
 *     response.body("id: "+request.attribute("id"));
 *   })
 *   .POST("/test/{username}/create", (request, response) -> {
 *     User user = new User(request.attribute("username"));
 *     response.body(user);
 *     response.transformer(new JsonTransformer());
 *     response.status(HttpURLConnection.HTTP_CREATED);
 *   });
 * server.start();
 * }
 * </pre>
 */
package com.github.xgp.http.server;
