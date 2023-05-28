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

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractDualTreeBidiMap<K, V> extends AbstractDualBidiMap<K, V> implements SortedBidiMap<K, V>, NavigableMap<K, V> {
    public AbstractDualTreeBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
    }

    public AbstractDualTreeBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap) {
        super(normalMap, reverseMap);
    }

    /**
     * Creates an empty map.
     * <p>
     * This constructor remains in place for deserialization.
     */
    @SuppressWarnings("unused")
    protected AbstractDualTreeBidiMap() {
    }

    @Override
    protected NavigableMap<K, V> normalMap() {
        return (NavigableMap<K, V>) super.normalMap();
    }

    @Override
    protected NavigableMap<V, K> reverseMap() {
        return (NavigableMap<V, K>) super.reverseMap();
    }

    protected abstract AbstractDualTreeBidiMap<K, V> createSubMap(NavigableMap<K,V> normalMap);

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
        return new BidiOrderedMapIterator<>(this, normalMap());
    }

    @Override
    public SortedBidiMap<V, K> inverseBidiMap() {
        return (SortedBidiMap<V, K>) super.inverseBidiMap();
    }

    @Override
    protected V setValueViaCollection(K key, V value) {
        if (reverseMap().containsKey(value) && !keyEquals(reverseMap().get(value), key)) {
            throw new IllegalArgumentException(
                    "Cannot use setValue() when the object being set is already in the map");
        }
        return put(key, value);
    }

    private boolean keyEquals(K a, K b) {
        Comparator<? super K> comparator = comparator();
        if (comparator != null)
            return comparator.compare(a, b) == 0;
        else
            return Objects.equals(a, b);
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
    public AbstractDualTreeBidiMap<K, V> headMap(final K toKey) {
        return createSubMap(normalMap().headMap(toKey, false));
    }

    @Override
    public AbstractDualTreeBidiMap<K, V> tailMap(final K fromKey) {
        return createSubMap( normalMap().tailMap(fromKey, true));
    }

    @Override
    public AbstractDualTreeBidiMap<K, V> subMap(final K fromKey, final K toKey) {
        return createSubMap(normalMap().subMap(fromKey, true, toKey, false));
    }
    @Override
    public AbstractDualTreeBidiMap<K, V> subMap(final K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return createSubMap(normalMap().subMap(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    public AbstractDualTreeBidiMap<K, V> headMap(final K toKey, boolean inclusive) {
        return createSubMap(normalMap().headMap(toKey, inclusive));
    }

    @Override
    public AbstractDualTreeBidiMap<K, V> tailMap(final K fromKey, boolean inclusive) {
        return createSubMap(normalMap().tailMap(fromKey, inclusive));
    }

    /**
     * Inner class MapIterator.
     */
    protected static class BidiOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {

        /**
         * The parent map
         */
        private final AbstractDualBidiMap<K, V> parent;
        private final NavigableMap<K, V> normalMap;

        private Entry<K, V> nextEntry;
        private Entry<K, V> prevEntry;

        /**
         * The last returned entry
         */
        private Entry<K, V> current;

        /**
         * Whether remove is allowed at present
         */
        protected boolean canRemove;

        /**
         * Constructor.
         *
         * @param parent the parent map
         */
        protected BidiOrderedMapIterator(final AbstractDualBidiMap<K, V> parent, final NavigableMap<K, V> normalMap) {
            this.parent = parent;
            this.normalMap = normalMap;
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
            nextEntry = normalMap.higherEntry(current.getKey());
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
            prevEntry = normalMap.lowerEntry(current.getKey());
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
                    nextEntry = normalMap.firstEntry();
                else
                    nextEntry = normalMap.higherEntry(prevEntry.getKey());
            } else {
                if (nextEntry == null)
                    prevEntry = normalMap.lastEntry();
                else
                    prevEntry = normalMap.lowerEntry(nextEntry.getKey());
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
            final V oldValue = parent.setValueViaCollection(key, value);

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
            nextEntry = normalMap.firstEntry();
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
}
