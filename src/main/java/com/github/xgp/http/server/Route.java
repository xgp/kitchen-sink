// modified from:
// https://github.com/ninjaframework/ninja/blob/develop/ninja-core/src/main/java/ninja/Route.java

/**
 * Copyright (C) 2012-2017 the original author or authors.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xgp.http.server;

import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A route */
public class Route {

  // Matches: {id} AND {id: .*?}
  // group(1) extracts the name of the group (in that case "id").
  // group(3) extracts the regex if defined
  static final Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE =
      Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");

  /** This regex matches everything in between path slashes. */
  static final String VARIABLE_ROUTES_DEFAULT_REGEX = "([^/]*)";

  // private static final String PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = "\\{.*?:\\s(.*?)\\}";
  private final String method;
  private final String uri;
  private final HttpHandler handler;
  private final List<String> parameters;
  private final Pattern regex;
  private final Optional<Transformer> transformer;

  public Route(String method, String uri, HttpHandler handler, Optional<Transformer> transformer) {
    this.method = method;
    this.uri = uri;
    this.handler = handler;
    this.parameters = parseNamedParameters(uri);
    this.regex = Pattern.compile(convertRawUriToRegex(uri));
    this.transformer = transformer;
  }

  public String getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public HttpHandler getHandler() {
    return handler;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public Optional<Transformer> getTransformer() {
    return transformer;
  }

  /**
   * Matches /index to /index or /me/1 to /{person}/{id}
   *
   * @return True if the actual route matches a raw route. False if not.
   */
  public boolean matches(String method, String uri) {
    if (this.method.equalsIgnoreCase(method)) {
      Matcher matcher = regex.matcher(uri);
      return matcher.matches();
    } else {
      return false;
    }
  }

  /**
   * This method does not do any decoding / encoding.
   *
   * <p>If you want to decode you have to do it yourself.
   *
   * <p>Most likely with: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
   *
   * @param uri The whole encoded uri.
   * @return A map with all parameters of that uri. Encoded in =&gt; encoded out.
   */
  public Map<String, String> getPathParametersEncoded(String uri) {
    Map<String, String> map = new HashMap<>();

    if (this.parameters != null) {
      Matcher m = regex.matcher(uri);
      if (m.matches()) {
        Iterator<String> it = this.parameters.iterator();
        for (int i = 1; i < m.groupCount() + 1; i++) {
          String parameterName = it.next();
          map.put(parameterName, m.group(i));
        }
      }
    }

    return map;
  }

  /**
   * Gets a raw uri like "/{name}/id/*" and returns "/([^/]*)/id/*."
   *
   * <p>Also handles regular expressions if defined inside routes: For instance "/users/{username:
   * [a-zA-Z][a-zA-Z_0-9]}" becomes "/users/([a-zA-Z][a-zA-Z_0-9])"
   *
   * @return The converted regex with default matching regex - or the regex specified by the user.
   */
  protected static String convertRawUriToRegex(String rawUri) {

    // convert capturing groups in route regex to non-capturing groups
    // this is to avoid count mismatch of path params and groups in uri regex
    Matcher groupMatcher = Pattern.compile("\\(([^?].*)\\)").matcher(rawUri);
    String converted = groupMatcher.replaceAll("\\(?:$1\\)");

    Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(converted);

    StringBuffer stringBuffer = new StringBuffer();

    while (matcher.find()) {

      // By convention group 3 is the regex if provided by the user.
      // If it is not provided by the user the group 3 is null.
      String namedVariablePartOfRoute = matcher.group(3);
      String namedVariablePartOfORouteReplacedWithRegex;

      if (namedVariablePartOfRoute != null) {
        // we convert that into a regex matcher group itself
        namedVariablePartOfORouteReplacedWithRegex =
            "(" + Matcher.quoteReplacement(namedVariablePartOfRoute) + ")";
      } else {
        // we convert that into the default namedVariablePartOfRoute regex group
        namedVariablePartOfORouteReplacedWithRegex = VARIABLE_ROUTES_DEFAULT_REGEX;
      }
      // we replace the current namedVariablePartOfRoute group
      matcher.appendReplacement(stringBuffer, namedVariablePartOfORouteReplacedWithRegex);
    }

    // .. and we append the tail to complete the stringBuffer
    matcher.appendTail(stringBuffer);

    return stringBuffer.toString();
  }

  /**
   * Parse a path such as "/user/{id: [0-9]+}/email/{addr}" for the named parameters.
   *
   * @param path The path to parse
   * @return A list containing the parameter name in the order they were parsed or null if no
   *     parameters were parsed.
   */
  protected static List<String> parseNamedParameters(String path) {
    List<String> params = null;

    // extract any named parameters
    Matcher matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(path);
    while (matcher.find()) {
      if (params == null) {
        params = new ArrayList<String>();
      }
      params.add(matcher.group(1));
    }

    return params;
  }
}
