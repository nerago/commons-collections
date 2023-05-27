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

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.AbstractNavigableMapDecorator;
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
        this.comparator =  normalMap.comparator();
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
    protected DualTreeBidiMap<V, K> createBidiMap(final Map<V, K> normalMap, final Map<K, V> reverseMap,
                                                  final BidiMap<K, V> inverseMap) {
        return new DualTreeBidiMap<>((NavigableMap<V,K>) normalMap, (NavigableMap<K,V>) reverseMap, inverseMap);
    }

    @Override
    protected NavigableMap<K, V> normalMap() {
        return (NavigableMap<K, V>) super.normalMap();
    }

    @Override
    protected NavigableMap<V, K> reverseMap() {
        return (NavigableMap<V, K>) super.reverseMap();
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

    @Override
    public NavigableMap<K, V> headMap(final K toKey) {
        final NavigableMap<K, V> sub = normalMap().headMap(toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> tailMap(final K fromKey) {
        final NavigableMap<K, V> sub = normalMap().tailMap(fromKey, true);
        return new ViewMap<>(this, sub);
    }

    @Override
    public NavigableMap<K, V> subMap(final K fromKey, final K toKey) {
        final NavigableMap<K, V> sub = normalMap().subMap(fromKey, true, toKey, false);
        return new ViewMap<>(this, sub);
    }

    @Override
    public DualTreeBidiMap<V, K> inverseBidiMap() {
        return (DualTreeBidiMap<V, K>) super.inverseBidiMap();
    }

    /**
     * Internal sorted map view.
     */
    protected static class ViewMap<K, V> extends AbstractDualBidiMap<K, V> implements SortedBidiMap<K, V>, NavigableMap<K,V> {
        //private final DualTreeBidiMap<K, V> parent;

        /**
         * Constructor.
         * @param parent  the parent bidi map
         * @param sub  the subMap sorted map
         */
        protected ViewMap(final DualTreeBidiMap<K, V> parent, final NavigableMap<K, V> sub) {
            // the implementation is not great here...
            // use the normalMap as the filtered map, but reverseMap as the full map
            // this forces containsValue and clear to be overridden
            super(sub, parent.reverseMap());
        }

        protected ViewMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseMap) {
            super(normalMap, reverseMap, inverseMap);
        }

        @Override
        protected ViewMap<V, K> createBidiMap(Map<V, K> normalMap, Map<K, V> reverseMap, BidiMap<K, V> inverseMap) {
            return new ViewMap<>(normalMap, reverseMap, inverseMap);
        }

        protected ViewMap<K, V> createSubMap(Map<K, V> subMap) {
            return new ViewMap<>(subMap, reverseMap(), null);
        }
        
        @Override
        protected NavigableMap<K, V> normalMap() {
            return (NavigableMap<K, V>) super.normalMap();
        }

        @Override
        protected NavigableMap<V, K> reverseMap() {
            return (NavigableMap<V, K>) super.reverseMap();
        }

        @Override
        public SortedBidiMap<V, K> inverseBidiMap() {
            return super.inverseBidiMap();
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
        public V put(K key, V value) {
            throw new UnsupportedOperationException("not tracked if in sub map range");
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> mapToCopy) {
            throw new UnsupportedOperationException("not tracked if in sub map range");
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
        public boolean containsValue(final Object value) {
            // override as default implementation uses reverseMap as a simple lookup
            // we need to also check it's in our filtered keys
            if (reverseMap().containsKey(value)) {
                K key = reverseMap().get(value);
                return normalMap().containsKey(key);
            }
            return false;
        }

        @Override
        public K removeValue(Object value) {
            if (reverseMap().containsKey(value)) {
                K key = reverseMap().get(value);
                if (normalMap().containsKey(key)) {
                    normalMap().remove(key);
                    reverseMap().remove(value);
                    return key;
                }
            }
            return null;
        }

        @Override
        protected boolean removeValueViaCollection(V value) {
            if (reverseMap().containsKey(value)) {
                Object key = reverseMap().get(value);
                if (normalMap().containsKey(key)) {
                    normalMap().remove(key);
                    reverseMap().remove(value);
                    return true;
                }
            }
            return false;
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
        public K nextKey(K key) {
            return normalMap().higherKey(key);
        }

        @Override
        public K previousKey(K key) {
            return normalMap().lowerKey(key);
        }

        @Override
        public Entry<K, V> lowerEntry(K key) {
            return normalMap().lowerEntry(key);
        }

        @Override
        public K lowerKey(K key) {
            return normalMap().lowerKey(key);
        }

        @Override
        public Entry<K, V> floorEntry(K key) {
            return normalMap().floorEntry(key);
        }

        @Override
        public K floorKey(K key) {
            return normalMap().floorKey(key);
        }

        @Override
        public Entry<K, V> ceilingEntry(K key) {
            return normalMap().ceilingEntry(key);
        }

        @Override
        public K ceilingKey(K key) {
            return normalMap().ceilingKey(key);
        }

        @Override
        public Entry<K, V> higherEntry(K key) {
            return normalMap().higherEntry(key);
        }

        @Override
        public K higherKey(K key) {
            return normalMap().higherKey(key);
        }

        @Override
        public Entry<K, V> firstEntry() {
            return normalMap().firstEntry();
        }

        @Override
        public Entry<K, V> lastEntry() {
            return normalMap().lastEntry();
        }

        @Override
        public Entry<K, V> pollFirstEntry() {
            return normalMap().pollFirstEntry();
        }

        @Override
        public Entry<K, V> pollLastEntry() {
            return normalMap().pollLastEntry();
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            return normalMap().navigableKeySet();
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return normalMap().descendingKeySet();
        }

        @Override
        public NavigableMap<K, V> descendingMap() {
            return createSubMap(normalMap().descendingMap());
        }

        @Override
        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return createSubMap(normalMap().subMap(fromKey, fromInclusive, toKey, toInclusive));
        }

        @Override
        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            return createSubMap(normalMap().headMap(toKey, inclusive));
        }

        @Override
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            return createSubMap(normalMap().tailMap(fromKey, inclusive));
        }

        @Override
        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            return createSubMap(normalMap().subMap(fromKey, toKey));
        }

        @Override
        public SortedMap<K, V> headMap(K toKey) {
            return createSubMap(normalMap().headMap(toKey));
        }

        @Override
        public SortedMap<K, V> tailMap(K fromKey) {
            return createSubMap(normalMap().tailMap(fromKey));
        }
    }

    /**
     * Inner class MapIterator.
     */
    protected static class BidiOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {

        /** The parent map */
        private final DualTreeBidiMap<K, V> parent;

        private Map.Entry<K, V> nextEntry;
        private Map.Entry<K, V> prevEntry;

        /** The last returned entry */
        private Map.Entry<K, V> current;

        /** Whether remove is allowed at present */
        protected boolean canRemove;

        /**
         * Constructor.
         * @param parent  the parent map
         */
        protected BidiOrderedMapIterator(final DualTreeBidiMap<K, V> parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public K next() {
            current = nextEntry;
            if (current == null) {
                throw new NoSuchElementException();
            }
            prevEntry = current;
            nextEntry = parent.normalMap().higherEntry(current.getKey());
            canRemove = true;
            return current.getKey();
        }

        @Override
        public boolean hasPrevious() {
            return prevEntry != null;
        }

        @Override
        public K previous() {
            current = prevEntry;
            if (current == null) {
                // should fail due to already changing current
                throw new NoSuchElementException();
            }
            nextEntry = current;
            prevEntry = parent.normalMap().lowerEntry(current.getKey());
            canRemove = true;
            return current.getKey();
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next() or previous()");
            }
            parent.remove(current.getKey());
            if (nextEntry == current) {
                if (prevEntry == null)
                    nextEntry = parent.normalMap().firstEntry();
                else
                    nextEntry = parent.normalMap().higherEntry(prevEntry.getKey());
            } else {
                if (nextEntry == null)
                    prevEntry = parent.normalMap().lastEntry();
                else
                    prevEntry = parent.normalMap().lowerEntry(nextEntry.getKey());
            }
            current = null;
            canRemove = false;
        }

        @Override
        public K getKey() {
            if (current == null) {
                throw new IllegalStateException(
                        "Iterator getKey() can only be called after next() or previous() and before remove()");
            }
            return current.getKey();
        }

        @Override
        public V getValue() {
            if (current == null) {
                throw new IllegalStateException(
                        "Iterator getValue() can only be called after next() or previous() and before remove()");
            }
            return current.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (current == null) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() or previous() and before remove()");
            }

            final K key = current.getKey();
            if (parent.reverseMap().containsKey(value) &&
                    !Objects.equals(parent.reverseMap().get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use setValue() when the object being set is already in the map");
            }

            final V oldValue = parent.put(key, value);

            // entry objects as returned from TreeMap navigation methods don't support setValue
            UnmodifiableMapEntry<K, V> replacementEntry = new UnmodifiableMapEntry<>(key, value);
            if (nextEntry == current)
                nextEntry = replacementEntry;
             else
                prevEntry = replacementEntry;
            current = replacementEntry;

            return oldValue;
        }

        @Override
        public void reset() {
            nextEntry = parent.normalMap().firstEntry();
            prevEntry = null;
            current = null;
            canRemove = false;
        }

        @Override
        public String toString() {
            if (current != null) {
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
        setInternalMaps(new TreeMap<>(), new TreeMap<>());
        @SuppressWarnings("unchecked") // will fail at runtime if the stream is incorrect
        final Map<K, V> map = (Map<K, V>) in.readObject();
        putAll(map);
    }

}
