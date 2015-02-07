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
package org.echovantage.util.io;

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
