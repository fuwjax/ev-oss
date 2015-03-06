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
package org.echovantage.wonton;

import static java.util.Collections.singletonList;
import static org.echovantage.util.Objects2.nullIf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.tools.internal.ws.spi.WSToolsObjectFactory;
import org.echovantage.util.Objects2;
import org.echovantage.util.Strings;
import org.echovantage.util.collection.ListDecorator;
import org.echovantage.util.collection.MapDecorator;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton {
    public static Type<Void> VOID = w -> null;

    public static Type<Boolean> BOOLEAN = w -> ((WBoolean)w).asBoolean();

    public static Type<Number> NUMBER = w -> ((WNumber)w).asNumber();

    public static Type<String> STRING = w -> ((WString)w).asString();

    public static Type<List<? extends Wonton>> ARRAY = w -> ((WArray)w).asArray();

    public static Type<Map<String, ? extends Wonton>> STRUCT = w -> ((WStruct)w).asStruct();

    public static Type<Integer> INTEGER = w -> nullIf(NUMBER.get(w), Number::intValue);

    public static Type<Byte> BYTE = w -> nullIf(NUMBER.get(w), Number::byteValue);

    public static Type<Short> SHORT = w -> nullIf(NUMBER.get(w), Number::shortValue);

    public static Type<Long> LONG = w -> nullIf(NUMBER.get(w), Number::longValue);

    public static Type<Float> FLOAT = w -> nullIf(NUMBER.get(w), Number::floatValue);

    public static Type<Double> DOUBLE = w -> nullIf(NUMBER.get(w), Number::doubleValue);

    public static Type<String> RELAXED_STRING = w -> nullIf(first(w).value(), String::valueOf);

    public static Type<Boolean> RELAXED_BOOLEAN = w -> Objects2.inferBoolean(first(w).value());

    public static Type<Boolean> RELAXED_NUMBER = w -> Objects2.inferBoolean(first(w).value());

    public static Type<List<? extends Wonton>> RELAXED_ARRAY = Wonton::relaxedArray;

    public static Type<Object> NATURAL = Wonton::naturalValue;

    public static Object naturalValue(Wonton w){
        if(w instanceof WArray){
            return new ListDecorator<>(ARRAY.get(w), Wonton::naturalValue);
        }
        if(w instanceof WStruct){
            return new MapDecorator<>(STRUCT.get(w), Wonton::naturalValue);
        }
        return w.value();
    }

    public static List<? extends Wonton> relaxedArray(Wonton w){
        if(w instanceof WArray){
            return ARRAY.get(w);
        }
        return singletonList(w);
    }

    public static Wonton first(Wonton w){
        if(w == null){
            return NULL;
        }
        if(w instanceof WArray){
            return first(ARRAY.get(w).stream().findFirst().orElse(null));
        }
        if(w instanceof WStruct){
            return first(STRUCT.get(w).values().stream().findFirst().orElse(null));
        }
        return w;
    }

    public static final Wonton NULL = new WVoid(){};
	public static final Wonton TRUE = (WBoolean)() -> true;
	public static final Wonton FALSE = (WBoolean)() -> false;

    public class NoSuchPathException extends RuntimeException {
        public NoSuchPathException(final Path path) {
            super(path.toString());
        }

        public NoSuchPathException(final Throwable cause) {
            super(cause);
        }
    }

    /**
	 * The Visitor interface for {@link Wonton#accept(Visitor)}.
	 * @author fuwjax
	 */
	public interface Visitor {
		/**
		 * Visits a particular entry from the accepting wonton.
		 * @param path the entry path
		 * @param value the entry value
		 */
		public void visit(final Path path, final Wonton value);
	}

    public interface Type<T>{
        T get(Wonton wonton);
    }

    public interface WVoid extends Wonton {
        @Override
        default Type<?> type(){
            return VOID;
        }

        @Override
        default <T> T as(Type<T> type) throws ClassCastException{
            return null;
        }
    }

    public interface WBoolean extends Wonton {
        @Override
        default Type<?> type(){
            return BOOLEAN;
        }

        Boolean asBoolean();
    }

    public interface WNumber extends Wonton {
        @Override
        default Type<?> type(){
            return NUMBER;
        }

        Number asNumber();
    }

    public interface WString extends Wonton {
        @Override
        default Type<?> type(){
            return STRING;
        }

        String asString();
    }

    public interface WArray extends Wonton {
        @Override
        default Type<?> type(){
            return ARRAY;
        }

        List<? extends Wonton> asArray();

        @Override
        default Wonton get(Path path) throws NoSuchPathException {
            try {
                return path.isEmpty() ? this : get(Integer.parseInt(path.key())).get(path.tail());
            }catch(NumberFormatException | IndexOutOfBoundsException e){
                throw new NoSuchPathException(e);
            }
        }

        default Wonton get(int index) throws IndexOutOfBoundsException {
            return asArray().get(index);
        }

        @Override
        default void accept(Path root, final Visitor visitor){
            Wonton.super.accept(root, visitor);
            int index = 0;
            for(Wonton wonton: asArray()){
                wonton.accept(root.append(Integer.toString(index++)), visitor);
            }
        }
    }

    public interface WStruct extends Wonton {
        @Override
        default Type<?> type(){
            return STRUCT;
        }

        Map<String, ? extends Wonton> asStruct();

        @Override
        default Wonton get(final Path path){
            return path.isEmpty() ? this : nullIf(asStruct().get(path.key()), w -> w.get(path.tail()), () -> {throw new NoSuchPathException(path);});
        }

        @Override
        default void accept(Path root, final Visitor visitor){
            Wonton.super.accept(root, visitor);
            for(Map.Entry<String, ? extends Wonton> entry: asStruct().entrySet()){
                entry.getValue().accept(root.append(entry.getKey()), visitor);
            }
        }
    }

    default <T> T as(Type<T> type) throws ClassCastException {
        return type.get(this);
    }

	default Object value() {
		return type().get(this);
	}

	Type<?> type();

    /**
     * Returns the sub-wonton anchored at path.
     *
     * @implNote Implementations should override this method when using custom Path implementations.
     * @param path the qualified relative path to the sub-wonton
     * @return the sub-wonton
     * @throws NoSuchPathException if the path does not exist for this wonton
     */
	default Wonton get(final String path) throws NoSuchPathException {
		return get(Path.path(path));
	}

    /**
     * Returns the sub-wonton anchored at path.
     *
     * @implNote Container implementations should override this method.
     * @param path the qualified relative path to the sub-wonton
     * @return the sub-wonton
     * @throws NoSuchPathException if the path does not exist for this wonton
     */
    default Wonton get(Path path) throws NoSuchPathException {
        if (path.isEmpty()) {
            return this;
        }
        throw new NoSuchPathException(path);
    }

    /**
     * Accepts the visitor for itself and all nested sub-wontons.
     *
     * @implNote Implementations should override this method when using custom Path implementations.
     * @param visitor the visitor which will visit this wonton and all nested sub-wontons
     * @throws NoSuchPathException if the path does not exist for this wonton
     */
	default void accept(final Visitor visitor) {
		accept(Path.EMPTY, visitor);
	}

    /**
     * Accepts the visitor for itself and all nested sub-wontons.
     *
     * @implNote Container implementations should override this method.
     * @param visitor the visitor which will visit this wonton and all nested sub-wontons
     * @throws NoSuchPathException if the path does not exist for this wonton
     */
    default void accept(Path root, Visitor visitor){
        visitor.visit(root, this);
    }
}
