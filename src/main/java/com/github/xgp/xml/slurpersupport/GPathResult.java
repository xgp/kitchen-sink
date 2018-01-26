package com.github.xgp.xml.slurpersupport;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Base class for representing lazy evaluated GPath expressions.
 *
 * @author John Wilson
 */
public abstract class GPathResult {
    protected final GPathResult parent;
    protected final String name;
    protected final String namespacePrefix;
    protected final Map namespaceMap = new HashMap();
    protected final Map<String, String> namespaceTagHints;

    /**
     * @param parent the GPathResult prior to the application of the expression creating this GPathResult
     * @param name if the GPathResult corresponds to something with a name, e.g. a node
     * @param namespacePrefix the namespace prefix if any
     * @param namespaceTagHints the known tag to namespace mappings
     */
    public GPathResult(final GPathResult parent, final String name, final String namespacePrefix, final Map<String, String> namespaceTagHints) {
        if (parent == null) {
            // we are the top of the tree
            this.parent = this;
            this.namespaceMap.put("xml", "http://www.w3.org/XML/1998/namespace");  // The XML namespace is always defined
        } else {
            this.parent = parent;
            this.namespaceMap.putAll(parent.namespaceMap);
        }
        this.name = name;
        this.namespacePrefix = namespacePrefix;
        this.namespaceTagHints = namespaceTagHints;
    }

    public GPathResult a(final String property) {
	//        } else if (property.startsWith("@")) {
	if (property.indexOf(":") != -1) {
	    final int i = property.indexOf(":");
	    return new Attributes(this, "@" + property.substring(i + 1), property.substring(1, i), this.namespaceTagHints);
	} else {
	    return new Attributes(this, "@" + property, this.namespaceTagHints);
	}
    }

    public GPathResult e(final String property) {
	if (property.indexOf(":") != -1) {
	    final int i = property.indexOf(":");
	    return new NodeChildren(this, property.substring(i + 1), property.substring(0, i), this.namespaceTagHints);
	} else {
	    return new NodeChildren(this, property, this.namespaceTagHints);
	}
    }

    public Object getProperty(final String property) {
        if ("..".equals(property)) {
            return parent();
        } else if ("*".equals(property)) {
            return children();
        } else if ("**".equals(property)) {
            return depthFirst();
        } else if (property.startsWith("@")) {
            if (property.indexOf(":") != -1) {
                final int i = property.indexOf(":");
                return new Attributes(this, "@" + property.substring(i + 1), property.substring(1, i), this.namespaceTagHints);
            } else {
                return new Attributes(this, property, this.namespaceTagHints);
            }
        } else {
            if (property.indexOf(":") != -1) {
                final int i = property.indexOf(":");
                return new NodeChildren(this, property.substring(i + 1), property.substring(0, i), this.namespaceTagHints);
            } else {
                return new NodeChildren(this, property, this.namespaceTagHints);
            }
        }
    }

    public String name() {
        return this.name;
    }

    public GPathResult parent() {
        return this.parent;
    }

    public GPathResult children() {
        return new NodeChildren(this, this.namespaceTagHints);
    }
    
    public String lookupNamespace(final String prefix) {
        return this.namespaceTagHints.get(prefix);
    }

    public String toString() {
        return text();
    }

    public Integer toInteger() {
        return Integer.valueOf(text());
    }

    public Long toLong() {
        return Long.valueOf(text());
    }

    public Float toFloat() {
        return Float.valueOf(text());
    }

    public Double toDouble() {
        return Double.valueOf(text());
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(text());
    }

    public BigInteger toBigInteger() {
        return new BigInteger(text());
    }

    public URL toURL() throws MalformedURLException {
        return new URL(text());
    }

    public URI toURI() throws URISyntaxException {
        return new URI(text());
    }

    public Boolean toBoolean() {
        return Boolean.valueOf(text());
    }

    public GPathResult declareNamespace(final Map newNamespaceMapping) {
        this.namespaceMap.putAll(newNamespaceMapping);
        return this;
    }

    /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
    public boolean equals(Object obj) {
        return text().equals(obj.toString());
    }

    public Object getAt(final int index) {
        if (index < 0) {
            // calculate whole list in this case
            // recommend avoiding -ve's as this is obviously not as efficient
            List list = list();
            int adjustedIndex = index + list.size();
            if (adjustedIndex >= 0 && adjustedIndex < list.size())
                return list.get(adjustedIndex);
        } else {
            final Iterator iter = iterator();
            int count = 0;

            while (iter.hasNext()) {
                if (count++ == index) {
                    return iter.next();
                } else {
                    iter.next();
                }
            }
        }

        return new NoChildren(this, this.name, this.namespaceTagHints);
    }

//     public Object getAt(final IntRange range) {
//         return DefaultGroovyMethods.getAt(list(), range);
//     }

    public Iterator<GPathResult> depthFirst() {
        return new Iterator<GPathResult>() {
            private final List list = new LinkedList();
            private final Stack stack = new Stack();
            private Iterator iter = iterator();
            private GPathResult next = getNextByDepth();

            public boolean hasNext() {
                return this.next != null;
            }

            public GPathResult next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByDepth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByDepth() {
                while (this.iter.hasNext()) {
                    final GPathResult node = (GPathResult) this.iter.next();
                    this.list.add(node);
                    this.stack.push(this.iter);
                    this.iter = node.children().iterator();
                }

                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult) this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.stack.pop();
                    return result;
                }
            }
        };
    }

    /**
     * An iterator useful for traversing XML documents/fragments in breadth-first order.
     *
     * @return Iterator the iterator of GPathResult objects
     */
    public Iterator breadthFirst() {
        return new Iterator() {
            private final List list = new LinkedList();
            private Iterator iter = iterator();
            private GPathResult next = getNextByBreadth();

            public boolean hasNext() {
                return this.next != null;
            }

            public Object next() {
                try {
                    return this.next;
                } finally {
                    this.next = getNextByBreadth();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private GPathResult getNextByBreadth() {
                List children = new ArrayList();
                while (this.iter.hasNext() || !children.isEmpty()) {
                    if (this.iter.hasNext()) {
                        final GPathResult node = (GPathResult) this.iter.next();
                        this.list.add(node);
                        this.list.add(this.iter);
                        children.add(node.children());
                    } else {
                        List nextLevel = new ArrayList();
                        for (Object child : children) {
                            GPathResult next = (GPathResult) child;
                            Iterator iterator = next.iterator();
                            while (iterator.hasNext()) {
                                nextLevel.add(iterator.next());
                            }
                        }
                        this.iter = nextLevel.iterator();
                        children = new ArrayList();
                    }
                }
                if (this.list.isEmpty()) {
                    return null;
                } else {
                    GPathResult result = (GPathResult)this.list.get(0);
                    this.list.remove(0);
                    this.iter = (Iterator) this.list.get(0);
                    this.list.remove(0);
                    return result;
                }
            }
        };
    }

    public List list() {
        final Iterator iter = nodeIterator();
        final List result = new LinkedList();
        while (iter.hasNext()) {
            result.add(new NodeChild((Node) iter.next(), this.parent, this.namespacePrefix, this.namespaceTagHints));
        }
        return result;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    
    public abstract int size();

    public abstract String text();

    public abstract GPathResult parents();

    public abstract Iterator<GPathResult> childNodes();

    public abstract Iterator<GPathResult> iterator();

//     public abstract GPathResult find(Closure closure);

//     public abstract GPathResult findAll(Closure closure);

    public abstract Iterator<GPathResult> nodeIterator();
}
