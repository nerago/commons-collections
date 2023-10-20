package org.apache.commons.collections4.bits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

public interface BitSetInterface<T extends BitSetInterface> extends Bits, Cloneable, java.io.Serializable {
    /**
     * Returns the value of the bit with the specified {@code index}.
     *
     * @param bitIndex index, should be non-negative and &lt; {@link #length()}. The result of passing
     *     negative or out of bounds values is undefined by this interface, <b>just don't do it!</b>
     * @return {@code true} if the bit is set, {@code false} otherwise.
     */
    T get(int startIndex, int endIndex);

    /** Set the bit at {@code bitIndex}. */
    default void set(final int bitIndex) {
        set(bitIndex, bitIndex + 1);
    }

    /**
     * Set the bit at {@code bitIndex} to specified value
     *
     * @param bitIndex specified index
     * @param value value of the bit to set
     */
    default void set(final int bitIndex, final boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    void set(int startIndex, int endIndex);

    default void set(final int startIndex, final int endIndex, final boolean value) {
        if (value) {
            set(startIndex, endIndex);
        } else {
            clear(startIndex, endIndex);
        }
    }

    /** Clear the bit at {@code bitIndex}. */
    default void clear(final int bitIndex) {
        clear(bitIndex, bitIndex + 1);
    }

    /**
     * Clears a range of bits.
     *
     * @param startIndex lower index
     * @param endIndex one-past the last bit to clear
     */
    void clear(int startIndex, int endIndex);

    /**
     * Clear all the bits of the set.
     *
     * <p>Depending on the implementation, this may be significantly faster than clear(0, length).
     */
    default void clear() {
        clear(0, length());
    }


    /**
     * Set specified bit to its complement.
     * @param bitIndex
     */
    default void flip(final int bitIndex) {
        flip(bitIndex, bitIndex + 1);
    }

    /**
     * Flips a range of bits
     *
     * @param startIndex lower index
     * @param endIndex one-past the last bit to flip
     */
    void flip(int startIndex, int endIndex);


    /** Set the bit at {@code i}, returning {@code true} if it was previously set. */
    default boolean getAndSet(int i) {
        final boolean prev = get(i);
        set(i);
        return prev;
    }

    /**
     * Returns the index of the last set bit before or on the index specified. -1 is returned if there
     * are no more set bits.
     */
    int prevSetBit(int index);

    /**
     * Returns the index of the first set bit starting at the index specified. -1 is returned if there
     * are no more set bits.
     */
    int nextSetBit(int index);

    /**
     * Returns the index of the last clear bit before or on the index specified. -1 is returned if there
     * are no more clear bits.
     */
    int prevClearBit(int index);

    /**
     * Returns the index of the first clear bit starting at the index specified. -1 is returned if there
     * are no more clear bits.
     */
    int nextClearBit(int index);

    /** Is bit set entirely clear. */
    boolean isEmpty();

    boolean intersects(T set);

    /** Return the number of bits that are set. NOTE: this method is likely to run in linear time */
    int cardinality();

    void and(T set);

    void or(T set);

    void xor(T set);

    void andNot(T set);

    int hashCode();

    boolean equals(Object obj);

    T clone();

    String toString();

    IntStream stream();

    // TODO maybe ranges only
//    byte[] toByteArray();
//
//    long[] toLongArray();

}
