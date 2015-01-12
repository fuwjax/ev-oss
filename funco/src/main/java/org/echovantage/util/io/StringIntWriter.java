package org.echovantage.util.io;

import java.io.IOException;

public class StringIntWriter implements IntWriter {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public void write(int value) {
        builder.appendCodePoint(value);
    }

    @Override
    public void write(CharSequence value) throws IOException {
        builder.append(value);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
