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
package org.fuwjax.oss.inject;

import static org.fuwjax.oss.generic.GenericMember.MemberType.CONSTRUCTOR;
import static org.fuwjax.oss.generic.GenericMember.TargetType.INSTANCE;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.fuwjax.oss.generic.GenericMember;
import org.fuwjax.oss.generic.Spec;

public class Injection {
    private final GenericMember constructor;
    private final List<GenericMember> members;

    Injection(final Spec spec) {
        constructor = constructor(spec);
        members = spec.members().filter(INSTANCE.and(Injection::isInject)).collect(Collectors.toList());
    }

    private static GenericMember constructor(final Spec type) {
        final List<GenericMember> constructors = type.members().filter(CONSTRUCTOR).collect(Collectors.toList());
        if (constructors.size() == 0) {
            return null;
        }
        if (constructors.size() == 1) {
            return constructors.get(0);
        }
        final List<GenericMember> injects = constructors.stream().filter(Injection::isInject).collect(Collectors.toList());
        if (injects.isEmpty()) {
            return constructors.stream().filter(m -> m.paramTypes().length == 0).findAny().orElse(null);
        }
        if (injects.size() == 1) {
            return injects.get(0);
        }
        return null;
    }

    private static boolean isInject(final GenericMember member) {
        return member.source().isAnnotationPresent(Inject.class);
    }

    public Stream<GenericMember> members() {
        return members.stream();
    }

    public GenericMember constructor() {
    	assert constructor != null: "Inject specifications require a single @Inject constructor, a default constructor, or only one constructor on the class";
        return constructor;
    }
}
