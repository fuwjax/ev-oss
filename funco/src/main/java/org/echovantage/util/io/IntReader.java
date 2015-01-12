package org.echovantage.util.io;

import org.echovantage.util.RunWrapException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterators.AbstractIntSpliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static java.util.stream.StreamSupport.intStream;

public interface IntReader {
    static IntReader codepoints(final CharSequence chars) {
        return new StringIntReader(chars);
    }

    static IntReader codepoints(final IntStream codepoints) {
        final OfInt iter = codepoints.iterator();
        return () -> iter.hasNext() ? iter.nextInt() : -1;
    }

    static IntReader utf8ToCodepoint(final IntReader bytes) {
        return new Utf8IntReader(bytes);
    }

    static IntReader utf8ToCodepoint(final InputStream bytes) {
        return utf8ToCodepoint(bytes::read);
    }

    static IntReader utf8ToCodepoint(final ByteBuffer bytes) {
        return utf8ToCodepoint(() -> bytes.hasRemaining() ? bytes.get() : -1);
    }

    static IntReader charToCodepoint(final IntReader chars) {
        return new Utf16IntReader(chars);
    }

    static IntReader charToCodepoint(final Reader reader) {
        return charToCodepoint(reader::read);
    }

    /**
     * Returns the next byte, char, or codepoint from the stream.
     *
     * @return the next codepoint or -1 if the stream has ended
     * @throws IOException if there is an error reading from the underlying
     *                     stream
     */
    int read() throws IOException;

    default IntStream stream() {
        return intStream(new AbstractIntSpliterator(Long.MAX_VALUE, 0) {
            @Override
            public boolean tryAdvance(IntConsumer action) {
                try {
                    int c = read();
                    if (c != -1) {
                        action.accept(c);
                    }
                    return c != -1;
                }catch(IOException e){
                    throw new RunWrapException(e, "Failed reading from IntReader");
                }
            }
        }, false);
    }
}
