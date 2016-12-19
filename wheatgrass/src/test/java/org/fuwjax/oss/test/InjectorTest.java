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
package org.fuwjax.oss.test;

import static org.fuwjax.oss.util.assertion.Assertions.assertThat;
import static org.fuwjax.oss.util.assertion.Assertions.is;

import java.util.function.Supplier;

import org.fuwjax.oss.generic.TypeLiteral;
import org.fuwjax.oss.inject.Injector;
import org.fuwjax.oss.sample.SampleGenericInject;
import org.fuwjax.oss.sample.SampleNamedDependency;
import org.fuwjax.oss.sample.SampleResource;
import org.fuwjax.oss.sample.SelfReferencingType;
import org.fuwjax.oss.sample.ServiceLoaderInject;
import org.fuwjax.oss.sample.module.SampleAnnotatedModule;
import org.fuwjax.oss.sample.module.SampleMethodModule;
import org.fuwjax.oss.sample.module.SampleModule;
import org.fuwjax.oss.sample.module.ServiceLoaderModule;
import org.junit.Test;

public class InjectorTest {
    @Test
    public void testReturnField() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource("public")));
    }

    @Test
    public void testReturnFieldFromCreatedModule() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(SampleModule.class);
        assertThat(injector.get(SampleResource.class), is(new SampleResource("public")));
    }

    @Test
    public void testReturnMethod() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleMethodModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource("method")));
    }

    @Test
    public void testModuleOrder() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleMethodModule(), new SampleModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource("method")));
    }

    @Test
    public void testSelfReferencingInjection() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector();
        SelfReferencingType self = injector.get(SelfReferencingType.class);
        assertThat(self.getSelf(), is(self));
    }

    @Test
    public void testSupplier() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleModule());
        Supplier<SampleResource> supplier = injector.get(new TypeLiteral<Supplier<SampleResource>>(){});
        assertThat(supplier.get(), is(new SampleResource("private")));
    }

    @Test
    public void testAnnotated() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleAnnotatedModule());
        assertThat(injector.get(SampleResource.class, Injector.named("first")), is(new SampleResource("public.first")));
    }

    @Test
    public void testFieldAnnotated() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleAnnotatedModule());
        assertThat(injector.get(SampleNamedDependency.class).resource(), is(new SampleResource("public.second")));
    }

    @Test
    public void testVariableInjection() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleModule());
        assertThat(injector.get(new TypeLiteral<SampleGenericInject<SampleResource>>(){}).value(), is(new SampleResource("public")));
    }

    @Test
    public void testServiceLoaderInjection() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new ServiceLoaderModule());
        assertThat(injector.get(ServiceLoaderInject.class).getClass(), is(ServiceLoaderInject.class));
    }
}
