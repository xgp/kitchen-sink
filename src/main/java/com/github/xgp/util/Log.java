package com.github.xgp.util;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;

/** Simple logger that sends everything to stderr. */
public class Log {

  public static Log get(String category) {
    return logFactory.get(category);
  }

  public static Log get(Class clazz) {
    return logFactory.get(clazz);
  }

  private Log(String category) {
    this.category = category;
  }

  private final String category;

  public void debug(String msg, Object... args) {
    _debug(category, msg, args);
  }

  public void info(String msg, Object... args) {
    _info(category, msg, args);
  }

  public void error(String msg, Object... args) {
    _error(category, null, msg, args);
  }

  public void error(Throwable throwable, String msg, Object... args) {
    _error(category, throwable, msg, args);
  }

  private static final PrintWriter out =
      new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
  private static final PrintWriter err =
      new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err)));

  private static final ThreadLocal<DateFormat> dateFormat =
      new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
          return new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        }
      };

  static {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              public void run() {
                out.flush();
                out.close();
                err.flush();
                err.close();
              }
            });
  }

  static enum Level {
    DEBUG("DEB"),
    INFO("INF"),
    ERROR("ERR");
    private final String abbr;

    Level(String abbr) {
      this.abbr = abbr;
    }

    public String abbr() {
      return this.abbr;
    }
  }

  public static void _err(String msg) {
    err.println(msg);
  }

  public static void _out(String msg) {
    out.println(msg);
  }

  public static void _debug(String category, String msg, Object... args) {
    _log(category, Level.DEBUG, null, msg, args);
  }

  public static void _info(String category, String msg, Object... args) {
    _log(category, Level.INFO, null, msg, args);
  }

  public static void _error(String category, String msg, Object... args) {
    _log(category, Level.ERROR, null, msg, args);
  }

  public static void _error(String category, Throwable throwable, String msg, Object... args) {
    _log(category, Level.ERROR, throwable, msg, args);
  }

  public static void _log(
      String category, Level level, Throwable throwable, String msg, Object... args) {
    if (level.ordinal() >= systemLevel().ordinal()) {
      _err(
          String.format(
              "%s [%s] %s: %s",
              level.abbr(),
              dateFormat.get().format(new Date()),
              category,
              String.format(msg, args)));
      if (throwable != null) {
        throwable.printStackTrace(err);
      }
    }
  }

  private static Level levelForName(String name) {
    try {
      return Level.valueOf(name);
    } catch (Exception e) {
    }
    return Level.DEBUG;
  }

  public static final String LOG_LEVEL_PROPERTY = "log.level";
  public static final String LOG_BOOTSTRAP_CATEGORY = "log.bootstrap";

  private static Level systemLevel() {
    return levelForName(System.getProperty(LOG_LEVEL_PROPERTY, "DEBUG"));
  }

  private static final LogFactory logFactory;

  static {
    List<LogFactory> providerList = findServiceProviders();
    if (providerList.size() == 0) {
      _error(
          LOG_BOOTSTRAP_CATEGORY,
          "No LogFactory present. Using stderr at %s level.",
          systemLevel());
      logFactory =
          new LogFactory() {
            @Override
            public Log get(String category) {
              return new Log(category);
            }

            @Override
            public Log get(Class clazz) {
              return new Log(clazz.getSimpleName());
            }
          };
    } else {
      if (providerList.size() > 1)
        _error(
            LOG_BOOTSTRAP_CATEGORY,
            "More than one LogFactory (%d) present. Using %s.",
            providerList.size(),
            providerList.get(0));
      logFactory = providerList.get(0);
    }
  }

  private static List<LogFactory> findServiceProviders() {
    ServiceLoader<LogFactory> serviceLoader = ServiceLoader.load(LogFactory.class);
    List<LogFactory> providerList = new ArrayList<LogFactory>();
    for (LogFactory provider : serviceLoader) {
      providerList.add(provider);
    }
    return providerList;
  }

  public static void main(String[] argv) throws Exception {
    Log log = Log.get(Log.class);
    log.debug("Debug");
    log.debug("Debug %s", "something");
    log.info("Info");
    log.info("Info %s", "something");
    log.error("Error");
    log.error("Error %s", "something");
    log.error(new RuntimeException(), "Error %s", "error");
    System.exit(0);
  }
}
