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
#Gilded Lily
####Gold copy compare test harness

Gold copy compare is easily one of the worst testing strategies available. Run the test, and then compare the test output to the known correct output.

In theory this sounds like an ideal test strategy. In practice, the "known" correct output is generated as well, so it's no more "golden" than any other output. The
output tends to be very fragile, so if the system under test isn't explicitly producing an output file, then the output is usually a serialized state of the system
under test. So when you refactor, all your "gold" tests suddenly break. In addition, any non-determinism will fail your tests, e.g. timestamps, unordered lists, sets 
or maps, or any multithreaded operations.

Gild attempts to alleviate many of these concerns. Instead of mocking out parts of the system under test, Gild encourages you to test the system in its live state, but 
to mock out the external services (queues, databases, web servers and clients) used by the system. Gild initializes these external systems according to a standard serialization
model, runs your normal jUnit tests, and then serializes the external system state for comparison. The serialized external state can pass through transforms to negate
the impact of non-determinism. Then the serialized state is compared through a standard diff process against the gold copy. If there are errors, you can use any diff
utility including most major IDEs to find all the errors between the gold state and the test state, not just the one reported by the exception.

###Usage
Gild is implements as a jUnit Rule. The Gild Rule is configured with a set of `ServiceProxy objects that describe the external services. For example, a `FileSystemProxy object
moves a set of input files into a specified location before the test and then after the test compares the files in the specified location to the gold copy. Let's walk through the example.

```
package org.example.test;

// the gild source root for this class will be src/test/gild/org.example.test.ExampleTest
public class ExampleTest {
	// the proxy will copy initial files to and persist test files from "target/files"
	private final FileSystemProxy files = new FileSystemProxy(Paths.get("target/files"));
	
	// gild is initialized with a file system proxy named "quotations"
	@Rule
	public final Gild gild = new Gild().with("quotations", files);

	// the gild source root for this test will be src/test/gild/org.example.test.ExampleTest/testSimonSays
	@Test
	public void testSimonSays() {
		// read quotes from the quotes file
		List<String> lines = Files.readAllLines(Paths.get("target/files/quotes.txt"));
		// write what simon says to the output file
		Files.write(Paths.get("target/files/simon.txt"), lines.stream().map(s -> "Simon says, \""+s+"\"")::iterator);
	}
}
```

Now we need to put the `*.txt files in the right place.

```
	src
	+---test
		+---gild
			+---org.example.test.ExampleTest
				+---testSimonSays
					+---input
					|	+---quotations
					|		+---quotes.txt
					+---output
						+---quotations
							+---quotes.txt
							+---simon.txt
```

Each test class gets its own root under src/test/gild. Each test method gets a folder under its test class. Then there is an "input" folder and an "output" folder.
Everything in "input" is used to initialize the corresponding ServiceProxy object. Everything in "output" is the golden copy used to compare against the generated output.
Each proxy gets its own folder under both "input" and "output". The proxy is free to define what the structure is under its input and output folders, but in general, it is 
expected that they obey the same contract, i.e. the input folder could be used as an output folder and vice-versa.

Before the test is run, everything under src/test/gild/org.example.test.ExampleTest/testSimonSays/input/quotations is copied to target/files. After the test is run, everything
in target/files is compared to src/test/gild/org.example.test.ExampleTest/testSimonSays/output/quotations. This is a byte-for-byte comparison, so any discrepencies between the files,
or any files exclusively in either the output or golden copies will result in an error.

The FileSystemProxy is relatively straightforward; the real power of Gild lies in proxies like the DataSourceProxy which loads a set of tables from input CSVs and after the test
exports any tables back to CSVs for comparison. Gild additionally provides an extension point for transforms, along with some stock transforms. Also, tests may be staged with multiple
checkpoints within the test execution.

If you discover a bug or refactor your database, your gold copy will need to change. The easiest way to update the gold copy is through the rule.

```
	@Rule
	public final Gild gild = new Gild().with( ... ).updateGoldCopy();
```

This will update the gold copy, but will fail the build.

Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
