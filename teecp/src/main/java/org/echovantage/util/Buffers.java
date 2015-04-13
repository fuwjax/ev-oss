package org.echovantage.util;

import java.nio.ByteBuffer;

public class Buffers {
	public static void copy(final ByteBuffer src, final ByteBuffer dest) {
		if(src.remaining() <= dest.remaining()) {
			dest.put(src);
		} else {
			final ByteBuffer slice = src.slice();
			slice.limit(dest.remaining());
			dest.put(slice);
			src.position(src.position() + slice.position());
		}
	}
}
