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

import org.apache.commons.collections4.NavigableRangedMap;
import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;

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
public abstract class AbstractNavigableMapDecorator<K, V,
                TDecorated extends NavigableMap<K, V>,
                TSubMap extends AbstractNavigableMapDecorator<K, V, TDecorated, TSubMap, ?, ?, ?>,
                TKeySet extends NavigableRangedSet<K, ?>,
                TEntrySet extends NavigableRangedSet<Map.Entry<K, V>, ?>,
                TValueSet extends SequencedCollection<V>>
        extends AbstractSortedMapDecorator<K, V, TDecorated, TSubMap, TKeySet, TEntrySet, TValueSet>
        implements NavigableRangedMap<K, V, TSubMap> {

    private static final long serialVersionUID = 7643981107509930313L;

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
    protected AbstractNavigableMapDecorator(final TDecorated map, final SortedMapRange<K> keyRange) {
        super(map, keyRange);
    }

    @Override
    public K nextKey(final K key) {
        return decorated().higherKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return decorated().lowerKey(key);
    }

    @Override
    public Entry<K, V> lowerEntry(final K key) {
        return decorated().lowerEntry(key);
    }

    @Override
    public K lowerKey(final K key) {
        return decorated().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(final K key) {
        return decorated().floorEntry(key);
    }

    @Override
    public K floorKey(final K key) {
        return decorated().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(final K key) {
        return decorated().ceilingEntry(key);
    }

    @Override
    public K ceilingKey(final K key) {
        return decorated().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(final K key) {
        return decorated().higherEntry(key);
    }

    @Override
    public K higherKey(final K key) {
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
    public NavigableSet<K> navigableKeySet() {
        return decorated().navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return decorated().descendingKeySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public NavigableMap<K, V> descendingMap() {
        return decorateDerived((TDecorated) decorated().descendingMap(), getKeyRange().reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubMap subMap(final SortedMapRange<K> range) {
        return decorateDerived((TDecorated) range.applyToNavigableMap(decorated()), range);
    }
}
