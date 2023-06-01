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
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

/**
 * Internal sub map view.
 */
class DualTreeBidiSubMapInverseX<K, V> extends AbstractDualTreeBidiMap<K, V> implements Unmodifiable {
    static final Object NULL = new Object();

    protected DualTreeBidiSubMapInverseX(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseMap) {
        super(normalMap, reverseMap, inverseMap);
    }

    @Override
    protected DualTreeBidiSubMapInverseX<V, K> createInverse() {
        throw new IllegalStateException("should never get called");
    }

    @Override
    protected AbstractDualTreeBidiMap<K, V> createSubMap(NavigableMap<K, V> normalMap) {
        return new DualTreeBidiSubMapInverseX<>(normalMap, reverseMap(),null);
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
    public NavigableSet<K> navigableKeySet() {
        return super.navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return super.descendingKeySet();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return super.descendingMap();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return super.mapIterator();
    }

    @Override
    public int size() {
        return reverseMap().size();
    }

    @Override
    public boolean isEmpty() {
        return reverseMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        V value = normalMap().getOrDefault(key, (V) NULL);
        if (value != NULL) {
            return reverseMap().containsKey(value);
        }
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        K key = reverseMap().getOrDefault(value, (K) NULL);
        if (key != NULL) {
            return normalMap().containsKey(key);
        }
        return false;
    }

    @Override
    public V get(Object key) {
        V value = normalMap().getOrDefault(key, (V) NULL);
        if (value != NULL && reverseMap().containsKey(value)) {
            return value;
        }
        return null;
    }

    @Override
    public K getKey(Object value) {
        K key = reverseMap().getOrDefault(value, (K) NULL);
        if (key != NULL && normalMap().containsKey(key)) {
            return key;
        }
        return null;
    }

    @Override
    protected Values<V> createValues() {
        return new ValuesUsingReverse<>(this);
    }

    @Override
    protected KeySet<K> createKeySet() {
        return new KeySetUsingReverse<>(this);
    }

    @Override
    protected Set<Map.Entry<K, V>> createEntrySet() {
        return new EntrySetInverted<>(this);
    }

    @Override
    protected Iterator<Entry<K, V>> createEntrySetIterator(Iterator<Entry<K, V>> iterator) {
        return new EntrySetIteratorInverted<>(this);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K removeValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean removeValueViaCollection(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean removeViaCollection(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object expectedValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected V setValueViaCollection(K key, V value) {
        throw new UnsupportedOperationException();
    }

    protected static class KeySetUsingReverse<K> extends KeySet<K> {
        protected KeySetUsingReverse(AbstractDualBidiMap<K, ?> parent) {
            super(parent.reverseMap().values(), parent);
        }

        @Override
        public boolean contains(Object key) {
            return parent.containsKey(key);
        }
    }

    protected static class ValuesUsingReverse<V> extends Values<V> {
        public ValuesUsingReverse(DualTreeBidiSubMapInverseX<?, V> parent) {
            super(parent.reverseMap().keySet(), parent);
        }
    }

    protected static class EntrySetInverted<K, V> implements Set<Entry<K, V>> {
        private final DualTreeBidiSubMapInverseX<K, V> parent;
        private final Set<Entry<V, K>> reverseEntrySet;

        public EntrySetInverted(DualTreeBidiSubMapInverseX<K, V> parent) {
            this.parent = parent;
            this.reverseEntrySet = parent.reverseMap().entrySet();
        }

        @Override
        public int size() {
            return reverseEntrySet.size();
        }

        @Override
        public boolean isEmpty() {
            return reverseEntrySet.isEmpty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            Entry<K, V> entry = (Entry<K, V>) o;
            Entry<V, K> inverseEntry = new UnmodifiableMapEntry<>(entry.getValue(), entry.getKey());
            return reverseEntrySet.contains(inverseEntry);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return parent.createEntrySetIterator(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object[] toArray() {
            final Object[] array = reverseEntrySet.toArray();
            for (int i = 0; i < array.length; i++) {
                Entry<V, K> entry = (Entry<V, K>) array[i];
                Entry<K, V> inverseEntry = new UnmodifiableMapEntry<>(entry.getValue(), entry.getKey());
                array[i] = inverseEntry;
            }
            return array;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] array) {
            // we must create a new array to handle multithreaded situations
            // where another thread could access data before we invert it
            T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), reverseEntrySet.size());
            result = reverseEntrySet.toArray(result);
            if (result.length > array.length) {
                for (int i = 0; i < result.length; i++) {
                    Entry<V, K> entry = (Entry<V, K>) result[i];
                    Entry<K, V> inverseEntry = new UnmodifiableMapEntry<>(entry.getValue(), entry.getKey());
                    result[i] = (T) inverseEntry;
                }
                return result;
            } else {
                for (int i = 0; i < result.length; i++) {
                    Entry<V, K> entry  = (Entry<V, K>) result[i];
                    Entry<K, V> inverseEntry = new UnmodifiableMapEntry<>(entry.getValue(), entry.getKey());
                    array[i] = (T) inverseEntry;
                }
                array[result.length] = null;
                return array;
            }
        }

        @Override
        public boolean containsAll(Collection<?> coll) {
            if (coll == null) {
                return false;
            }
            for (final Object item : coll) {
                if (!contains(item)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean add(final Entry<K, V> object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends Entry<K, V>> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeIf(final Predicate<? super Entry<K, V>> filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            throw new UnsupportedOperationException();
        }
    }

    protected static class EntrySetIteratorInverted<K, V> implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<V, K>> reverseIterator;

        public EntrySetIteratorInverted(DualTreeBidiSubMapInverseX<K, V> parent) {
            this.reverseIterator = parent.reverseMap().entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return reverseIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            Entry<V, K> entry = reverseIterator.next();
            return new UnmodifiableMapEntry<>(entry.getValue(), entry.getKey());
        }
    }
}
