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

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.map.AbstractIterableMapAlternate;
import org.apache.commons.collections4.set.AbstractMapViewSortedSet;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Spliterator;

public abstract class AbstractExtendedBidiMap<K, V,
        TSubMap extends AbstractExtendedBidiMap<K, V, TSubMap, TInverseMap>,
        TInverseMap extends AbstractExtendedBidiMap<V, K, TInverseMap, TSubMap>>
        extends AbstractIterableMapAlternate<K, V>
        implements SortedBidiMap<K, V, TSubMap, TInverseMap> {

    private static final long serialVersionUID = -9181666289732043651L;

    @Override
    public final K getKey(final Object value) {
        return getKeyOrDefault(value, null);
    }

    @Override
    public abstract OrderedMapIterator<K, V> mapIterator();

    @Override
    protected final SequencedSet<K> createKeySet() {
        return new AbsExMapKeys();
    }

    @Override
    public final SortedRangedSet<K, ?> keySet() {
        return (SortedRangedSet<K, ?>) super.keySet();
    }

    @Override
    public final SortedRangedSet<K, ?> sequencedKeySet() {
        return (SortedRangedSet<K, ?>) super.keySet();
    }

    @Override
    protected final SequencedSet<V> createValuesCollection() {
        return new AbsExMapValues();
    }

    @Override
    public final SortedRangedSet<V, ?> values() {
        return (SortedRangedSet<V, ?>) super.values();
    }

    @Override
    public final SortedRangedSet<V, ?> sequencedValues() {
        return (SortedRangedSet<V, ?>) super.values();
    }

    @Override
    protected final SortedRangedSet<Entry<K, V>, ?> createEntrySet() {
        return new AbsExMapEntries();
    }

    @Override
    public final SortedRangedSet<Entry<K, V>, ?> entrySet() {
        return (SortedRangedSet<Entry<K, V>, ?>) super.entrySet();
    }

    @Override
    public final SortedRangedSet<Entry<K, V>, ?> sequencedEntrySet() {
        return (SortedRangedSet<Entry<K, V>, ?>) super.entrySet();
    }

    protected final class AbsExMapValues extends AbstractMapViewSortedSet<V, SortedRangedSet<V, ?>> {
        @Override
        public SortedMapRange<V> getRange() {
            return AbstractExtendedBidiMap.this.getValueRange();
        }

        @Override
        public SortedRangedSet<V, ?> subSet(final SortedMapRange<V> range) {
            // TODO consider adding a Map.valuesSubMap interface
            TInverseMap a = inverseBidiMap();
            TInverseMap b = a.subMap(range);
            TSubMap c = b.inverseBidiMap();
            SequencedSet<V> d = c.values();

            return inverseBidiMap().subMap(range).inverseBidiMap().values();
        }

        @Override
        public Iterator<V> iterator() {
            return new AbsIterMapValueIterator<>(mapIterator());
        }

        @Override
        public Comparator<? super V> comparator() {
            return AbstractExtendedBidiMap.this.valueComparator();
        }

        @Override
        public V first() {
            return AbstractExtendedBidiMap.this.firstValue();
        }

        @Override
        public V last() {
            return AbstractExtendedBidiMap.this.lastValue();
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> v);
        }

        @Override
        public int size() {
            return AbstractExtendedBidiMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractExtendedBidiMap.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractExtendedBidiMap.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedBidiMap.this.removeValueAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractExtendedBidiMap.this.clear();
        }
    }

    private class AbsExMapKeys extends AbstractMapViewSortedSet<K, AbsExMapKeys> implements SequencedSet<K> {

        @Override
        public SortedMapRange<K> getRange() {
            return AbstractExtendedBidiMap.this.getKeyRange();
        }

        @Override
        public AbsExMapKeys subSet(SortedMapRange<K> range) {
            return null;
        }

        @Override
        public Comparator<? super K> comparator() {
            return AbstractExtendedBidiMap.this.comparator();
        }

        @Override
        public K first() {
            return AbstractExtendedBidiMap.this.firstKey();
        }

        @Override
        public K last() {
            return AbstractExtendedBidiMap.this.lastKey();
        }

        @Override
        public int size() {
            return AbstractExtendedBidiMap.this.size();
        }

        @Override
        public boolean contains(Object o) {
            return AbstractExtendedBidiMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return null;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public void clear() {

        }
    }
    private class AbsExMapEntries extends AbstractMapViewSortedSet<Entry<K, V>, AbsExMapEntries> implements SequencedSet<Entry<K, V>> {


    }

}
