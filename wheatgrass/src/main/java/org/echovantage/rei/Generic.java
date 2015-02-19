package org.echovantage.rei;

import org.echovantage.util.Arrays2;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by fuwjax on 2/18/15.
 */
public interface Generic {
    public static Generic of(Type type) {
        return type == null ? null : new Rei<>(type);
    }

    boolean isAssignableFrom(Generic value);

    List<GenericMember> members();
}
