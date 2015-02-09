/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.assertion;

import org.echovantage.util.collection.ReflectList;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.echovantage.util.assertion.Assertion.toExpected;
import static org.echovantage.util.function.Functions.runnable;

/**
 * Created by fuwjax on 1/25/15.
 */
public class Assertions {
    public static <T> void assertThat(T value, Assertion<? super T> assertion){
        assertion.expects(value);
    }

    public static <T> Assertion<T> asserts(Supplier<String> expected, Predicate<T> test){
        return new Assertion<T>(){
            @Override
            public Supplier<String> expected() {
                return expected;
            }

            @Override
            public boolean test(T value) {
                return test.test(value);
            }
        };
    }

    public static Assertion<Object> isA(Class<?> type){
        assertThat(type, isNotNull());
        return asserts(() -> "instance of "+ toExpected(type).get(), type::isInstance);
    }

    public static Assertion<Object> is(Supplier<String> expected, Object value){
        if(value == null){
            return isNull(expected);
        }
        assertThat(value, isA(Throwable.class).negate());
        if(value.getClass().isArray()){
            List<?> list = ReflectList.asList(value);
            return isJustA(value.getClass()).and(asserts(expected, list::equals).of(ReflectList::asList));
        }
        return asserts(expected, t -> Objects.equals(value, t));
    }

    public static Assertion<Object> is(Object value){
        return is(toExpected(value), value);
    }

    public static Assertion<Object> isNotNull(){
        return asserts(() -> "not null", t -> t != null);
    }

    public static <T> Assertion<T> notNull(Assertion<T> assertion){
        return asserts(assertion.expected(), t -> t != null && assertion.test(t));
    }

    public static Assertion<Object> isNull(Supplier<String> message){
        return asserts(message, t -> t == null);
    }

    public static Assertion<Object> isNull(){
        return isNull(() -> "null");
    }

    public static Assertion<Object> isJustA(Class<?> type){
        assertThat(type, isNotNull());
        return isNotNull().and(is(type).of(() -> "direct instance of "+toExpected(type).get(), Object::getClass));
    }

    public static Assertion<Class<?>> isAssignableFrom(Class<?> type){
        assertThat(type, isNotNull());
        return asserts(() -> "assignable from "+toExpected(type).get(), type::isAssignableFrom);
    }

    public static Assertion<? super Throwable> isException(Throwable expected){
        return expected == null ? isNull() : isException(is(expected.getClass()), is(expected.getMessage()), isException(expected.getCause()));
    }

    public static Assertion<Throwable> isException(Assertion<? super Class<?>> type, Assertion<? super String> message){
        return isException(type, message, isNull());
    }

    public static Assertion<Throwable> isException(Assertion<? super Class<?>> type, Assertion<? super String> message, Assertion<? super Throwable> cause){
        assertThat(type, isNotNull());
        assertThat(message, isNotNull());
        assertThat(cause, isNotNull());
        return notNull(type.of(() -> "instance of " + type.expected().get(), Throwable::getClass))
                .and(message.of(() -> "message is [" + message.expected().get() + "]", Throwable::getMessage))
                .and(cause.of(() -> "cause is [" + cause.expected().get() + "]", Throwable::getCause));
    }

    public static Assertion<Runnable> fails(){
        return failsWith(isNotNull());
    }

    public static Assertion<Callable<?>> failsToReturn(){
        return failsToReturnWith(isNotNull());
    }

    public static Assertion<Runnable> failsWith(Assertion<? super Throwable> cause){
        return new Assertion<Runnable>(){
            @Override
            public boolean test(Runnable value) {
                try{
                    value.run();
                    return false;
                } catch(Throwable t){
                    try {
                        cause.expects(t);
                        return true;
                    }catch(AssertionError e){
                        return false;
                    }
                }
            }

            @Override
            public Supplier<String> expected() {
                return cause.expected();
            }

            @Override
            public void expects(Runnable value) throws AssertionError {
                try{
                    value.run();
                } catch(Throwable t){
                    try {
                        cause.expects(t);
                        return;
                    }catch(AssertionError e){
                        e.initCause(t);
                        throw e;
                    }
                }
                throw cause.fail(null);
            }
        };
    }

    public static Assertion<Callable<?>> failsToReturnWith(Assertion<? super Throwable> cause){
        return failsWith(cause).of(c -> runnable(c::call));
    }
}
