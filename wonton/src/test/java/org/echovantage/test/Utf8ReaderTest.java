package org.echovantage.test;

import org.echovantage.util.io.ByteCountingInputStream;
import org.echovantage.util.parser.IntReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

public class Utf8ReaderTest {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test
    public void testReader() throws IOException {
        for (int cp = 0; cp < Character.MAX_CODE_POINT; cp++) {
            if (Character.isDefined(cp)
                    && !Character.isHighSurrogate((char) cp)
                    && !Character.isLowSurrogate((char) cp)) {
                assertCodepoint(cp, cp);
                assertArrayEquals(realcode(cp), fauxcode(cp));
            }
        }
    }

    @Test
    public void testInvalid() throws IOException {
        // continuation bytes with no leading byte is invalid
        for (int i = 0x80; i < 0xC0; i++) {
            assertEncoding(fauxcode(i, 1), 0xFFFD);
        }
        // overlong sequences are invalid
        for (int i = 0; i < 1 << 7; i++) {
            assertEncoding(fauxcode(i, 2), 0xFFFD);
            assertEncoding(fauxcode(i, 3), 0xFFFD);
            assertEncoding(fauxcode(i, 4), 0xFFFD);
        }
        for (int i = 1 << 7; i < 1 << 11; i++) {
            assertEncoding(fauxcode(i, 3), 0xFFFD);
            assertEncoding(fauxcode(i, 4), 0xFFFD);
        }
        for (int i = 1 << 11; i < 1 << 16; i++) {
            assertEncoding(fauxcode(i, 4), 0xFFFD);
        }
        // sequences over max char are invalid
        for (int i = 0x110000; i < 1 << 21; i++) {
            assertEncoding(fauxcode(i), 0xFFFD);
        }
        // UTF-16 surrogates are invalid
        for (int i = 0xD800; i < 0xE000; i++) {
            assertEncoding(fauxcode(i), 0xFFFD);
        }
        // underlong sequences are invalid
        for (int i = 1 << 7; i < 1 << 21; i += 1 << 6) {
            byte[] bytes = fauxcode(i);
            while (bytes.length > 1) {
                assertEncoding(replaceLast(bytes, 0x24), 0xFFFD);
                bytes = stripLast(bytes);
                assertEncoding(bytes, 0xFFFD);
            }
        }
    }

    private static byte[] stripLast(final byte[] bytes) {
        return Arrays.copyOfRange(bytes, 0, bytes.length - 1);
    }

    private static byte[] replaceLast(final byte[] bytes, final int b) {
        bytes[bytes.length - 1] = (byte) b;
        return bytes;
    }

    private static byte[] fauxcode(final int cp) {
        if (cp < 1 << 7) {
            return fauxcode(cp, 1);
        }
        if (cp < 1 << 11) {
            return fauxcode(cp, 2);
        }
        if (cp < 1 << 16) {
            return fauxcode(cp, 3);
        }
        return fauxcode(cp, 4);
    }

    private static byte[] fauxcode(int cp, final int length) {
        if (length == 1) {
            return new byte[]{(byte) cp};
        }
        byte[] bytes = new byte[length];
        for (int i = length - 1; i >= 0; i--) {
            bytes[i] = (byte) (cp & 0x3F);
            bytes[i] |= 0x80;
            cp >>= 6;
        }
        bytes[0] |= (byte) 0x80 >> length - 1;
        return bytes;
    }

    private static void assertCodepoint(final int cp, final int expected) throws IOException {
        assertEncoding(fauxcode(cp), expected);
    }

    private static byte[] realcode(final int cp) {
        return new String(Character.toChars(cp)).getBytes(UTF_8);
    }

    private static void assertEncoding(final byte[] bytes, final int expected) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             ByteCountingInputStream counter = new ByteCountingInputStream(input)) {
            IntReader reader = IntReader.utf8ToCodepoint(counter);
            int codepoint = reader.read();
            assertEquals(bytes.length, counter.count());
            assertEquals(expected, codepoint);
        }
    }
}
