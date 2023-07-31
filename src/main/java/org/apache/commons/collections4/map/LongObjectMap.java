package org.apache.commons.collections4.map;

import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.ResettableIterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.*;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public final class LongObjectMap<V> implements Serializable {
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
    private transient LongHashEntry<V>[] data;
    /** Size at which to rehash */
    private transient int threshold;

    /** Key set */
    private transient KeySet<V> keySet;
    /** Values */
    private transient Values<V> values;
    private transient MapAdapter<V> mapAdapter;

    /**
     * Constructs a new empty map with default size and load factor.
     */
    public LongObjectMap() {
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
    public LongObjectMap(final int initialCapacity, final float loadFactor, final int threshold) {
        this.loadFactor = loadFactor;
        this.data = new LongHashEntry[initialCapacity];
        this.threshold = threshold;
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity  the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public LongObjectMap(final int initialCapacity) {
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
    public LongObjectMap(int initialCapacity, final float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity must be a non negative number");
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Load factor must be greater than 0");
        }
        this.loadFactor = loadFactor;
        initialCapacity = calculateNewCapacity(initialCapacity);
        this.threshold = calculateThreshold(initialCapacity, loadFactor);
        this.data = new LongHashEntry[initialCapacity];
    }

    /**
     * Constructor copying elements from another map.
     *
     * @param map  the map to copy
     * @throws NullPointerException if the map is null
     */
    public LongObjectMap(final Map<? extends Long, ? extends V> map) {
        this(Math.max(2 * map.size(), DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(map);
    }

    /**
     * Constructor copying elements from another map.
     *
     * @param map  the map to copy
     * @throws NullPointerException if the map is null
     */
    public LongObjectMap(final LongObjectMap<? extends V> map) {
        this(Math.max(2 * map.size(), DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(map);
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Checks whether the map is currently empty.
     *
     * @return true if the map is currently size zero
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Gets the value mapped to the key specified.
     *
     * @param key  the key
     * @return the mapped value, null if no match
     */
    public V get(final long key) {
        return getOrDefault(key, null);
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key
     */
    public V getOrDefault(final long key, final V defaultValue) {
        final int hashCode = hash(key);
        LongHashEntry<V> entry = data[hashIndex(hashCode, data.length)];
        while (entry != null) {
            if (entry.key == key) {
                return entry.value;
            }
            entry = entry.next;
        }
        return defaultValue;
    }

    /**
     * Checks whether the map contains the specified key.
     *
     * @param key  the key to search for
     * @return true if the map contains the key
     */
    public boolean containsKey(final long key) {
        final int hashCode = hash(key);
        LongHashEntry<V> entry = data[hashIndex(hashCode, data.length)];
        while (entry != null) {
            if (entry.key == key) {
                return true;
            }
            entry = entry.next;
        }
        return false;
    }

    /**
     * Checks whether the map contains the specified value.
     *
     * @param value  the value to search for
     * @return true if the map contains the value
     */
    public boolean containsValue(final V value) {
        if (value == null) {
            for (final LongHashEntry<V> element : data) {
                LongHashEntry<V> entry = element;
                while (entry != null) {
                    if (entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (final LongHashEntry<V> element : data) {
                LongHashEntry<V> entry = element;
                while (entry != null) {
                    if (isEqualValue(value, entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    /**
     * Puts a key-value mapping into this map.
     *
     * @param key  the key to add
     * @param value  the value to add
     * @return the value previously mapped to this key, null if none
     */
    public V put(final long key, final V value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entry = entry.next;
        }

        addMapping(index, hashCode, key, value);
        return null;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     */
    public V putIfAbsent(final long key, final V value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final V oldValue = entry.value;
                if (oldValue == null) {
                    entry.value = value;
                    return null;
                } else {
                    return oldValue;
                }
            }
            entry = entry.next;
        }

        addMapping(index, hashCode, key, value);
        return null;
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     */
    public boolean replace(final long key, final V oldValue, final V newValue) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final V currentValue = entry.value;
                if (isEqualValue(currentValue, oldValue)) {
                    entry.value = newValue;
                    return true;
                } else {
                    return false;
                }
            }
            entry = entry.next;
        }
        return false;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     * @since 1.8
     */
    public V replace(final long key, final V value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Puts all the values from the specified map into this map.
     * <p>
     * This implementation iterates around the specified map and
     * uses {@link #put(long, Object)}.
     *
     * @param map  the map to add
     * @throws NullPointerException if the map is null
     * @throws ClassCastException if the class of a value in the
     *      specified map prevents it from being stored in this map.
     * @throws NullPointerException if the specified map is null, if the
     *      specified map contains null keys
     */
    public void putAll(final Map<? extends Long, ? extends V> map) {
        if (checkPutAll(map.size())) {
            for (final Map.Entry<? extends Long, ? extends V> entry : map.entrySet()) {
                Long key = entry.getKey();
                Objects.requireNonNull(key);
                put(key, entry.getValue());
            }
        }
    }

    /**
     * Puts all the values from the specified LongMap into this map.
     * <p>
     * This implementation iterates around the specified map and
     * uses {@link #put(long, Object)}.
     *
     * @param map  the map to add
     * @throws NullPointerException if the map is null
     * * @throws ClassCastException if the class of a key or value in the
     *      *         specified map prevents it from being stored in this map
     *      * @throws NullPointerException if the specified map is null, or if
     *      *         this map does not permit null keys or values, and the
     *      *         specified map contains null keys or values
     */
    public void putAll(final LongObjectMap<? extends V> map) {
        if (checkPutAll(map.size())) {
            for (final LongHashEntry<? extends V> element : map.data) {
                LongHashEntry<? extends V> entry = element;
                while (entry != null) {
                    put(entry.key, entry.value);
                    entry = entry.next;
                }
            }
        }
    }

    private boolean checkPutAll(final int mapSize) {
        if (mapSize == 0) {
            return false;
        }
        final int newSize = (int) ((size + mapSize) / loadFactor + 1);
        ensureCapacity(calculateNewCapacity(newSize));
        return true;
    }

    /**
     * Removes the specified mapping from this map.
     *
     * @param key  the mapping to remove
     * @return {@code true} if the value was removed
     */
    public boolean removeKey(final long key) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        LongHashEntry<V> previous = null;
        while (entry != null) {
            if (entry.key == key) {
                removeMapping(entry, index, previous);
                return true;
            }
            previous = entry;
            entry = entry.next;
        }
        return false;
    }

    /**
     * Removes the specified mapping from this map.
     *
     * @param key  the mapping to remove
     * @return the value mapped to the removed key, null if key not in map
     */
    public V remove(final long key) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        LongHashEntry<V> previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final V oldValue = entry.value;
                removeMapping(entry, index, previous);
                return oldValue;
            }
            previous = entry;
            entry = entry.next;
        }
        return null;
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     */
    public boolean remove(final long key, final V value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry<V> entry = data[index];
        LongHashEntry<V> previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final V oldValue = entry.value;
                if (isEqualValue(oldValue, value)) {
                    removeMapping(entry, index, previous);
                    return true;
                } else {
                    return false;
                }
            }
            previous = entry;
            entry = entry.next;
        }
        return false;
    }

    /**
     * Clears the map, resetting the size to zero and nullifying references
     * to avoid garbage collection issues.
     */
    public void clear() {
        Arrays.fill(this.data, null);
        size = 0;
    }

    /**
     * Returns adapter that follows the standard java map interface but will require key boxing.
     */
    public Map<Long, V> asMap() {
        if (mapAdapter == null) {
            mapAdapter = new MapAdapter<>(this);
        }
        return mapAdapter;
    }

    public long[] toKeyArray() {
        final long[] array = new long[size];
        int index = 0;
        for (final LongHashEntry<V> element : data) {
            LongHashEntry<V> entry = element;
            while (entry != null) {
                array[index++] = entry.key;
                entry = entry.next;
            }
        }
        return array;
    }

    /**
     * Performs the given action for each entry in this map until all entries
     * have been processed or the action throws an exception.
     * Actions are performed in the order of entry set iteration.
     * Exceptions thrown by the action are relayed to the caller.

     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     */
    public void forEach(ConsumerWithLong<? super V> action) {
        Objects.requireNonNull(action);
        for (final LongHashEntry<V> element : data) {
            LongHashEntry<V> entry = element;
            while (entry != null) {
                action.accept(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }

    @FunctionalInterface
    public interface ConsumerWithLong<V> {
        void accept(long k, V v);
    }

    public boolean containsEntry(final long key, final V value) {
        final int hashCode = hash(key);
        LongHashEntry<V> e = data[hashIndex(hashCode, data.length)];
        while (e != null) {
            if (e.key == key)
                return isEqualValue(e.value, value);
            e = e.next;
        }
        return false;
    }

    public boolean containsEntry(final Map.Entry<Long, V> entry) {
        return containsEntry(entry.getKey(), entry.getValue());
    }

    public boolean removeEntry(final Map.Entry<Long, V> entry) {
        return remove(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    public boolean containsEntry(final Object obj) {
        if (!(obj instanceof Map.Entry)) {
            return false;
        }
        final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
        return containsEntry((long) entry.getKey(), (V) entry.getValue());
    }

    @SuppressWarnings("unchecked")
    public boolean removeEntry(final Object obj) {
        if (!(obj instanceof Map.Entry)) {
            return false;
        }
        final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
        return remove((long) entry.getKey(), (V) entry.getValue());
    }

    public LongSet keySet() {
        if (keySet == null) {
            keySet = new KeySet(this);
        }
        return keySet;
    }

    public Collection<V> values() {
        if (values == null) {
            values = new Values<>(this);
        }
        return values;
    }

    /**
     * Gets the hash code for the key specified.
     * This implementation uses the additional hashing routine from JDK1.4.
     * Subclasses can override this to return alternate hash codes.
     *
     * @param key  the key to get a hash code for
     * @return the hash code
     */
    private int hash(final long key) {
        int h = (int) (key ^ (key >>> 32));
        h += ~(h << 9);
        h ^=  h >>> 14;
        h +=  h << 4;
        h ^=  h >>> 10;
        return h;
    }

    /**
     * Compares two values, in external form, to see if they are equal.
     * This implementation uses the equals method and assumes neither value is null.
     * Subclasses can override this to match differently.
     *
     * @param value1  the first value to compare passed in from outside
     * @param value2  the second value extracted from the entry via {@code getValue()}
     * @return true if equal
     */
    private boolean isEqualValue(final V value1, final V value2) {
        return Objects.equals(value1, value2);
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
     * Adds a new key-value mapping into this map.
     * <p>
     * This implementation calls {@code createEntry()}, {@code addEntry()}
     * and {@code checkCapacity()}.
     * It also handles changes to {@code modCount} and {@code size}.
     * Subclasses could override to fully control adds to the map.
     *
     * @param hashIndex  the index into the data array to store at
     * @param hashCode  the hash code of the key to add
     * @param key  the key to add
     * @param value  the value to add
     */
    private void addMapping(final int hashIndex, final int hashCode, final long key, final V value) {
        final LongHashEntry<V> entry = new LongHashEntry<>(data[hashIndex], key, value);
        data[hashIndex] = entry;
        size++;
        checkCapacity();
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
    private void removeMapping(final LongHashEntry<V> entry, final int hashIndex, final LongHashEntry<V> previous) {
        removeEntry(entry, hashIndex, previous);
        size--;
        destroyEntry(entry);
    }

    /**
     * Removes an entry from the chain stored in a particular index.
     * <p>
     * This implementation removes the entry from the data storage table.
     * The size is not updated.
     * Subclasses could override to handle changes to the map.
     *
     * @param entry  the entry to remove
     * @param hashIndex  the index into the data structure
     * @param previous  the previous entry in the chain
     */
    private void removeEntry(final LongHashEntry<V> entry, final int hashIndex, final LongHashEntry<V> previous) {
        if (previous == null) {
            data[hashIndex] = entry.next;
        } else {
            previous.next = entry.next;
        }
    }

    /**
     * Kills an entry ready for the garbage collector.
     * <p>
     * This implementation prepares the LongHashEntry for garbage collection.
     * Subclasses can override this to implement caching (override clear as well).
     *
     * @param entry  the entry to destroy
     */
    private static void destroyEntry(final LongHashEntry<?> entry) {
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
            final int newCapacity = data.length * 2;
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
            data = new LongHashEntry[newCapacity];
        } else {
            final LongHashEntry<V>[] oldEntries = data;
            final LongHashEntry<V>[] newEntries = new LongHashEntry[newCapacity];

            for (int i = oldCapacity - 1; i >= 0; i--) {
                LongHashEntry<V> entry = oldEntries[i];
                if (entry != null) {
                    oldEntries[i] = null;  // gc
                    do {
                        final LongHashEntry<V> next = entry.next;
                        final int index = hashIndex(hash(entry.key), newCapacity);
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
    private int calculateNewCapacity(final int proposedCapacity) {
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
    private int calculateThreshold(final int newCapacity, final float factor) {
        return (int) (newCapacity * factor);
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeFloat(loadFactor);
        out.writeInt(data.length);
        out.writeInt(size);
        for (final MapIterator.LongKeys<V> it = mapIterator(); it.hasNext();) {
            out.writeLong(it.nextLong());
            out.writeObject(it.getValue());
        }
    }

    /**
     * Read the map in using a custom routine.
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
        data = new LongHashEntry[capacity];
        for (int i = 0; i < size; i++) {
            final long key = in.readLong();
            final V value = (V) in.readObject();
            put(key, value);
        }
    }

    @Override
    public int hashCode() {
        return MapUtils.hashCode(mapIterator());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map)) {
            return false;
        }
        final Map<?, ?> map = (Map<?, ?>) obj;
        try {
            return MapUtils.isEqualMap(mapIterator(), map);
        } catch (final ClassCastException | NullPointerException ignored) {
            return false;
        }
    }

    @Override
    public String toString() {
        return MapUtils.toString(mapIterator());
    }

    private final static class MapAdapter<V> extends AbstractMap<Long, V> implements IterableMap<Long, V> {
        private final LongObjectMap<V> parent;
        private EntrySet<V> entrySet;

        public MapAdapter(LongObjectMap<V> parent) {
            this.parent = parent;
        }

        private long checkKey(final Object key) {
            Objects.requireNonNull(key, "null key not supported");
            if (!(key instanceof Long)) {
                throw new ClassCastException("key must be a Long");
            }
            return (long) key;
        }

        @SuppressWarnings("unchecked")
        private V checkValue(final Object value) {
            return (V) value;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return parent.containsKey(checkKey(key));
        }

        @Override
        public boolean containsValue(final Object value) {
            return parent.containsValue(checkValue(value));
        }

        @Override
        public V get(final Object key) {
            return parent.getOrDefault(checkKey(key), null);
        }

        @Override
        public V getOrDefault(final Object key, final V defaultValue) {
            return parent.getOrDefault(checkKey(key), defaultValue);
        }

        @Override
        public V put(final Long key, final V value) {
            return parent.put(checkKey(key), value);
        }

        @Override
        public V putIfAbsent(final Long key, final V value) {
            return parent.putIfAbsent(checkKey(key), value);
        }

        @Override
        public boolean replace(final Long key, final V oldValue, final V newValue) {
            return parent.replace(checkKey(key), oldValue, newValue);
        }

        @Override
        public V replace(final Long key, final V value) {
            return parent.replace(checkKey(key), value);
        }

        @Override
        public V remove(final Object key) {
            return parent.remove(checkKey(key));
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            return parent.remove(checkKey(key), checkValue(value));
        }

        @Override
        public void putAll(final Map<? extends Long, ? extends V> m) {
            parent.putAll(m);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public void forEach(final BiConsumer<? super Long, ? super V> action) {
            parent.forEach(action::accept);
        }

        @Override
        public void replaceAll(final BiFunction<? super Long, ? super V, ? extends V> function) {
            Objects.requireNonNull(function);
            for (final LongHashEntry<V> element : parent.data) {
                LongHashEntry<V> entry = element;
                while (entry != null) {
                    entry.value = function.apply(entry.key, entry.value);
                    entry = entry.next;
                }
            }
        }

        @Override
        public V computeIfAbsent(final Long key, final Function<? super Long, ? extends V> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry<V> entry = parent.data[index];
            while (entry != null) {
                if (entry.key == key) {
                    final V oldValue = entry.value;
                    if (oldValue == null) {
                        final V newValue = mappingFunction.apply(key);
                        entry.value = newValue;
                        return newValue;
                    } else {
                        return oldValue;
                    }
                }
                entry = entry.next;
            }

            final V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                parent.addMapping(index, hashCode, key, newValue);
            }
            return newValue;
        }

        @Override
        public V computeIfPresent(final Long key, final BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry<V> entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final V oldValue = entry.value;
                    if (oldValue != null) {
                        final V newValue = remappingFunction.apply(key, oldValue);
                        if (newValue != null) {
                            entry.value = newValue;
                            return newValue;
                        } else {
                            parent.removeMapping(entry, index, previous);
                        }
                    }
                    return null;
                }
                previous = entry;
                entry = entry.next;
            }
            return null;
        }

        @Override
        public V compute(final Long key, final BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry<V> entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final V oldValue = entry.value;
                    final V newValue = remappingFunction.apply(key, oldValue);
                    if (newValue != null) {
                        entry.value = newValue;
                        return newValue;
                    } else {
                        parent.removeMapping(entry, index, previous);
                        return null;
                    }
                }
                previous = entry;
                entry = entry.next;
            }

            final V newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
                parent.addMapping(index, hashCode, key, newValue);
            }
            return newValue;
        }

        @Override
        public V merge(final Long key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            Objects.requireNonNull(value);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry<V> entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final V oldValue = entry.value;
                    if (oldValue != null) {
                        final V newValue = remappingFunction.apply(oldValue, value);
                        if (newValue != null) {
                            entry.value = newValue;
                            return newValue;
                        } else {
                            parent.removeMapping(entry, index, previous);
                            return null;
                        }
                    } else {
                        entry.value = value;
                        return value;
                    }
                }
                previous = entry;
                entry = entry.next;
            }

            parent.addMapping(index, hashCode, key, value);
            return value;
        }

        @Override
        public MapIterator.LongKeys<V> mapIterator() {
            return parent.mapIterator();
        }

        @Override
        public Set<Long> keySet() {
            if (parent.keySet == null) {
                parent.keySet = new KeySet(parent);
            }
            return parent.keySet;
        }

        @Override
        public Collection<V> values() {
            return parent.values();
        }

        @Override
        public Set<Entry<Long, V>> entrySet() {
            if (entrySet == null) {
                entrySet = new EntrySet<>(parent);
            }
            return entrySet;
        }
    }

    private final static class LongHashEntry<V> implements Map.Entry<Long, V> {
        /** The next entry in the hash chain */
        LongHashEntry<V> next;
        /** The key */
        final long key;
        /** The value */
        V value;

        LongHashEntry(final LongHashEntry<V> next, final long key, final V value) {
            this.next = next;
            this.key = key;
            this.value = value;
        }

        @Override
        public Long getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(final V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
            if (!(that.getKey() instanceof Long))
                return false;
            return key == (Long) that.getKey() && Objects.equals(value, that.getValue());
        }

        @Override
        public int hashCode() {
            return Long.hashCode(key) ^ Objects.hashCode(value);
        }
    }

    private final static class KeySet<V> extends AbstractSet<Long> implements LongSet, Set<Long> {
        private final LongObjectMap<V> parent;

        public KeySet(LongObjectMap<V> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size;
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean contains(final Object k) {
            if (!(k instanceof Long))
                return false;
            return parent.containsKey((long) k);
        }

        @Override
        public boolean contains(final long k) {
            return parent.containsKey(k);
        }

        @Override
        public boolean remove(final long k) {
            return parent.removeKey(k);
        }

        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Long))
                return false;
            return parent.removeKey((long) o);
        }

        @Override
        public boolean containsAll(final Collection<?> coll) {
            for (final Object item : coll) {
                if (!contains(item))
                    return false;
            }
            return true;
        }

        @Override
        public void forEach(final Consumer<? super Long> action) {
            Objects.requireNonNull(action);
            for (final LongHashEntry<?> element : parent.data) {
                LongHashEntry<?> entry = element;
                while (entry != null) {
                    action.accept(entry.key);
                    entry = entry.next;
                }
            }
        }

        @Override
        public void forEach(final LongConsumer action) {
            Objects.requireNonNull(action);
            for (final LongHashEntry<?> element : parent.data) {
                LongHashEntry<?> entry = element;
                while (entry != null) {
                    action.accept(entry.key);
                    entry = entry.next;
                }
            }
        }

        @Override
        public long[] toLongArray() {
            return parent.toKeyArray();
        }

        @Override
        public Object[] toArray() {
            Object[] array = new Object[parent.size];
            int index = 0;
            for (final LongHashEntry<?> element : parent.data) {
                LongHashEntry<?> entry = element;
                while (entry != null) {
                    array[index++] = entry.key;
                    entry = entry.next;
                }
            }
            return array;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] suppliedArray) {
            final int size = parent.size;
            final T[] array = suppliedArray.length >= size
                              ? suppliedArray
                              : (T[]) Array.newInstance(suppliedArray.getClass().getComponentType(), size);
            int index = 0;
            for (final LongHashEntry<?> element : parent.data) {
                LongHashEntry<?> entry = element;
                while (entry != null) {
                    array[index++] = (T) (Long) entry.key;
                    entry = entry.next;
                }
            }
            if (index < array.length)
                array[index] = null;
            return array;
        }

        @Override
        public boolean add(Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends Long> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            boolean changed = false;
            for (final Object item : coll) {
                changed |= remove(item);
            }
            return changed;
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
           return removeIf(key -> !coll.contains(key));
        }

        @Override
        public boolean removeIf(final Predicate<? super Long> filter) {
            Objects.requireNonNull(filter);
            boolean changed = false;
            LongHashEntry<V>[] data = parent.data;
            for (int index = data.length - 1; index >= 0; index--) {
                LongHashEntry<V> entry = data[index];
                if (entry != null) {
                    while (entry != null && filter.test(entry.key)) {
                        final LongHashEntry<V> next = entry.next;
                        destroyEntry(entry);
                        parent.size--;
                        entry = next;
                        changed = true;
                    }
                    if (changed) {
                        data[index] = entry;
                    }
                    if (entry != null) {
                        LongHashEntry<V> previous = entry;
                        entry = entry.next;
                        while (entry != null) {
                            if (filter.test(entry.key)) {
                                final LongHashEntry<V> next = entry.next;
                                destroyEntry(entry);
                                parent.size--;
                                previous.next = next;
                                entry = next;
                                changed = true;
                            } else {
                                previous = entry;
                                entry = entry.next;
                            }
                        }
                    }
                }
            }
            return changed;
        }

        @Override
        public PrimitiveIterator.OfLong iterator() {
            return parent.keyIterator();
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return parent.keySpliterator();
        }
    }

    public interface LongSet {
        int size();
        boolean isEmpty();
        boolean contains(long k);
        boolean remove(long k);

        void forEach(LongConsumer action);
        long[] toLongArray();
        PrimitiveIterator.OfLong iterator();
        Spliterator.OfLong spliterator();

        default LongStream longStream() {
            return StreamSupport.longStream(spliterator(), false);
        }

        default LongStream longParallelStream() {
            return StreamSupport.longStream(spliterator(), true);
        }
    }

    private final static class EntrySet<V> extends AbstractSet<Map.Entry<Long, V>> {
        /** The parent map */
        private final LongObjectMap<V> parent;

        public EntrySet(final LongObjectMap<V> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean contains(final Object entry) {
            return parent.containsEntry(entry);
        }

        @Override
        public boolean remove(final Object obj) {
            return parent.removeEntry(obj);
        }

        @Override
        public Iterator<Map.Entry<Long, V>> iterator() {
            return parent.entrySetIterator();
        }

        @Override
        public Spliterator<Map.Entry<Long, V>> spliterator() {
            return parent.entrySetSpliterator();
        }
    }

    private final static class Values<V> extends AbstractCollection<V> {
        /** The parent map */
        private final LongObjectMap<V> parent;

        private Values(final LongObjectMap<V> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(final Object value) {
            return parent.containsValue((V) value);
        }

        @Override
        public Iterator<V> iterator() {
            return parent.valuesIterator();
        }

        @Override
        public Spliterator<V> spliterator() {
            return parent.valuesSpliterator();
        }
    }

    public MapIterator.LongKeys<V> mapIterator() {
        return new LongMapIterator();
    }

    public PrimitiveIterator.OfLong keyIterator() {
        return new LongKeyIterator();
    }

    public Spliterator.OfLong keySpliterator() {
        return new LongKeySpliterator();
    }

    public Iterator<V> valuesIterator() {
        return new LongValuesIterator();
    }

    public Spliterator<V> valuesSpliterator() {
        return new LongValuesSpliterator();
    }

    public Iterator<Map.Entry<Long,V>> entrySetIterator() {
        return new EntrySetIterator();
    }

    public Spliterator<Map.Entry<Long,V>> entrySetSpliterator() {
        return new EntrySetSpliterator();
    }

    private abstract class LongBaseIterator {
        private LongHashEntry<V> next;
        protected LongHashEntry<V> current;
        private LongHashEntry<V> previous;
        private int currentIndex;
        private int nextIndex;

        LongBaseIterator() {
            reset();
        }

        public void reset() {
            currentIndex = -1;
            current = null;
            previous = null;
            for (int i = data.length - 1; i >= 0; i--) {
                final LongHashEntry<V> e = data[i];
                if (e != null) {
                    next = e;
                    nextIndex = i;
                    return;
                }
            }
            next = null;
            nextIndex = -1;
        }

        public boolean hasNext() {
            return next != null;
        }

        protected LongHashEntry<V> nextEntry() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            if (current != null) {
                previous = current;
            }
            current = next;
            if (currentIndex != nextIndex) {
                currentIndex = nextIndex;
                previous = null;
            }

            if (next.next != null) {
                next = next.next;
            } else {
                for (int i = nextIndex - 1; i >= 0; i--) {
                    final LongHashEntry<V> e = data[i];
                    if (e != null) {
                        next = e;
                        nextIndex = i;
                        return current;
                    }
                }
                next = null;
                nextIndex = -1;
            }
            return current;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            removeMapping(current, currentIndex, previous);
            current = null;
        }
    }

    private final class LongKeyIterator extends LongBaseIterator implements PrimitiveIterator.OfLong, ResettableIterator<Long> {
        @Override
        public long nextLong() {
            return nextEntry().key;
        }
    }

    private final class LongValuesIterator extends LongBaseIterator implements Iterator<V> {
        @Override
        public V next() {
            return nextEntry().value;
        }
    }

    private final class EntrySetIterator extends LongBaseIterator implements Iterator<Map.Entry<Long,V>> {
        @Override
        public Map.Entry<Long, V> next() {
            return nextEntry();
        }
    }

    private class LongMapIterator extends LongBaseIterator implements MapIterator.LongKeys<V> {
        @Override
        public long nextLong() {
            return nextEntry().key;
        }

        @Override
        public V getValue() {
            if (current == null)
                throw new IllegalStateException();
            return current.value;
        }

        @Override
        public long getKeyLong() {
            if (current == null)
                throw new IllegalStateException();
            return current.key;
        }

        @Override
        public V setValue(final V value) {
            if (current == null)
                throw new IllegalStateException();
            return current.setValue(value);
        }
    }

    private abstract class LongBaseSpliterator<T extends LongBaseSpliterator<T>> {
        private LongHashEntry<V> next;
        private int nextIndex;
        private final int lastIndex;
        private long estimateSize;
        protected boolean isSplit;

        LongBaseSpliterator() {
            nextIndex = data.length - 1;
            lastIndex = 0;
            estimateSize = size;
        }

        LongBaseSpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            this.nextIndex = nextIndex;
            this.lastIndex = lastIndex;
            this.estimateSize = estimateSize;
        }

        protected abstract T newSplit(int nextIndex, int lastIndex, long estimateSize);

        public boolean tryAdvanceEntry(final Consumer<LongHashEntry<V>> action) {
            if (next != null) {
                action.accept(next);
                next = next.next;
                return true;
            }
            for (int i = nextIndex; i >= lastIndex; i--) {
                final LongHashEntry<V> entry = data[i];
                if (entry != null) {
                    action.accept(entry);
                    next = entry.next;
                    nextIndex = i - 1;
                    return true;
                }
            }
            nextIndex = -1;
            return false;
        }

        public T trySplit() {
            final int mid = lastIndex + (nextIndex - lastIndex) / 2;
            if (lastIndex < mid && mid < nextIndex) {
                T split = newSplit(nextIndex, mid + 1, estimateSize >>>= 1);
                nextIndex = mid;
                return split;
            }
            return null;
        }

        public long estimateSize() {
            return estimateSize;
        }
    }

    private final class LongKeySpliterator extends LongBaseSpliterator<LongKeySpliterator> implements Spliterator.OfLong {
        public LongKeySpliterator() {
        }

        public LongKeySpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            super(nextIndex, lastIndex, estimateSize);
        }

        @Override
        protected LongKeySpliterator newSplit(final int nextIndex, final int lastIndex, final long estimateSize) {
            return new LongKeySpliterator(nextIndex, lastIndex, estimateSize);
        }

        @Override
        public boolean tryAdvance(final LongConsumer action) {
            return tryAdvanceEntry(entry -> action.accept(entry.key));
        }

        @Override
        public int characteristics() {
            int characteristics = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT;
            if (!isSplit)
                characteristics |= Spliterator.SIZED;
            return characteristics;
        }
    }

    private final class LongValuesSpliterator extends LongBaseSpliterator<LongValuesSpliterator> implements Spliterator<V> {
        public LongValuesSpliterator() {
        }

        public LongValuesSpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            super(nextIndex, lastIndex, estimateSize);
        }

        @Override
        protected LongValuesSpliterator newSplit(final int nextIndex, final int lastIndex, final long estimateSize) {
            return new LongValuesSpliterator(nextIndex, lastIndex, estimateSize);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super V> action) {
            return tryAdvanceEntry(entry -> action.accept(entry.value));
        }

        @Override
        public int characteristics() {
            int characteristics = Spliterator.ORDERED;
            if (!isSplit)
                characteristics |= Spliterator.SIZED;
            return characteristics;
        }
    }

    private final class EntrySetSpliterator extends LongBaseSpliterator<EntrySetSpliterator> implements Spliterator<Map.Entry<Long,V>> {
        public EntrySetSpliterator() {
        }

        public EntrySetSpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            super(nextIndex, lastIndex, estimateSize);
        }

        @Override
        protected EntrySetSpliterator newSplit(final int nextIndex, final int lastIndex, final long estimateSize) {
            return new EntrySetSpliterator(nextIndex, lastIndex, estimateSize);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super Map.Entry<Long, V>> action) {
            return tryAdvanceEntry(action::accept);
        }

        @Override
        public int characteristics() {
            int characteristics = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT;
            if (!isSplit)
                characteristics |= Spliterator.SIZED;
            return characteristics;
        }
    }
}
