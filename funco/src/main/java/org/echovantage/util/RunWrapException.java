package org.echovantage.util;

import java.util.function.Supplier;

/**
 * Created by fuwjax on 12/20/14.
 */
public class RunWrapException extends RuntimeException {
    private final Supplier<String> message;

    public RunWrapException(Throwable cause, String pattern, Object... args){
        this(cause, () -> String.format(pattern, args));
    }

    public RunWrapException(Throwable cause, String message){
        this(cause, () -> message);
    }

    public RunWrapException(Throwable cause, Supplier<String> message){
        super(cause);
        this.message = message;
    }

    protected Supplier<String> message(){
        return message;
    }

    @Override
    public String getMessage() {
        return message.get();
    }

    public <X extends Throwable> RunWrapException throwIf(Class<X> exceptionType) throws X {
        if(exceptionType.isInstance(getCause())){
            throw exceptionType.cast(getCause());
        }
        return this;
    }
}
