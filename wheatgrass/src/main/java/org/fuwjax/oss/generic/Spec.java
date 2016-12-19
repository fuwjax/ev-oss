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
package org.fuwjax.oss.generic;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.fuwjax.oss.util.collection.ListDecorator;

/**
 * Created by fuwjax on 2/18/15.
 */
public class Spec {
    private final Type type;

    private static boolean isNotStatic(Member member){
        return !Modifier.isStatic(member.getModifiers());
    }

    private List<GenericMember> members;

    public Spec(Type type) {
        this.type = type;
        if (type instanceof Class) {
            members = members((Class<?>)type);
        } else {
            ParameterizedType p = (ParameterizedType) type;
            members = new ListDecorator<>(members((Class<?>)p.getRawType()), m -> new ResolvedMember(m, p));
        }
    }

	private static List<GenericMember> members(Class<?> cls) {
		List<GenericMember> members = new ArrayList<>();
		Arrays.asList(cls.getDeclaredConstructors()).forEach(c -> members.add(new ConstructorMember(c)));
		Arrays.asList(cls.getDeclaredMethods()).forEach(m -> members.add(new MethodMember(m)));
		Arrays.asList(cls.getDeclaredFields()).forEach(f -> members.add(new GetFieldMember(f)));
		Arrays.asList(cls.getDeclaredFields()).forEach(f -> members.add(new SetFieldMember(f)));
		for (Class<?> sup = cls.getSuperclass(); sup != null; sup = sup.getSuperclass()) {
		    Arrays.asList(sup.getDeclaredMethods()).stream().filter(Spec::isNotStatic).forEach(m -> members.add(new MethodMember(m)));
		    Arrays.asList(sup.getDeclaredFields()).stream().filter(Spec::isNotStatic).forEach(f -> members.add(new GetFieldMember(f)));
		    Arrays.asList(sup.getDeclaredFields()).stream().filter(Spec::isNotStatic).forEach(f -> members.add(new SetFieldMember(f)));
		}
		return members;
	}

    public Stream<GenericMember> members() {
        return members.stream();
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
