/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IoStreams {
	public static byte[] readAllBytes(final InputStream input) throws IOException {
		final List<ByteBuffer> buffers = new ArrayList<>();
		int c;
		int total = 0;
		byte[] bytes = new byte[4096];
		while((c = input.read(bytes)) != -1) {
			if(c > 0) {
				total += c;
				buffers.add(ByteBuffer.wrap(bytes, 0, c));
				bytes = new byte[4096];
			}
		}
		bytes = new byte[total];
		c = 0;
		for(final ByteBuffer buffer : buffers) {
			c += buffer.remaining();
			buffer.get(bytes, c - buffer.remaining(), buffer.remaining());
		}
		return bytes;
	}
}
