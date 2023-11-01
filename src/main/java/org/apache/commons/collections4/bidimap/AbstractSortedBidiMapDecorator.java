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

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to a SortedBidiMap via decoration.
 * <p>
 * Methods are forwarded directly to the decorated map.
 * </p>
 * <p>
 * This implementation does not perform any special processing with the map views.
 * Instead it simply returns the inverse from the wrapped map. This may be
 * undesirable, for example if you are trying to write a validating implementation
 * it would provide a loophole around the validation.
 * But, you might want that loophole, so this class is kept simple.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public abstract class AbstractSortedBidiMapDecorator<K, V,
            TDecorated extends SortedBidiMap<K, V, ?, ?>,
            TDecoratedInverse extends SortedBidiMap<V, K, ?, ?>,
            TSubMap extends AbstractSortedBidiMapDecorator<K, V, ?, ?, ?, ?, ?, ?, ?>,
            TInverseMap extends AbstractSortedBidiMapDecorator<V, K, ?, ?, ?, ?, ?, ?, ?>,
            TKeySet extends SortedRangedSet<K, ?>,
            TEntrySet extends SortedRangedSet<Map.Entry<K, V>, ?>,
            TValueSet extends SortedRangedSet<V, ?>>
        extends AbstractOrderedBidiMapDecorator<K, V, TDecorated, TDecoratedInverse, TSubMap, TInverseMap, TKeySet, TEntrySet, TValueSet>
        implements SortedBidiMap<K, V, TSubMap, TInverseMap> {

    private static final long serialVersionUID = -2025553015999206418L;

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    public AbstractSortedBidiMapDecorator(final TDecorated map) {
        super(map);
    }

    protected abstract TSubMap decorateDerived(final TDecorated map);

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return decorated().valueComparator();
    }

    @Override
    public TSubMap subMap(final SortedMapRange<K> range) {
        return decorateDerived(range.applyToMap(decorated()));
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return decorated().getKeyRange();
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return decorated().getValueRange();
    }

    @Override
    public TKeySet sequencedKeySet() {
        return (TKeySet) decorated().sequencedKeySet();
    }

    @Override
    public final TKeySet keySet() {
        return sequencedKeySet();
    }

    @Override
    public SortedRangedSet<Entry<K, V>, ?> sequencedEntrySet() {
        return decorated().sequencedEntrySet();
    }

    @Override
    public final TEntrySet entrySet() {
        return (TEntrySet) sequencedEntrySet();
    }

    @Override
    public TValueSet sequencedValues() {
        return (TValueSet) decorated().sequencedValues();
    }

    @Override
    public final TValueSet values() {
        return sequencedValues();
    }
}
