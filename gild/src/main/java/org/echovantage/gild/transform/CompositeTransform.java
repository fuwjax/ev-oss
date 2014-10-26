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
