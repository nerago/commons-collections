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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.NavigableMapOrderedMapIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.set.ReverseSortedSet;

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
public class DualTreeBidiMap<K, V>
        extends AbstractDualBidiMap<K, V, DualTreeBidiMap<K, V>, DualTreeBidiMap<V, K>, NavigableMap<K, V>, NavigableMap<V, K>>
        implements SortedBidiMap<K, V, DualTreeBidiMap<K, V>, DualTreeBidiMap<V, K>> {

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
        this.comparator = null;
        this.valueComparator = null;
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
                              final DualTreeBidiMap<V, K> inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
        this.comparator = normalMap.comparator();
        this.valueComparator = reverseMap.comparator();
    }


    /**
     * Creates an inverted instance of this object.
     */
    @Override
    protected DualTreeBidiMap<V, K> createInverseBidiMap() {
        return new DualTreeBidiMap<>(reverseMap, normalMap, this);
    }

    @Override
    public Comparator<? super K> comparator() {
        return normalMap.comparator();
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return reverseMap.comparator();
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

    /**
     * Obtains an ordered map iterator.
     * <p>
     * This implementation copies the elements to an ArrayList in order to
     * provide the forward/backward behavior.
     *
     * @return a new ordered map iterator
     */
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new BidiOrderedMapIterator<>(this);
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return null; // TODO
    }

    public DualTreeBidiMap<V, K> inverseSortedBidiMap() {
        return inverseBidiMap();
    }

    public DualTreeBidiMap<V, K> inverseOrderedBidiMap() {
        return inverseBidiMap();
    }

    @Override
    public DualTreeBidiMap<K, V> reversed() {
        return new DualTreeBidiMap<>(normalMap.reversed(), reverseMap.reversed(), null);
    }

    @Override
    public DualTreeBidiMap<K, V> subMap(final SortedMapRange<K> range) {
        return new ViewMap<>(range.applyToNavigableMap(normalMap), reverseMap, range);
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return SortedMapRange.full(comparator);
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return SortedMapRange.full(valueComparator);
    }

    @Override
    public SortedRangedSet<K> keySet() {
        return (SortedRangedSet<K>) super.keySet();
    }

    @Override
    public SortedRangedSet<Entry<K, V>> entrySet() {
        return (SortedRangedSet<Entry<K, V>>) super.entrySet();
    }

    @Override
    public SortedRangedSet<V> values() {
        return (SortedRangedSet<V>) super.values();
    }

    /**
     * Internal sorted map view.
     */
    protected static class ViewMap<K, V> extends DualTreeBidiMap<K, V> {
        private static final long serialVersionUID = -3145985885836970321L;
        private final SortedMapRange<K> keyRange;

        /**
         * Constructor.
         *
         * @param subMap      the normal tree map's restricted key range sub-map
         * @param reverseMap  the parent's original reverse tree map
         */
        protected ViewMap(final NavigableMap<K, V> subMap, final NavigableMap<V, K> reverseMap, final SortedMapRange<K> keyRange) {
            // the implementation is not great here...
            // use the normalMap as the filtered map, but reverseMap as the full map
            // this forces containsValue, clear, values.contains, values.remove, put to be overridden
            super(subMap, reverseMap, null);
            this.keyRange = keyRange;
        }

        @Override
        public boolean containsValue(final Object value) {
            // override as default implementation uses reverseMap only
            if (reverseMap.containsKey(value)) {
                final Object key = reverseMap.get(value);
                return normalMap.containsKey(key);
            }
            return false;
        }

        @Override
        public void clear() {
            // override as default implementation would clear everything
            for (final Iterator<K> it = keySet().iterator(); it.hasNext(); ) {
                it.next();
                it.remove();
            }
        }

        @Override
        public V put(final K key, final V value) {
            // override to avoid cases where we'd need to clean up on parent maps
            if (!containsKey(key)) {
                throw new IllegalArgumentException(
                        "Cannot use put on sub map unless the key is already in that map");
            } else if (reverseMap.containsKey(value) &&
                    !Objects.equals(reverseMap.get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use put on sub map when the value being set is already in the map");
            } else {
                return put(key, value);
            }
        }

        @Override
        public SortedMapRange<K> getKeyRange() {
            return keyRange;
        }

        @Override
        public SortedRangedSet<V> values() {
            if (values == null) {
                values = new ValuesView<>(this);
            }
            return (SortedRangedSet<V>) values;
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            return new BidiOrderedMapIterator<>(this);
        }

        private static class ValuesView<K, V> extends Values<K, V> implements SortedRangedSet<V> {
            private static final long serialVersionUID = 734663374206042436L;

            protected ValuesView(final DualTreeBidiMap<K, V> parent) {
                super(parent);
            }

            @SuppressWarnings("unchecked")
            protected DualTreeBidiMap<K, V> parent() {
                return (DualTreeBidiMap<K, V>) parent;
            }

            @Override
            public SortedMapRange<V> getRange() {
                return parent().getValueRange();
            }

            @Override
            public SortedRangedSet<V> reversed() {
                return new ReverseSortedSet<>(this, getRange());
            }

            @Override
            public SortedRangedSet<V> subSet(final SortedMapRange<V> range) {
                final DualTreeBidiMap<K, V> parent = parent();
                return parent.inverseBidiMap().subMap(range).inverseBidiMap().values();
            }

            @Override
            public boolean contains(final Object value) {
                // override as default implementation uses reverseMap only
                if (parent.reverseMap.containsKey(value)) {
                    final Object key = parent.reverseMap.get(value);
                    return parent.normalMap.containsKey(key);
                }
                return false;
            }

            @Override
            public boolean remove(final Object value) {
                // override as we need to check if key is in range
                if (parent.reverseMap.containsKey(value)) {
                    final Object key = parent.reverseMap.get(value);
                    if (parent.normalMap.containsKey(key)) {
                        parent.normalMap.remove(key);
                        parent.reverseMap.remove(value);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void addFirst(V v) {
                SortedRangedSet.super.addFirst(v);
            }

            @Override
            public void addLast(V v) {
                SortedRangedSet.super.addLast(v);
            }

            @Override
            public V getFirst() {
                return SortedRangedSet.super.getFirst();
            }

            @Override
            public V getLast() {
                return SortedRangedSet.super.getLast();
            }

            @Override
            public V removeFirst() {
                return SortedRangedSet.super.removeFirst();
            }

            @Override
            public V removeLast() {
                return SortedRangedSet.super.removeLast();
            }

            @Override
            public boolean equals(Object object) {
                return super.equals(object);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public boolean removeIf(Predicate<? super V> filter) {
                return super.removeIf(filter);
            }

            @Override
            public boolean removeAll(Collection<?> coll) {
                return super.removeAll(coll);
            }

            @Override
            public boolean retainAll(Collection<?> coll) {
                return super.retainAll(coll);
            }

            @Override
            public V first() {
                return null;
            }

            @Override
            public V last() {
                return null;
            }

            @Override
            public Iterator<V> descendingIterator() {
                return null;
            }

            @Override
            public Iterator<V> iterator() {
                return super.iterator();
            }
        }
    }

    /**
     * Inner class MapIterator.
     */
    protected static class BidiOrderedMapIterator<K, V> extends NavigableMapOrderedMapIterator<K, V> {

        /** The parent map */
        private final DualTreeBidiMap<K, V> parent;

        /**
         * Constructor.
         * @param parent  the parent map
         */
        protected BidiOrderedMapIterator(final DualTreeBidiMap<K, V> parent) {
            super(parent.normalMap);
            this.parent = parent;
        }

        @Override
        public void remove() {
            final K key = getKey();
            super.remove();
            parent.remove(key);
            // TODO not sure if this is right, concurrency or unfinished change?
        }

        @Override
        public V setValue(final V value) {
            if (current == null) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() and before remove()");
            }
            final K key = current.getKey();
            if (parent.reverseMap.containsKey(value) &&
                    !Objects.equals(parent.reverseMap.get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use setValue() when the object being set is already in the map");
            }
            final V oldValue = parent.put(key, value);
            // Map.Entry specifies that the behavior is undefined when the backing map
            // has been modified (as we did with the put), so we also set the value
            if (current instanceof Unmodifiable) {
                current = new UnmodifiableMapEntry<>(key, value);
            } else {
                current.setValue(value);
            }
            return oldValue;
        }
    }

    // Serialization
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(normalMap);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        normalMap = new TreeMap<>(comparator);
        reverseMap = new TreeMap<>(valueComparator);
        @SuppressWarnings("unchecked") // will fail at runtime if the stream is incorrect
        final Map<K, V> map = (Map<K, V>) in.readObject();
        putAll(map);
    }
}
