package org.echovantage.util.assertion;

import org.echovantage.util.collection.ReflectList;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by fuwjax on 1/25/15.
 */
public interface Assertion<T> {
    static Supplier<String> toExpected(Object expected){
        return () -> {
            if (expected == null) {
                return "null";
            }
            if (expected instanceof Class) {
                return ((Class<?>) expected).getCanonicalName();
            }
            if (expected.getClass().isArray()) {
                return ReflectList.asList(expected).toString();
            }
            if (expected instanceof Throwable) {
                String message = ((Throwable) expected).getMessage();
                return toExpected(expected.getClass()).get() + "(" + (message == null ? "null" : '"' + message + '"') + ", " + toExpected(((Throwable) expected).getCause()).get() + ")";
            }
            return expected.toString();
        };
    }

    default AssertionError fail(T actual) {
        return new AssertionError(String.format("expected:<%s> but was:<%s>", expected().get(), toExpected(actual).get()));
    }

    Supplier<String> expected();

    default void expects(T value) throws AssertionError{
        if(!test(value)){
            throw fail(value);
        }
    }

    boolean test(T value);

    default <U> Assertion<U> of(Function<U, T> function) {
        return of(expected(), function);
    }

    default <U> Assertion<U> of(Supplier<String> expected, Function<U, T> function) {
        return new Assertion<U>(){

            @Override
            public Supplier<String> expected() {
                return expected;
            }

            @Override
            public void expects(U value) throws AssertionError {
                Assertion.this.expects(function.apply(value));
            }

            @Override
            public boolean test(U value) {
                return Assertion.this.test(function.apply(value));
            }
        };
    }

    default Assertion<T> and(Assertion<? super T> assertion) {
        return and(() -> expected().get() + " and " + assertion.expected().get(), assertion);
    }

    default Assertion<T> and(Supplier<String> expected, Assertion<? super T> assertion){
        return new Assertion<T>(){
            @Override
            public Supplier<String> expected() {
                return expected;
            }

            @Override
            public void expects(T value) throws AssertionError {
                Assertion.this.expects(value);
                assertion.expects(value);
            }

            @Override
            public boolean test(T value) {
                return Assertion.this.test(value) && assertion.test(value);
            }
        };
    }

    default Assertion<T> or(Assertion<? super T> assertion) {
        return or(() -> expected().get() + " or " + assertion.expected().get(), assertion);
    }

    default Assertion<T> or(Supplier<String> expected, Assertion<? super T> assertion){
        return new Assertion<T>(){
            @Override
            public Supplier<String> expected() {
                return expected;
            }

            @Override
            public boolean test(T value) {
                return Assertion.this.test(value) || assertion.test(value);
            }
        };
    }

    default Assertion<T> negate() {
        return negate(() -> "not " + expected().get());
    }

    default Assertion<T> negate(Supplier<String> expected){
        return new Assertion<T>(){
            @Override
            public Supplier<String> expected() {
                return expected;
            }

            @Override
            public boolean test(T value) {
                return !Assertion.this.test(value);
            }
        };
    }
}
