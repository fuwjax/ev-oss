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

import org.echovantage.util.Annotations;

import javax.inject.Named;
import java.lang.reflect.Type;

public class Injector implements ObjectFactory {
    public static Injector newInjector(final Object... modules) throws ReflectiveOperationException {
        InjectorStrategy[] injectors = new InjectorStrategy[modules.length + 1];
        Injector injector = new Injector(new ChainStrategy(injectors));
        injectors[modules.length] = injector::spawn;
        for (int i = 0; i < modules.length; i++) {
            Object module = modules[i] instanceof Type ? injector.get(new BindConstraint((Type) modules[i])) : modules[i];
            injectors[i] = module instanceof InjectorStrategy ? (InjectorStrategy) module : new ReflectStrategy(module);
        }
        return injector;
    }

    public static Named named(String name) {
        return Annotations.of(Named.class, name);
    }

    private Binding spawn(BindConstraint constraint) {
        return scope -> scope.create(constraint);
    }

    private final InjectorStrategy strategy;

    protected Injector(final InjectorStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void inject(BindConstraint constraint, Object target) throws ReflectiveOperationException {
        scope().inject(constraint, target);
    }

    public Scope scope() {
        return new Scope(strategy);
    }

    @Override
    public Object get(BindConstraint constraint) throws ReflectiveOperationException {
        return scope().get(constraint);
    }
}
