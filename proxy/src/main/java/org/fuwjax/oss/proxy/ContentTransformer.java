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
package org.fuwjax.oss.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * <p>Allows applications to transform upstream and downstream content.</p>
 * <p>Typical use cases of transformations are URL rewriting of HTML anchors
 * (where the value of the <code>href</code> attribute of &lt;a&gt; elements
 * is modified by the proxy), field renaming of JSON documents, etc.</p>
 */
public interface ContentTransformer {
	/**
	 * The identity transformer that does not perform any transformation.
	 */
	public static final ContentTransformer IDENTITY = (input, finished, output) -> output.add(input);
	
	public class BufferInputStream extends InputStream{
		private List<ByteBuffer> buffers;
		private ByteBuffer buffer;

		public BufferInputStream(List<ByteBuffer> buffers) {
			this.buffers = new ArrayList<>(buffers);
		}

		@Override
		public int read() throws IOException {
			while(buffer == null || !buffer.hasRemaining()){
				if(buffers.isEmpty()){
					return -1;
				}
				buffer = buffers.remove(0);
			}
			return buffer.get() & 0xFF;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			while(buffer == null || !buffer.hasRemaining()){
				if(buffers.isEmpty()){
					return -1;
				}
				buffer = buffers.remove(0);
			}
			int length = Math.min(buffer.remaining(), len);
			buffer.get(b, off, length);
			if(length < len){
				int count = read(b, off + length, len - length);
				if(count > 0){
					length += count;
				}
			}
			return length;
		}
	}
	
	public class Buffer implements ContentTransformer {
		private BiConsumer<List<ByteBuffer>, List<ByteBuffer>> handler;
		private List<ByteBuffer> inputs = new ArrayList<>();
		
		public Buffer(BiConsumer<List<ByteBuffer>, List<ByteBuffer>> handler){
			this.handler = handler;
		}

		public static Buffer buffer(BiConsumer<InputStream, OutputStream> handler){
			return new Buffer((i,o) -> {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				handler.accept(new BufferInputStream(i), out);
				o.add(ByteBuffer.wrap(out.toByteArray()));
			});
		}
		
		public void transform(ByteBuffer input, boolean finished, List<ByteBuffer> output) throws IOException {
			if(finished){
				inputs.add(input);
				handler.accept(inputs, output);
			}else{
				ByteBuffer buffer = ByteBuffer.allocate(input.remaining());
				buffer.put(input);
				inputs.add(buffer);
			}
		}
	}
	
	/**
	 * <p>Transforms the given input byte buffers into (possibly multiple)
	 * byte buffers.</p> <p>The transformation must happen synchronously in
	 * the context of a call to this method (it is not supported to perform
	 * the transformation in another thread spawned during the call to this
	 * method). The transformation may happen or not, depending on the
	 * transformer implementation. For example, a buffering transformer may
	 * buffer the input aside, and only perform the transformation when the
	 * whole input is provided (by looking at the {@code finished}
	 * flag).</p> <p>The input buffer will be cleared and reused after the
	 * call to this method. Implementations that want to buffer aside the
	 * input (or part of it) must copy the input bytes that they want to
	 * buffer.</p> <p>Typical implementations:</p> <pre> // Identity
	 * transformation (no transformation, the input is copied to the output)
	 * public void transform(ByteBuffer input, boolean finished,
	 * List&lt;ByteBuffer&gt; output) { output.add(input); }
	 *
	 * // Discard transformation (all input is discarded) public void
	 * transform(ByteBuffer input, boolean finished, List&lt;ByteBuffer&gt;
	 * output) { // Empty }
	 *
	 * // Buffering identity transformation (all input is buffered aside
	 * until it is finished) public void transform(ByteBuffer input, boolean
	 * finished, List&lt;ByteBuffer&gt; output) { ByteBuffer copy =
	 * ByteBuffer.allocate(input.remaining()); copy.put(input).flip();
	 * store(copy);
	 *
	 * if (finished) { List&lt;ByteBuffer&gt; copies = retrieve();
	 * output.addAll(copies); } } </pre>
	 *
	 * @param input the input content to transform (may be of length
	 * zero) @param finished whether the input content is finished or more
	 * will come @param output where to put the transformed output
	 * content @throws IOException in case of transformation failures
	 * @param finished true if this is the last call to transform for this request, false otherwise
	 * @param output the transformed results
	 * @throws IOException if there is an error transforming
	 */
	public void transform(ByteBuffer input, boolean finished, List<ByteBuffer> output) throws IOException;
}