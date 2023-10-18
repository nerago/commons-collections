// see https://issues.apache.org/jira/projects/COLLECTIONS/issues/COLLECTIONS-743?filter=allopenissues

package org.apache.commons.collections4.bits;

import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.list.TreeList;

public class VerySparseBitSet implements BitSetInterface<VerySparseBitSet> {
    private static final int ENTRY_BITS = Long.SIZE;
    private static final long FULLY_SET =  0xffffffffffffffffL;
    private static final long HI_BIT_SET = 0x8000000000000000L;
    private final TreeList<Entry> content;

    public VerySparseBitSet() {
        content = new TreeList<>();
        content.add(new Entry(0, 0));
    }
    
    private static int indexToBlockStart(final int bitIndex) {
        return (bitIndex >>> 6) << 6;
    }
    
    private static void checkRange(final int startIndex, final int endIndex) {
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            throw new IllegalArgumentException();
        }
    }
    
    private Entry findEntry(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        final int blockStartIndex = indexToBlockStart(index);
        return ListUtils.binarySearchValue(content, e -> e.startIndex, blockStartIndex);
    }

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

    /**
     * Is bit set entirely clear.
     */
    @Override
    public boolean isEmpty() {
        return content.size() == 1 && content.get(0).bits == 0;
    }

    /**
     * Returns the number of bits in this set.
     * Maximum bit index range with storage allocated.
     */
    @Override
    public int length() {
        final Entry entry = ListUtils.getLast(content);
        if (entry != null) {
            return entry.lastIndex() + 1;
        } else {
            return 0;
        }
    }

    /**
     * Return the number of bits that are set. NOTE: this method is likely to run in linear time
     */
    @Override
    public int cardinality() {
        int result = 0;
        for (final Entry e : content) {
            result += Long.bitCount(e.bits);
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
        final Entry entry = findEntry(index);
        if (entry != null) {
            return (entry.bits & maskSingleSet(index - entry.startIndex)) != 0;
        } else {
            return false;
        }
    }

    private Entry getOrAddEntry(int blockStartBitIndex) {
        int listIndex = ListUtils.binarySearchIndex(content, e -> e.startIndex, blockStartIndex);
    }

    @Override
    public void set(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);

        final int blockStartBitIndex = indexToBlockStart(startIndex);

        Entry entry = getOrAddEntry(blockStartBitIndex);

//        int listIndex = ListUtils.binarySearchIndex(content, e -> e.startIndex, blockStartIndex);
//
//        Entry entry;
//        if (listIndex == -1) {
//            entry = new Entry(blockStartIndex, 0);
//        } else {
//            entry = content.get(listIndex++);
//        }


//        while (entry.startIndex < endIndex) {
//            final Entry entry = content.get(listIndex++);
//            if (entry.startIndex > endIndex) {
//                break;
//            }
//
//            if (endIndex >= entry.lastIndex()) {
//                entry.bits = FULLY_SET;
//            }
//
//
//        }

//        if (startIndex == blockStartIndex && rangeLength < ENTRY_BITS) {
//            entry.bits |= maskFirstNSet(rangeLength);
//            return;
//        } else if (startIndex == blockStartIndex) {
//            entry.bits = FULLY_SET;
//        } else if (endIndex < entry.lastIndex()) {
//            entry.bits |= maskRangeSet(startIndex - entry.startIndex, endIndex - entry.startIndex);
//            return;
//        }

    }


    public void set0(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);

        final int blockStartIndex = indexToBlockStart(startIndex);
        final int rangeLength = endIndex - startIndex;

        int listIndex = ListUtils.binarySearchIndex(content, e -> e.startIndex, blockStartIndex);

        Entry entry;
        if (listIndex == -1) {
            entry = new Entry(blockStartIndex, 0);
        } else {
            entry = content.get(listIndex++);
        }

        if (startIndex == blockStartIndex && rangeLength < ENTRY_BITS) {
            entry.bits |= maskFirstNSet(rangeLength);
            return;
        } else if (startIndex == blockStartIndex) {
            entry.bits = FULLY_SET;
        } else if (endIndex < entry.lastIndex()) {
            entry.bits |= maskRangeSet(startIndex - entry.startIndex, endIndex - entry.startIndex);
            return;
        }

//        Entry entry = content.get(listIndex);
        while (true) {
            final Entry entry = content.get(listIndex++);
            if (entry.startIndex > endIndex) {
                break;
            }

            if (endIndex >= entry.lastIndex()) {
                entry.bits = FULLY_SET;
            }


        }
    }

    /**
     * Clears a range of bits.
     *
     * @param startIndex lower index
     * @param endIndex   one-past the last bit to clear
     */
    @Override
    public void clear(int startIndex, int endIndex) {

    }

    /**
     * Flips a range of bits
     *
     * @param startIndex lower index
     * @param endIndex   one-past the last bit to flip
     */
    @Override
    public void flip(int startIndex, int endIndex) {

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
     * Returns the index of the first set bit starting at the index specified. -1 is returned if there
     * are no more set bits.
     *
     * @param index
     */
    @Override
    public int nextSetBit(int index) {
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

    private static final class Entry {
        final int startIndex;
        long bits;

        private Entry(final int startIndex, final int bits) {
            this.startIndex = startIndex;
            this.bits = bits;
        }

        public int lastIndex() {
            return startIndex + ENTRY_BITS - 1;
        }

        public boolean fullyInRange(final int rangeStart, final int rangeEnd) {
            return rangeStart <= startIndex && lastIndex() <= rangeEnd;
        }
    }
}
