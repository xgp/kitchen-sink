package com.github.xgp.util;

import java.util.ArrayList;
import java.util.List;

/** A trivial tree data structure. */
public class Tree<T> {

  private final T data;
  private final Tree<T> parent;
  private final List<Tree<T>> children;

  public Tree(T data) {
    this(data, null);
  }

  public Tree(T data, Tree<T> parent) {
    this.data = data;
    this.parent = parent;
    this.children = new ArrayList<Tree<T>>();
  }

  public T getData() {
    return this.data;
  }

  public Tree<T> getParent() {
    return this.parent;
  }

  public boolean isRoot() {
    return (this.parent == null);
  }

  public List<Tree<T>> getChildren() {
    return this.children;
  }

  public Tree<T> addChild(T data) {
    Tree<T> child = new Tree<T>(data, this);
    this.children.add(child);
    return child;
  }

  public List<T> flatten() {
    return flatten(new ArrayList<T>(), this);
  }

  private List<T> flatten(List<T> list, Tree<T> item) {
    list.add(item.getData());
    for (Tree<T> child : item.getChildren()) {
      flatten(list, child);
    }
    return list;
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(data).append(" (root=").append(isRoot()).append(")\n");
    toString(s, getChildren(), 2);
    return s.toString();
  }

  private void toString(StringBuilder s, List<Tree<T>> children, int indent) {
    for (Tree<T> child : children) {
      for (int i = 0; i < indent; i++) s.append(' ');
      s.append(child.getData()).append("\n");
      toString(s, child.getChildren(), indent + indent);
    }
  }
}
