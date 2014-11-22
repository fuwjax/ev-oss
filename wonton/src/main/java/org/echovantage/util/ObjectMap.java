package org.echovantage.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ObjectMap {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface MapEntries {
		Class<? extends Enum<?>> value();
	}

	public static Map<String, Object> mapOf(final Object o) {
		if(o == null) {
			return null;
		}
		if(!o.getClass().isAnnotationPresent(MapEntries.class)) {
			throw new IllegalArgumentException("class must be annotated with @MapEntries");
		}
		final MapEntries entries = o.getClass().getAnnotation(MapEntries.class);
		final Map<String, Function> map = new HashMap<>();
		for(final Object prop : entries.value().getEnumConstants()) {
			map.put(prop.toString(), (Function)prop);
		}
		return new MapDecorator<>(map, f -> f.apply(o));
	}
}
