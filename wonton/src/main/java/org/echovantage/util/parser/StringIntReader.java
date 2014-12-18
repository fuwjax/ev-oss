package org.echovantage.util.parser;

import java.io.IOException;
import java.util.PrimitiveIterator;

public class StringIntReader implements IntReader {
    private final PrimitiveIterator.OfInt iter;
    private final CharSequence chars;
    private int count;

    public StringIntReader(CharSequence chars) {
        this.chars = chars;
        iter = chars.codePoints().iterator();
    }

    @Override
    public int read() throws IOException {
        if (iter.hasNext()) {
            count++;
            return iter.nextInt();
        }
        return -1;
    }

    @Override
    public String toString() {
        return chars.subSequence(count, chars.length()).toString();
    }
}
