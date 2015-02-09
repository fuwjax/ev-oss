/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
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
package org.echovantage.util.io;

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
