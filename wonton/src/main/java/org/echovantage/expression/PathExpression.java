package org.echovantage.expression;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.standard.StandardPath;

/**
 * Created by fuwjax on 2/4/15.
 */
public class PathExpression  implements Expression, Wonton.Path {
    private final String key;
    private PathExpression tail;

    public PathExpression(String segment){
        this.key = segment;
    }

    public PathExpression sub(String segment){
        tail = new PathExpression(segment);
        return tail;
    }

    @Override
    public Object apply(Wonton wonton) {
        return wonton.get(this);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Wonton.Path tail() {
        return tail == null ? StandardPath.EMPTY : tail;
    }

    @Override
    public Wonton.Path append(Wonton.Path suffix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
