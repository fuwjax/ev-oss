package org.echovantage.util.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.PrimitiveIterator;

public interface IntWriter {
    static IntWriter codepointToUtf8(final OutputStream bytes) {
        return codepointToUtf8(bytes::write);
    }

    static IntWriter codepointToUtf8(final IntWriter bytes) {
        return new Utf8IntWriter(bytes);
    }

    static IntWriter codepointBuffer(){
        return new StringIntWriter();
    }

    static IntWriter codepointToUtf16(IntWriter chars){
        return new Utf16IntWriter(chars);
    }

    static IntWriter codepointToUtf16(Writer writer){
        return codepointToUtf16(writer::write);
    }

    void write(int value) throws IOException;

    default void write(CharSequence value) throws IOException {
        PrimitiveIterator.OfInt iter = value.codePoints().iterator();
        while (iter.hasNext()) {
            write(iter.nextInt());
        }
    }
}
