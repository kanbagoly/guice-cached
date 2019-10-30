package com.kanbagoly.guice.cached;

import java.util.Arrays;
import java.util.Objects;

/**
 * Provide equals() and hashCode() methods for the wrapped array.
 *
 * It is necessary to use this wrapper class due to how Java's equals is
 * implemented on arrays and that is why Guava's cache can't handle arrays
 * as a keys of the cache.
 */
class ParameterContainer {

    private final Object[] parameters;

    ParameterContainer(Object[] parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ParameterContainer
                && Arrays.deepEquals(parameters, ((ParameterContainer) other).parameters);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(parameters);
    }

}
