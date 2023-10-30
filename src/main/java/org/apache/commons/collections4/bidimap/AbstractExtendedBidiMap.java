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
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.AbstractIterableMapAlternate;
import org.apache.commons.collections4.set.AbstractMapViewSortedSet;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
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

    protected abstract class AbsExMapView<E, TSubSet extends SortedRangedSet<E, ?>>
            extends AbstractMapViewSortedSet<E, TSubSet> {
        @Override
        public final int size() {
            return AbstractExtendedBidiMap.this.size();
        }

        @Override
        public final boolean isEmpty() {
            return AbstractExtendedBidiMap.this.isEmpty();
        }

        @Override
        public final void clear() {
            AbstractExtendedBidiMap.this.clear();
        }
    }

    protected final class AbsExMapValues extends AbsExMapView<V, SortedRangedSet<V, ?>> {
        @Override
        public SortedMapRange<V> getRange() {
            return AbstractExtendedBidiMap.this.getValueRange();
        }

        @Override
        public SortedRangedSet<V, ?> subSet(final SortedMapRange<V> range) {
            // TODO consider adding a Map.valuesSubMap interface
            return inverseBidiMap().subMap(range).inverseBidiMap().values();
        }

        @Override
        public Iterator<V> iterator() {
            return new AbsIterMapValueIterator<>(mapIterator());
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> v);
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
        public boolean contains(final Object o) {
            return AbstractExtendedBidiMap.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedBidiMap.this.removeValueAsBoolean(o);
        }
    }

    private class AbsExMapKeys extends AbsExMapView<K, SortedRangedSet<K, ?>> {
        @Override
        public SortedMapRange<K> getRange() {
            return AbstractExtendedBidiMap.this.getKeyRange();
        }

        @Override
        public SortedRangedSet<K, ?> subSet(final SortedMapRange<K> range) {
            return subMap(range).keySet();
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
        public boolean contains(Object o) {
            return AbstractExtendedBidiMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return mapIterator();
        }

        @Override
        public Spliterator<K> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> k);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedBidiMap.this.removeAsBoolean(o);
        }
    }

    private class AbsExMapEntries extends AbsExMapView<Entry<K, V>, SortedRangedSet<Entry<K, V>, ?>> {
        @Override
        public SortedMapRange<Entry<K, V>> getRange() {
            final SortedMapRange<K> range = AbstractExtendedBidiMap.this.getKeyRange();
            final SortedMapRange<Entry<K, V>> entryRangeFull = SortedMapRange.full(comparator());
            return entryRangeFull.subRange(new UnmodifiableMapEntry<>(range.getFromKey(), null), range.isFromInclusive(),
                                           new UnmodifiableMapEntry<>(range.getToKey(), null), range.isToInclusive());
        }

        @Override
        public SortedRangedSet<Entry<K, V>, ?> subSet(final SortedMapRange<Entry<K, V>> range) {
            final SortedMapRange<K> parentKeyRange = AbstractExtendedBidiMap.this.getKeyRange();
            final SortedMapRange<K> subKeyRange = parentKeyRange.subRange(range.getFromKey().getKey(), range.isFromInclusive(),
                    range.getToKey().getKey(), range.isToInclusive());
            return subMap(subKeyRange).entrySet();
        }

        @Override
        public Comparator<? super Entry<K, V>> comparator() {
            final Comparator<? super K> keyComparator = AbstractExtendedBidiMap.this.comparator();
            return Entry.comparingByKey(keyComparator);
        }

        @Override
        public Entry<K, V> first() {
            return AbstractExtendedBidiMap.this.firstEntry();
        }

        @Override
        public Entry<K, V> last() {
            return AbstractExtendedBidiMap.this.lastEntry();
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return entryIterator();
        }
    }

}
