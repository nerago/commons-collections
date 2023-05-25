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

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;

import java.util.*;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to a Map via decoration.
 * <p>
 * Methods are forwarded directly to the decorated map.
 * </p>
 * <p>
 * This implementation does not perform any special processing with the map views.
 * Instead it simply returns the set/collection from the wrapped map. This may be
 * undesirable, for example if you are trying to write a validating implementation
 * it would provide a loophole around the validation.
 * But, you might want that loophole, so this class is kept simple.
 * </p>
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 * @since 3.0
 */
public abstract class AbstractNavigableMapDecorator<K, V> extends AbstractSortedMapDecorator<K, V> implements
        NavigableMap<K, V> {

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractNavigableMapDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public AbstractNavigableMapDecorator(final NavigableMap<K, V> map) {
        super(map);
    }

    protected abstract NavigableMap<K, V> createWrappedMap(NavigableMap<K, V> other);

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    @Override
    protected NavigableMap<K, V> decorated() {
        return (NavigableMap<K, V>) super.decorated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new NavigableMapIterator<>(this);
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return decorated().lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return decorated().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return decorated().floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return decorated().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return decorated().ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return decorated().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return decorated().higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return decorated().higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return decorated().firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return decorated().lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return decorated().pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return decorated().pollLastEntry();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return createWrappedMap(decorated().descendingMap());
    }

    /**
     * Returns a {@link NavigableSet} view of the keys contained in this map.
     * The set's iterator returns the keys in ascending order.
     * The set is backed by the map, so changes to the map are reflected in
     * the set, and vice-versa.  If the map is modified while an iteration
     * over the set is in progress (except through the iterator's own {@code
     * remove} operation), the results of the iteration are undefined.  The
     * set supports element removal, which removes the corresponding mapping
     * from the map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations.
     * It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a navigable set view of the keys in this map
     */
    @Override
    public NavigableSet<K> navigableKeySet() {
        return null;
    }

    /**
     * Returns a reverse order {@link NavigableSet} view of the keys contained in this map.
     * The set's iterator returns the keys in descending order.
     * The set is backed by the map, so changes to the map are reflected in
     * the set, and vice-versa.  If the map is modified while an iteration
     * over the set is in progress (except through the iterator's own {@code
     * remove} operation), the results of the iteration are undefined.  The
     * set supports element removal, which removes the corresponding mapping
     * from the map, via the {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations.
     * It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a reverse order navigable set view of the keys in this map
     */
    @Override
    public NavigableSet<K> descendingKeySet() {
        return null;
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return createWrappedMap(decorated().subMap(fromKey,fromInclusive,toKey,toInclusive));
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return createWrappedMap(decorated().headMap(toKey,inclusive));
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return createWrappedMap(decorated().tailMap(fromKey,inclusive));
    }

    @Override
    public NavigableMap<K, V> subMap(final K fromKey, final K toKey) {
        return createWrappedMap(decorated().subMap(fromKey, true, toKey, false));
    }

    @Override
    public NavigableMap<K, V> headMap(final K toKey) {
        return createWrappedMap(decorated().headMap(toKey, false));
    }

    @Override
    public NavigableMap<K, V> tailMap(final K fromKey) {
        return createWrappedMap(decorated().tailMap(fromKey, true));
    }

    /**
     * OrderedMapIterator implementation.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     */
    protected static class NavigableMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
        private final AbstractNavigableMapDecorator<K, V> map;
        private Map.Entry<K, V> lastReturnedNode;
        /** The next node to be returned by the iterator. */
        private Map.Entry<K, V> nextNode;
        /** The previous node in the sequence returned by the iterator. */
        private Map.Entry<K, V> previousNode;

        /**
         * Create a new AbstractNavigableMapDecorator.NavigableMapIterator.
         * @param map the map to iterate
         */
        protected NavigableMapIterator(final AbstractNavigableMapDecorator<K,V> map) {
            this.map = map;
        }

        @Override
        public synchronized void reset() {
            previousNode = null;
            lastReturnedNode = null;
            nextNode = map.firstEntry();
        }

        @Override
        public boolean hasNext() {
            return nextNode != null;
        }

        @Override
        public K next() {
            Entry<K, V> current = nextNode;
            if (current == null)
                throw new NoSuchElementException();
            K key = current.getKey();
            previousNode = current;
            lastReturnedNode = current;
            nextNode = map.higherEntry(key);
            return key;
        }

        @Override
        public boolean hasPrevious() {
            return previousNode != null;
        }

        @Override
        public K previous() {
            Entry<K, V> current = previousNode;
            if (current == null)
                throw new NoSuchElementException();
            K key = current.getKey();
            previousNode = map.lowerEntry(key);
            lastReturnedNode = current;
            nextNode = current;
            return key;
        }

        protected synchronized Map.Entry<K, V> current() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException();
            }
            return lastReturnedNode;
        }

        @Override
        public K getKey() {
            return current().getKey();
        }

        @Override
        public V getValue() {
            return current().getValue();
        }

        @Override
        public void remove() {
            Entry<K, V> current = current();
            map.remove(current.getKey());
            lastReturnedNode = null;
        }

        @Override
        public V setValue(V value) {
            Entry<K, V> current = current();
            current.setValue(value);
            return map.put(current.getKey(), current.getValue());
        }
    }
}
