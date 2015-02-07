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
package org.echovantage.expression;

import org.echovantage.wonton.Wonton;
import org.echovantage.wonton.standard.StandardPath;

/**
 * Created by fuwjax on 2/4/15.
 */
public class PathExpression  implements Expression, Wonton.Path {
    private final String key;
    private PathExpression tail;

    public PathExpression(String segment){
        this.key = segment;
    }

    public PathExpression sub(String segment){
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
    public Wonton.Path tail() {
        return tail == null ? StandardPath.EMPTY : tail;
    }

    @Override
    public Wonton.Path append(Wonton.Path suffix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
