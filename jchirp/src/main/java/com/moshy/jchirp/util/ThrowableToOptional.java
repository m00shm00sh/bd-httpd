package com.moshy.jchirp.util;

import java.util.Optional;
import java.util.function.Supplier;

public class ThrowableToOptional {
    /** Some methods returning type [T] throw [E] instead of returning null on failure.
     * This provides an interface to retrofit that into an [Optional]. */
    public static <T, E extends RuntimeException> Optional<T> orEmpty(Supplier<T> func, Class<E> errorClass) {
        try {
            return Optional.of(func.get());
        } catch (RuntimeException e) {
            if (errorClass.isAssignableFrom(e.getClass()))
                return Optional.empty();
            throw e;
        }
    }
}
