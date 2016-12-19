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
# Wheatgrass
## Healthier Dependency Injection

I love Inversion of Control (IoC) and Dependency Injection (DI) in theory. But the popular tools resemble some sort of arcane magic rather than reliable technology. Why do I need a custom domain language to do basic `new`s, `set`s and method calls? Why can't my DI solution look like Java?

##Dependency

```
	<dependency>
		<groupId>org.fuwjax.oss</groupId>
		<artifactId>wheatgrass</artifactId>
		<version>...</version>
	</dependency>
```

##Usage

Like any DI framework out there, the entry point is an `Injector`.

```
	import org.fuwjax.oss.inject.Injector;
	...
	Injector injector = Injector.newInjector();
	SomeType obj = injector.get(SomeType.class);
```

And like any DI framework out there, you indicate what you want injected on your class with `Inject`.

```
	import javax.inject.Inject;
	...
	public class SomeType {
		// You can inject fields of any visibility.
		@Inject
		private SomeResource resource;

		// You can inject arguments of constructors of any visibility.
		private SomeId id;		
		@Inject
		private SomeType(SomeId id){
			this.id = id;
		}
		
		// You can inject arguments of methods of any visibility.
		private SomeRegistry registry;
		@Inject
		private void setRegistry(SomeRegistry registry){
			this.registry = registry;
		}
	}
```

Some Injection details that may or may not be what you're used to. To pick a constructor, Wheatgrass uses the following selection criteria:
1) The one constructor annotated with @Inject
2) If there's only one constructor, that one
3) If there's a default (no arg) constructor, that one

If there are multiple @Inject constructors or multiple constructors with no @Inject or default, then you'll get an assertion error. (You do run your tests with -ea, right?)

Once constructed, there is no enforced order to how injection occurs. In other words, you may see field and method injections interleaved. If you need to enforce injection order, then build the logic into a method with multiple arguments. For instance, if you needed to make sure you had the `SomeResource` when the `setRegistry` method was called in the example above, you could do the following:

```
	@Inject
	private void setRegistry(SomeResource resource, SomeRegistry registry){
		this.resource = resource;
		this.registry = registry;
		// do your thing
	}
```

It might happen that you'll need to force that the `SomeResource` is created before the `SomeRegistry`. To discuss that we need to talk about bindings.

##Binding

Now, if Wheatgrass were exactly like every other DI framework out there, there wouldn't be much use in writing it. The problem with DI is in creating the bindings - the domain language that links a field, method argument or constructor argument to an object in the injector's registry.

Let's start with the gotcha's. Wheatgrass doesn't use @Singleton, @Scope, or @Provider. Those are binding domain concepts and sadly part of the reason DI tools are overcomplicated. Wheatgrass does have a concept of configuration modules, and there's no other way to define bindings aside from those. Let's look at a simple module.

```
	public class SomeModule {
		public SomeRegistry registry = new SomeRegistry();
	}
	
	...
	
	Injector injector = new Injector(new SomeModule());
```

Yep. That's it. No module interface or lifecycle. You define some objects you want to use and the injector will use those and create anything else it needs along the way. You can even inject the module.

```
	public class SomeModule {
		@Inject
		public SomeRegistry registry;
	}
	
	Injector injector = new Injector(SomeModule.class);
```

This is the standard way of doing @Singleton. Define a module with a field. That field value will always be the used for any injector.get() calls using that module. What about @Provider?

```
	public class SomeModule {
		public SomeResource resource(SomeFactory factory){
			return factory.create();
		}
	}
```

Yep. That's it. You want to do some elaborate process each and every time the Injector creates an object for you? Elaborate to your heart's content, and do it in normal, everyday Java. You don't have to annotate the method. You wouldn't put it in the module if you didn't want the Injector to use it.

This will also force the existence of an argument (In this case SomeFactory) before the return value (here, SomeResource) can be created. If ordering is ever important, use module methods to enforce it.

##Generic Support

Ok, let's take a look at my favorite part about Wheatgrass.

```
	public class SomeObject {
		@Inject
		SomeResource<SomeThing> resource;
	}
	
	public class SomeModule {
		public <T> SomeResource<T> resource(SomeFactory<T> factory) {
			return factory.create();
		}
	}
	
	public class SomeOtherModule {
		@Inject
		SomeFactory<SomeThing> thingFactory;
	}
	
	public class SomeFactory<T> {
		private T t;
		public SomeFactory(T t) {
			this.t = t;
		}
		
		public T create(){
			return t;
		}
	}
	
	Injector injector = Injector.newInjector(SomeModule.class, SomeOtherModule.class);
	SomeObject obj = injector.get(SomeObject.class);
```

Yep. Now I don't know why you'd have a factory that returns the same object every time. Testing maybe? Doesn't matter though. I don't judge. You can do whatever you want, even with generics, and wheatgrass will do the right thing. I whipped up a little Reified Type System that mimics javac. There shouldn't be any surprises, other than the fact that it works. Let me know if you find something you didn't expect, other than its existence.

You might want a `TypeLiteral` at some point. This works a lot like Guice.

```
	import org.fuwjax.oss.generic.TypeLiteral;
	...
	SomeResource<SomeThing> resource = injector.get(new TypeLiteral<SomeResource<SomeThing>>(){});
```

But the major reason for such hacks is usually because of the binding domain language, so you won't find as much of a need for TypeLiterals in Wheatgrass.

##Scoped Injectors

What about @Scope? This is a conflated term with respect to actual language level scoping. Scope can mean that I want to memoize objects so that I can retrieve the same objects as long as I haven't left the scope. It can also mean that I want to have nested scopes that can inherit the state of a parent scope without polluting it. The injector isn't really a scope. It forgets everything it just created in between successive calls to get(). Wheatgrass does support both meanings of scope independently, so you can mix and match as needed.

```
	Injector injector = Injector.newInjector(SomeModule.class);
	Injector memoScope = injector.scope();
	Injector childScope = Injector.newInjector(SomeOtherModule.class, injector);'
```

##Named Injection

Wheatgrass supports @Named to enforce right leg/left leg discernment.

```
	public class SomeModule {
		@Named("primary")
		SomeResource primary;
		@Named("secondary")
		SomeResource secondary;
	}
	
	public class SomeObject {
		@Inject
		@Named("primary")
		SomeResource resource;
	}
```

Note that in this situation injecting a SomeResource without an @Named annotation would work and one would be injected, but there's no telling which one.

Broadly speaking, @Named works the way it does because of @Qualifier, so you can make your own annotations that are annotated @Qualifier and get similar behavior.

```
	@Qualifier
	@Retention(RUNTIME)
	public @interface Primary {}
	
	public class SomeModule {
		@Primary
		SomeResource primary;
	}
	
	public class SomeObject {
		@Inject
		@Primary
		SomeResource resource;
	}
```

You may at times want more complicated allocation of similar objects than @Named or a custom @Qualifier annotation could support. I understand the frustration. An Inversion of Control solution much choose between being either simple or magical. There's little magic to Wheatgrass. It injects dependencies. But it does it very reliably, so when you need to do something complicated, do it in code.

```
	public class SomeModule {
		<T> ServiceLoader<T>

Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
