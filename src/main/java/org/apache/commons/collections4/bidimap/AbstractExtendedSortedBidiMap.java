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
import org.apache.commons.collections4.iterators.MapIteratorToEntryAdapter;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.AbstractIterableMapAlternate;
import org.apache.commons.collections4.map.AbstractMapViewSortedSet;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

public abstract class AbstractExtendedSortedBidiMap<K, V,
            TSubMap extends SortedBidiMap<K, V, ?, ?>,
            TInverseMap extends SortedBidiMap<V, K, ?, ?>,
            TKeySet extends SortedRangedSet<K>,
            TEntrySet extends SortedRangedSet<Map.Entry<K, V>>,
            TValueSet extends SortedRangedSet<V>>
        extends AbstractIterableMapAlternate<K, V, TKeySet, TEntrySet, TValueSet>
        implements SortedBidiMap<K, V, TSubMap, TInverseMap> {

    private static final long serialVersionUID = -9181666289732043651L;

    private transient TSubMap reverse;
    private transient TInverseMap inverse;

    @Override
    public final K getKey(final Object value) {
        return getKeyOrDefault(value, null);
    }

    @Override
    public abstract OrderedMapIterator<K, V> mapIterator();

    @Override
    protected TKeySet createKeySet() {
        return (TKeySet) new AbsExMapKeys();
    }

    @Override
    protected TEntrySet createEntrySet() {
        return (TEntrySet) (SortedRangedSet<Map.Entry<K, V>>) new AbsExMapEntries();
    }

    @Override
    protected TValueSet createValuesCollection() {
        return (TValueSet) new AbsExMapValues();
    }

    @Override
    public final TSubMap reversed() {
        if (reverse == null) {
            reverse = createReverse();
        }
        return reverse;
    }

    protected TSubMap createReverse() {
        return (TSubMap) new ReverseSortedBidiMap<>(this);
    }

    @Override
    public TInverseMap inverseBidiMap() {
        if (inverse == null) {
            inverse = createInverse();
        }
        return inverse;
    }

    protected abstract TInverseMap createInverse();

    protected abstract class AbsExMapView<E> extends AbstractMapViewSortedSet<E, SortedRangedSet<E>> {
        @Override
        public final int size() {
            return AbstractExtendedSortedBidiMap.this.size();
        }

        @Override
        public final boolean isEmpty() {
            return AbstractExtendedSortedBidiMap.this.isEmpty();
        }

        @Override
        public final void clear() {
            AbstractExtendedSortedBidiMap.this.clear();
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            AbstractExtendedSortedBidiMap.this.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            AbstractExtendedSortedBidiMap.this.readExternal(in);
        }
    }

    protected final class AbsExMapValues extends AbsExMapView<V> {
        @Override
        public SortedMapRange<V> getRange() {
            return AbstractExtendedSortedBidiMap.this.getValueRange();
        }

        @Override
        public SortedRangedSet<V> subSet(final SortedMapRange<V> range) {
            // TODO consider adding a Map.valuesSubMap interface
            return inverseBidiMap().subMap(range).inverseBidiMap().values();
        }

        @Override
        public Iterator<V> iterator() {
            return new AbsIterMapValueIterator<>(mapIterator());
        }

        @Override
        public Iterator<V> descendingIterator() {
            return new AbsIterMapValueIterator<>(descendingMapIterator());
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> v);
        }

        @Override
        public Comparator<? super V> comparator() {
            return AbstractExtendedSortedBidiMap.this.valueComparator();
        }

        @Override
        public V first() {
            return AbstractExtendedSortedBidiMap.this.firstValue();
        }

        @Override
        public V last() {
            return AbstractExtendedSortedBidiMap.this.lastValue();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractExtendedSortedBidiMap.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedSortedBidiMap.this.removeValueAsBoolean(o);
        }
    }

    private class AbsExMapKeys extends AbsExMapView<K> {
        @Override
        public SortedMapRange<K> getRange() {
            return AbstractExtendedSortedBidiMap.this.getKeyRange();
        }

        @Override
        public SortedRangedSet<K> subSet(final SortedMapRange<K> range) {
            return subMap(range).keySet();
        }

        @Override
        public Comparator<? super K> comparator() {
            return AbstractExtendedSortedBidiMap.this.comparator();
        }

        @Override
        public K first() {
            return AbstractExtendedSortedBidiMap.this.firstKey();
        }

        @Override
        public K last() {
            return AbstractExtendedSortedBidiMap.this.lastKey();
        }

        @Override
        public boolean contains(Object o) {
            return AbstractExtendedSortedBidiMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return mapIterator();
        }

        @Override
        public Iterator<K> descendingIterator() {
            return descendingMapIterator();
        }

        @Override
        public Spliterator<K> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> k);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedSortedBidiMap.this.removeAsBoolean(o);
        }
    }

    private class AbsExMapEntries extends AbsExMapView<Entry<K, V>> {
        @Override
        public SortedMapRange<Entry<K, V>> getRange() {
            final SortedMapRange<K> range = AbstractExtendedSortedBidiMap.this.getKeyRange();
            final SortedMapRange<Entry<K, V>> entryRangeFull = SortedMapRange.full(comparator());
            return entryRangeFull.subRange(new UnmodifiableMapEntry<>(range.getFromKey(), null), range.isFromInclusive(),
                                           new UnmodifiableMapEntry<>(range.getToKey(), null), range.isToInclusive());
        }

        @Override
        public SortedRangedSet<Entry<K, V>> subSet(final SortedMapRange<Entry<K, V>> range) {
            final SortedMapRange<K> parentKeyRange = AbstractExtendedSortedBidiMap.this.getKeyRange();
            final SortedMapRange<K> subKeyRange = parentKeyRange.subRange(range.getFromKey().getKey(), range.isFromInclusive(),
                    range.getToKey().getKey(), range.isToInclusive());
            return subMap(subKeyRange).entrySet();
        }

        @Override
        public Comparator<? super Entry<K, V>> comparator() {
            final Comparator<? super K> keyComparator = AbstractExtendedSortedBidiMap.this.comparator();
            return Entry.comparingByKey(keyComparator);
        }

        @Override
        public Entry<K, V> first() {
            return AbstractExtendedSortedBidiMap.this.firstEntry();
        }

        @Override
        public Entry<K, V> last() {
            return AbstractExtendedSortedBidiMap.this.lastEntry();
        }

        @Override
        public boolean contains(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Entry<?, ?> entry = (Entry<?, ?>) obj;
            return AbstractExtendedSortedBidiMap.this.containsMapping(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean remove(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Entry<?, ?> entry = (Entry<?, ?>) obj;
            return AbstractExtendedSortedBidiMap.this.remove(entry.getKey(), entry.getValue());
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return entryIterator();
        }

        @Override
        public Iterator<Entry<K, V>> descendingIterator() {
            return new MapIteratorToEntryAdapter<>(descendingMapIterator());
        }
    }

}