package org.echovantage.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RingBuffer {
	public interface Handler {
		void accept(ByteBuffer buffer) throws IOException;
	}

	private class Packet {
		private final ByteBuffer buffer;
		private Handler handler;

		public Packet(final int size) {
			this.buffer = ByteBuffer.allocateDirect(size);
		}

		public boolean accept(final Handler producer, final Handler consumer) throws IOException {
			if(handler != null) {
				//TODO: log packet drop
				handler = null;
				buffer.clear();
			}
			producer.accept(buffer);
			if(buffer.position() == 0) {
				return false;
			}
			buffer.flip();
			handler = consumer;
			return true;
		}

		public boolean process() {
			if(handler != null && buffer.hasRemaining()) {
				try {
					handler.accept(buffer);
					if(buffer.hasRemaining()) {
						return false;
					}
				} catch(final IOException e) {
					//TODO: log failed packet handle
					// continue
				}
			}
			handler = null;
			buffer.clear();
			return true;
		}
	}

	private final Packet[] packets;
	private long index;
	private long limit;

	public RingBuffer(final int count, final int size) {
		packets = new Packet[count];
		for(int i = 0; i < count; i++) {
			packets[i] = new Packet(size);
		}
	}

	public long insert(final Handler producer, final Handler consumer) throws IOException {
		if(!packet(limit).accept(producer, consumer)) {
			return -1;
		}
		do {
			limit++;
		} while(packet(limit).accept(producer, consumer));
		return limit - 1;
	}

	private Packet packet(final long i) {
		return packets[(int)(i % packets.length)];
	}

	public long consume() {
		long p = Math.max(index, limit - packets.length);
		index = p;
		while(p < limit) {
			if(packet(p).process() && index == p) {
				index++;
			}
			p++;
		}
		return index;
	}
}
