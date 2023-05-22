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

import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;

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
public abstract class AbstractSortedMapDecorator<K, V> extends AbstractMapDecorator<K, V> implements
        IterableSortedMap<K, V> {

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractSortedMapDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public AbstractSortedMapDecorator(final SortedMap<K, V> map) {
        super(map);
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    @Override
    protected SortedMap<K, V> decorated() {
        return (SortedMap<K, V>) super.decorated();
    }

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    @Override
    public K firstKey() {
        return decorated().firstKey();
    }

    @Override
    public K lastKey() {
        return decorated().lastKey();
    }

    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
        return decorated().subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<K, V> headMap(final K toKey) {
        return decorated().headMap(toKey);
    }

    @Override
    public SortedMap<K, V> tailMap(final K fromKey) {
        return decorated().tailMap(fromKey);
    }

    @Override
    public K previousKey(final K key) {
        SortedMap<K, V> decorated = decorated();
        if (decorated instanceof NavigableMap) {
            NavigableMap<K, V> navigable = (NavigableMap<K, V>) decorated;
            return navigable.lowerKey(key);
        } else {
            final SortedMap<K, V> headMap = decorated.headMap(key);
            return headMap.isEmpty() ? null : headMap.lastKey();
        }
    }

    @Override
    public K nextKey(final K key) {
        SortedMap<K, V> decorated = decorated();
        if (decorated instanceof NavigableMap) {
            NavigableMap<K, V> navigable = (NavigableMap<K, V>) decorated;
            return navigable.higherKey(key);
        } else {
            final Iterator<K> it = decorated.tailMap(key).keySet().iterator();
            it.next();
            return it.hasNext() ? it.next() : null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        SortedMap<K, V> decorated = decorated();
        if (decorated instanceof NavigableMap) {
            return new SortedMapIteratorNavigable<>((NavigableMap<K, V>) decorated);
        } else {
            return new SortedMapIterator<>(decorated.entrySet());
        }
    }

    @Override
    public OrderedMapIterator<K, V> mapIteratorBetween(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, boolean reverse) {
        SortedMap<K, V> map = decorated().subMap(fromKey, toKey);
        if (map instanceof NavigableMap) {
            return new SortedMapIteratorNavigable<>((NavigableMap<K, V>) map);
        } else {
            return new SortedMapIterator<>(map.entrySet());
        }
    }

    protected static class SortedMapIteratorNavigable<K, V>            implements ResettableIterator<K>, OrderedMapIterator<K, V> {
        transient final NavigableMap<K, V> map;
        transient Map.Entry<K, V> next;
        transient Map.Entry<K, V> curr;
        transient Map.Entry<K, V> prev;

        public SortedMapIteratorNavigable(NavigableMap<K, V> map) {
            this.map = map;
            this.next = map.firstEntry();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K getKey() {
            return current().getKey();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V getValue() {
            return current().getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V setValue(final V value) {
            return current().setValue(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K next() {
            Map.Entry<K, V> entry = next;
            if (entry == null)
                throw new NoSuchElementException();
            K key = entry.getKey();
            prev = entry;
            curr = entry;
            next = map.higherEntry(key);
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return prev != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K previous() {
            Map.Entry<K, V> entry = prev;
            if (entry == null)
                throw new NoSuchElementException();
            K key = entry.getKey();
            prev = map.lowerEntry(key);
            curr = entry;
            next = entry;
            return key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            if (curr == null)
                throw new IllegalStateException();
            map.remove(curr.getKey(), curr.getValue());
            curr = null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Map.Entry<K, V> entry = next, last = null;
            while (entry != null) {
                K key = entry.getKey();
                action.accept(key);
                last = entry;
                entry = map.higherEntry(key);
            }
            prev = last;
            curr = null;
            next = null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            this.next = map.firstEntry();
            this.prev = null;
            this.curr = null;
        }

        /**
         * Get the currently active entry.
         * @return Map.Entry&lt;K, V&gt;
         */
        protected synchronized Map.Entry<K, V> current() {
            if (curr == null) {
                throw new IllegalStateException();
            }
            return curr;
        }
    }

    /**
     * OrderedMapIterator implementation.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     */
    protected static class SortedMapIterator<K, V> extends EntrySetToMapIteratorAdapter<K, V>
            implements OrderedMapIterator<K, V> {

        /**
         * Create a new AbstractSortedMapDecorator.SortedMapIterator.
         * @param entrySet  the entrySet to iterate
         */
        protected SortedMapIterator(final Set<Map.Entry<K, V>> entrySet) {
            super(entrySet);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void reset() {
            super.reset();
            // is it ok to assume listIterator?
            iterator = new ListIteratorWrapper<>(iterator);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return ((ListIterator<Map.Entry<K, V>>) iterator).hasPrevious();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K previous() {
            entry = ((ListIterator<Map.Entry<K, V>>) iterator).previous();
            return getKey();
        }
    }
}
