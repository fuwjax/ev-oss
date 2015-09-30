/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
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
package org.fuwjax.oss.wonton;

import org.fuwjax.oss.util.BitSets;
import org.fuwjax.oss.util.io.IntReader;
import org.fuwjax.oss.util.io.IntWriter;
import org.fuwjax.oss.util.parser.ParseState;
import org.fuwjax.oss.util.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

import static org.fuwjax.oss.wonton.WontonFactory.newArray;
import static org.fuwjax.oss.wonton.WontonFactory.newStruct;

public class WontonParser {
    private static final Pattern DOUBLE = Pattern.compile("[.Ee]");
    private static final IntPredicate WS = Character::isWhitespace;
    private static final IntPredicate RESERVED = BitSets.bitSetOf('{', '[', '"', ':', ',', ']', '}')::get;
    private static final IntPredicate LITERALS = Parser.EOF.or(WS).or(RESERVED).negate();
    private final Parser stream;
    private final WontonFactory factory;

    public WontonParser(final IntReader stream) {
        this(stream, WontonFactory.FACTORY);
    }

    public WontonParser(final ByteBuffer buffer) {
        this(IntReader.utf8ToCodepoint(buffer));
    }

    public WontonParser(final InputStream input) {
        this(IntReader.utf8ToCodepoint(input));
    }

    public WontonParser(final Reader reader) {
        this(IntReader.charToCodepoint(reader));
    }

    public WontonParser(final CharSequence input) {
        this(IntReader.codepoints(input));
    }

    public WontonParser(IntReader stream, WontonFactory factory) {
        this.stream = new Parser(stream);
        this.factory = factory;
    }

    public Wonton parse() throws ParseException, IOException {
        return parseValue();
    }

    private Wonton parseValue() throws ParseException, IOException {
        int cp = stream.skip(WS).peek();
        switch (cp) {
            case '{':
                return parseObject();
            case '[':
                return parseArray();
            case '"':
                return factory.wontonOf(parseString());
            default:
                //continue
        }
        return parseLiteralValue();
    }

    private String parseKey() throws IOException, ParseException {
        int cp = stream.skip(WS).peek();
        if (cp == '"') {
            return parseString();
        }
        String literal = parseLiteralString();
        if (literal.isEmpty()) {
            throw stream.fail("Key expected");
        }
        return literal;
    }

    private Wonton parseLiteralValue() throws IOException, ParseException {
        ParseState start = stream.state();
        String buffer = parseLiteralString();
        switch (buffer) {
            case "null":
                return Wonton.NULL;
            case "true":
                return Wonton.TRUE;
            case "false":
                return Wonton.FALSE;
            default:
                try {
                    return factory.wontonOf(numberOf(buffer));
                } catch (NumberFormatException e) {
                    throw start.fail("Value expected");
                }
        }
    }

    private Number numberOf(String buffer) {
        if(DOUBLE.matcher(buffer).find()){
            return Double.parseDouble(buffer);
        }
        long l = Long.parseLong(buffer);
        if((int)l == l){
            return (int)l;
        }
        return l;
    }

    private String parseLiteralString() throws IOException, ParseException {
        IntWriter buffer = IntWriter.codepointBuffer();
        while (LITERALS.test(stream.peek())) {
            buffer.write(stream.read());
        }
        return buffer.toString();
    }

    private String parseString() throws IOException, ParseException {
        stream.expect('"');
        IntWriter buffer = IntWriter.codepointBuffer();
        while (!stream.isa('"')) {
            if (stream.isa('\\')) {
                buffer.write(escape());
            } else {
                buffer.write(stream.read());
            }
        }
        return buffer.toString();
    }

    private int escape() throws IOException, ParseException {
        int cp = stream.read();
        switch (cp) {
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                IntWriter buffer = IntWriter.codepointBuffer();
                buffer.write(stream.read());
                buffer.write(stream.read());
                buffer.write(stream.read());
                buffer.write(stream.read());
                return Integer.parseInt(buffer.toString(), 16);
            default:
                return cp;
        }
    }

    private Wonton parseObject() throws IOException, ParseException {
        ParseState start = stream.expect('{');
        try {
            WontonFactory.MutableWonton wonton = newStruct(LinkedHashMap::new);
            if (!stream.skip(WS).isa('}')) {
                do {
                    String key = parseKey();
                    stream.skip(WS).expect(':');
                    Wonton value = parseValue();
                    wonton.set(key, value);
                } while (stream.skip(WS).isa(','));
                stream.expect('}');
            }
            return wonton;
        } catch (ParseException p) {
            throw start.fail("while parsing object", p);
        }
    }

    private Wonton parseArray() throws IOException, ParseException {
        ParseState start = stream.expect('[');
        try {
            WontonFactory.MutableArray wonton = newArray(ArrayList::new);
            if (!stream.skip(WS).isa(']')) {
                do {
                    Wonton value = parseValue();
                    wonton.append(value);
                } while (stream.skip(WS).isa(','));
                stream.expect(']');
            }
            return wonton;
        } catch (ParseException p) {
            throw start.fail("while parsing array", p);
        }
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}
