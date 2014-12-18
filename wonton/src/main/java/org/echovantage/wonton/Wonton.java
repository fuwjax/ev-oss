package org.echovantage.wonton;

import org.echovantage.util.Lists;
import org.echovantage.util.ObjectMap;
import org.echovantage.util.ObjectMap.MapEntries;
import org.echovantage.wonton.standard.AbstractListWonton;
import org.echovantage.wonton.standard.AbstractMapWonton;
import org.echovantage.wonton.standard.BooleanWonton;
import org.echovantage.wonton.standard.NullWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StandardPath;
import org.echovantage.wonton.standard.StringWonton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The standard transport interface for Whatever Object NotaTiON. Wontons all
 * open this interface, but the implementation classes may vary wildly. It is
 * suggested to never instanceof with an implementation class, but to instead
 * rely on {@link #type()} for determining the Wonton type information.
 * @author fuwjax
 */
public interface Wonton {
	public static final Wonton NULL = new NullWonton();
	public static final Wonton TRUE = new BooleanWonton(true);
	public static final Wonton FALSE = new BooleanWonton(false);

	public static Path path(final String path) {
		return StandardPath.path(path);
	}

	public static Wonton wontonOf(final Object object) {
		if(object == null) {
			return NULL;
		}
		if(object instanceof Wonton) {
			return (Wonton) object;
		}
		if(object instanceof Boolean) {
			return (Boolean) object ? TRUE : FALSE;
		}
		if(object instanceof Number) {
			return new NumberWonton((Number) object);
		}
		if(object instanceof CharSequence) {
			return new StringWonton(object.toString());
		}
		if(object instanceof Map) {
			return AbstractMapWonton.wontonOf((Map<String, ?>) object);
		}
		if(object instanceof Iterable) {
			return AbstractListWonton.wontonOf(Lists.toList((Iterable<?>) object));
		}
		if(object instanceof Object[]) {
			return AbstractListWonton.wontonOf(Arrays.asList((Object[]) object));
		}
		if(object.getClass().isArray()) {
			return AbstractListWonton.wontonOf(Lists.reflectiveList(object));
		}
		if(object.getClass().isAnnotationPresent(MapEntries.class)) {
			return AbstractMapWonton.wontonOf(ObjectMap.mapOf(object));
		}
		throw new IllegalArgumentException("No standard transformation for " + object.getClass());
	}

	public interface Path {
		String key();

		Path tail();

		Path append(Path suffix);

		boolean isEmpty();
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

	public interface Mutable {
		default Mutable set(Path path, Wonton value){
			assert path != null && !path.isEmpty();
			if(path.tail().isEmpty()){
				set(path.key(), value);
			}else{
				getOrCreate(path.key()).set(path.tail(), value);
			}
			return this;
		}

		Mutable getOrCreate(String key);

		Mutable set(final String key, final Wonton value);

		default Mutable append(Path path, Wonton value){
			assert path != null;
			if(path.isEmpty()){
				append(value);
			}else{
				getOrCreate(path.key()).append(path.tail(), value);
			}
			return this;
		}

		Mutable append(final Wonton value);

		Wonton build();
	}

	public enum Type {
		VOID(wonton -> null),
		BOOLEAN(Wonton::asBoolean),
		NUMBER(Wonton::asNumber),
		STRING(Wonton::asString),
		ARRAY(Wonton::asArray),
		STRUCT(Wonton::asStruct);
		private final Function<Wonton, Object> value;

		private Type(final Function<Wonton, Object> value) {
			this.value = value;
		}

		public Object valueOf(final Wonton wonton) {
			return value.apply(wonton);
		}
	}

	public interface WVoid extends Wonton{
		@Override
		default Type type(){
			return Type.VOID;
		}

		@Override
		default String asString(){
			return null;
		}

		@Override
		default Boolean asBoolean(){
			return null;
		}

		@Override
		default List<? extends Wonton> asArray(){
			return null;
		}

		@Override
		default Map<String, ? extends Wonton> asStruct(){
			return null;
		}

		@Override
		default Number asNumber(){
			return null;
		}
	}

	public interface WString extends Wonton{
		@Override
		default Type type(){
			return Type.STRING;
		}

		@Override
		String asString();
	}

	public interface WBoolean extends Wonton{
		@Override
		default Type type(){
			return Type.BOOLEAN;
		}

		@Override
		Boolean asBoolean();
	}

	public interface WNumber extends Wonton{
		@Override
		default Type type(){
			return Type.NUMBER;
		}

		@Override
		Number asNumber();
	}

	public interface WStruct extends Wonton{
		@Override
		default Type type(){
			return Type.STRUCT;
		}

		@Override
		Map<String, ? extends Wonton> asStruct();

		@Override
		default Wonton get(final String key){
			return asStruct().get(key);
		}

		@Override
		void accept(final Visitor visitor);
	}

	public interface WArray extends Wonton{
		@Override
		default Type type(){
			return Type.ARRAY;
		}

		@Override
		List<? extends Wonton> asArray();

		@Override
		default Wonton get(final String key){
			try {
				return get(Integer.parseInt(key));
			}catch(NumberFormatException e){
				throw new NoSuchPathException(e);
			}
		}

		default Wonton get(int index){
			try{
				return asArray().get(index);
			}catch(IndexOutOfBoundsException e){
				throw new NoSuchPathException(e);
			}
		}

		@Override
		void accept(final Visitor visitor);
	}

	public class InvalidTypeException extends RuntimeException {
	}

	public class NoSuchPathException extends RuntimeException {
		public NoSuchPathException(final Path path) {
			super(path.toString());
		}

		public NoSuchPathException(final Throwable cause) {
			super(cause);
		}
	}

	default String asString() {
		throw new InvalidTypeException();
	}

	default Boolean asBoolean() {
		throw new InvalidTypeException();
	}

	default Number asNumber() {
		throw new InvalidTypeException();
	}

	default Map<String, ? extends Wonton> asStruct() {
		throw new InvalidTypeException();
	}

	default List<? extends Wonton> asArray() {
		throw new InvalidTypeException();
	}

	default Object value() {
		return type().valueOf(this);
	}

	Type type();

	default Wonton get(final Path path) {
		assert path != null;
		Wonton elm = this;
		for(Path p = path; !p.isEmpty(); p = p.tail()){
			elm = elm.get(p.key());
			if(elm == null) {
				throw new NoSuchPathException(path);
			}
		}
		return elm;
	}

	default Wonton get(final String key) { return null;}

	default void accept(final Visitor visitor) {
		// do nothing
	}
}
