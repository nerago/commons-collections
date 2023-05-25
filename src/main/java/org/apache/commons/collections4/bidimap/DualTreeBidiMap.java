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
package org.apache.commons.collections4.bidimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.map.AbstractNavigableMapDecorator;
import org.apache.commons.collections4.map.NavigableMapIterator;

/**
 * Implementation of {@link BidiMap} that uses two {@link TreeMap} instances.
 * <p>
 * The setValue() method on iterators will succeed only if the new value being set is
 * not already in the bidi map.
 * </p>
 * <p>
 * When considering whether to use this class, the {@link TreeBidiMap} class should
 * also be considered. It implements the interface using a dedicated design, and does
 * not store each object twice, which can save on memory use.
 * </p>
 * <p>
 * NOTE: From Commons Collections 3.1, all subclasses will use {@link TreeMap}
 * and the flawed {@code createMap} method is ignored.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class DualTreeBidiMap<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractDualBidiMap<K, V, NavigableMap<K, V>, NavigableMap<V, K>>
        implements SortedBidiMap<K, V>, NavigableMap<K,V>, Serializable {

    /** Ensure serialization compatibility */
    private static final long serialVersionUID = 721969328361809L;

    /** The key comparator to use */
    private final Comparator<? super K> comparator;

    /** The value comparator to use */
    private final Comparator<? super V> valueComparator;

    /**
     * Creates an empty {@link DualTreeBidiMap}.
     */
    public DualTreeBidiMap() {
        super(new TreeMap<>(), new TreeMap<>());
        this.comparator = null;
        this.valueComparator = null;
    }

    /**
     * Constructs a {@link DualTreeBidiMap} and copies the mappings from
     * specified {@link Map}.
     *
     * @param map  the map whose mappings are to be placed in this map
     */
    public DualTreeBidiMap(final Map<? extends K, ? extends V> map) {
        super(new TreeMap<>(), new TreeMap<>());
        putAll(map);
        this.comparator = normalMap.comparator();
        this.valueComparator = reverseMap.comparator();
    }

    /**
     * Constructs a {@link DualTreeBidiMap} using the specified {@link Comparator}.
     *
     * @param keyComparator  the comparator
     * @param valueComparator  the values comparator to use
     */
    public DualTreeBidiMap(final Comparator<? super K> keyComparator, final Comparator<? super V> valueComparator) {
        super(new TreeMap<>(keyComparator), new TreeMap<>(valueComparator));
        this.comparator = keyComparator;
        this.valueComparator = valueComparator;
    }

    /**
     * Constructs a {@link DualTreeBidiMap} that decorates the specified maps.
     *
     * @param normalMap  the normal direction map
     * @param reverseMap  the reverse direction map
     * @param inverseBidiMap  the inverse BidiMap
     */
    protected DualTreeBidiMap(final NavigableMap<K, V> normalMap, final NavigableMap<V, K> reverseMap,
                              final BidiMap<V, K> inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
        this.comparator = normalMap.comparator();
        this.valueComparator = reverseMap.comparator();
    }

    /**
     * Creates a new instance of this object.
     *
     * @param normalMap  the normal direction map
     * @param reverseMap  the reverse direction map
     * @param inverseMap  the inverse BidiMap
     * @return new bidi map
     */
    @Override
    protected DualTreeBidiMap<V, K> createBidiMap(final NavigableMap<V, K> normalMap, final NavigableMap<K, V> reverseMap,
                                                  final BidiMap<K, V> inverseMap) {
        return new DualTreeBidiMap<>(normalMap, reverseMap, inverseMap);
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return valueComparator;
    }

    @Override
    public K firstKey() {
        return normalMap.firstKey();
    }

    @Override
    public K lastKey() {
        return normalMap.lastKey();
    }

    @Override
    public K nextKey(final K key) {
        return normalMap.higherKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return normalMap.lowerKey(key);
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return normalMap.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return normalMap.lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return normalMap.floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return normalMap.floorKey(key);
    }
    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return normalMap.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return normalMap.ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return normalMap.higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return normalMap.higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return normalMap.firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return normalMap.lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return normalMap.pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return normalMap.pollLastEntry();
    }

    /**
     * Obtains an ordered map iterator.
     *
     * @return a new ordered map iterator
     */
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new BidiOrderedMapIterator<>(this);
    }

    @Override
    public SortedBidiMap<V, K> inverseBidiMap() {
        return (SortedBidiMap<V, K>) super.inverseBidiMap();
    }

    public SortedBidiMap<V, K> inverseSortedBidiMap() {
        return inverseBidiMap();
    }

    public OrderedBidiMap<V, K> inverseOrderedBidiMap() {
        return inverseBidiMap();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        final NavigableMap<K, V> sub = normalMap.descendingMap();
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        // TODO
        return null;
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return null;
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        final NavigableMap<K, V> sub = normalMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> subMap(final K fromKey, final K toKey) {
        final NavigableMap<K, V> sub = normalMap.subMap(fromKey, true, toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> headMap(final K toKey) {
        final NavigableMap<K, V> sub = normalMap.headMap(toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        final NavigableMap<K, V> sub = normalMap.headMap(toKey, inclusive);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> tailMap(final K fromKey) {
        final NavigableMap<K, V> sub = normalMap.tailMap(fromKey, true);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        final NavigableMap<K, V> sub = normalMap.tailMap(fromKey, inclusive);
        return new ViewMap<>(this, sub);
    }

    /**
     * Internal sorted map view.
     */
    protected static class ViewMap<K extends Comparable<K>, V extends Comparable<V>> extends AbstractNavigableMapDecorator<K, V> {
        /**
         * Constructor.
         * @param bidi  the parent bidi map
         * @param sm  the subMap sorted map
         */
        protected ViewMap(final DualTreeBidiMap<K, V> bidi, final NavigableMap<K, V> sm) {
            // the implementation is not great here...
            // use the normalMap as the filtered map, but reverseMap as the full map
            // this forces containsValue and clear to be overridden
            super(new DualTreeBidiMap<>(sm, bidi.reverseMap, bidi.inverseBidiMap));
        }

        @Override
        protected DualTreeBidiMap<K, V> decorated() {
            return (DualTreeBidiMap<K, V>) super.decorated();
        }

        @Override
        protected ViewMap<K, V> createWrappedMap(NavigableMap<K, V> other) {
            return new ViewMap<>(decorated(), other);
        }

        @Override
        public boolean containsValue(final Object value) {
            // override as default implementation uses reverseMap
            return decorated().normalMap.containsValue(value);
        }

        @Override
        public void clear() {
            // override as default implementation uses reverseMap
            for (final Iterator<K> it = keySet().iterator(); it.hasNext();) {
                it.next();
                it.remove();
            }
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            return new BidiOrderedMapIterator<>(decorated());
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return super.navigableKeySet();
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return super.descendingKeySet();
        }
    }

    protected static class BidiOrderedMapIterator<K, V> extends NavigableMapIterator<K,V> {

        /** The parent map */
        private final AbstractDualBidiMap<K, V, ?, ?> parent;

        /**
         * Constructor.
         * @param parent  the parent map
         */
        protected BidiOrderedMapIterator(final AbstractDualBidiMap<K, V, NavigableMap<K,V>, NavigableMap<V,K>> parent) {
            super(parent.normalMap);
            this.parent = parent;
            reset();
        }

        @Override
        public void remove() {
            Map.Entry<K, V> current = current();
            parent.remove(current.getKey());
            lastReturnedNode = null;
        }

        @Override
        public V setValue(final V value) {
            if (lastReturnedNode == null) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() and before remove()");
            }
            if (parent.reverseMap.containsKey(value) &&
                    !Objects.equals(parent.reverseMap.get(value), lastReturnedNode.getKey())) {
                throw new IllegalArgumentException(
                        "Cannot use setValue() when the object being set is already in the map");
            }
            final V oldValue = parent.put(lastReturnedNode.getKey(), value);
            // Map.Entry specifies that the behavior is undefined when the backing map
            // has been modified (as we did with the put), so we also set the value
            lastReturnedNode.setValue(value);
            return oldValue;
        }

        @Override
        public String toString() {
            if (lastReturnedNode != null) {
                return "MapIterator[" + getKey() + "=" + getValue() + "]";
            }
            return "MapIterator[]";
        }
    }

    // Serialization
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(normalMap);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        normalMap = new TreeMap<>(comparator);
        reverseMap = new TreeMap<>(valueComparator);
        @SuppressWarnings("unchecked") // will fail at runtime if the stream is incorrect
        final Map<K, V> map = (Map<K, V>) in.readObject();
        putAll(map);
    }

}
