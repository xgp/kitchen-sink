package com.github.xgp.util;

import java.util.concurrent.Future;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Conveniences for working with Java reflection.
 */
public class Reflections {


  /**
   * Get methods of a class that are annontated with the given annotation.
   *
   * @param clazz The class to look for annotated methods.
   * @param annotationClass The annotation to look for.
   * @return an immutable list of methods in the given class with the given annotation.
   */
  public static List<Method> annotatedMethods(
      Class<?> clazz, Class<? extends Annotation> annotationClass) {
    ImmutableList.Builder<Method> annotatedMethods = new ImmutableList.Builder<Method>();
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(annotationClass)) {
        annotatedMethods.add(method);
      }
    }
    return annotatedMethods.build();
  }

}
