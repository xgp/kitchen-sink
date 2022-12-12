package com.github.xgp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Static utility functions. Inspired somewhat by python's built-in functions
 * (https://docs.python.org/3/library/functions.html). Meant to be statically
 * imported U.*. Lots of nauseating method overloading.
 */
public class U {

  /*
    class list([iterable])
   */
  
  public static <T> List<T> _list() {
    return new ArrayList<>();
  }

  public static <T> List<T> _list(Iterable<T> iter) {
    List<T> result = new ArrayList<T>();
    iter.forEach(result::add);
    return result;
  }
  
  /*
    hex(x)
  */

  public static String _hex(int i) {
    return Integer.toHexString(i);
  }
  
  public static String _hex(Object o) {
    if (o instanceof Integer) return Integer.toHexString(((Integer)o).intValue());
    else return Integer.toHexString(o.hashCode());
  }
      
  /*
    print(*objects, sep=' ', end='\n', file=sys.stdout, flush=False)¶
    oops. don't forget about flush
  */

  private static final String DEFAULT_SEP = " ";
  private static final String DEFAULT_END = System.lineSeparator();;
  
  public static void _print(Object... objects) {
    _print(System.out, objects);
  }

  public static void _print(String sep, Object... objects) {
    _print(sep, System.out, objects);
  }

  public static void _print(String sep, String end, Object... objects) {
    _print(sep, end, System.out, objects);
  }

  public static void _print(File file, Object... objects) {
    try {
      _print(new PrintStream(file), objects);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static void _print(String sep, File file, Object... objects) {
    try {
      _print(sep, new PrintStream(file), objects);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static void _print(String sep, String end, File file, Object... objects) {
    try {
      _print(sep, end, new PrintStream(file), objects);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static void _out(Object... objects) {
    _print(System.out, objects);
  }

  public static void _err(Objects... objects) {
    _print(System.err, objects);
  }
  
  public static void _print(PrintStream str, Object... objects) {
    _print(DEFAULT_SEP, DEFAULT_END, str, objects);
  }

  public static void _print(String sep, PrintStream str, Object... objects) {
    _print(sep, DEFAULT_END, str, objects);
  }

  public static void _print(String sep, String end, PrintStream str, Object... objects) {
    for (int i=0; i<objects.length; i++) {
      str.print(objects[i].toString());
      if (i < objects.length-1) str.print(sep);
    }
    str.print(end);
  }

  
}
