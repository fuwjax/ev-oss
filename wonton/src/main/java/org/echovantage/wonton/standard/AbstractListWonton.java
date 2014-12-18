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
        String delim = "";
        for (final Wonton v : asArray()) {
            builder.append(delim).append(v);
            delim = ",";
        }
        return builder.append("]").toString();
    }
}
