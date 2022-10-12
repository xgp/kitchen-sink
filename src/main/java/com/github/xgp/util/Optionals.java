package com.github.xgp.util;

import java.util.Optional;
import java.util.function.Supplier;

/** Conveniences for working with Java optional. */
public class Optionals {

  /** Something like the Java 9 Optional.or. Chain it if you need more. */
  public static <T> Optional<T> or(Optional<T> first, Optional<T> second) {
    return first.isPresent() ? first : second;
  }

  /** Something like the Java 9 Optional.or. Chain it if you need more. */
  public static <T> Optional<T> or(Optional<T> first, Supplier<Optional<T>> supplier) {
    return first.isPresent() ? first : supplier.get();
  }

  /**
   * Convenience for returning an Optional value from a supplier. Useful for method chains where one
   * thing might be null before you get to the value you're after.
   *
   * @param supplier the supplier function that will supply the value
   * @return Optional of the result or empty
   */
  public static <T> Optional<T> optionalOf(Supplier<T> supplier) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (Exception e) {
    }
    return Optional.empty();
  }

  /**
   * Convenience for returning a value from a supplier or a fallback if it either throws an
   * exception or == null. Useful for method chains where one thing might be null before you get to
   * the value you're after.
   *
   * @param supplier the supplier function that will supply the value
   * @param fallback returned if supplier.get throws an exception or is null
   * @return the value or fallback
   */
  public static <T> T getOrElse(Supplier<T> supplier, T fallback) {
    try {
      T t = supplier.get();
      if (t != null) return t;
    } catch (Exception e) {
    }
    return fallback;
  }

  /**
   * Convenience for testing a value for non null. Useful for method chains where one thing might be
   * null before you get to the value you're after.
   *
   * @param supplier the supplier function that will supply the value
   * @return true if the result isn't null and doesn't throw an exception
   */
  public static boolean notNull(Supplier supplier) {
    try {
      return supplier.get() != null;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Convenience for testing a value for null. Useful for method chains where one thing might be
   * null before you get to the value you're after.
   *
   * @param supplier the supplier function that will supply the value
   * @return true if the result is null or throws an exception
   */
  public static boolean isNull(Supplier supplier) {
    try {
      return supplier.get() == null;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Convenience for testing a value for non-null and equality. Useful for method chains where one
   * thing might be null before you get to the value you're after.
   *
   * @param value the value to test for equality
   * @param supplier the supplier function that will supply the value
   * @return true if the result isn't null and doesn't throw an exception
   */
  public static <T> boolean is(T value, Supplier<T> supplier) {
    try {
      return supplier.get().equals(value);
    } catch (Exception e) {
    }
    return false;
  }
}
