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
#Funco
####Functions, Collections, I/O and Assertions

Java 8 comes with some powerful new features, but it still feels a little rough around the edges. Funco extends
parts of the core JDK to leverage the power and simplicity of lambdas.

###Functions

The org.fuwjax.oss.util.function package includes a set of functional interfaces that extend the standard java.util.function
interfaces with "unsafe" methods that can throw exceptions. These interfaces may be used directly, or through the "method cast" static 
methods available on the [Functions](src/main/java/org.fuwjax.oss/util/function/Functions.java) class. `RuntimeExceptions are passed through, while checked exceptions
are wrapped in an [UnsafeException](src/main/java/org.fuwjax.oss/util/function/UnsafeException.java) runtime exception. Unsafe exceptions provide a simple way to
rethrow causes by type.

In addition to the Unsafe extensions, the o.e.u.function package also includes a [Deferred](src/main/java/org.fuwjax.oss/util/function/Deferred.java)
class and corresponding static method on Functions that turns a Supplier<T> into a singleton supplier. The source supplier's get() method will be called exactly
once, the result of that call then cached for subsequent calls to the resulting supplier.

###Collections

The org.fuwjax.oss.util.collection package is primarily a set of transforms for the java.util collections. These transformed collections are read-only
but have no internal storage of their own, save for the backing collection. Again, these interfaces may be used directly or accessed through the
[Decorators](src/main/java/org.fuwjax.oss/util/collection/Decorators.java) class.

In addition to the transform decorators, there are two additional generated default-value implementations for List and Map. These utility decorators
deliver a non-null value derived from the key or index used in the respective get method. These implementations are accessible through Decorators.defaultMap()
and Decorators.defaultList().

Finally, there is an implementation for transforming an arbitrary, possibly primitive, array into a List available through Decorators.asList().

###I/O

The pain of byte/char/codepoint reading and writing can be intense. The org.fuwjax.oss.util.io package provides two interfaces IntReader and IntWriter for
consistent access to streams of bytes or text. The static methods available on each interface allow for direct codepoint access to a stream. Just remember 
to use methods like StringBuilder.appendCodePoint() when using this package.

###Assertions

If you've ever tried to write your own org.hamcrest.Matcher you've probably discovered the pain of trying to have your matchers enforce type safety.
Many times you'll have an object that must be cast, to satisfy the matcher's generic type, which frankly makes generics even more than useless.

org.fuwjax.oss.util.assertion tries to remedy this problem. The types work the way you use them, not in theory, but in practice. Assertions.asserts
is the primary way to define your own custom assertions, and the rest of the static methods in that class assert common test situations. We could have
put this package in gild, but assertions can be powerful tools in production code as well.


Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
