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

import org.fuwjax.oss.util.Objects2;
import org.fuwjax.oss.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    Pattern PATH = Pattern.compile("(\\w+)\\.?|(?<!\\.)\\[([^\\]]+)\\]");
    public static Path path(final String path) {
        if(Strings.nullOrEmpty(path)){
            return EMPTY;
        }
        List<String> segments = new ArrayList<>();
        Matcher matcher = PATH.matcher(path);
        while(matcher.find()){
            segments.add(Objects2.coalesce(matcher.group(1), matcher.group(2)));
        }
        assert matcher.hitEnd();
        return new BasePath(segments.toArray(new String[segments.size()]));
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

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Path){
                Path o = (Path) obj;
                if(isEmpty()){
                    return o.isEmpty();
                }
                return key().equals(o.key()) && tail().equals(o.tail());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return isEmpty() ? 0 : key().hashCode() * 31 + tail().hashCode();
        }

        @Override
        public String toString() {
            if(isEmpty()){
                return "";
            }
            String base = key();
            if(base.contains(".")){
                base = "["+base.replaceAll("\\]","\\\\]")+"]";
            }
            return tail().isEmpty() ? base : base+"."+tail();
        }
    }

    String key();

    Path tail();

    Path append(String suffix);

    boolean isEmpty();
}
