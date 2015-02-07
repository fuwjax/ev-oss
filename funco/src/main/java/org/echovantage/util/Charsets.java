/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
package org.echovantage.util;

import java.nio.charset.Charset;

public class Charsets {
	/**
	 * UTF-8: eight-bit UCS Transformation Format.
	 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final Charset UTF_16 = Charset.forName("UTF-16");
	public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
	public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
	public static final Charset US_ASCII = Charset.forName("US-ASCII");
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
}
