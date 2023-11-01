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
package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.AbstractUntypedIteratorDecorator;
import org.apache.commons.collections4.iterators.TransformListIterator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

/**
 * Decorates a {@code Map} to ensure that the order of addition is retained
 * using a {@code List} to maintain order.
 * <p>
 * The order will be used via the iterators and toArray methods on the views.
 * The order is also returned by the {@code MapIterator}.
 * The {@code orderedMapIterator()} method accesses an iterator that can
 * iterate both forwards and backwards through the map.
 * In addition, non-interface methods are provided to access the map by index.
 * </p>
 * <p>
 * If an object is added to the Map for a second time, it will remain in the
 * original position in the iteration.
 * </p>
 * <p>
 * <strong>Note that ListOrderedMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 * </p>
 * <p>
 * <strong>Note that ListOrderedMap doesn't work with
 * {@link java.util.IdentityHashMap IdentityHashMap}, {@link CaseInsensitiveMap},
 * or similar maps that violate the general contract of {@link java.util.Map}.</strong>
 * The {@code ListOrderedMap} (or, more precisely, the underlying {@code List})
 * is relying on {@link Object#equals(Object) equals()}. This is fine, as long as the
 * decorated {@code Map} is also based on {@link Object#equals(Object) equals()},
 * and {@link Object#hashCode() hashCode()}, which
 * {@link java.util.IdentityHashMap IdentityHashMap}, and
 * {@link CaseInsensitiveMap} don't: The former uses {@code ==}, and
 * the latter uses {@link Object#equals(Object) equals()} on a lower-cased
 * key.
 * </p>
 * <p>
 * This class is {@link Serializable} starting with Commons Collections 3.1.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class ListOrderedMap<K, V>
        extends AbstractMapDecorator<K, V, OrderedMap<K, V, ?>, Set<K>, Set<Map.Entry<K, V>>, Collection<V>>
        implements OrderedMap<K, V, OrderedMap<K, V, ?>> {

    /** Serialization version */
    private static final long serialVersionUID = 2728177751851003750L;

    /** Internal list to hold the sequence of objects */
    private List<K> insertOrder = new ArrayList<>();

    /**
     * Factory method to create an ordered map.
     * <p>
     * An {@code ArrayList} is used to retain order.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new list ordered map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> ListOrderedMap<K, V> listOrderedMap(final Map<K, V> map) {
        return new ListOrderedMap<>(map);
    }

    /**
     * Constructs a new empty {@code ListOrderedMap} that decorates
     * a {@code HashMap}.
     *
     * @since 3.1
     */
    public ListOrderedMap() {
        this(new HashMap<>());
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    protected ListOrderedMap(final Map<K, V> map) {
        super(map);
        insertOrder.addAll(decorated().keySet());
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     * @since 3.1
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(insertOrder);
        super.writeExternal(out);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     * @since 3.1
     */
    @SuppressWarnings("unchecked") // (1) should only fail if input stream is incorrect
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        insertOrder = (List<K>) in.readObject();
        super.readExternal(in);
    }

    // Implement OrderedMap
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new ListOrderedMapIterator<>(this);
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return new DescendingListOrderedMapIterator<>(this);
    }

    /**
     * Gets the first key in this map by insert order.
     *
     * @return the first key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K firstKey() {
        if (isEmpty()) {
            throw new NoSuchElementException("Map is empty");
        }
        return insertOrder.get(0);
    }

    /**
     * Gets the last key in this map by insert order.
     *
     * @return the last key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K lastKey() {
        if (isEmpty()) {
            throw new NoSuchElementException("Map is empty");
        }
        return insertOrder.get(size() - 1);
    }

    /**
     * Gets the next key to the one specified using insert order.
     * This method performs a list search to find the key and is O(n).
     *
     * @param key  the key to find previous for
     * @return the next key, null if no match or at start
     */
    @Override
    public K nextKey(final Object key) {
        final int index = insertOrder.indexOf(key);
        if (index >= 0 && index < size() - 1) {
            return insertOrder.get(index + 1);
        }
        return null;
    }

    /**
     * Gets the previous key to the one specified using insert order.
     * This method performs a list search to find the key and is O(n).
     *
     * @param key  the key to find previous for
     * @return the previous key, null if no match or at start
     */
    @Override
    public K previousKey(final Object key) {
        final int index = insertOrder.indexOf(key);
        if (index > 0) {
            return insertOrder.get(index - 1);
        }
        return null;
    }

    @Override
    public V put(final K key, final V value) {
        if (decorated().containsKey(key)) {
            // re-adding doesn't change order
            return decorated().put(key, value);
        }
        // first add, so add to both map and list
        final V result = decorated().put(key, value);
        insertOrder.add(key);
        return result;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Puts the values contained in a supplied Map into the Map starting at
     * the specified index.
     *
     * @param index the index in the Map to start at.
     * @param map the Map containing the entries to be added.
     * @throws IndexOutOfBoundsException if the index is out of range [0, size]
     */
    public void putAll(int index, final Map<? extends K, ? extends V> map) {
        if (index < 0 || index > insertOrder.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + insertOrder.size());
        }
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final K key = entry.getKey();
            final boolean contains = containsKey(key);
            // The return value of put is null if the key did not exist OR the value was null
            // so it cannot be used to determine whether the key was added
            put(index, entry.getKey(), entry.getValue());
            if (!contains) {
                // if no key was replaced, increment the index
                index++;
            } else {
                // otherwise put the next item after the currently inserted key
                index = indexOf(entry.getKey()) + 1;
            }
        }
    }

    @Override
    public V remove(final Object key) {
        V result = null;
        if (decorated().containsKey(key)) {
            result = decorated().remove(key);
            insertOrder.remove(key);
        }
        return result;
    }

    @Override
    public void clear() {
        decorated().clear();
        insertOrder.clear();
    }

    /**
     * Gets a view over the keys in the map.
     * <p>
     * The Collection will be ordered by object insertion into the map.
     *
     * @return the fully modifiable collection view over the keys
     * @see #keyList()
     */
    @Override
    public SequencedSet<K> keySet() {
        return new KeySetView<>(this);
    }

    @Override
    public SequencedSet<K> sequencedKeySet() {
        return keySet();
    }

    /**
     * Gets a view over the keys in the map as a List.
     * <p>
     * The List will be ordered by object insertion into the map.
     * The List is unmodifiable.
     *
     * @see #keySet()
     * @return the unmodifiable list view over the keys
     * @since 3.2
     */
    public List<K> keyList() {
        return UnmodifiableList.unmodifiableList(insertOrder);
    }

    /**
     * Gets a view over the values in the map.
     * <p>
     * The Collection will be ordered by object insertion into the map.
     * <p>
     * From Commons Collections 3.2, this Collection can be cast
     * to a list, see {@link #valueList()}
     *
     * @return the fully modifiable collection view over the values
     * @see #valueList()
     */
    @Override
    public SequencedCollection<V> values() {
        return new ValuesView<>(this);
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
        return values();
    }

    /**
     * Gets a view over the values in the map as a List.
     * <p>
     * The List will be ordered by object insertion into the map.
     * The List supports remove and set, but does not support add.
     *
     * @see #values()
     * @return the partially modifiable list view over the values
     * @since 3.2
     */
    public List<V> valueList() {
        return new ValuesView<>(this);
    }

    /**
     * Gets a view over the entries in the map.
     * <p>
     * The Set will be ordered by object insertion into the map.
     *
     * @return the fully modifiable set view over the entries
     */
    @Override
    public SequencedSet<Entry<K, V>> entrySet() {
        return new EntrySetView<>(this);
    }

    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return entrySet();
    }

    /**
     * Returns the Map as a string.
     *
     * @return the Map as a String
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        boolean first = true;
        for (final Map.Entry<K, V> entry : entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(key == this ? "(this Map)" : key);
            buf.append('=');
            buf.append(value == this ? "(this Map)" : value);
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * Gets the key at the specified index.
     *
     * @param index  the index to retrieve
     * @return the key at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public K get(final int index) {
        return insertOrder.get(index);
    }

    /**
     * Gets the value at the specified index.
     *
     * @param index  the index to retrieve
     * @return the key at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public V getValue(final int index) {
        return get(insertOrder.get(index));
    }

    /**
     * Gets the index of the specified key.
     *
     * @param key  the key to find the index of
     * @return the index, or -1 if not found
     */
    public int indexOf(final Object key) {
        return insertOrder.indexOf(key);
    }

    /**
     * Sets the value at the specified index.
     *
     * @param index  the index of the value to set
     * @param value  the new value to set
     * @return the previous value at that index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @since 3.2
     */
    public V setValue(final int index, final V value) {
        final K key = insertOrder.get(index);
        return put(key, value);
    }

    /**
     * Puts a key-value mapping into the map at the specified index.
     * <p>
     * If the map already contains the key, then the original mapping
     * is removed and the new mapping added at the specified index.
     * The remove may change the effect of the index. The index is
     * always calculated relative to the original state of the map.
     * <p>
     * Thus, the steps are: (1) remove the existing key-value mapping,
     * then (2) insert the new key-value mapping at the position it
     * would have been inserted had the remove not occurred.
     *
     * @param index  the index at which the mapping should be inserted
     * @param key  the key
     * @param value  the value
     * @return the value previously mapped to the key
     * @throws IndexOutOfBoundsException if the index is out of range [0, size]
     * @since 3.2
     */
    public V put(int index, final K key, final V value) {
        if (index < 0 || index > insertOrder.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + insertOrder.size());
        }

        final Map<K, V> m = decorated();
        if (m.containsKey(key)) {
            final V result = m.remove(key);
            final int pos = insertOrder.indexOf(key);
            insertOrder.remove(pos);
            if (pos < index) {
                index--;
            }
            insertOrder.add(index, key);
            m.put(key, value);
            return result;
        }
        insertOrder.add(index, key);
        m.put(key, value);
        return null;
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index of the object to remove
     * @return the removed value, or {@code null} if none existed
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public V remove(final int index) {
        return remove(get(index));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override to iterate in list order
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        insertOrder.forEach(key -> action.accept(key, map.get(key)));
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        if (map.remove(key, value)) {
            insertOrder.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        final V oldValue = map.get(key);
        if (oldValue != null) {
            return oldValue;
        } else if (!map.containsKey(key)) {
            insertOrder.add(key);
        }
        map.put(key, value);
        return null;
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        return map.computeIfAbsent(key,
            k -> {
                final boolean wasContained = map.containsKey(key);
                final V newValue = mappingFunction.apply(key);
                if (!wasContained && newValue != null)
                    insertOrder.add(key);
                else if (wasContained && newValue == null)
                    insertOrder.remove(key);
                return newValue;
            }
        );
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key,
            (k, oldValue) -> {
                final V newValue = remappingFunction.apply(k, oldValue);
                if (newValue == null)
                    insertOrder.remove(key);
                return newValue;
            }
        );
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return map.compute(key,
            (k, oldValue) -> {
                final V newValue = remappingFunction.apply(k, oldValue);
                final boolean wasContained = oldValue != null || map.containsKey(k);
                if (!wasContained && newValue != null)
                    insertOrder.add(key);
                else if (wasContained && newValue == null)
                    insertOrder.remove(key);
                return newValue;
            }
        );
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        final V oldValue = map.get(key);
        if (oldValue != null) {
            final V newValue = remappingFunction.apply(oldValue, value);
            if (newValue != null) {
                map.put(key, newValue);
            } else {
                map.remove(key);
                insertOrder.remove(key);
            }
            return newValue;
        } else if (map.containsKey(key)) {
            map.put(key, value);
            return value;
        } else {
            map.put(key, value);
            insertOrder.add(key);
            return value;
        }
    }

    /**
     * Gets an unmodifiable List view of the keys which changes as the map changes.
     * <p>
     * The returned list is unmodifiable because changes to the values of
     * the list (using {@link java.util.ListIterator#set(Object)}) will
     * effectively remove the value from the list and reinsert that value at
     * the end of the list, which is an unexpected side effect of changing the
     * value of a list.  This occurs because changing the key, changes when the
     * mapping is added to the map and thus where it appears in the list.
     * <p>
     * An alternative to this method is to use the better named
     * {@link #keyList()} or {@link #keySet()}.
     *
     * @see #keyList()
     * @see #keySet()
     * @return The ordered list of keys.
     */
    public List<K> asList() {
        return keyList();
    }

    static class ValuesView<V> extends AbstractList<V> {
        private final ListOrderedMap<Object, V> parent;

        @SuppressWarnings("unchecked")
        ValuesView(final ListOrderedMap<?, V> parent) {
            this.parent = (ListOrderedMap<Object, V>) parent;
        }

        @Override
        public int size() {
            return this.parent.size();
        }

        @Override
        public boolean contains(final Object value) {
            return this.parent.containsValue(value);
        }

        @Override
        public void clear() {
            this.parent.clear();
        }

        @Override
        public Iterator<V> iterator() {
            return new ListOrderedValueIterator<>(parent);
        }

        @Override
        public ListIterator<V> listIterator() {
            return new ValueListIterator<>(parent, parent.insertOrder.listIterator());
        }

        @Override
        public ListIterator<V> listIterator(int index) {
            return new ValueListIterator<>(parent, parent.insertOrder.listIterator(index));
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformSpliterator<>(parent.insertOrder.spliterator(), key -> parent.decorated().get(key));
        }

        @Override
        public V get(final int index) {
            return this.parent.getValue(index);
        }

        @Override
        public V set(final int index, final V value) {
            return this.parent.setValue(index, value);
        }

        @Override
        public V remove(final int index) {
            return this.parent.remove(index);
        }
    }

    static class KeySetView<K> extends AbstractSet<K> implements SequencedSet<K> {
        protected final ListOrderedMap<K, Object> parent;

        @SuppressWarnings("unchecked")
        KeySetView(final ListOrderedMap<K, ?> parent) {
            this.parent = (ListOrderedMap<K, Object>) parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean contains(final Object key) {
            return parent.containsKey(key);
        }

        @Override
        public boolean remove(final Object key) {
            if (parent.map.containsKey(key)) {
                parent.map.remove(key);
                parent.insertOrder.remove(key);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public Iterator<K> iterator() {
            return new ListOrderedKeyIterator<>(parent);
        }

        @Override
        public Spliterator<K> spliterator() {
            return parent.insertOrder.spliterator();
        }

        @Override
        public SequencedSet<K> reversed() {
            return new KeySetViewReverse<>(parent);
        }

        @Override
        public K getFirst() {
            return parent.firstKey();
        }

        @Override
        public K getLast() {
            return parent.lastKey();
        }

        @Override
        public K removeFirst() {
            final K key = parent.insertOrder.removeFirst();
            parent.map.remove(key);
            return key;
        }

        @Override
        public K removeLast() {
            final K key = parent.insertOrder.removeLast();
            parent.map.remove(key);
            return key;
        }
    }

    static class KeySetViewReverse<K> extends KeySetView<K> {
        KeySetViewReverse(final ListOrderedMap<K, ?> parent) {
            super(parent);
        }

        @Override
        public Iterator<K> iterator() {
            return parent.insertOrder.reversed().iterator();
        }

        @Override
        public Spliterator<K> spliterator() {
            return parent.insertOrder.reversed().spliterator();
        }

        @Override
        public SequencedSet<K> reversed() {
            return new KeySetView<>(parent);
        }

        @Override
        public K getFirst() {
            return super.getLast();
        }

        @Override
        public K getLast() {
            return super.getFirst();
        }

        @Override
        public K removeFirst() {
            return super.removeLast();
        }

        @Override
        public K removeLast() {
            return super.removeFirst();
        }
    }

    static class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> implements SequencedSet<Map.Entry<K, V>> {
        private final ListOrderedMap<K, V> parent;
        private Set<Map.Entry<K, V>> entrySet;

        EntrySetView(final ListOrderedMap<K, V> parent) {
            this.parent = parent;
        }

        private Set<Map.Entry<K, V>> getEntrySet() {
            if (entrySet == null) {
                entrySet = parent.decorated().entrySet();
            }
            return entrySet;
        }

        @Override
        public int size() {
            return this.parent.size();
        }
        @Override
        public boolean isEmpty() {
            return this.parent.isEmpty();
        }

        @Override
        public boolean contains(final Object obj) {
            return getEntrySet().contains(obj);
        }

        @Override
        public boolean containsAll(final Collection<?> coll) {
            return getEntrySet().containsAll(coll);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            if (getEntrySet().contains(obj)) {
                final Object key = ((Map.Entry<K, V>) obj).getKey();
                parent.remove(key);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            this.parent.clear();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            return getEntrySet().equals(obj);
        }

        @Override
        public int hashCode() {
            return getEntrySet().hashCode();
        }

        @Override
        public String toString() {
            return getEntrySet().toString();
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new ListOrderedEntryIterator<>(parent);
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return new TransformSpliterator<>(parent.insertOrder.spliterator(),
                    key -> new TiedMapEntry<>(parent.decorated(), key));
        }

        @Override
        public SequencedSet<Entry<K, V>> reversed() {
            return new EntrySetViewReversed<>(parent);
        }

        @Override
        public Entry<K, V> getFirst() {
            return new TiedMapEntry<>(parent.decorated(), parent.firstKey());
        }

        @Override
        public Entry<K, V> getLast() {
            return new TiedMapEntry<>(parent.decorated(), parent.lastKey());
        }

        @Override
        public Entry<K, V> removeFirst() {
            final K key = parent.insertOrder.removeFirst();
            final V value = parent.map.remove(key);
            return new UnmodifiableMapEntry<>(key, value);
        }

        @Override
        public Entry<K, V> removeLast() {
            final K key = parent.insertOrder.removeLast();
            final V value = parent.map.remove(key);
            return new UnmodifiableMapEntry<>(key, value);
        }
    }

    static class EntrySetViewReversed<K, V> extends EntrySetView<K, V> {
        EntrySetViewReversed(final ListOrderedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return super.iterator();
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return super.spliterator();
        }

        @Override
        public SequencedSet<Entry<K, V>> reversed() {
            return super.reversed();
        }

        @Override
        public Entry<K, V> getFirst() {
            return super.getLast();
        }

        @Override
        public Entry<K, V> getLast() {
            return super.getFirst();
        }

        @Override
        public Entry<K, V> removeFirst() {
            return super.removeLast();
        }

        @Override
        public Entry<K, V> removeLast() {
            return super.removeFirst();
        }
    }

    static abstract class ListOrderedIterator<K, V, E> extends AbstractUntypedIteratorDecorator<K, E> {
        protected final ListOrderedMap<K, V> parent;
        protected K lastKey;

        ListOrderedIterator(final ListOrderedMap<K, V> parent) {
            super(parent.insertOrder.iterator());
            this.parent = parent;
        }

        @Override
        public void remove() {
            super.remove();
            parent.decorated().remove(lastKey);
        }
    }

    static class ListOrderedEntryIterator<K, V> extends ListOrderedIterator<K, V, Map.Entry<K, V>> {
        ListOrderedEntryIterator(final ListOrderedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public Map.Entry<K, V> next() {
            lastKey = getIterator().next();
            return new TiedMapEntry<>(parent.decorated(), lastKey);
        }
    }

    static class ListOrderedKeyIterator<K, V> extends ListOrderedIterator<K, V, K> {
        ListOrderedKeyIterator(final ListOrderedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public K next() {
            lastKey = getIterator().next();
            return lastKey;
        }
    }

    static class ListOrderedValueIterator<K, V> extends ListOrderedIterator<K, V, V> {
        ListOrderedValueIterator(final ListOrderedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public V next() {
            lastKey = getIterator().next();
            return parent.get(lastKey);
        }
    }

    static class ListOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
        protected final ListOrderedMap<K, V> parent;
        protected ListIterator<K> iterator;
        protected K last;
        protected boolean readable;

        ListOrderedMapIterator(final ListOrderedMap<K, V> parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            last = iterator.next();
            readable = true;
            return last;
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public K previous() {
            last = iterator.previous();
            readable = true;
            return last;
        }

        @Override
        public void remove() {
            if (!readable) {
                throw new IllegalStateException(AbstractHashedMap.REMOVE_INVALID);
            }
            iterator.remove();
            parent.map.remove(last);
            readable = false;
        }

        @Override
        public K getKey() {
            if (!readable) {
                throw new IllegalStateException(AbstractHashedMap.GETKEY_INVALID);
            }
            return last;
        }

        @Override
        public V getValue() {
            if (!readable) {
                throw new IllegalStateException(AbstractHashedMap.GETVALUE_INVALID);
            }
            return parent.get(last);
        }

        @Override
        public V setValue(final V value) {
            if (!readable) {
                throw new IllegalStateException(AbstractHashedMap.SETVALUE_INVALID);
            }
            return parent.map.put(last, value);
        }

        @Override
        public void reset() {
            iterator = parent.insertOrder.listIterator();
            last = null;
            readable = false;
        }

        @Override
        public String toString() {
            if (readable) {
                return "Iterator[" + getKey() + "=" + getValue() + "]";
            }
            return "Iterator[]";
        }
    }

    static class DescendingListOrderedMapIterator<K, V> extends ListOrderedMapIterator<K, V> {
        DescendingListOrderedMapIterator(final ListOrderedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public boolean hasNext() {
            return super.hasPrevious();
        }

        @Override
        public K next() {
            return super.previous();
        }

        @Override
        public boolean hasPrevious() {
            return super.hasNext();
        }

        @Override
        public K previous() {
            return super.next();
        }

        @Override
        public void reset() {
            iterator = parent.insertOrder.listIterator(parent.insertOrder.size());
            last = null;
            readable = false;
        }
    }

    private static class ValueListIterator<K, V> extends TransformListIterator<K, V> {
        private final ListOrderedMap<K, V> parent;
        private boolean hasLast;
        private K lastKey;

        public ValueListIterator(final ListOrderedMap<K, V> parent, final ListIterator<K> listIterator) {
            super(listIterator);
            this.parent = parent;
        }

        @Override
        protected V transform(K source) {
            return parent.map.get(source);
        }

        @Override
        public V next() {
            lastKey = getIterator().next();
            hasLast = true;
            return transform(lastKey);
        }

        @Override
        public V previous() {
            lastKey = getIterator().previous();
            hasLast = true;
            return transform(lastKey);
        }

        @Override
        public void remove() {
            if (!hasLast)
                throw new IllegalStateException();
            super.remove();
            parent.remove(lastKey);
            hasLast = false;
        }

        @Override
        public void set(final V newValue) {
            if (!hasLast)
                throw new IllegalStateException();
            parent.map.put(lastKey, newValue);
        }

        @Override
        public void add(V o) {
            throw new UnsupportedOperationException();
        }
    }
}
