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
package org.echovantage.generic;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import org.echovantage.util.ObjectAssist;

/**
 * Created by fuwjax on 2/19/15.
 * @param <M> member type
 */
public abstract class AbstractMember<M extends AccessibleObject & Member & AnnotatedElement> extends ObjectAssist.Impl implements GenericMember {
	private final M m;

	public AbstractMember(final M m) {
		m.setAccessible(true);
		this.m = m;
	}

	@Override
	public M source() {
		return m;
	}

	@Override
	public String name() {
		return m.getName();
	}

	@Override
	public AnnotatedDeclaration declaringClass() {
		return new AnnotatedDeclaration(source().getDeclaringClass());
	}

	@Override
	public MemberAccess access() {
		return MemberAccess.of(m.getModifiers());
	}

	@Override
	public TargetType target() {
		return TargetType.of(m.getModifiers());
	}

	@Override
	public Object[] ids() {
		return GenericMember.ids(this);
	}
}
