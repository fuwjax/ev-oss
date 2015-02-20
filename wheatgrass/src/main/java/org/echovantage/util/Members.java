package org.echovantage.util;

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
