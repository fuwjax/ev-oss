package org.echovantage.wonton;

import org.echovantage.util.Strings;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Created by fuwjax on 3/4/15.
*/
public interface Path {
    public static final Path EMPTY = pathOf();

    public static Path pathOf(String... segments){
        return new BasePath(segments);
    }

    public static Path path(final String path) {
        if(Strings.nullOrEmpty(path)){
            return EMPTY;
        }
        return new BasePath(path.split("\\.\\[|\\]\\.|\\]\\[|\\[|\\]"));
    }

    public static class BasePath implements Path{
        private String[] segments;
        private int offset;
        public BasePath(String... segments){
            this(0, segments);
        }

        public BasePath(int offset, String... segments){
            this.offset = offset;
            this.segments = segments;
        }

        @Override
        public String key() {
            return isEmpty() ? "" : segments[offset];
        }

        @Override
        public Path tail() {
            return offset+1>= segments.length ? EMPTY: new BasePath(offset+1, segments);
        }

        @Override
        public Path append(String suffix) {
            String[] newSegments = Arrays.copyOf(segments, segments.length + 1);
            newSegments[segments.length] = suffix;
            return new BasePath(offset, newSegments);
        }

        @Override
        public boolean isEmpty() {
            return offset >= segments.length;
        }
    }

    String key();

    Path tail();

    Path append(String suffix);

    boolean isEmpty();
}
