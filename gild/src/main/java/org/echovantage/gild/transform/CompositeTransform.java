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
package org.echovantage.gild.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class CompositeTransform implements StreamTransform {
	private final StreamTransform[] transforms;

	public CompositeTransform(final StreamTransform... transforms) {
		assert transforms.length > 0;
		this.transforms = transforms;
	}

	@Override
	public void transform(final InputStream input, final OutputStream output) throws IOException {
		InputStream in = input;
		ByteArrayOutputStream out = null;
		for(final StreamTransform transform : transforms) {
			out = new ByteArrayOutputStream();
			transform.transform(in, out);
			in = new ByteArrayInputStream(out.toByteArray());
		}
		final ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
		final WritableByteChannel channel = Channels.newChannel(output);
		while(buffer.hasRemaining()) {
			channel.write(buffer);
		}
	}
}
