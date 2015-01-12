package org.echovantage.util.function;

import java.util.function.IntSupplier;

public interface UnsafeIntSupplier extends IntSupplier {
    int getAsIntUnsafe() throws Exception;

    @Override
    default int getAsInt() {
        try {
            return getAsIntUnsafe();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new UnsafeException(e, "supplier did not get int safely");
        }
    }
}
