/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.primitive;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.ResettableIterator;

@SuppressWarnings({"unused", "PublicMethodNotExposedInInterface"})
public final class LongToLongMap implements Externalizable {
    private static final long serialVersionUID = -8244029886089985815L;

    public static final long NOT_FOUND = Long.MIN_VALUE; 
    
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
    // TODO try with just a flat array?
    /** Map entries */
    private transient LongHashEntry[] data;
    /** Size at which to rehash */
    private transient int threshold;

    /** Key set */
    private transient KeySet keySet;
    /** Values */
    private transient Values values;
    private transient MapAdapter mapAdapter;

    /**
     * Constructs a new empty map with default size and load factor.
     */
    public LongToLongMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_THRESHOLD);
    }

    /**
     * Constructor which performs no validation on the passed in parameters.
     *
     * @param initialCapacity  the initial capacity, must be a power of two
     * @param loadFactor  the load factor, must be &gt; 0.0f and generally &lt; 1.0f
     * @param threshold  the threshold, must be sensible
     */
    public LongToLongMap(final int initialCapacity, final float loadFactor, final int threshold) {
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
    public LongToLongMap(final int initialCapacity) {
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
    public LongToLongMap(int initialCapacity, final float loadFactor) {
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
    public LongToLongMap(final Map<? extends Long, ? extends Long> map) {
        this(Math.max(2 * map.size(), DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(map);
    }

    /**
     * Constructor copying elements from another map.
     *
     * @param map  the map to copy
     * @throws NullPointerException if the map is null
     */
    public LongToLongMap(final LongToLongMap map) {
        this(Math.max(2 * map.size(), DEFAULT_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAll(map);
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_longALUE}.
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
     * @return the mapped value, -1 if no match
     */
    public long get(final long key) {
        return getOrDefault(key, NOT_FOUND);
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
    public long getOrDefault(final long key, final long defaultValue) {
        final int hashCode = hash(key);
        LongHashEntry entry = data[hashIndex(hashCode, data.length)];
        while (entry != null) {
            if (entry.key == key) {
                return entry.value;
            }
            entry = entry.next;
        }
        return defaultValue;
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key.
     * Uses object types to allow null.
     * <p>
     * More compatible with {@link Map#getOrDefault}.
     */
    public Long getOrDefaultNullable(final long key, final Long defaultValue) {
        final int hashCode = hash(key);
        LongHashEntry entry = data[hashIndex(hashCode, data.length)];
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
        LongHashEntry entry = data[hashIndex(hashCode, data.length)];
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
    public boolean containsValue(final long value) {
        for (final LongHashEntry element : data) {
            LongHashEntry entry = element;
            while (entry != null) {
                if (value == entry.value) {
                    return true;
                }
                entry = entry.next;
            }
        }

        return false;
    }

    /**
     * Puts a key-value mapping into this map.
     *
     * @param key  the key to add
     * @param value  the value to add
     * @return the value previously mapped to this key, or {@link #NOT_FOUND} if none
     */
    public long put(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entry = entry.next;
        }

        addMapping(index, hashCode, key, value);
        return NOT_FOUND;
    }

    public Long putNullable(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entry = entry.next;
        }

        addMapping(index, hashCode, key, value);
        return null;
    }

    /**
     * If the specified key is not already associated with a value associates it with the given value and returns
     * {@link #NOT_FOUND}, else returns the current value.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@link #NOT_FOUND} if there was no mapping for the key.
     */
    public long putIfAbsent(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                return entry.value;
            }
            entry = entry.next;
        }

        addMapping(index, hashCode, key, value);
        return NOT_FOUND;
    }

    public Long putIfAbsentNullable(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                return entry.value;
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
    public boolean replace(final long key, final long oldValue, final long newValue) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final long currentValue = entry.value;
                if (currentValue == oldValue) {
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
     *         {@link #NOT_FOUND} if there was no mapping for the key.
     */
    public long replace(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            entry = entry.next;
        }
        return NOT_FOUND;
    }

    private Long replaceNullable(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
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
     * uses {@link #put(long, long)}.
     *
     * @param map  the map to add
     * @throws NullPointerException if the map is null
     * @throws ClassCastException if the class of a value in the
     *      specified map prevents it from being stored in this map.
     * @throws NullPointerException if the specified map is null, if the
     *      specified map contains null keys
     */
    public void putAll(final Map<? extends Long, ? extends Long> map) {
        if (checkPutAll(map.size())) {
            for (final Map.Entry<? extends Long, ? extends Long> entry : map.entrySet()) {
                final Long key = entry.getKey();
                final Long value = entry.getKey();
                Objects.requireNonNull(key);
                Objects.requireNonNull(value);
                put(key, value);
            }
        }
    }

    /**
     * Puts all the values from the specified LongMap into this map.
     * <p>
     * This implementation iterates around the specified map and
     * uses {@link #put(long, long)}.
     *
     * @param map  the map to add
     * @throws NullPointerException if the map is null
     * * @throws ClassCastException if the class of a key or value in the
     *      *         specified map prevents it from being stored in this map
     *      * @throws NullPointerException if the specified map is null, or if
     *      *         this map does not permit null keys or values, and the
     *      *         specified map contains null keys or values
     */
    public void putAll(final LongToLongMap map) {
        if (checkPutAll(map.size())) {
            for (final LongHashEntry element : map.data) {
                LongHashEntry entry = element;
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
        LongHashEntry entry = data[index];
        LongHashEntry previous = null;
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
     * @return the value mapped to the removed key, {@link #NOT_FOUND} if key not in map
     */
    public long remove(final long key) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        LongHashEntry previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                removeMapping(entry, index, previous);
                return oldValue;
            }
            previous = entry;
            entry = entry.next;
        }
        return NOT_FOUND;
    }

    public Long removeNullable(final long key) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        LongHashEntry previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
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
    public boolean remove(final long key, final long value) {
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        LongHashEntry previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                if (oldValue == value) {
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

    public boolean removeValue(final long value) {
        for (int index = data.length - 1; index >= 0; index--) {
            LongHashEntry entry = data[index];
            LongHashEntry previous = null;
            while (entry != null) {
                if (value == entry.value) {
                    removeMapping(entry, index, previous);
                    return true;
                }
                previous = entry;
                entry = entry.next;
            }
        }
        return false;
    }

    public Long compute(final long key, final LongCompute remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final int hashCode = hash(key);
        final int index = hashIndex(hashCode, data.length);
        LongHashEntry entry = data[index];
        LongHashEntry previous = null;
        while (entry != null) {
            if (entry.key == key) {
                final long oldValue = entry.value;
                final Long newValue = remappingFunction.apply(key, true, oldValue);
                if (newValue != null) {
                    entry.value = newValue;
                } else {
                    removeMapping(entry, index, previous);
                }
                return newValue;
            }
            previous = entry;
            entry = entry.next;
        }

        final Long newValue = remappingFunction.apply(key, false, 0);
        if (newValue != null) {
            addMapping(index, hashCode, key, newValue);
        }
        return newValue;
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
    public Map<Long, Long> asMap() {
        if (mapAdapter == null) {
            mapAdapter = new MapAdapter(this);
        }
        return mapAdapter;
    }

    public long[] toKeyArray() {
        final long[] array = new long[size];
        int index = 0;
        for (final LongHashEntry element : data) {
            LongHashEntry entry = element;
            while (entry != null) {
                array[index++] = entry.key;
                entry = entry.next;
            }
        }
        return array;
    }

    public long[] toValueArray() {
        final long[] array = new long[size];
        int index = 0;
        for (final LongHashEntry element : data) {
            LongHashEntry entry = element;
            while (entry != null) {
                array[index++] = entry.value;
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
    public void forEach(final LongBiConsumer action) {
        Objects.requireNonNull(action);
        for (final LongHashEntry element : data) {
            LongHashEntry entry = element;
            while (entry != null) {
                action.accept(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }

    // TODO maybe should be containsMapping
    public boolean containsEntry(final long key, final long value) {
        final int hashCode = hash(key);
        LongHashEntry e = data[hashIndex(hashCode, data.length)];
        while (e != null) {
            if (e.key == key)
                return e.value == value;
            e = e.next;
        }
        return false;
    }

    public boolean containsEntry(final Map.Entry<Long, Long> entry) {
        return containsEntry(entry.getKey(), entry.getValue());
    }

    public boolean removeEntry(final Map.Entry<Long, Long> entry) {
        return remove(entry.getKey(), entry.getValue());
    }

    public LongCollection keySet() {
        if (keySet == null) {
            keySet = new KeySet(this);
        }
        return keySet;
    }

    public LongCollection values() {
        if (values == null) {
            values = new Values(this);
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
    @SuppressWarnings("MagicNumber")
    private int hash(final long key) {
        int h = (int) (key ^ (key >>> 32));
        h += ~(h << 9);
        h ^=  h >>> 14;
        h +=  h << 4;
        h ^=  h >>> 10;
        return h;
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
    private void addMapping(final int hashIndex, final int hashCode, final long key, final long value) {
        final LongHashEntry entry = new LongHashEntry(data[hashIndex], key, value);
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
    private void removeMapping(final LongHashEntry entry, final int hashIndex, final LongHashEntry previous) {
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
    private void removeEntry(final LongHashEntry entry, final int hashIndex, final LongHashEntry previous) {
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
    private static void destroyEntry(final LongHashEntry entry) {
        entry.next = null;
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
    private void ensureCapacity(final int newCapacity) {
        final int oldCapacity = data.length;
        if (newCapacity <= oldCapacity) {
            return;
        }
        if (size == 0) {
            threshold = calculateThreshold(newCapacity, loadFactor);
            data = new LongHashEntry[newCapacity];
        } else {
            final LongHashEntry[] oldEntries = data;
            final LongHashEntry[] newEntries = new LongHashEntry[newCapacity];

            for (int i = oldCapacity - 1; i >= 0; i--) {
                LongHashEntry entry = oldEntries[i];
                if (entry != null) {
                    oldEntries[i] = null;  // gc
                    do {
                        final LongHashEntry next = entry.next;
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
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeFloat(loadFactor);
        out.writeInt(data.length);
        out.writeInt(size);
        for (final MapIteratorLongToLong it = mapIterator(); it.hasNext();) {
            out.writeLong(it.nextLong());
            out.writeLong(it.getValueLong());
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
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        loadFactor = in.readFloat();
        final int capacity = in.readInt();
        final int size = in.readInt();
        threshold = calculateThreshold(capacity, loadFactor);
        data = new LongHashEntry[capacity];
        for (int i = 0; i < size; i++) {
            final long key = in.readLong();
            final long value = in.readLong();
            put(key, value);
        }
    }

    @Override
    public int hashCode() {
        // TODO primitive version
        return MapUtils.hashCode(mapIterator());
    }

    @Override
    public boolean equals(final Object obj) {
        // TODO primitive version
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

    private static final class MapAdapter extends AbstractMap<Long, Long> implements IterableMap<Long, Long> {
        private static final long serialVersionUID = 7665757042811288057L;

        private final LongToLongMap parent;
        private EntrySet entrySet;

        private MapAdapter(final LongToLongMap parent) {
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
        private long checkValue(final Object value) {
            if (!(value instanceof Long)) {
                throw new ClassCastException("value must be a Long");
            }
            return (long) value;
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
        public Long get(final Object key) {
            return parent.getOrDefaultNullable(checkKey(key), null);
        }

        @Override
        public Long getOrDefault(final Object key, final Long defaultValue) {
            return parent.getOrDefaultNullable(checkKey(key), defaultValue);
        }

        @Override
        public Long put(final Long key, final Long value) {
            return parent.putNullable(checkKey(key), checkValue(value));
        }

        @Override
        public Long putIfAbsent(final Long key, final Long value) {
            return parent.putIfAbsentNullable(checkKey(key), checkValue(value));
        }

        @Override
        public boolean replace(final Long key, final Long oldValue, final Long newValue) {
            return parent.replace(checkKey(key), checkValue(oldValue), checkValue(newValue));
        }

        @Override
        public Long replace(final Long key, final Long value) {
            return parent.replaceNullable(checkKey(key), checkValue(value));
        }

        @Override
        public Long remove(final Object key) {
            return parent.removeNullable(checkKey(key));
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            return parent.remove(checkKey(key), checkValue(value));
        }

        @Override
        public void putAll(final Map<? extends Long, ? extends Long> m) {
            parent.putAll(m);
        }

        @Override
        public void putAll(final MapIterator<? extends Long, ? extends Long> it) {
            it.forEachRemaining((Long k, Long v) -> put(k, v));
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public void forEach(final BiConsumer<? super Long, ? super Long> action) {
            parent.forEach(action::accept);
        }

        @Override
        public void replaceAll(final BiFunction<? super Long, ? super Long, ? extends Long> function) {
            Objects.requireNonNull(function);
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    entry.value = checkValue(function.apply(entry.key, entry.value));
                    entry = entry.next;
                }
            }
        }

        @Override
        public Long computeIfAbsent(final Long key, final Function<? super Long, ? extends Long> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry entry = parent.data[index];
            while (entry != null) {
                if (entry.key == key) {
                    return entry.value;
                }
                entry = entry.next;
            }

            final Long newValue = mappingFunction.apply(key);
            if (newValue != null) {
                parent.addMapping(index, hashCode, key, newValue);
            }
            return newValue;
        }

        @Override
        public Long computeIfPresent(final Long key, final BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final long oldValue = entry.value;
                    final Long newValue = remappingFunction.apply(key, oldValue);
                    if (newValue != null) {
                        entry.value = newValue;
                    } else {
                        parent.removeMapping(entry, index, previous);
                    }
                    return newValue;
                }
                previous = entry;
                entry = entry.next;
            }
            return null;
        }

        @Override
        public Long compute(final Long key, final BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final long oldValue = entry.value;
                    final Long newValue = remappingFunction.apply(key, oldValue);
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

            final Long newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
                parent.addMapping(index, hashCode, key, newValue);
            }
            return newValue;
        }

        @Override
        public Long merge(final Long key, final Long value, final BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            checkValue(value);
            final int hashCode = parent.hash(checkKey(key));
            final int index = hashIndex(hashCode, parent.data.length);
            LongHashEntry entry = parent.data[index], previous = null;
            while (entry != null) {
                if (entry.key == key) {
                    final long oldValue = entry.value;
                    final Long newValue = remappingFunction.apply(oldValue, value);
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

            parent.addMapping(index, hashCode, key, value);
            return value;
        }

        @Override
        public MapIteratorLongToLong mapIterator() {
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
        public Collection<Long> values() {
            if (parent.values == null) {
                parent.values = new Values(parent);
            }
            return parent.values;
        }

        @Override
        public Set<Entry<Long, Long>> entrySet() {
            if (entrySet == null) {
                entrySet = new EntrySet(parent);
            }
            return entrySet;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            parent.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            parent.readExternal(in);
        }
    }

    private static final class LongHashEntry implements Map.Entry<Long, Long> {
        /** The next entry in the hash chain */
        LongHashEntry next;
        /** The key */
        final long key;
        /** The value */
        long value;

        LongHashEntry(final LongHashEntry next, final long key, final long value) {
            this.next = next;
            this.key = key;
            this.value = value;
        }

        /**
         * Should only use for compatibility with Map interface.
         */
        @Override
        @Deprecated
        public Long getKey() {
            return key;
        }

        /**
         * Should only use for compatibility with Map interface.
         */
        @Override
        @Deprecated
        public Long getValue() {
            return value;
        }

        @Override
        @Deprecated
        public Long setValue(final Long newValue) {
            return setValueLong(newValue);
        }

        public long setValueLong(final long newValue) {
            final long oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o instanceof Map.Entry) {
                final Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
                if (other.getKey() instanceof Long && other.getValue() instanceof Long) {
                    return key == (Long) other.getKey() && value == (Long) other.getValue();
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(key) ^ Long.hashCode(value);
        }
    }

    private static final class KeySet extends AbstractSet<Long> implements LongCollection {
        private final LongToLongMap parent;

        private KeySet(final LongToLongMap parent) {
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
            if (k instanceof Long) {
                return parent.containsKey((long) k);
            } else {
                return false;
            }
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
        public boolean remove(final Object k) {
            if (k instanceof Long) {
                return parent.removeKey((long) k);
            } else {
                return false;
            }
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
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    action.accept(entry.key);
                    entry = entry.next;
                }
            }
        }

        @Override
        public void forEach(final LongConsumer action) {
            Objects.requireNonNull(action);
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
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
            final Object[] array = new Object[parent.size];
            int index = 0;
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
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
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
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
        public boolean add(final Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends Long> c) {
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
            final LongHashEntry[] data = parent.data;
            for (int index = data.length - 1; index >= 0; index--) {
                LongHashEntry entry = data[index];
                if (entry != null) {
                    while (entry != null && filter.test(entry.key)) {
                        final LongHashEntry next = entry.next;
                        destroyEntry(entry);
                        parent.size--;
                        entry = next;
                        changed = true;
                    }
                    if (changed) {
                        data[index] = entry;
                    }
                    if (entry != null) {
                        LongHashEntry previous = entry;
                        entry = entry.next;
                        while (entry != null) {
                            if (filter.test(entry.key)) {
                                final LongHashEntry next = entry.next;
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

    private static final class Values extends AbstractCollection<Long> implements LongCollection {
        /** The parent map */
        private final LongToLongMap parent;

        private Values(final LongToLongMap parent) {
            this.parent = parent;
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
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean contains(final long v) {
            return parent.containsValue(v);
        }

        @Override
        public boolean contains(final Object v) {
            if (v instanceof Long) {
                return parent.containsValue((long) v);
            } else {
                return false;
            }
        }

        @Override
        public boolean remove(final long v) {
            return parent.removeValue(v);
        }

        @Override
        public boolean remove(final Object v) {
            if (v instanceof Long) {
                return parent.removeValue((long) v);
            } else {
                return false;
            }
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
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    action.accept(entry.value);
                    entry = entry.next;
                }
            }
        }

        @Override
        public void forEach(final LongConsumer action) {
            Objects.requireNonNull(action);
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    action.accept(entry.value);
                    entry = entry.next;
                }
            }
        }

        @Override
        public long[] toLongArray() {
            return parent.toValueArray();
        }

        @Override
        public Object[] toArray() {
            final Object[] array = new Object[parent.size];
            int index = 0;
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    array[index++] = entry.value;
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
            for (final LongHashEntry element : parent.data) {
                LongHashEntry entry = element;
                while (entry != null) {
                    array[index++] = (T) (Long) entry.value;
                    entry = entry.next;
                }
            }
            if (index < array.length)
                array[index] = null;
            return array;
        }

        @Override
        public boolean add(final Long aLong) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends Long> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            return removeIf(coll::contains);
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            return removeIf(key -> !coll.contains(key));
        }

        @Override
        public boolean removeIf(final Predicate<? super Long> filter) {
            Objects.requireNonNull(filter);
            boolean changed = false;
            final LongHashEntry[] data = parent.data;
            for (int index = data.length - 1; index >= 0; index--) {
                LongHashEntry entry = data[index];
                if (entry != null) {
                    while (entry != null && filter.test(entry.value)) {
                        final LongHashEntry next = entry.next;
                        destroyEntry(entry);
                        parent.size--;
                        entry = next;
                        changed = true;
                    }
                    if (changed) {
                        data[index] = entry;
                    }
                    if (entry != null) {
                        LongHashEntry previous = entry;
                        entry = entry.next;
                        while (entry != null) {
                            if (filter.test(entry.value)) {
                                final LongHashEntry next = entry.next;
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
            return parent.valuesIterator();
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return parent.valuesSpliterator();
        }
    }

    private static final class EntrySet extends AbstractSet<Map.Entry<Long, Long>> {
        /** The parent map */
        private final LongToLongMap parent;

        private EntrySet(final LongToLongMap parent) {
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

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object entry) {
            if (entry instanceof Map.Entry) {
                return parent.containsEntry((Map.Entry<Long, Long>) entry);
            } else {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(final Object entry) {
            if (entry instanceof Map.Entry) {
                return parent.removeEntry((Map.Entry<Long, Long>) entry);
            } else {
                return false;
            }
        }

        @Override
        public Iterator<Map.Entry<Long, Long>> iterator() {
            return parent.entrySetIterator();
        }

        @Override
        public Spliterator<Map.Entry<Long, Long>> spliterator() {
            return parent.entrySetSpliterator();
        }
    }

    public MapIteratorLongToLong mapIterator() {
        return new LongMapIterator();
    }

    public PrimitiveIterator.OfLong keyIterator() {
        return new LongKeyIterator();
    }

    public Spliterator.OfLong keySpliterator() {
        return new LongKeySpliterator();
    }

    public PrimitiveIterator.OfLong valuesIterator() {
        return new LongValuesIterator();
    }

    public Spliterator.OfLong valuesSpliterator() {
        return new LongValuesSpliterator();
    }

    public Iterator<Map.Entry<Long, Long>> entrySetIterator() {
        return new EntrySetIterator();
    }

    public Spliterator<Map.Entry<Long, Long>> entrySetSpliterator() {
        return new EntrySetSpliterator();
    }

    private abstract class LongBaseIterator {
        private LongHashEntry next;
        protected LongHashEntry current;
        private LongHashEntry previous;
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
                final LongHashEntry e = data[i];
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

        LongHashEntry nextEntry() {
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
                    final LongHashEntry e = data[i];
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

    private final class LongValuesIterator extends LongBaseIterator implements PrimitiveIterator.OfLong, ResettableIterator<Long> {
        @Override
        public long nextLong() {
            return nextEntry().value;
        }
    }

    private final class EntrySetIterator extends LongBaseIterator implements Iterator<Map.Entry<Long, Long>> {
        @Override
        public Map.Entry<Long, Long> next() {
            return nextEntry();
        }
    }

    private class LongMapIterator extends LongBaseIterator implements MapIteratorLongToLong {
        @Override
        public long nextLong() throws NoSuchElementException {
            return nextEntry().key;
        }

        @Override
        public long getKeyLong() {
            if (current == null)
                throw new IllegalStateException();
            return current.key;
        }

        @Override
        public long getValueLong() {
            if (current == null)
                throw new IllegalStateException();
            return current.value;
        }

        @Override
        public long setValue(final long value) {
            if (current == null)
                throw new IllegalStateException();
            return current.setValueLong(value);
        }
    }

    private abstract class LongBaseSpliterator<T extends LongToLongMap.LongBaseSpliterator<T>> {
        private LongHashEntry next;
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

        boolean tryAdvanceEntry(final Consumer<LongHashEntry> action) {
            if (next != null) {
                action.accept(next);
                next = next.next;
                return true;
            }
            for (int i = nextIndex; i >= lastIndex; i--) {
                final LongHashEntry entry = data[i];
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
                final T split = newSplit(nextIndex, mid + 1, estimateSize >>>= 1);
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
        private LongKeySpliterator() {
        }

        private LongKeySpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
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

    private final class LongValuesSpliterator extends LongBaseSpliterator<LongValuesSpliterator> implements Spliterator.OfLong {
        private LongValuesSpliterator() {
        }

        private LongValuesSpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            super(nextIndex, lastIndex, estimateSize);
        }

        @Override
        protected LongValuesSpliterator newSplit(final int nextIndex, final int lastIndex, final long estimateSize) {
            return new LongValuesSpliterator(nextIndex, lastIndex, estimateSize);
        }

        @Override
        public boolean tryAdvance(final LongConsumer action) {
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

    private final class EntrySetSpliterator extends LongBaseSpliterator<EntrySetSpliterator> implements Spliterator<Map.Entry<Long, Long>> {
        private EntrySetSpliterator() {
        }

        private EntrySetSpliterator(final int nextIndex, final int lastIndex, final long estimateSize) {
            super(nextIndex, lastIndex, estimateSize);
        }

        @Override
        protected EntrySetSpliterator newSplit(final int nextIndex, final int lastIndex, final long estimateSize) {
            return new EntrySetSpliterator(nextIndex, lastIndex, estimateSize);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super Map.Entry<Long, Long>> action) {
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
