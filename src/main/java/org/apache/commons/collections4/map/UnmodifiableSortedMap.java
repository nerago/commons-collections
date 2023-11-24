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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.collection.UnmodifiableSequencedCollection;
import org.apache.commons.collections4.set.UnmodifiableSequencedSet;

/**
 * Decorates another {@code SortedMap} to ensure it can't be altered.
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 * </p>
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public final class UnmodifiableSortedMap<K, V>
        extends AbstractSortedMapDecorator<K, V, SortedMap<K, V>, UnmodifiableSortedMap<K, V>,
            SequencedCommonsSet<K>, SequencedCommonsSet<Map.Entry<K, V>>, SequencedCommonsCollection<V>>
        implements Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = 5805344239827376360L;

    /**
     * Factory method to create an unmodifiable sorted map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new unmodifiable sorted map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> IterableSortedMap<K, V, ?> unmodifiableSortedMap(final SortedMap<K, V> map) {
        return new UnmodifiableSortedMap<>(map, SortedMapRange.full(map.comparator()));
    }

    /**
     * Factory method to create an unmodifiable sorted iterable map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new unmodifiable sorted map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> IterableSortedMap<K, V, ?> unmodifiableSortedMap(final IterableSortedMap<K, V, ?> map) {
        if (map instanceof Unmodifiable) {
            return map;
        }
        return new UnmodifiableSortedMap<>(map, map.getKeyRange());
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map      the map to decorate, must not be null
     * @param keyRange
     * @throws NullPointerException if map is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableSortedMap(final SortedMap<K, ? extends V> map, final SortedMapRange<K> keyRange) {
        super((SortedMap<K, V>) map, keyRange);
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
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SequencedCommonsSet<Entry<K, V>> entrySet() {
        return UnmodifiableSequencedEntrySet.unmodifiableEntrySet(decorated().sequencedEntrySet());
    }

    @Override
    public SequencedCommonsSet<K> keySet() {
        return UnmodifiableSequencedSet.unmodifiableSequencedSet(decorated().sequencedKeySet());
    }

    @Override
    public SequencedCommonsCollection<V> values() {
        return UnmodifiableSequencedCollection.unmodifiableSequencedCollection(decorated().sequencedValues());
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
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    @Override
    protected UnmodifiableSortedMap<K, V> decorateDerived(final SortedMap<K, V> subMap, final SortedMapRange<K> keyRange) {
        return new UnmodifiableSortedMap<>(subMap, keyRange);
    }

}
