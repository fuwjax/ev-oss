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
package org.echovantage.inject;

import org.echovantage.generic.GenericMember.MemberAccess;
import org.echovantage.generic.TypeTemplate;

import java.lang.reflect.Type;

import static org.echovantage.generic.GenericMember.MemberAccess.PUBLIC;

public class Injector implements ObjectFactory{
    public static Injector newInjector(final Object... modules) throws ReflectiveOperationException {
        InjectorStrategy[] injectors = new InjectorStrategy[modules.length + 1];
        injectors[modules.length] = new SpawnStrategy();
        Injector injector = new Injector(new ChainStrategy(injectors));
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get((Type)modules[i], PUBLIC) : modules[i];
            injectors[i] = module instanceof InjectorStrategy ? (InjectorStrategy) module : new ReflectStrategy(module);
        }
        return injector;
    }

    private final InjectorStrategy strategy;

    protected Injector(final InjectorStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public <T> T inject(final T object) throws ReflectiveOperationException {
        return scope().inject(object);
    }

    @Override
    public <T> T inject(TypeTemplate<T> type, T object) throws ReflectiveOperationException {
        return scope().inject(type, object);
    }

    protected ObjectFactory scope(){
        return new ScopeFactory(strategy);
    }

    @Override
    public Object get(Type type, MemberAccess access) throws ReflectiveOperationException {
        return scope().get(type, access);
    }
}
