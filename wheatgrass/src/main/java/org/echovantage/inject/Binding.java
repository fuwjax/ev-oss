package org.echovantage.inject;

/**
 * Created by fuwjax on 2/20/15.
 */
public interface Binding {
    Object get(Scope scope) throws ReflectiveOperationException;
}
