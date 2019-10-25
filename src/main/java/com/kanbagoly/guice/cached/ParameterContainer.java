package com.kanbagoly.guice.cached;

import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Provide equals() and hashCode() methods for the wrapped array.
 *
 * It is necessary to use this wrapper class as Guava's cache
 * couldn't handle array as a key of the cache. The problem is that
 * the cache can't recognise if the content of two arrays are equal.
 */
class ParameterContainer {

    private final Object[] parameters;

    ParameterContainer(Object[] parameters) {
        this.parameters = Preconditions.checkNotNull(parameters);
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof ParameterContainer)
                && Arrays.deepEquals(parameters, ((ParameterContainer) other).parameters);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(parameters);
    }

}
