package org.apache.commons.collections4.set;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.EmptyIterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HashedSet<E> implements Set<E>, Serializable, Cloneable {

    private static final long serialVersionUID = -4842904738908370556L;

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
    /** An object for masking null */
    private static final Object NULL = new Object();

    /** Load factor, normally 0.75 */
    private transient float loadFactor;
    /** The size of the map */
    private transient int size;
    /** Map entries */
    private transient HashEntry<E>[] data;
    /** Size at which to rehash */
    private transient int threshold;
    /** Modification count for iterators */
    private transient int modCount;

    /**
     * Constructs a new empty set with default size and load factor.
     */
    public HashedSet() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD);
    }

    /**
     * Constructor which performs no validation on the passed in parameters.
     *
     * @param initialCapacity  the initial capacity, must be a power of two
     * @param loadFactor  the load factor, must be &gt; 0.0f and generally &lt; 1.0f
     * @param threshold  the threshold, must be sensible
     */
    @SuppressWarnings("unchecked")
    public HashedSet(final int initialCapacity, final float loadFactor, final int threshold) {
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
    public HashedSet(final int initialCapacity) {
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
    public HashedSet(int initialCapacity, final float loadFactor) {
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
    public HashedSet(final Collection<? extends E> collection) {
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
        final Object convertedValue = convertValue(value);
        final int hashCode = hash(convertedValue);
        HashEntry<E> entry = data[hashIndex(hashCode, data.length)]; // no local for hash index
        while (entry != null) {
            if (entry.hashCode == hashCode && isEqualValue(convertedValue, entry.value)) {
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
    public boolean add(final E value) {
        final Object convertedValue = convertValue(value);
        final int hashCode = hash(convertedValue);
        final int index = hashIndex(hashCode, data.length);

        HashEntry<E> entry = data[index];
        while (entry != null) {
            if (entry.hashCode == hashCode && isEqualValue(convertedValue, entry.value)) {
                return false;
            }
            entry = entry.next;
        }

        data[index] = new HashEntry<>(data[index], hashCode, convertedValue);;

        size++;
        modCount++;

        checkCapacity();
        return true;
    }

    /**
     * Puts all the values from the specified collection into this set.
     * <p>
     * This implementation iterates around the specified collection and
     * uses {@link #add(Object)}.
     *
     * @param coll  the collection to add
     * @return was collection modified
     * @throws NullPointerException if the collection is null
     */
    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        final int collSize = coll.size();
        if (collSize == 0) {
            return false;
        }
        final int capacity = (int) ((size + collSize) / loadFactor + 1);
        ensureCapacity(calculateNewCapacity(capacity));

        int newSize = size;
        boolean changed = false;
        for (final E element : coll) {
            final Object convertedValue = convertValue(element);
            final int hashCode = hash(convertedValue);
            final int index = hashIndex(hashCode, data.length);

            boolean missing = true;
            HashEntry<E> entry = data[index];
            while (entry != null) {
                if (entry.hashCode == hashCode && isEqualValue(convertedValue, entry.value)) {
                    missing = false;
                    break;
                }
                entry = entry.next;
            }

            if (missing) {
                data[index] = new HashEntry<>(data[index], hashCode, convertedValue);
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
        final Object convertedValue = convertValue(value);
        final int hashCode = hash(convertedValue);
        final int index = hashIndex(hashCode, data.length);
        HashEntry<E> entry = data[index];
        HashEntry<E> previous = null;
        while (entry != null) {
            if (entry.hashCode == hashCode && isEqualValue(convertedValue, entry.value)) {
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
    public boolean removeIf(final Predicate<? super E> filter) {
        final int expectedModCount = modCount;
        final HashEntry<E>[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry<E> entry = data[index];
            HashEntry<E> previous = null;
            while (entry != null) {
                final boolean remove = filter.test(entry.getValue());
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }

                final HashEntry<E> next = entry.next;
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
        final HashEntry<E>[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry<E> entry = data[index];
            HashEntry<E> previous = null;
            while (entry != null) {
                final boolean remove = !coll.contains(entry.getValue());
                final HashEntry<E> next = entry.next;
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
        final HashEntry<E>[] data = this.data;
        int newSize = size;
        boolean changed = false;
        for (int index = data.length - 1; index >= 0; --index) {
            HashEntry<E> entry = data[index];
            HashEntry<E> previous = null;
            while (entry != null) {
                final boolean remove = coll.contains(entry.getValue());
                final HashEntry<E> next = entry.next;
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
     * Converts input value to another object for storage in the array.
     * This implementation masks nulls.
     * <p>
     * The reverse conversion can be changed, if required, by overriding the
     * getValue() method in the hash entry.
     *
     * @param value  the value to convert
     * @return the converted value
     */
    private static Object convertValue(final Object value) {
        return value == null ? NULL : value;
    }

    /**
     * Gets the hash code for the key specified.
     * This implementation uses the additional hashing routine from JDK1.4.
     * Subclasses can override this to return alternate hash codes.
     *
     * @param value  the key to get a hash code for
     * @return the hash code
     */
    private static int hash(final Object value) {
        // same as JDK 1.4
        int h = value.hashCode();
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
    private void removeMapping(final HashEntry<E> entry, final int hashIndex, final HashEntry<E> previous) {
        if (previous == null) {
            data[hashIndex] = entry.next;
        } else {
            previous.next = entry.next;
        }

        entry.next = null;
        entry.value = null;
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
    @SuppressWarnings("unchecked")
    private void ensureCapacity(final int newCapacity) {
        final int oldCapacity = data.length;
        if (newCapacity <= oldCapacity) {
            return;
        }
        if (size == 0) {
            threshold = calculateThreshold(newCapacity, loadFactor);
            data = new HashEntry[newCapacity];
        } else {
            final HashEntry<E>[] oldEntries = data;
            final HashEntry<E>[] newEntries = new HashEntry[newCapacity];

            modCount++;
            for (int i = oldCapacity - 1; i >= 0; i--) {
                HashEntry<E> entry = oldEntries[i];
                if (entry != null) {
                    oldEntries[i] = null;  // gc
                    do {
                        final HashEntry<E> next = entry.next;
                        final int index = hashIndex(entry.hashCode, newCapacity);
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
    public Iterator<E> iterator() {
        if (isEmpty()) {
            return EmptyIterator.emptyIterator();
        }
        return new HashIterator<>(this);
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        final int expectedModCount = modCount;
        final HashEntry<E>[] data = this.data;
        int hashIndex = data.length;
        do {
            HashEntry<E> curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                action.accept(curr.getValue());
                curr = curr.next;
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        } while (hashIndex > 0);
    }

    @Override
    public Object[] toArray() {
        final Object[] array = new Object[size];
        final HashEntry<E>[] data = this.data;
        int hashIndex = data.length, arrayIndex = 0;
        do {
            HashEntry<E> curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                array[arrayIndex++] = curr.getValue();
                curr = curr.next;
            }
        } while (hashIndex > 0);
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] array) {
        // Extend the array if needed
        if (array.length < size) {
            final Class<?> componentType = array.getClass().getComponentType();
            array = (T[]) Array.newInstance(componentType, size);
        }
        // Copy the values into the array
        final HashEntry<E>[] data = this.data;
        int hashIndex = data.length, arrayIndex = 0;
        do {
            HashEntry<E> curr = null;
            while (hashIndex > 0 && curr == null) {
                curr = data[--hashIndex];
            }
            while (curr != null) {
                array[arrayIndex++] = (T) curr.getValue();
                curr = curr.next;
            }
        } while (hashIndex > 0);
        // Set the value after the last value to null
        if (array.length > size) {
            array[size] = null;
        }
        return array;
    }

    /**
     * Creates a values spliterator.
     * Subclasses can override this to return spliterator with different properties.
     *
     * @return the values spliterator
     */
    @Override
    public Spliterator<E> spliterator() {
        if (isEmpty()) {
            return Spliterators.emptySpliterator();
        }
        return new HashSpliterator<>(this, Spliterator.SIZED);
    }

    /**
     * HashEntry used to store the data.
     * <p>
     * If you subclass {@code HashedSet} but not {@code HashEntry}
     * then you will not be able to access the protected fields.
     * The {@code entryXxx()} methods on {@code HashedSet} exist
     * to provide the necessary access.
     *
     * @param <E> the type of the values
     */
    protected static class HashEntry<E> {
        /** The next entry in the hash chain */
        protected HashEntry<E> next;
        /** The hash code of the key */
        protected int hashCode;
        /** The value */
        protected Object value;

        protected HashEntry(final HashEntry<E> next, final int hashCode, final Object value) {
            this.next = next;
            this.hashCode = hashCode;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        public E getValue() {
            if (value == NULL) {
                return null;
            }
            return (E) value;
        }
    }

    /**
     * Base Iterator
     *
     * @param <E> the type of the values in the set
     */
    static class HashIterator<E> implements ResettableIterator<E> {

        /** The parent map */
        private final HashedSet<E> parent;
        /** The current index into the array of buckets */
        private int hashIndex;
        /** The last returned entry */
        private HashEntry<E> last;
        /** The next entry */
        private HashEntry<E> next;
        /** The modification count expected */
        private int expectedModCount;

        HashIterator(final HashedSet<E> parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public void reset() {
            final HashEntry<E>[] data = parent.data;
            int i = data.length;
            HashEntry<E> next = null;
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

        @Override
        public E next() {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            final HashEntry<E> newCurrent = next;
            if (newCurrent == null)  {
                throw new NoSuchElementException(NO_NEXT_ENTRY);
            }
            final HashEntry<E>[] data = parent.data;
            int i = hashIndex;
            HashEntry<E> n = newCurrent.next;
            while (n == null && i > 0) {
                n = data[--i];
            }
            next = n;
            hashIndex = i;
            last = newCurrent;
            return newCurrent.getValue();
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalStateException(REMOVE_INVALID);
            }
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            parent.remove(last.getValue());
            last = null;
            expectedModCount = parent.modCount;
        }

        @Override
        public String toString() {
            if (last != null) {
                return "Iterator[" + last.getValue() + "]";
            }
            return "Iterator[]";
        }
    }

    static class HashSpliterator<E> implements Spliterator<E> {

        /** The parent map */
        private final HashedSet<E> parent;
        /** The modification count expected */
        private final int expectedModCount;
        private int characteristics;
        private long estimatedSize;
        /** The current index into the array of buckets */
        private int hashIndex;
        /** The final index this spliterator should check */
        private final int lastHashIndex;
        /** The next entry */
        private HashEntry<E> next;

        HashSpliterator(final HashedSet<E> parent, final int characteristics) {
            this.parent = parent;
            this.hashIndex = parent.data.length - 1;
            this.lastHashIndex = 0;
            this.expectedModCount = parent.modCount;
            this.estimatedSize = parent.size;
            this.characteristics = characteristics;
        }

        HashSpliterator(final HashedSet<E> parent,
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
        public boolean tryAdvance(final Consumer<? super E> action) {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            if (next != null)  {
                action.accept(next.getValue());
                next = next.next;
                return true;
            }

            final HashEntry<E>[] data = parent.data;
            int i = hashIndex, z = lastHashIndex;
            if (i < z) {
                return false;
            }

            HashEntry<E> n;
            do {
                n = data[i--];
            } while (n == null && i >= z);
            hashIndex = i;

            if (n != null) {
                action.accept(n.getValue());
                next = n.next;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<E> trySplit() {
            final int mid = lastHashIndex + (hashIndex - lastHashIndex) / 2;
            if (lastHashIndex < mid && mid < hashIndex) {
                estimatedSize >>>= 1L;
                characteristics &= ~Spliterator.SIZED;
                final Spliterator<E> split = new HashSpliterator<>(parent, hashIndex, mid + 1, estimatedSize, characteristics);
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
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeFloat(loadFactor);
        out.writeInt(data.length);
        out.writeInt(size);
        for (final Iterator<E> it = iterator(); it.hasNext();) {
            out.writeObject(it.next());
        }
    }

    /**
     * Read the set in using a custom routine.
     *
     * @param in the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadFactor = in.readFloat();
        final int capacity = in.readInt();
        final int size = in.readInt();
        threshold = calculateThreshold(capacity, loadFactor);
        data = new HashEntry[capacity];
        for (int i = 0; i < size; i++) {
            final E value = (E) in.readObject();
            add(value);
        }
    }

    /**
     * Clones the set without cloning the element objects.
     *
     * @return a shallow clone
     */
    @Override
    @SuppressWarnings("unchecked")
    public HashedSet<E> clone() {
        try {
            final HashedSet<E> cloned = (HashedSet<E>) super.clone();
            cloned.data = new HashEntry[data.length];
            cloned.modCount = 0;
            cloned.size = 0;
            cloned.addAll(this);
            return cloned;
        } catch (final CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
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
        for (final Iterator<E> it = iterator(); it.hasNext();) {
            final E e = it.next();
            if (e != null) {
                code += e.hashCode();
            }
        }
        return code;
    }

    @Override
    public String toString() {
        return IteratorUtils.toString(iterator());
    }
}
