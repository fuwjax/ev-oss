package org.echovantage.expression;

import org.echovantage.wonton.Wonton;

import java.util.function.Function;

/**
 * Created by fuwjax on 2/3/15.
 */
public interface Expression extends Function<Wonton, Object> {
    default boolean evalBoolean(Wonton o){
        return (Boolean)apply(o);
    }

    default Number evalNumeric(Wonton o){
        return (Number)apply(o);
    }
}
