<!--

    Copyright (C) 2015 fuwjax.org (info@fuwjax.org)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
#MetaFactory
####ServiceLoader META-INF/services annotations

Dependency Injection is a powerful tool; for those of us who can't use Guice or Spring, core Java
has always included a poor man's DI in the ServiceLoader. Creating the ServiceLoader configuration
files in META-INF/services isn't a pain, but it is easy to forget, and sometimes difficult to remember
which services need to be listed and which are no longer part of the build.

Kohsuke Kawaguchi solved this problem a long time ago with [his annotation processor](http://metainf-services.kohsuke.org),
but lately we've wished it could do a little bit more.

MetaFactory allows you an extra level of indirection, generating custom factory instances for
your ServiceLoader services.

###Usage

MetaFactory offers the `MetaService annotation which works identically to Kohsuke's `MetaInfServices annotation

```
@MetaService
public class CustomService implements SomeService {
	...
}
```

A ServiceLoader for SomeService will pick up CustomService.

```
ServiceLoader<SomeService> services = ServiceLoader.load(SomeService.class);
for(SomeService service: services) {
	// one service is an instance of CustomService
}
```

But what happens when services need a non-default constructor to be useful? Traditionally, you'd have to write a factory
for the real service. This factory is what would be loaded by the ServiceLoader.

```
public interface SomeFactory {
	SomeService create(String name);
}

@MetaService
public class CustomFactory implements SomeFactory {
	public SomeService create(String name){
		return new SomeService(name);
	}
}
```

If you have a lot of services, you'll wind up with a bunch of SomeFactory sub-classes. The MetaFactory annotation can generate the 
factory code for you. You still have to create the factory interface, as we did with SomeFactory above, and then you have to create an annotation
that references it.

```
@Retention(RUNTIME)
@Target(TYPE)
@MetaFactory(SomeFactory.class)
public @interface SomeAnnotation {}
```

This custom annotation is what you'll use to mark your services. The service will need to have a constructor that matches the factory
method's signature.

```
@SomeAnnotation
public class CustomService implements SomeService {
	public CustomService(String name) {
		...
	}
	...
}
```

Now MetaFactory will generate the CustomFactory code for you. It will pass the SomeAnnotation on to the factory instance, which means you
can use the custom annotation to select the right factory from the ServiceLoader.

```
@Retention(RUNTIME)
@Target(TYPE)
@MetaFactory(SomeFactory.class)
public @interface RichAnnotation {
	String value();
}

@RichAnnotation("bob")
public class RichService implements SomeService {
	...
}

ServiceLoader<SomeFactory> factories = ServiceLoader.load(SomeFactory.class);
for(SomeFactory factory: factories) {
	RichAnnotation anno = factory.getClass().getAnnotation(RichAnnotation.class);
	if(anno != null && "bob".equals(anno.value())){
		SomeService service = factory.create("hope");
		// always true:
		assert "bob".equals(service.getClass().getAnnotation(RichAnnotation.class).value());
	}
}
```

MetaFactory and MetaService can be used together to generate a META-INF/services file for the same interface. Note that Kohsuke's interface
may not work in tandem, depending on how your project is arranged.

Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
