package org.echovantage.test;

import org.echovantage.inject.Injector;
import org.echovantage.generic.TypeTemplate;
import org.echovantage.sample.SampleMethodModule;
import org.echovantage.sample.SampleModule;
import org.echovantage.sample.SampleResource;
import org.echovantage.sample.SelfReferencingType;
import org.junit.Test;

import java.util.function.Supplier;

import static org.echovantage.util.assertion.Assertions.assertThat;
import static org.echovantage.util.assertion.Assertions.is;

public class InjectorTest {
    @Test
    public void testReturnField() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource(7)));
    }

    @Test
    public void testReturnFieldFromCreatedModule() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(SampleModule.class);
        assertThat(injector.get(SampleResource.class), is(new SampleResource(7)));
    }

    @Test
    public void testReturnMethod() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleMethodModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource(123)));
    }

    @Test
    public void testModuleOrder() throws ReflectiveOperationException {
        final Injector injector = Injector.newInjector(new SampleMethodModule(), new SampleModule());
        assertThat(injector.get(SampleResource.class), is(new SampleResource(123)));
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
        Supplier<SampleResource> supplier = injector.get(new TypeTemplate<Supplier<SampleResource>>(){});
        assertThat(supplier.get(), is(new SampleResource(8)));
    }
}
