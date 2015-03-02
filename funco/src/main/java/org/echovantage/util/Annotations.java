package org.echovantage.util;

import org.echovantage.util.collection.ReflectList;
import org.echovantage.util.function.Functions;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.summingInt;
import static org.echovantage.util.function.Functions.supplier;

/**
 * Created by fuwjax on 2/26/15.
 */
public class Annotations {
    public static <T> T proxy(Class<T> type, InvocationHandler handler){
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    public static <A extends Annotation> A of(Class<A> annotationType, Map<String, ?> values){
        return proxy(annotationType, new AnnotationHandler(annotationType, values));
    }

    public static <A extends Annotation> A of(Class<A> annotationType, Object value){
        return of(annotationType, singletonMap("value", value));
    }

    public static <A extends Annotation> A of(Class<A> annotationType){
        return of(annotationType, emptyMap());
    }

    private static final class AnnotationHandler implements InvocationHandler, Annotation {
        private static final Method cloneMethod;
        static{
            try {
                cloneMethod = Object.class.getDeclaredMethod("clone");
                cloneMethod.setAccessible(true);
            }catch(ReflectiveOperationException e){
                throw new RunWrapException(e);
            }
        }
        private final Class<? extends Annotation> type;
        private final List<Method> methods;
        private final Map<String, Object> values = new HashMap<>();

        public AnnotationHandler(Class<? extends Annotation> type, Map<String, ?> values) {
            this.type = type;
            methods = Arrays.asList(type.getDeclaredMethods());
            for(Method m: methods){
                Object o = values.get(m.getName());
                if(o == null){
                    o = m.getDefaultValue();
                }
                if (o == null) {
                    throw new IncompleteAnnotationException(type, m.getName());
                }
                this.values.put(m.getName(), o);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(type.equals(method.getDeclaringClass())){
                return copyOf(values.get(method.getName()));
            }
            return method.invoke(this, args);
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return type;
        }

        private static Object copyOf(Object value) throws InvocationTargetException, IllegalAccessException {
            if(!value.getClass().isArray() || Array.getLength(value)==0){
                return value;
            }
            return cloneMethod.invoke(value);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('@').append(type.getName()).append('(').append(values.entrySet().stream()
                    .map(e -> e.getKey() + '=' + valueToString(e.getValue())).collect(Collectors.joining(", ")));
            return builder.append(')').toString();
        }

        private String valueToString(Object value) {
            if(!value.getClass().isArray()){
                return value.toString();
            }
            return ReflectList.asList(value).toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(!type.isInstance(obj)){
                return false;
            }
            try {
                for (Method m : methods) {
                    Object o = m.invoke(obj);
                    if (!Objects.deepEquals(o, values.get(m.getName()))) {
                        return false;
                    }
                }
                return true;
            }catch(ReflectiveOperationException e){
                return false;
            }
        }

        @Override
        public int hashCode() {
            return values.entrySet().stream().collect(summingInt(e -> (127 * e.getKey().hashCode()) ^ valueHash(e.getValue())));
        }

        private static int valueHash(Object value){
            return Arrays.deepHashCode(new Object[]{value})-31;
        }
    }
}
