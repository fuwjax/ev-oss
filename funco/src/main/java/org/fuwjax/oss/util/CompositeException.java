/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.util;

import org.fuwjax.oss.util.function.UnsafeRunnable;

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
