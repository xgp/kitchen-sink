package com.github.xgp.util;

import java.util.ArrayList;
import java.util.List;

/** Unsafe, but convenient interface to give a class simple logging. */
public interface Loggable {

  default Logger log(String format, Object... args) {
    return new Logger(this.getClass(), format, args);
  }

  default Logger log(String format) {
    return new Logger(this.getClass(), format);
  }

  /** Fluent logger. */
  static class Logger {

    private final Class clazz;
    private final String format;
    private final List<Object> args;

    private Throwable throwable;

    private Logger(Class clazz, String format, Object... args) {
      this.clazz = clazz;
      this.format = format;
      this.args = new ArrayList<>();
      args(args);
    }

    public void args(Object... args) {
      for (Object arg : args) {
        arg(arg);
      }
    }

    public void arg(Object arg) {
      this.args.add(arg);
    }

    public void ex(Throwable throwable) {
      this.throwable = throwable;
    }

    public void debug() {
      Log.get(clazz).debug(format, args);
    }

    public void info() {
      Log.get(clazz).info(format, args);
    }

    public void error() {
      Log.get(clazz).error(throwable, format, args);
    }
  }
}
