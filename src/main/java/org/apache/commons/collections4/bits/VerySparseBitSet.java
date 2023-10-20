// see https://issues.apache.org/jira/projects/COLLECTIONS/issues/COLLECTIONS-743?filter=allopenissues

package org.apache.commons.collections4.bits;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.collections4.primitive.LongToLongMap;

public class VerySparseBitSet implements BitSetInterface<VerySparseBitSet> {
    private static final int ENTRY_BITS = Long.SIZE;
    private static final long FULLY_SET =  0xffffffffffffffffL;
    private static final long HI_BIT_SET = 0x8000000000000000L;
    private final LongToLongMap map = new LongToLongMap();
    private int length;

    private static int indexToBlockStart(final int bitIndex) {
        return (bitIndex >>> 6) << 6;
    }
    
    private static void checkRange(final int startIndex, final int endIndex) {
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkRange(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException();
        }
    }

    
//    private SparseEntry findEntry(final int index) {
//        if (index < 0) {
//            throw new IndexOutOfBoundsException();
//        }
//        final int blockStartIndex = indexToBlockStart(index);
//        return ListUtils.binarySearchValue(content, e -> e.startIndex, blockStartIndex);
//    }

    private static long maskSingleSet(final int n) {
        return HI_BIT_SET >>> n;
    }

    private static long maskFirstNSet(final int n) {
        return FULLY_SET << (Long.SIZE - n);
    }

    private static long maskLastNSet(final int n) {
        return FULLY_SET >>> (Long.SIZE - n);
    }

    private static long maskRangeSet(final int a, final int z) {
        return (FULLY_SET << (Long.SIZE - z)) & (FULLY_SET >>> a);
    }

    private static long maskFirstNClear(final int n) {
        return FULLY_SET >>> n;
    }

    private static Long nullIfZero(final long val) {
        return val == 0 ? null : val;
    }

    /**
     * Is bit set entirely clear.
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns the number of bits in this set.
     * Maximum bit index range with storage allocated.
     */
    @Override
    public int length() {
        return length;
    }

    /**
     * Return the number of bits that are set. NOTE: this method is likely to run in linear time
     */
    @Override
    public int cardinality() {
        int result = 0;
        final PrimitiveIterator.OfLong it = map.valuesIterator();
        while (it.hasNext()) {
            result += Long.bitCount(it.next());
        }
        return result;
    }
    
    /**
     * Returns the value of the bit with the specified {@code index}.
     *
     * @param index index, should be non-negative and &lt; {@link #length()}.
     *              Positive out of range values will return false.
     * @return {@code true} if the bit is set, {@code false} otherwise.
     * @throws IndexOutOfBoundsException for negative values
     */
    @Override
    public boolean get(final int index) {
        final int blockStartIndex = indexToBlockStart(index);
        final Long bits = map.getOrDefaultNullable(blockStartIndex, null);
        if (bits != null) {
            return (bits & maskSingleSet(index - blockStartIndex)) != 0;
        } else {
            return false;
        }
    }

    @Override
    public void set(final int bitIndex) {
        checkRange(bitIndex);
        final int blockStart = indexToBlockStart(bitIndex);
        final long mask = maskSingleSet(bitIndex - blockStart);
        map.compute(blockStart, (key, present, oldValue) -> present ? (oldValue | mask) : mask);
    }

    @Override
    public void set(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);

        int blockStart = indexToBlockStart(startIndex);
        final int lastBlockStart = indexToBlockStart(endIndex);

        if (blockStart == lastBlockStart) {
            final long mask = maskRangeSet(startIndex - blockStart, endIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> present ? (oldValue | mask) : mask);
            return;
        } else if (startIndex != blockStart) {
            final long mask = maskFirstNClear(startIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> (present ? (oldValue | mask) : mask));
            blockStart += ENTRY_BITS;
        }

        while (blockStart + ENTRY_BITS < endIndex) {
            map.put(blockStart, FULLY_SET);
            blockStart += ENTRY_BITS;
        }

