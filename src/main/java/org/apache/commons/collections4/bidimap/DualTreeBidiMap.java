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
import java.util.function.Predicate;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.map.AbstractSortedMapDecorator;

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
public class DualTreeBidiMap<K, V> extends AbstractDualBidiMap<K, V>
        implements SortedBidiMap<K, V>, Serializable {

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
                              final BidiMap<V, K> inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
        this.comparator = normalMap.comparator();
        this.valueComparator = reverseMap.comparator();
    }

    /**
     * Creates a new instance of this object.
     *
     * @return new bidi map
     */
    @Override
    protected BidiMap<V, K> createInverse() {
        return new DualTreeBidiMap<V, K>(reverseMap(), normalMap(), this);
    }

    protected TreeMap<K, V> normalMap() {
        return (TreeMap<K, V>) super.normalMap();
    }

    protected TreeMap<V, K> reverseMap() {
        return (TreeMap<V, K>) super.reverseMap();
    }

    @Override
    public Comparator<? super K> comparator() {
        return normalMap().comparator();
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return reverseMap().comparator();
    }

    @Override
    public K firstKey() {
        return normalMap().firstKey();
    }

    @Override
    public K lastKey() {
        return normalMap().lastKey();
    }

    @Override
    public K nextKey(final K key) {
        return normalMap().higherKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return normalMap().lowerKey(key);
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

    public SortedBidiMap<V, K> inverseSortedBidiMap() {
        return inverseBidiMap();
    }

    public OrderedBidiMap<V, K> inverseOrderedBidiMap() {
        return inverseBidiMap();
    }


    @Override
    public SortedMap<K, V> headMap(final K toKey) {
        final NavigableMap<K, V> sub = normalMap().headMap(toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public SortedMap<K, V> tailMap(final K fromKey) {
        final NavigableMap<K, V> sub = normalMap().tailMap(fromKey, true);
        return new ViewMap<>(this, sub);
    }

    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
        final NavigableMap<K, V> sub = normalMap().subMap(fromKey, true, toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public SortedBidiMap<V, K> inverseBidiMap() {
        return (SortedBidiMap<V, K>) super.inverseBidiMap();
    }

    /**
     * Internal sorted map view.
     */
    protected static class ViewMap<K, V> extends AbstractSortedMapDecorator<K, V> {
        transient Set<V> values;

        /**
         * Constructor.
         *
         * @param bidi the parent bidi map
         * @param sm   the subMap sorted map
         */
        protected ViewMap(final DualTreeBidiMap<K, V> bidi, final NavigableMap<K, V> sm) {
            // the implementation is not great here...
            // use the normalMap as the filtered map, but reverseMap as the full map
            // this forces containsValue, clear, values.contains, values.remove, put to be overridden
            super(new DualTreeBidiMap<>(sm, bidi.reverseMap(), null));
        }

        @Override
        public boolean containsValue(final Object value) {
            // override as default implementation uses reverseMap only
            if (decorated().reverseMap().containsKey(value)) {
                Object key = decorated().reverseMap().get(value);
                return  decorated().normalMap().containsKey(key);
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
        public V put(K key, V value) {
            // override to avoid cases where we'd need to clean up on parent maps
            if (!containsKey(key)) {
                throw new IllegalArgumentException(
                        "Cannot use put on sub map unless the key is already in that map");
            } else if (decorated().reverseMap().containsKey(value) &&
                    !Objects.equals(decorated().reverseMap().get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use put on sub map when the value being set is already in the map");
            } else {
                return decorated().put(key, value);
            }
        }

        @Override
        public SortedMap<K, V> headMap(final K toKey) {
            return new ViewMap<>(decorated(), decorated().normalMap().headMap(toKey, false));
        }

        @Override
        public SortedMap<K, V> tailMap(final K fromKey) {
            return new ViewMap<>(decorated(), decorated().normalMap().tailMap(fromKey, true));
        }

        @Override
        public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
            return new ViewMap<>(decorated(), decorated().normalMap().subMap(fromKey, true, toKey, false));
        }

        @Override
        protected DualTreeBidiMap<K, V> decorated() {
            return (DualTreeBidiMap<K, V>) super.decorated();
        }

        @Override
        public K previousKey(final K key) {
            return decorated().previousKey(key);
        }

        @Override
        public K nextKey(final K key) {
            return decorated().nextKey(key);
        }

        @Override
        public Collection<V> values() {
            if (values == null) {
                values = new ValuesView<>(decorated());
            }
            return values;
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            return new BidiOrderedMapIterator<>(decorated());
        }

        private static class ValuesView<V> extends Values<V> {
            public ValuesView(final AbstractDualBidiMap<?, V> parent) {
                super(parent);
            }

            @Override
            public boolean contains(Object value) {
                // override as default implementation uses reverseMap only
                if (parent.reverseMap().containsKey(value)) {
                    Object key = parent.reverseMap().get(value);
                    return parent.normalMap().containsKey(key);
                }
                return false;
            }

            @Override
            public boolean remove(Object value) {
                // override as we need to check if key is in range
                if (parent.reverseMap().containsKey(value)) {
                    Object key = parent.reverseMap().get(value);
                    if (parent.normalMap().containsKey(key)) {
                        parent.normalMap().remove(key);
                        parent.reverseMap().remove(value);
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Inner class MapIterator.
     */
    protected static class BidiOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {

        /** The parent map */
        private final AbstractDualBidiMap<K, V> parent;

        /** The iterator being decorated */
        private ListIterator<Map.Entry<K, V>> iterator;

        /** The last returned entry */
        private Map.Entry<K, V> last;

        /**
         * Constructor.
         * @param parent  the parent map
         */
        protected BidiOrderedMapIterator(final AbstractDualBidiMap<K, V> parent) {
            this.parent = parent;
            iterator = new ArrayList<>(parent.entrySet()).listIterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            last = iterator.next();
            return last.getKey();
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public K previous() {
            last = iterator.previous();
            return last.getKey();
        }

        @Override
        public void remove() {
            iterator.remove();
            parent.remove(last.getKey());
            last = null;
        }

        @Override
        public K getKey() {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator getKey() can only be called after next() and before remove()");
            }
            return last.getKey();
        }

        @Override
        public V getValue() {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator getValue() can only be called after next() and before remove()");
            }
            return last.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() and before remove()");
            }
            final K key = last.getKey();
            if (parent.reverseMap().containsKey(value) &&
                    !Objects.equals(parent.reverseMap().get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use setValue() when the object being set is already in the map");
            }
            final V oldValue = parent.put(key, value);
            // Map.Entry specifies that the behavior is undefined when the backing map
            // has been modified (as we did with the put), so we also set the value
            last.setValue(value);
            return oldValue;
        }

        @Override
        public void reset() {
            iterator = new ArrayList<>(parent.entrySet()).listIterator();
            last = null;
        }

        @Override
        public String toString() {
            if (last != null) {
                return "MapIterator[" + getKey() + "=" + getValue() + "]";
            }
            return "MapIterator[]";
        }
    }

    // Serialization
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(normalMap());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setInternalMaps(new TreeMap<>(comparator), new TreeMap<>(valueComparator));
        @SuppressWarnings("unchecked") // will fail at runtime if the stream is incorrect
        final Map<K, V> map = (Map<K, V>) in.readObject();
        putAll(map);
    }

}
