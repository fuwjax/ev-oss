package org.echovantage.wonton;

import org.echovantage.util.ListDecorator;
import org.echovantage.util.Lists;
import org.echovantage.util.MapDecorator;
import org.echovantage.util.ObjectMap;
import org.echovantage.wonton.standard.AbstractListWonton;
import org.echovantage.wonton.standard.AbstractMapWonton;
import org.echovantage.wonton.standard.ListWonton;
import org.echovantage.wonton.standard.MapWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StringWonton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface WontonFactory {
    WontonFactory FACTORY = new WontonFactory(){};

    public interface MutableWonton extends Wonton {
        default void set(Wonton.Path path, Wonton value) {
            assert path != null && !path.isEmpty();
            if (path.tail().isEmpty()) {
                set(path.key(), value);
            } else {
                Wonton wonton = get(path.key());
                if (wonton == null) {
                    MutableWonton newWonton = factory().newStruct();
                    newWonton.set(path.tail(), value);
                    set(path.key(), newWonton);
                } else if (wonton instanceof MutableWonton) {
                    ((MutableWonton) wonton).set(path.tail(), value);
                } else {
                    throw new IllegalArgumentException("Cannot set " + path + ", immutable value");
                }
            }
        }

        WontonFactory factory();

        void set(final String key, final Wonton value);

        default MutableWonton with(String path, Object value) {
            set(Wonton.path(path), factory().wontonOf(value));
            return this;
        }
    }

    public interface MutableArray extends MutableWonton{
        MutableArray append(final Wonton value);

        Wonton get(int index);

        void set(int index, Wonton value);

        @Override
        default void set(final String key, final Wonton value){
            try {
                set(Integer.parseInt(key), value);
            }catch(NumberFormatException e){
                throw new NoSuchPathException(e);
            }
        }
    }

    default MutableWonton newStruct(){
        return new MapWonton(this);
    }

    default MutableArray newArray(){
        return new ListWonton(this);
    }

    default Wonton wontonOf(CharSequence value){
        return value == null ? Wonton.NULL : new StringWonton(value.toString());
    }

    default Wonton wontonOf(Number value){
        return value == null ? Wonton.NULL : new NumberWonton(value);
    }

    default Wonton wontonOf(Boolean value){
        return value == null ? Wonton.NULL : value ? Wonton.TRUE : Wonton.FALSE;
    }

    default Wonton wontonOf(Map<String, ?> value){
        return AbstractMapWonton.wrap(new MapDecorator<>(value, this::wontonOf));
    }

    default Wonton wontonOf(List<?> list) {
        return AbstractListWonton.wrap(new ListDecorator<>(list, this::wontonOf));
    }

    default Wonton wontonOf(Object object){
        if(object == null) {
            return Wonton.NULL;
        }
        if(object instanceof Wonton) {
            return (Wonton) object;
        }
        if(object instanceof Boolean) {
            return wontonOf((Boolean) object);
        }
        if(object instanceof Number) {
            return wontonOf((Number) object);
        }
        if(object instanceof CharSequence) {
            return wontonOf(object.toString());
        }
        if(object instanceof Map) {
            return wontonOf((Map<String, ?>) object);
        }
        if(object instanceof Iterable) {
            return wontonOf(Lists.toList((Iterable<?>) object));
        }
        if(object instanceof Object[]) {
            return wontonOf(Arrays.asList((Object[]) object));
        }
        if(object.getClass().isArray()) {
            return wontonOf(Lists.reflectiveList(object));
        }
        if(object.getClass().isAnnotationPresent(ObjectMap.MapEntries.class)) {
            return wontonOf(ObjectMap.mapOf(object));
        }
        throw new IllegalArgumentException("No standard transformation for " + object.getClass());

    }
}