        if (blockStart <= endIndex) {
            final long mask = maskFirstNSet(endIndex - blockStart + 1);
            map.compute(blockStart, (key, present, oldValue) -> present ? (oldValue | mask) : mask);
        }
    }

    @Override
    public void clear(final int bitIndex) {
        checkRange(bitIndex);
        final int blockStart = indexToBlockStart(bitIndex);
        final long mask = ~maskSingleSet(bitIndex - blockStart);
        map.compute(blockStart, (key, present, oldValue) -> present ? nullIfZero(oldValue & mask) : null);
    }

    /**
     * Clears a range of bits.
     *
     * @param startIndex lower index
     * @param endIndex   one-past the last bit to clear
     */
    @Override
    public void clear(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);

        int blockStart = indexToBlockStart(startIndex);
        final int lastBlockStart = indexToBlockStart(endIndex);

        if (blockStart == lastBlockStart) {
            final long mask = ~maskRangeSet(startIndex - blockStart, endIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> present ? nullIfZero(oldValue & mask) : null);
            return;
        } else if (startIndex != blockStart) {
            final long mask = ~maskFirstNClear(startIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> present ? nullIfZero(oldValue & mask) : null);
            blockStart += ENTRY_BITS;
        }

        while (blockStart + ENTRY_BITS < endIndex) {
            map.remove(blockStart);
            blockStart += ENTRY_BITS;
        }

        if (blockStart <= endIndex) {
            final long mask = ~maskFirstNSet(endIndex - blockStart + 1);
            map.compute(blockStart, (key, present, oldValue) -> present ? nullIfZero(oldValue & mask) : null);
        }
    }

    /**
     * Flips a range of bits
     *
     * @param startIndex lower index
     * @param endIndex   one-past the last bit to flip
     */
    @Override
    public void flip(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);

        int blockStart = indexToBlockStart(startIndex);
        final int lastBlockStart = indexToBlockStart(endIndex);

        if (blockStart == lastBlockStart) {
            final long mask = maskRangeSet(startIndex - blockStart, endIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> nullIfZero(present ? (oldValue ^ mask) : mask));
            return;
        } else if (startIndex != blockStart) {
            final long mask = maskFirstNClear(startIndex - blockStart);
            map.compute(blockStart, (key, present, oldValue) -> nullIfZero(present ? (oldValue ^ mask) : mask));
            blockStart += ENTRY_BITS;
        }

        while (blockStart + ENTRY_BITS < endIndex) {
            map.compute(blockStart, (key, present, oldValue) -> nullIfZero(present ? ~oldValue : FULLY_SET));
            blockStart += ENTRY_BITS;
        }

        if (blockStart <= endIndex) {
            final long mask = maskFirstNSet(endIndex - blockStart + 1);
            map.compute(blockStart, (key, present, oldValue) -> nullIfZero(present ? (oldValue ^ mask) : mask));
        }
    }

    @Override
    public void clear() {
        map.clear();
        length = 0;
    }

    /**
     * Returns the index of the first set bit starting at the index specified. -1 is returned if there
     * are no more set bits.
     *
     * @param index
     */
    @Override
    public int nextSetBit(final int index) {
        checkRange(index);
        // a linkedmap would be good right now
        return 0;
    }

    /**
     * Returns the index of the first clear bit starting at the index specified. -1 is returned if there
     * are no more clear bits.
     *
     * @param index
     */
    @Override
    public int nextClearBit(int index) {
        return 0;
    }

    /**
     * Returns the index of the last set bit before or on the index specified. -1 is returned if there
     * are no more set bits.
     *
     * @param index
     */
    @Override
    public int prevSetBit(int index) {
        return 0;
    }

    /**
     * Returns the index of the last clear bit before or on the index specified. -1 is returned if there
     * are no more clear bits.
     *
     * @param index
     */
    @Override
    public int prevClearBit(int index) {
        return 0;
    }

    /**
     * Returns the value of the bit with the specified {@code index}.
     *
     * @param startIndex
     * @param endIndex
     * @return {@code true} if the bit is set, {@code false} otherwise.
     */
    @Override
    public VerySparseBitSet get(int startIndex, int endIndex) {
        return null;
    }
    
    @Override
    public boolean intersects(VerySparseBitSet set) {
        return false;
    }

    @Override
    public void and(VerySparseBitSet set) {

    }

    @Override
    public void or(VerySparseBitSet set) {

    }

    @Override
    public void xor(VerySparseBitSet set) {

    }

    @Override
    public void andNot(VerySparseBitSet set) {

    }

    @Override
    public VerySparseBitSet clone() {
        return null;
    }

    @Override
    public IntStream stream() {
        return null;
    }
}
