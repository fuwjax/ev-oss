package org.echovantage.util.serial;

public class StringIntWriter implements IntWriter {
    private StringBuilder builder = new StringBuilder();

    @Override
    public void write(int value) {
        builder.appendCodePoint(value);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
