package org.echovantage.expression;

import org.echovantage.wonton.Wonton;

/**
 * Created by fuwjax on 2/3/15.
 */
public class TernaryExpression implements Expression {
    private final Expression test;
    private final Expression ifTrue;
    private final Expression ifFalse;

    public TernaryExpression(Expression test, Expression ifTrue, Expression ifFalse){
        this.test = test;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public Object apply(Wonton o) {
        return test.evalBoolean(o) ? ifTrue.apply(o) : ifFalse.apply(o);
    }
}
