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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Members {
	public static List<Field> fields(final Class<?> type, final Predicate<? super Field> filter) {
		return Arrays.asList(type.getDeclaredFields()).stream().filter(filter).map(Members::access).collect(Collectors.toList());
	}

	public static List<Method> methods(final Class<?> type, final Predicate<? super Method> filter) {
		return Arrays.asList(type.getDeclaredMethods()).stream().filter(filter).map(Members::access).collect(Collectors.toList());
	}

	public static List<Constructor<?>> constructors(final Class<?> type, final Predicate<? super Constructor<?>> filter) {
		return Arrays.asList(type.getDeclaredConstructors()).stream().filter(filter).map(Members::access).collect(Collectors.toList());
	}

	public static <T extends AccessibleObject> T access(final T member) {
		member.setAccessible(true);
		return member;
	}

    public static boolean isPublic(final Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static boolean isProtected(final Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    public static boolean isNotStatic(Member member){
        return !Modifier.isStatic(member.getModifiers());
    }

}
