/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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

import static java.util.Base64.getUrlEncoder;
import static org.echovantage.util.Charsets.UTF_8;
import static org.echovantage.util.function.Functions.function;

import java.security.MessageDigest;
import java.util.function.Function;

public class MessageDigests {
	private static final Function<String, MessageDigest> F = function(MessageDigest::getInstance);
	public static final MessageDigest MD5 = F.apply("MD5");

	public static String md5(final CharSequence input) {
		return getUrlEncoder().encodeToString(MD5.digest(input.toString().getBytes(UTF_8)));
	}
}
