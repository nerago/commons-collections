package org.apache.commons.collections4.primitive;

@FunctionalInterface
public interface LongBiConsumer {

    /**
     * Performs this operation on the given arguments.
     *
     * @param a the first input argument
     * @param b the second input argument
     */
    void accept(long a, long b);
}