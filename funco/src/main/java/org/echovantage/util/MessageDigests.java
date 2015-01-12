package org.echovantage.util;

import java.security.MessageDigest;

import static org.echovantage.util.function.Functions.supplier;

public class MessageDigests {
	public static final MessageDigest MD5 = supplier(MessageDigest::getInstance, "MD5").get();
}
