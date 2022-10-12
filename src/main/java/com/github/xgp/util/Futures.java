package com.github.xgp.util;

import java.util.concurrent.Future;

/**
 * Conveniences for working with Java Futures.
 */
public class Futures {

  /**
   * Get the result of a future, ignoring exceptions.
   *
   * @param future The future to call .get()
   * @return the value from get() or null if an exception is thrown
   */
  public static <T> T safeFutureGet(Future<T> future) {
    try {
      return future.get();
    } catch (Exception e) {
      return null;
    }
  }
}
