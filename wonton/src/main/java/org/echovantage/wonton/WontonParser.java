package org.echovantage.wonton;

import org.echovantage.util.IntSet;
import org.echovantage.util.StringNumber;
import org.echovantage.util.parser.IntReader;
import org.echovantage.util.parser.ParseState;
import org.echovantage.util.parser.Parser;
import org.echovantage.util.serial.IntWriter;
import org.echovantage.wonton.standard.ListWonton;
import org.echovantage.wonton.standard.MapWonton;
import org.echovantage.wonton.standard.NumberWonton;
import org.echovantage.wonton.standard.StringWonton;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.function.IntPredicate;

public class WontonParser {
    private static final IntPredicate WS = Character::isSpaceChar;
    private static final IntPredicate TERMINALS = WS.or(new IntSet().addAll('{', '[', '"', ':', ',', ']', '}', -1));
    private final Parser stream;

    public WontonParser(final IntReader stream) {
        this.stream = new Parser(stream);
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
                return new StringWonton(parseString());
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
                StringNumber number = new StringNumber(buffer);
                try {
                    number.doubleValue(); // test validity
                } catch (NumberFormatException e) {
                    throw start.fail("Value expected");
                }
                return new NumberWonton(number);
        }
    }

    private String parseLiteralString() throws IOException, ParseException {
        IntWriter buffer = IntWriter.codepointBuffer();
        while (!TERMINALS.test(stream.peek())) {
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
            MapWonton wonton = new MapWonton();
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
            ListWonton wonton = new ListWonton();
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
