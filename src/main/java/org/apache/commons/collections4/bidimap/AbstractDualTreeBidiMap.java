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

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.*;

public abstract class AbstractDualTreeBidiMap<K, V> extends AbstractDualBidiMap<K, V> implements SortedBidiMap<K, V>, NavigableMap<K, V> {
    protected AbstractDualTreeBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseBidiMap) {
        super(normalMap, reverseMap, inverseBidiMap);
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
        return new BidiNavigableMapIterator<>(this, normalMap());
    }

    @Override
    public SortedBidiMap<V, K> inverseBidiMap() {
        return (SortedBidiMap<V, K>) super.inverseBidiMap();
    }

    @Override
    protected V setValueViaCollection(K key, V value) {
        if (reverseMap().containsKey(value) &&
                !keyEquals(reverseMap().get(value), key)) {
            throw new IllegalArgumentException(
                    "Cannot use setValue() when the value being set is already in the map");
        }
        return put(key, value);
    }

    @Override
    protected boolean keyEquals(K a, K b) {
        Comparator<? super K> comparator = comparator();
        if (comparator != null)
            return comparator.compare(a, b) == 0;
        else
            return Objects.equals(a, b);
    }

    @Override
    protected boolean valueEquals(V a, V b) {
        Comparator<? super V> comparator = valueComparator();
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
    protected KeySet<K> createKeySet() {
        return new NavKeySet<>(normalMap().navigableKeySet(), this);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new NavKeySet<>(normalMap().navigableKeySet(), this);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new NavKeySet<>(normalMap().descendingKeySet(), this);
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        // TODO
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
    protected static class BidiNavigableMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {

        private final AbstractDualBidiMap<K, V> parent;
        private final NavigableMap<K, V> normalMap;

        private Iterator<Entry<K, V>> forwardIterator;
        private Iterator<Entry<K, V>> backwardIterator;

        private boolean canRemove;
        private boolean forwardDirection;
        private Entry<K, V> current;

        /**
         * Constructor.
         *
         * @param parent the parent map
         */
        protected BidiNavigableMapIterator(final AbstractDualBidiMap<K, V> parent, final NavigableMap<K, V> normalMap) {
            this.parent = parent;
            this.normalMap = normalMap;
            reset();
        }

        private Iterator<Entry<K,V>> forwardIterator() {
            Iterator<Entry<K, V>> iterator = forwardIterator;
            if (iterator == null) {
                iterator = normalMap.tailMap(current.getKey(), true).entrySet().iterator();
                forwardIterator = iterator;
            }
            return iterator;
        }

        private Iterator<Entry<K,V>> backwardIterator() {
            Iterator<Entry<K, V>> iterator = backwardIterator;
            if (iterator != null) {
                return iterator;
            } else if (current == null) {
                return EmptyIterator.emptyIterator();
            } else {
                iterator = normalMap.headMap(current.getKey(), true).descendingMap().entrySet().iterator();
                backwardIterator = iterator;
                return iterator;
            }
        }

        @Override
        public boolean hasNext() {
            return forwardIterator().hasNext();
        }

        @Override
        public K next() {
            Iterator<Entry<K, V>> iterator = forwardIterator();
            current = iterator.next();
            forwardDirection = true;
            backwardIterator = null;
            canRemove = true;
            return current.getKey();
        }

        @Override
        public boolean hasPrevious() {
            return backwardIterator().hasNext();
        }

        @Override
        public K previous() {
            Iterator<Entry<K, V>> iterator = backwardIterator();
            current = iterator.next();
            forwardDirection = false;
            forwardIterator = null;
            canRemove = true;
            return current.getKey();
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next() or previous()");
            }
            if (forwardDirection) {
                forwardIterator.remove();
                backwardIterator = null;
            } else {
                backwardIterator.remove();
                forwardIterator = null;
            }
            parent.reverseMap().remove(current.getValue());
            canRemove = false;
        }

        @Override
        public K getKey() {
            if (!canRemove) {
                throw new IllegalStateException(
                        "Iterator getKey() can only be called after next() or previous() and before remove()");
            }
            return current.getKey();
        }

        @Override
        public V getValue() {
            if (!canRemove) {
                throw new IllegalStateException(
                        "Iterator getValue() can only be called after next() or previous() and before remove()");
            }
            return current.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (!canRemove) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() or previous() and before remove()");
            }

            final K key = current.getKey();
            final V oldValue = parent.setValueViaCollection(key, value);
            current.setValue(value);
            return oldValue;
        }

        @Override
        public void reset() {
            forwardDirection = true;
            forwardIterator = normalMap.entrySet().iterator();
            backwardIterator = null;
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

    protected static class NavKeySet<K> extends KeySet<K> implements NavigableSet<K> {
        protected NavKeySet(NavigableSet<K> set, AbstractDualBidiMap<K, ?> parent) {
            super(set, parent);
        }

        @Override
        protected NavigableSet<K> decorated() {
            return (NavigableSet<K>) super.decorated();
        }

        protected NavKeySet<K> createSubSet(NavigableSet<K> set) {
            return new NavKeySet<>(set, parent);
        }

        @Override
        public Comparator<? super K> comparator() {
            return decorated().comparator();
        }

        @Override
        public K lower(K k) {
            return decorated().lower(k);
        }

        @Override
        public K floor(K k) {
            return decorated().floor(k);
        }

        @Override
        public K ceiling(K k) {
            return decorated().ceiling(k);
        }

        @Override
        public K higher(K k) {
            return decorated().higher(k);
        }

        @Override
        public K pollFirst() {
            return decorated().pollFirst();
        }

        @Override
        public K pollLast() {
            return decorated().pollLast();
        }

        @Override
        public Iterator<K> iterator() {
            return decorated().iterator();
        }

        @Override
        public Iterator<K> descendingIterator() {
            return decorated().descendingIterator();
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return createSubSet(decorated().descendingSet());
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return createSubSet(decorated().subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return createSubSet(decorated().headSet(toElement, inclusive));
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return createSubSet(decorated().tailSet(fromElement, inclusive));
        }

        @Override
        public SortedSet<K> subSet(K fromElement, K toElement) {
            return createSubSet(decorated().subSet(fromElement, true, toElement, false));
        }

        @Override
        public SortedSet<K> headSet(K toElement) {
            return createSubSet(decorated().headSet(toElement, false));
        }

        @Override
        public SortedSet<K> tailSet(K fromElement) {
            return createSubSet(decorated().tailSet(fromElement, true));
        }

        @Override
        public K first() {
            return decorated().first();
        }

        @Override
        public K last() {
            return decorated().last();
        }
    }
}
