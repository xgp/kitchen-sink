package com.github.xgp.io;

import java.text.MessageFormat;
import java.util.function.Function;
import java.util.function.Supplier;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/* fluent api on top of MessageFormat */
/*
new Formatter().
  .ln("<html>")
  .ln("  <head>")
  .ln("    <title>{0}</title>", title)
  .ln("  </head>")
  .ln("</html>")
  .ln("<body>")
  .filter("  <h1>{0}</h1>", heading, h -> h != null);
  .ln("  <ul>")
  .iter("    <li>{0}</li>", names, n -> n.getDisplayName())
  .ln("  </ul>")
  .ln("</body>");
  .ln("</html>")
  .format();
*/

public class Formatter {

  private final PrintWriter o;

  public Formatter(Writer w) {
    this.o = new PrintWriter(w);
  }

  public Formatter() {
    this(new StringWriter());
  }

  public Formatter lns(String... lines) {
    for (String l : lines) {
      o.println(l);
    }
    return this;
  }
  
  public Formatter ln(String template, Object... args) {
    o.println(MessageFormat.format(template, args));
    return this;
  }

  public Formatter filter(String template, Supplier<Boolean> shouldRender, Object... args) {
    if (shouldRender.get()) ln(template, args);    
    return this;
  }

  public static Function<Object,String> TO_STRING = (o) -> o.toString();
  
  public Formatter iter(String template, Iterable<?> iterable) {
    return iter(template, TO_STRING, iterable);
  }
  
  public Formatter iter(String template, Function<Object,String> valueSupplier, Iterable<?> iterable) {
    iterable.forEach(i -> ln(template, valueSupplier.apply(i)));
    return this;
  }
  
}
