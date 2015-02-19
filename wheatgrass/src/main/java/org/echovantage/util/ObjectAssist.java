package org.echovantage.util;

import java.util.Arrays;

public interface ObjectAssist {
	Object[] ids();

	default boolean equalsImpl(final Object obj) {
		if(!(obj instanceof ObjectAssist)) {
			return false;
		}
		final ObjectAssist o = (ObjectAssist) obj;
		return Arrays.deepEquals(ids(), o.ids());
	}

	default int hashCodeImpl() {
		return Arrays.hashCode(ids());
	}

	default String toStringImpl() {
		return Arrays.deepToString(ids());
	}

    public abstract class Impl implements ObjectAssist{
        @Override
        public boolean equals(final Object obj) {
            return equalsImpl(obj);
        }

        @Override
        public int hashCode() {
            return hashCodeImpl();
        }

        @Override
        public String toString() {
            return toStringImpl();
        }
    }

    public class Base extends Impl{
        private final Object[] ids;

        protected Base(Object... args){
            ids = Arrays.copyOf(args, args.length +1);
            ids[args.length] = getClass();
        }

        @Override
        public Object[] ids() {
            return ids;
        }
    }
}
