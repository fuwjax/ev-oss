package org.fuwjax.oss.inject;

import static org.fuwjax.oss.util.function.Functions.function;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fuwjax.oss.generic.Spec;

public class InjectionLibrary {
	public static final InjectionLibrary DEFAULT = new InjectionLibrary();
	
	private ConcurrentMap<Type, Spec> specs = new ConcurrentHashMap<>();
    private ConcurrentMap<Type, Injection> injects = new ConcurrentHashMap<>();
    
    public Spec spec(final Type type){
		return specs.computeIfAbsent(type, function(Spec::new));
    }

    public Injection injection(final Type type) throws ReflectiveOperationException {
        return injects.computeIfAbsent(type, function(this::spec).andThen(Injection::new));
    }
}
