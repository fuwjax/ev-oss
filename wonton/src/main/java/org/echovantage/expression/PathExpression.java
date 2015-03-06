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
package org.echovantage.expression;

import org.echovantage.wonton.Path;
import org.echovantage.wonton.Wonton;

/**
 * Created by fuwjax on 2/4/15.
 */
public class PathExpression implements Expression, Path {
    private final String key;
    private PathExpression tail;

    public PathExpression(String segment) {
        this.key = segment;
    }

    public PathExpression sub(String segment) {
        tail = new PathExpression(segment);
        return tail;
    }

    @Override
    public Object apply(Wonton wonton) {
        return wonton.get(this);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Path tail() {
        return tail == null ? Path.EMPTY : tail;
    }

    @Override
    public Path append(String suffix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
