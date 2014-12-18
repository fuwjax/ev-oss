package org.echovantage.util.serial;

import java.io.IOException;

import static java.lang.Integer.highestOneBit;

public class Utf8IntWriter implements IntWriter {
    private final IntWriter writer;

    public Utf8IntWriter(final IntWriter writer) {
        this.writer = writer;
    }

    public void write(final int cp) throws IOException {
        if (cp <= 0x7F) {
            writer.write(cp);
        } else {
            int multis = (highestOneBit(cp) - 6) / 5;
            int mask = (-1 << (7 - multis)) & 0xFF;
            writer.write(cp >>> 6 * multis | mask);
            for (int s = (multis - 1) * 6; s >= 0; s -= 6) {
                writer.write((cp >>> s) & 0x3F | 0x80);
            }
        }
    }

    @Override
    public String toString() {
        return writer.toString();
    }
}
