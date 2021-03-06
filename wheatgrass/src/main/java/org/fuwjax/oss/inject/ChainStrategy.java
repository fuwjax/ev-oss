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

import org.fuwjax.oss.generic.GenericMember;

import java.lang.reflect.Type;

/**
 * Created by fuwjax on 2/20/15.
 */
public class ChainStrategy implements InjectorStrategy {
    private final InjectorStrategy[] injectors;

    public ChainStrategy(InjectorStrategy... injectors) {
        this.injectors = injectors;
    }

    @Override
    public Binding bindingFor(BindConstraint constraint) {
        for (final InjectorStrategy injector : injectors) {
            if (injector != null) {
                final Binding result = injector.bindingFor(constraint);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
