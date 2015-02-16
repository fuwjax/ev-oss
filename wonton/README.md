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
#Wonton
####Transport-agnostic object notation

While JSON has become the de facto standard for data exchange on RESTful services, XML and CSV
are still quite common, and newer standards like YAML are becoming common as well. Within Java,
many connection endpoints like HTML containers, JDBC drivers, and JMS queues use varied objects
to define their own data models.

Functionally, these objects are all just key/typed-value collections with a reduced set of available
types. Taking the JSON-specified types as a guide, Wonton aims to be a unified structure for access
and creation of arbitrary key/type/value structures.

###Warning

While Wonton is used extensively in its current form at EchoVantage, the API is still very rough and 
should not be considered final. Use with care.

###Usage

Wonton can be used to define a custom layer to arbitrary data formats and transports. In the future,
the Wonton project may include standard access layers for transport objects such as JDBC ResultSet and PreparedStatement,
or standard codecs for formats such as XML and CSV. Right now, the only native support is the JSON codec.

To create a transport object, it is assumed that the transport will provide a WontonFactory backed by
transport specific objects. For a codec-neutral Wonton, WontonFactory.FACTORY may be used. A WontonFactory has
3 primary ways of creating mutable objects, createStruct(), createArray(), and wontonOf(Object).

To use a transport object, the transport will return an instance of Wonton, generally backed by a transport
specific object. 

Copyright (C) 2015 EchoVantage (info@echovantage.com)
