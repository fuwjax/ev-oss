package org.echovantage.expression;

import java.math.BigDecimal;

/**
 * Created by fuwjax on 2/4/15.
 */
public class Constant {
    public static Expression NULL = w -> null;
    public static Expression TRUE = w -> true;
    public static Expression FALSE = w -> false;
    public static Expression numberOf(String value){
        return w -> new BigDecimal(value);
    }
}
