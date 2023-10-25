package org.apache.commons.collections4.primitive;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SerializableTransitional;
import org.apache.commons.collections4.set.HashedSet;

/**
 * Slightly more efficient version of HashSet which implements Hash storage on primitive longs directly rather than nesting a HashMap.
 * 
 * @see LongToLongMap
 * @see HashedSet
 */
public final class LongSet extends AbstractCommonsCollection<Long> implements Set<Long>, LongCollection, SerializableTransitional, Cloneable {


    private static final String NO_NEXT_ENTRY = "No next() entry in the iteration";
    private static final String REMOVE_INVALID = "remove() can only be called once after next()";

    /** The default capacity to use */
    private static final int DEFAULT_CAPACITY = 16;
    /** The default threshold to use */
    private static final int DEFAULT_THRESHOLD = 12;
    /** The default load factor to use */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /** The maximum capacity allowed */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /** Load factor, normally 0.75 */
    private transient float loadFactor;
    /** The size of the map */
    private transient int size;
    /** Map entries */
    private transient HashEntry[] data;
    /** Size at which to rehash */
    private transient int threshold;
    /** Modification count for iterators */
    private transient int modCount;

    /**
     * Constructs a new empty set with default size and load factor.
     */
    public LongSet() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD);
    }

    /**
     * Constructor which performs no validation on the passed in parameters.
     *
     * @param initialCapacity  the initial capacity, must be a power of two
     * @param loadFactor  the load factor, must be &gt; 0.0f and generally &lt; 1.0f
     * @param threshold  the threshold, must be sensible
     */
    public LongSet(final int initialCapacity, final float loadFactor, final int threshold) {
        this.loadFactor = loadFactor;
        this.data = new HashEntry[initialCapacity];
        this.threshold = threshold;
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity  the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public LongSet(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * load factor.
     *
     * @param initialCapacity  the initial capacity
     * @param loadFactor  the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     * @throws IllegalArgumentException if the load factor is less than or equal to zero
     */
    @SuppressWarnings("unchecked")
    public LongSet(int initialCapacity, final float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity must be a non negative number");
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Load factor must be greater than 0");
        }
        this.loadFactor = loadFactor;
        initialCapacity = calculateNewCapacity(initialCapacity);
        this.threshold = calculateThreshold(initialCapacity, loadFactor);
        this.data = new HashEntry[initialCapacity];
    }

    /**
     * Constructor copying elements from another map.
     *
     * @param collection  the map to copy
     * @throws NullPointerException if the map is null
     */
    public LongSet(final Collection<Long> collection) {
        this(Math.max(2 * collection.size(), DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        addAll(collection);
    }

    /**
     * Gets the size of the set.
     *
     * @return the size
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checks whether the set is currently empty.
     *
     * @return true if the set is currently size zero
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks whether the set contains the specified value.
     *
     * @param value the value to search for
     * @return true if the set contains the value
     */
    @Override
    public boolean contains(final Object value) {
        if (value instanceof Long) {
            return contains((long) value);
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(final long value) {
        final int hashCode = hash(value);
        HashEntry entry = data[hashIndex(hashCode, data.length)]; // no local for hash index
        while (entry != null) {
            if (isEqualValue(value, entry.value)) {
                return true;
            }
            entry = entry.next;
        }
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> coll) {
        for (final Object item : coll) {
            if (!contains(item))
                return false;
        }
        return true;
    }

    /**
     * Adds an item to the set.
     *
     * @param value  the value to add
     * @return was the value added
     */
    @Override
    public boolean add(final Long value) {
        return add(value.longValue());
    }

    @Override
    public boolean add(final long value) {
        final int hashCode = hash(value);
        final int index = hashIndex(hashCode, data.length);

        HashEntry entry = data[index];
        while (entry != null) {
            if (isEqualValue(value, entry.value)) {
                return false;
            }
            entry = entry.next;
        }

        data[index] = new HashEntry(data[index], hashCode, value);

        size++;
        modCount++;

        checkCapacity();
        return true;
    }

    /**
     * Puts all the values from the specified collection into this set.
     * <p>
     * This implementation iterates around the specified collection and
     * uses {@link #add(long)}.
     *
     * @param coll  the collection to add
     * @return was collection modified
     * @throws NullPointerException if the collection is null
     */
    @Override
    public boolean addAll(final Collection<? extends Long> coll) {
        final int collSize = coll.size();
        if (collSize == 0) {
            return false;
        }
        final int capacity = (int) ((size + collSize) / loadFactor + 1);
        ensureCapacity(calculateNewCapacity(capacity));

        int newSize = size;
        boolean changed = false;
        for (final Long element : coll) {
            final int hashCode = hash(element);
            final int index = hashIndex(hashCode, data.length);

            boolean missing = true;
            HashEntry entry = data[index];
            while (entry != null) {
                if (isEqualValue(element, entry.value)) {
                    missing = false;
                    break;
                }
                entry = entry.next;
            }

            if (missing) {
                data[index] = new HashEntry(data[index], hashCode, element);
                changed = true;
                newSize++;
            }
        }

        if (changed) {
            size = newSize;
            modCount++;
        }
        return changed;
    }

    /**
     * Removes the specified value from this set.
     *
     * @param value the value to remove
     * @return was the value removed from the set
     */
    @Override
    public boolean remove(final Object value) {
        if (value instanceof Long) {
            return remove((long) value);
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(final long value) {
        final int hashCode = hash(value);
        final int index = hashIndex(hashCode, data.length);
        HashEntry entry = data[index];
        HashEntry previous = null;
        while (entry != null) {
            if (isEqualValue(value, entry.value)) {
                removeMapping(entry, index, previous);
                size--;
                modCount++;
                return true;
            }
            previous = entry;
            entry = entry.next;
        }
        return false;
    }

    @Override
    public boolean removeIf(final Predicate<? super Long> filter) {
        final int expectedModCount = modCount;
        final HashEntry[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry entry = data[index];
            HashEntry previous = null;
            while (entry != null) {
                final boolean remove = filter.test(entry.value);
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }

                final HashEntry next = entry.next;
                if (remove) {
                    removeMapping(entry, index, previous);
                    newSize--;
                    changed = true;
                } else {
                    previous = entry;
                }
                entry = next;
            }
        }
        if (changed) {
            size = newSize;
            modCount++;
        }
        return changed;
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        final HashEntry[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry entry = data[index];
            HashEntry previous = null;
            while (entry != null) {
                final boolean remove = !coll.contains(entry.value);
                final HashEntry next = entry.next;
                if (remove) {
                    removeMapping(entry, index, previous);
                    newSize--;
                    changed = true;
                } else {
                    previous = entry;
                }
                entry = next;
            }
        }
        if (changed) {
            size = newSize;
            modCount++;
        }
        return changed;
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        final HashEntry[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry entry = data[index];
            HashEntry previous = null;
            while (entry != null) {
                final boolean remove = coll.contains(entry.value);
                final HashEntry next = entry.next;
                if (remove) {
                    removeMapping(entry, index, previous);
                    newSize--;
                    changed = true;
                } else {
                    previous = entry;
                }
                entry = next;
            }
        }
        if (changed) {
            size = newSize;
            modCount++;
        }
        return changed;
    }

    /**
     * Clears the map, resetting the size to zero and nullifying references
     * to avoid garbage collection issues.
     */
    @Override
    public void clear() {
        Arrays.fill(data, null);
        size = 0;
        modCount++;
    }

    /**
     * Gets the hash code for the key specified.
     * This implementation uses the additional hashing routine from JDK1.4.
     * Subclasses can override this to return alternate hash codes.
     *
     * @param value  the key to get a hash code for
     * @return the hash code
     */
    @SuppressWarnings("MagicNumber")
    private int hash(final long value) {
        int h = (int) (value ^ (value >>> 32));
        h += ~(h << 9);
        h ^=  h >>> 14;
        h +=  h << 4;
        h ^=  h >>> 10;
        return h;
    }

    /**
     * Compares two values, in internal form, to see if they are equal.
     * This implementation uses the equals method and assumes neither value is null.
     *
     * @param value1  the first value to compare passed in from outside and converted
     * @param value2  the second value extracted from the entry {@link HashEntry#value}
     * @return true if equal
     */
    private static boolean isEqualValue(final Object value1, final Object value2) {
        return value1 == value2 || value1.equals(value2);
    }

    /**
     * Gets the index into the data storage for the hashCode specified.
     * This implementation uses the least significant bits of the hashCode.
     * Subclasses can override this to return alternate bucketing.
     *
     * @param hashCode  the hash code to use
     * @param dataSize  the size of the data to pick a bucket from
     * @return the bucket index
     */
    private static int hashIndex(final int hashCode, final int dataSize) {
        return hashCode & dataSize - 1;
    }

    /**
     * Removes a mapping from the map.
     * <p>
     * This implementation calls {@code removeEntry()} and {@code destroyEntry()}.
     * It also handles changes to {@code modCount} and {@code size}.
     * Subclasses could override to fully control removals from the map.
     *
     * @param entry  the entry to remove
     * @param hashIndex  the index into the data structure
     * @param previous  the previous entry in the chain
     */
    private void removeMapping(final HashEntry entry, final int hashIndex, final HashEntry previous) {
        if (previous == null) {
            data[hashIndex] = entry.next;
        } else {
            previous.next = entry.next;
        }

        entry.next = null;
    }

    /**
     * Checks the capacity of the map and enlarges it if necessary.
     * <p>
     * This implementation uses the threshold to check if the map needs enlarging
     */
    private void checkCapacity() {
        if (size >= threshold) {
            final int newCapacity = data.length << 1;
            if (newCapacity <= MAXIMUM_CAPACITY) {
                ensureCapacity(newCapacity);
            }
        }
    }

    /**
     * Changes the size of the data structure to the capacity proposed.
     *
     * @param newCapacity  the new capacity of the array (a power of two, less or equal to max)
     */
    private void ensureCapacity(final int newCapacity) {
        final int oldCapacity = data.length;
        if (newCapacity <= oldCapacity) {
            return;
        }
        if (size == 0) {
            threshold = calculateThreshold(newCapacity, loadFactor);
            data = new HashEntry[newCapacity];
        } else {
            final HashEntry[] oldEntries = data;
            final HashEntry[] newEntries = new HashEntry[newCapacity];

            modCount++;
            for (int i = oldCapacity - 1; i >= 0; i--) {
                HashEntry entry = oldEntries[i];
                if (entry != null) {
                    oldEntries[i] = null;  // gc
                    do {
                        final HashEntry next = entry.next;
                        final int hashCode = hash(entry.value);
                        final int index = hashIndex(hashCode, newCapacity);
                        entry.next = newEntries[index];
                        newEntries[index] = entry;
                        entry = next;
                    } while (entry != null);
                }
            }
            threshold = calculateThreshold(newCapacity, loadFactor);
            data = newEntries;
        }
    }

    /**
     * Calculates the new capacity of the map.
     * This implementation normalizes the capacity to a power of two.
     *
     * @param proposedCapacity  the proposed capacity
     * @return the normalized new capacity
     */
    private static int calculateNewCapacity(final int proposedCapacity) {
        int newCapacity = 1;
        if (proposedCapacity > MAXIMUM_CAPACITY) {
            newCapacity = MAXIMUM_CAPACITY;
        } else {
            while (newCapacity < proposedCapacity) {
                newCapacity <<= 1;  // multiply by two
            }
            if (newCapacity > MAXIMUM_CAPACITY) {
                newCapacity = MAXIMUM_CAPACITY;
            }
        }
        return newCapacity;
    }

    /**
     * Calculates the new threshold of the map, where it will be resized.
     * This implementation uses the load factor.
     *
     * @param newCapacity  the new capacity
     * @param factor  the load factor
     * @return the new resize threshold
     */
    private static int calculateThreshold(final int newCapacity, final float factor) {
        return (int) (newCapacity * factor);
    }

    /**
     * Creates a values iterator.
     * Subclasses can override this to return iterators with different properties.
     *
     * @return the values iterator
     */
    @Override
    public PrimitiveIterator.OfLong iterator() {
        if (isEmpty()) {
            return EmptyPrimitiveIterator.emptyIterator();
        }
        return new HashIterator(this);
    }

    @Override
    public void forEach(final Consumer<? super Long> action) {
        forEach((long val) -> action.accept(val));
    }

    @Override
    public void forEach(final LongConsumer action) {
        final int expectedModCount = modCount;
        final HashEntry[] data = this.data;
        int hashIndex = data.length;
        do {
            HashEntry curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                action.accept(curr.value);
                curr = curr.next;
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        } while (hashIndex > 0);
    }

    @Override
    protected void fillArray(final Object[] array) {
        final HashEntry[] data = this.data;
        int hashIndex = data.length, arrayIndex = 0;
        do {
            HashEntry curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                array[arrayIndex++] = curr.value;
                curr = curr.next;
            }
        } while (hashIndex > 0);
    }

    @Override
    public long[] toLongArray() {
        final long[] array = new long[size];
        final HashEntry[] data = this.data;
        int hashIndex = data.length, arrayIndex = 0;
        do {
            HashEntry curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                array[arrayIndex++] = curr.value;
                curr = curr.next;
            }
        } while (hashIndex > 0);
        return array;
    }

    /**
     * Creates a values spliterator.
     * Subclasses can override this to return spliterator with different properties.
     *
     * @return the values spliterator
     */
    @Override
    public Spliterator.OfLong spliterator() {
        if (isEmpty()) {
            return EmptyPrimitiveSpliterator.emptySpliterator();
        }
        return new HashSpliterator(this, Spliterator.SIZED);
    }

    protected static class HashEntry {
        /** The next entry in the hash chain */
        protected HashEntry next;
        /** The value */
        protected long value;

        protected HashEntry(final HashEntry next, final int hashCode, final long value) {
            this.next = next;
            this.value = value;
        }
    }

    /**
     * Base Iterator
     */
    static class HashIterator implements ResettableIterator<Long>, PrimitiveIterator.OfLong {

        /** The parent map */
        private final LongSet parent;
        /** The current index into the array of buckets */
        private int hashIndex;
        /** The last returned entry */
        private HashEntry last;
        /** The next entry */
        private HashEntry next;
        /** The modification count expected */
        private int expectedModCount;

        HashIterator(final LongSet parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public void reset() {
            final HashEntry[] data = parent.data;
            int i = data.length;
            HashEntry next = null;
            while (i > 0 && next == null) {
                next = data[--i];
            }
            this.next = next;
            this.hashIndex = i;
            this.expectedModCount = parent.modCount;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        /**
         * Returns the next {@code long} element in the iteration.
         *
         * @return the next {@code long} element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public long nextLong() {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            final HashEntry newCurrent = next;
            if (newCurrent == null)  {
                throw new NoSuchElementException(NO_NEXT_ENTRY);
            }
            final HashEntry[] data = parent.data;
            int i = hashIndex;
            HashEntry n = newCurrent.next;
            while (n == null && i > 0) {
                n = data[--i];
            }
            next = n;
            hashIndex = i;
            last = newCurrent;
            return newCurrent.value;
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalStateException(REMOVE_INVALID);
            }
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            parent.remove(last.value);
            last = null;
            expectedModCount = parent.modCount;
        }

        @Override
        public String toString() {
            if (last != null) {
                return "Iterator[" + last + "]";
            }
            return "Iterator[]";
        }
    }

    static class HashSpliterator implements Spliterator.OfLong {

        /** The parent map */
        private final LongSet parent;
        /** The modification count expected */
        private final int expectedModCount;
        private int characteristics;
        private long estimatedSize;
        /** The current index into the array of buckets */
        private int hashIndex;
        /** The final index this spliterator should check */
        private final int lastHashIndex;
        /** The next entry */
        private HashEntry next;

        HashSpliterator(final LongSet parent, final int characteristics) {
            this.parent = parent;
            this.hashIndex = parent.data.length - 1;
            this.lastHashIndex = 0;
            this.expectedModCount = parent.modCount;
            this.estimatedSize = parent.size;
            this.characteristics = characteristics;
        }

        HashSpliterator(final LongSet parent,
                        final int hashIndex, final int lastHashIndex,
                        final long estimatedSize, final int characteristics) {
            this.parent = parent;
            this.hashIndex = hashIndex;
            this.lastHashIndex = lastHashIndex;
            this.expectedModCount = parent.modCount;
            this.estimatedSize = estimatedSize;
            this.characteristics = characteristics;
        }

        @Override
        public boolean tryAdvance(final LongConsumer action) {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            if (next != null)  {
                action.accept(next.value);
                next = next.next;
                return true;
            }

            final HashEntry[] data = parent.data;
            int i = hashIndex, z = lastHashIndex;
            if (i < z) {
                return false;
            }

            HashEntry n;
            do {
                n = data[i--];
            } while (n == null && i >= z);
            hashIndex = i;

            if (n != null) {
                action.accept(n.value);
                next = n.next;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            final int mid = lastHashIndex + (hashIndex - lastHashIndex) / 2;
            if (lastHashIndex < mid && mid < hashIndex) {
                estimatedSize >>>= 1L;
                characteristics &= ~Spliterator.SIZED;
                final Spliterator.OfLong split = new HashSpliterator(parent, hashIndex, mid + 1, estimatedSize, characteristics);
                hashIndex = mid;
                return split;
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return estimatedSize;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }
    }

    /**
     * Write the set out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeFloat(loadFactor);
        out.writeInt(data.length);
        out.writeInt(size);
        for (final PrimitiveIterator.OfLong it = iterator(); it.hasNext();) {
            out.writeLong(it.next());
        }
    }

    /**
     * Read the set in using a custom routine.
     *
     * @param in the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        loadFactor = in.readFloat();
        final int capacity = in.readInt();
        final int size = in.readInt();
        threshold = calculateThreshold(capacity, loadFactor);
        data = new HashEntry[capacity];
        for (int i = 0; i < size; i++) {
            final long value = in.readLong();
            add(value);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Set) {
            final Set<?> set = (Set<?>) obj;
            return set.size() == this.size() && set.containsAll(this);
        }
        return false;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public int hashCode() {
        int code = 0;
        for (final PrimitiveIterator.OfLong it = iterator(); it.hasNext();) {
            final long value = it.next();
            code += hash(value);
        }
        return code;
    }

    @Override
    public String toString() {
        return IteratorUtils.toString(iterator());
    }
}
