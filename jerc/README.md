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
#JeRC
####Java Runtime Compiler

Creating new code at runtime is a powerful feature. Projects like ASM and bcel have been giving us generated
bytecode for a long time. But bytecode generation is difficult to get right and tedious to debug. Java
now exposes the Java compiler at runtime, but not in a way that is convenient for us to use. What we really
want is to compile Java source and have it accessible through a ClassLoader. The JeRC gives us exactly that.

###Usage

There are two main ways to use the JeRC. If you have a set of files generated in the file system, then you can pass the path.
For example if you've generated a file for class "SomeClass" in the "org.example" package at "scripts/generated/org/example/SomeClass," you
could compile and load it with the following:

```
RuntimeClassLoader jerc = new RuntimeClassLoader();
jerc.compile(Paths.get("scripts/generated"));
Class<?> type = jerc.loadClass("org.example.SomeClass");
```

Otherwise, if you're generating classes in a string, then you have to pass in a name for them as well.

```
String code = "package org.example;\n"+
"public class SomeClass implements Callable<String> {\n"+
"	public String call() {\n"+
"		return \"Hello, world!";\n"+
"	}\n"+
"}";
RuntimeClassLoader jerc = new RuntimeClassLoader();
jerc.compile("org.example.SomeClass", code);
Class<?> type = jerc.loadClass("org.example.SomeClass");
Callable<String> call = (Callable<String>)type.newInstance();
```

Of course you can't use "SomeClass" explicitly in your code, as it doesn't exist at build-time. 

If you want to build up classes over time and compile them all at once, the RuntimeCompiler abstraction might be worth
looking at, but most use cases only need the RuntimeClassLoader.

Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
