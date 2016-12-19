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

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.fuwjax.oss.util.Types;

/**
 * Created by fuwjax on 2/19/15.
 */
public class ConstructorMember extends AbstractMember<Constructor<?>> {
    public ConstructorMember(Constructor<?> c) {
        super(c);
    }

    @Override
    public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
    	try{
    		return source().newInstance(args);
    	}catch(ReflectiveOperationException e){
    		throw new ReflectiveOperationException("Could not create "+source(),e);
    	}
    }

    @Override
    public String name() {
        return "new";
    }

    @Override
    public AnnotatedDeclaration[] paramTypes() {
        return AnnotatedDeclaration.of(source().getGenericParameterTypes(), source().getParameterAnnotations());
    }

    @Override
    public AnnotatedDeclaration returnType() {
        return new AnnotatedDeclaration(source().getDeclaringClass(), source());
    }

    @Override
    public MemberType type() {
        return MemberType.CONSTRUCTOR;
    }

    @Override
    public TargetType target() {
        return TargetType.TYPE;
    }
    
    @Override
    public GenericMember coerceReturn(Type type) {
    	if(Types.isAssignable(type, returnType().type())){
//    		Type[] params = Types.coerce(type, returnType().type(), source().getTypeParameters());
//    		Types.subst(returnType().type(), Types.)
    		return this;
    	}
    	return null;
    }
}
