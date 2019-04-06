package com.github.xgp.xml.slurpersupport;

import java.util.Iterator;
import java.util.Map;

/**
 * Lazy evaluated representation of a node attribute.
 *
 * @author John Wilson
 */
public class Attribute extends GPathResult {
  private final String value;

  /**
   * @param name of the attribute
   * @param value of the attribute
   * @param parent the GPathResult prior to the application of the expression creating this
   *     GPathResult
   * @param namespacePrefix the namespace prefix if any
   * @param namespaceTagHints the known tag to namespace mappings
   */
  public Attribute(
      final String name,
      final String value,
      final GPathResult parent,
      final String namespacePrefix,
      final Map<String, String> namespaceTagHints) {
    super(parent, name, namespacePrefix, namespaceTagHints);
    this.value = value;
  }

  public String name() {
    // this name contains @name we need to return name
    return this.name.substring(1);
  }

  public int size() {
    return 1;
  }

  public String text() {
    return this.value;
  }

  public GPathResult parents() {
    // TODO Auto-generated method stub
    throw new RuntimeException("parents() not implemented yet");
  }

  public Iterator childNodes() {
    throw new RuntimeException("can't call childNodes() in the attribute " + this.name);
  }

  public Iterator iterator() {
    return nodeIterator();
  }

  //     public GPathResult find(final Closure closure) {
  //         if (DefaultTypeTransformation.castToBoolean(closure.call(new Object[]{this}))) {
  //             return this;
  //           } else {
  //             return new NoChildren(this, "", this.namespaceTagHints);
  //           }
  //     }

  //     public GPathResult findAll(final Closure closure) {
  //         return find(closure);
  //     }

  public Iterator nodeIterator() {
    return new Iterator() {
      private boolean hasNext = true;

      public boolean hasNext() {
        return this.hasNext;
      }

      public Object next() {
        try {
          return (this.hasNext) ? Attribute.this : null;
        } finally {
          this.hasNext = false;
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
