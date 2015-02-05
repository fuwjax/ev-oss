package org.echovantage.expression;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by fuwjax on 2/3/15.
 */
public enum Operation {
    OR{
        public Expression of(Expression x1, Expression x2) {
            return w -> x1.evalBoolean(w) || x2.evalBoolean(w);
        }
    },
    AND{
        public Expression of(Expression x1, Expression x2) {
            return w -> x1.evalBoolean(w) || x2.evalBoolean(w);
        }
    },
    EQ{
        public Expression of(Expression x1, Expression x2) {
            return w -> {
                Object v1 = x1.apply(w);
                Object v2 = x2.apply(w);
                if(v1 instanceof Boolean || v2 instanceof Boolean){
                    return Objects.equals(v1, v2);
                }
                return  number((Number) v1).compareTo(number((Number) v2)) == 0;
            };
        }
    },
    NE{
        public Expression of(Expression x1, Expression x2) {
            return NOT.of(EQ.of(x1, x2));
        }
    },
    LT{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).compareTo(number(x2.evalNumeric(w))) < 0;
        }
    },
    GT{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).compareTo(number(x2.evalNumeric(w))) > 0;
        }
    },
    LE{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).compareTo(number(x2.evalNumeric(w))) <= 0;
        }
    },
    GE{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).compareTo(number(x2.evalNumeric(w))) >= 0;
        }
    },
    ADD{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).add(number(x2.evalNumeric(w)));
        }
    },
    SUB{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).subtract(number(x2.evalNumeric(w)));
        }
    },
    MUL{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).multiply(number(x2.evalNumeric(w)));
        }
    },
    DIV{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).divide(number(x2.evalNumeric(w)));
        }
    },
    MOD{
        public Expression of(Expression x1, Expression x2) {
            return w -> number(x1.evalNumeric(w)).remainder(number(x2.evalNumeric(w)));
        }
    },
    POS{
        public Expression of(Expression x) {
            return x;
        }
    },
    NEG{
        public Expression of(Expression x) {
            return w -> number(x.evalNumeric(w)).negate();
        }
    },
    NOT{
        public Expression of(Expression x) {
            return w -> !x.evalBoolean(w);
        }
    };

    private static BigDecimal number(Number number) {
        if(number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        if((double)number.longValue() == number.doubleValue()){
            return new BigDecimal(number.longValue());
        }
        return new BigDecimal(number.doubleValue());
    }


    public Expression of(Expression x){
        throw new UnsupportedOperationException();
    }

    public Expression of(Expression x1, Expression x2){
        throw new UnsupportedOperationException();
    }
}
