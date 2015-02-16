<!--

    Copyright (C) 2015 EchoVantage (info@echovantage.com)

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
#EchoVantage OSS

At EchoVantage we use plenty of open source software. When we can, we try to give back to the excellent projects we use, but our contributions to those projects don't measure up to the benefit we derive from them.

We are a small shop, focused on products that we simply cannot share. However, as in any well-factored project, some tools we've written are generic enough that we can share them without any impact to our core business.

These are all tools we use, but they may or may not fit your needs, even where they overlap with our own. We share these only as a way of giving back to the community that has given so much to us.


##Projects

* [funco](funco) - Functions, Collections, Input/Output, and Assertions

* [gild](gild) - Gold Copy Test Harness

* [jerc](jerc) - Java Runtime Compiler

* [metafactory](metafactory) - ServiceLoader META-INF code generator

* [wonton](wonton) - Transport Object Notation

##Usage

We recommend using the import scope in your maven project.

```
	...
	<dependencyManagement>
		<dependencies>
			...
			<dependency>
				<groupId>org.echovantage</groupId>
				<artifactId>oss</artifactId>
				<version>0.54</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
```

Then to include a project in your build, you no longer need to specify versions. For example to have a test
dependency on gild:

```
	<dependencies>
		...
		<dependency>
			<groupId>org.echovantage</groupId>
			<artifactId>gild</artifactId>
		</dependency>
```

Copyright (C) 2015 EchoVantage (info@echovantage.com)
