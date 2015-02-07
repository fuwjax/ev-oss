/**
 * Copyright (C) 2014 EchoVantage (info@echovantage.com)
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
package org.echovantage.wonton;

import org.echovantage.util.io.IntWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

public class WontonSerial {
    public static String toString(final Wonton result) {
        IntWriter builder = IntWriter.codepointBuffer();
        try {
            new WontonSerial(builder).append(result);
        } catch (IOException e) {
            throw new RuntimeException("Impossible exception", e);
        }
        return builder.toString();
    }

    private final IntWriter writer;

    public WontonSerial(final IntWriter writer) {
        this.writer = writer;
    }

    public void append(final Wonton wonton) throws IOException {
        switch (wonton.type()) {
            case ARRAY:
                appendArray(wonton.asArray());
                break;
            case STRING:
                appendString(wonton.asString());
                break;
            case STRUCT:
                appendObject(wonton.asStruct());
                break;
            case NUMBER:
            case BOOLEAN:
            case VOID:
                writer.write(String.valueOf(wonton.value()));
                break;
            default:
                throw new IllegalArgumentException("No such type: " + wonton.type());
        }
    }

    private void appendObject(final Map<String, ? extends Wonton> struct) throws IOException {
        writer.write('{');
        boolean first = true;
        for (Map.Entry<String, ? extends Wonton> entry : struct.entrySet()) {
            if (first) {
                first = false;
            } else {
                writer.write(',');
            }
            appendString(entry.getKey());
            writer.write(':');
            append(entry.getValue());
        }
        writer.write('}');
    }

    private void appendString(final String string) throws IOException {
        writer.write('"');
        OfInt iter = string.codePoints().iterator();
        while (iter.hasNext()) {
            appendEncode(iter.nextInt());
        }
        writer.write('"');
    }

    private void appendEncode(final int cp) throws IOException {
        switch (cp) {
            case '\b':
                writer.write("\\b");
                return;
            case '\f':
                writer.write("\\f");
                return;
            case '\n':
                writer.write("\\n");
                return;
            case '\r':
                writer.write("\\r");
                return;
            case '\t':
                writer.write("\\t");
                return;
            case '\\':
                writer.write("\\\\");
                return;
            case '"':
                writer.write("\\\"");
                return;
            default:
                // continue
        }
        if (cp >= 0x20 && cp <= 0x7E) {
            writer.write(cp);
        } else if (cp <= 0xFFFF) {
            writer.write(hex(cp));
        } else {
            char[] chars = Character.toChars(cp);
            writer.write(hex(chars[0]));
            writer.write(hex(chars[1]));
        }
    }

    private static String hex(final int cp) {
        return String.format("\\u%04X", cp);
    }

    private void appendArray(final List<? extends Wonton> array) throws IOException {
        writer.write('[');
        boolean first = true;
        for (Wonton wonton : array) {
            if (first) {
                first = false;
            } else {
                writer.write(',');
            }
            append(wonton);
        }
        writer.write(']');
    }
}
