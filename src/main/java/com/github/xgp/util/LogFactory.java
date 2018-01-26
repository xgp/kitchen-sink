package com.github.xgp.util;

/**
 * Factory for Log. Create a LogFactory and Log, and put a file with the class
 * name in META-INF/services/com.github.xgp.util.LogFactory if you want to
 * override the default stderr logging behavior.
 */
public interface LogFactory {

    public Log get(String category);

    public Log get(Class clazz);

}
