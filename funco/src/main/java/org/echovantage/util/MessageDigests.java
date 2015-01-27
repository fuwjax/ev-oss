package org.echovantage.util;

import java.security.MessageDigest;
import java.util.function.Function;

import static org.echovantage.util.function.Functions.function;

public class MessageDigests {
	private static final Function<String, MessageDigest> F = function(MessageDigest::getInstance);
	public static final MessageDigest MD5 = F.apply("MD5");
}
