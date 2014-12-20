package org.echovantage.util;

import org.echovantage.util.function.UnsafeRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by fuwjax on 12/20/14.
 */
public class CompositeException extends RunWrapException {
    private List<Exception> causes = new ArrayList<>();

    public CompositeException(String pattern, Object... args) {
        super(null, pattern, args);
    }

    public CompositeException(String message) {
        super(null, message);
    }

    public CompositeException(Supplier<String> message) {
        super(null, message);
    }

    public void add(Exception cause){
        if(getCause() == null){
            initCause(cause);
        }
        causes.add(cause);
    }

    @Override
    public <X extends Throwable> CompositeException throwIf(Class<X> exceptionType) throws X {
        for(Exception cause: causes){
            if(exceptionType.isInstance(getCause())){
                throw exceptionType.cast(getCause());
            }
        }
        return this;
    }

    public boolean attempt(UnsafeRunnable operation){
        try{
            operation.run();
            return true;
        }catch(Exception e){
            add(e);
            return false;
        }
    }

    public void throwIfCaused() {
        if(getCause() != null){
            if(RuntimeException.class.isInstance(getCause())){
                throw (RuntimeException)getCause();
            }
            throw new RunWrapException(getCause(), message());
        }
        if(!causes.isEmpty()) {
            throw this;
        }
    }
}
