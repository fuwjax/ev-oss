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
#Fuwjax OSS

I use plenty of open source software. When I can, I try to give back to the excellent projects I use, but my contributions to those projects don't measure up to the benefit I derive from them.

My career is generally focused on products that I simply cannot share. However some tools I've written over the years are generic enough that I can share them without any impact to any core business.

These are all tools I use, but they may or may not fit your needs, even where they overlap with mine. I share these only as a way of giving back to the community that has given so much to me.


##Projects

* [funco](funco) - Functions, Collections, Input/Output, and Assertions

* [gild](gild) - Gold Copy Test Harness

* [jerc](jerc) - Java Runtime Compiler

* [metafactory](metafactory) - ServiceLoader META-INF code generator

* [wonton](wonton) - Transport Object Notation

##Usage

When possible, I recommend using the import scope in your maven project.

```
	...
	<dependencyManagement>
		<dependencies>
			...
			<dependency>
				<groupId>org.fuwjax.oss</groupId>
				<artifactId>oss</artifactId>
				<version>0.81</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
```

This will manage the dependencies for all Fuwjax OSS projects. Then to include a project in your build, you no longer need to specify versions. For example to have a test
dependency on gild:

```
	<dependencies>
		...
		<dependency>
			<groupId>org.fuwjax.oss</groupId>
			<artifactId>gild</artifactId>
		</dependency>
```

Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
