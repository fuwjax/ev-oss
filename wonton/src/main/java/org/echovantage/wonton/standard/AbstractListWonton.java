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
package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

import java.util.List;

public abstract class AbstractListWonton extends AbstractContainerWonton implements Wonton.WArray {
    public static Wonton wrap(List<? extends Wonton> list) {
        return new AbstractListWonton() {
            @Override
            public List<? extends Wonton> asArray() {
                return list;
            }
        };
    }

    @Override
    protected final void acceptShallow(final ShallowVisitor visitor) {
        int index = 0;
        for (final Wonton v : asArray()) {
            visitor.visit(Integer.toString(index++), v);
        }
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder("[");
        String delim = "\n\t";
        for (final Wonton v : asArray()) {
            builder.append(delim).append(v.toString().replaceAll("\n","\n\t"));
            delim = ",\n\t";
        }
        return builder.append("\n]").toString();
    }
}
