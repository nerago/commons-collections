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
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.SortedMap;

import org.apache.commons.collections4.IterableGet;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedMapUtils;
import org.apache.commons.collections4.Transformer;

/**
 * Decorates another {@code SortedMap } to transform objects that are added.
 * <p>
 * The Map put methods and Map.Entry setValue method are affected by this class.
 * Thus objects must be removed or searched for using their transformed form.
 * For example, if the transformation converts Strings to Integers, you must
 * use the Integer form to remove objects.
 * </p>
 * <p>
 * <strong>Note that TransformedSortedMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedSortedMap}. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 * </p>
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class TransformedSortedMap<K, V>
        extends TransformedMap<K, V>
        implements IterableSortedMap<K, V, TransformedSortedMap<K, V>> {

    /** Serialization version */
    private static final long serialVersionUID = -8751771676410385778L;
    private final SortedMapRange<K> keyRange;

    /**
     * Factory method to create a transforming sorted map.
     * <p>
     * If there are any elements already in the map being decorated, they are NOT transformed.
     * Contrast this with {@link #transformedSortedMap(SortedMap, Transformer, Transformer)}.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param keyTransformer  the predicate to validate the keys, null means no transformation
     * @param valueTransformer  the predicate to validate to values, null means no transformation
     * @return a new transformed sorted map
     * @throws NullPointerException if the map is null
     * @since 4.0
     */
    public static <K, V> TransformedSortedMap<K, V> transformingSortedMap(final SortedMap<K, V> map,
            final Transformer<? super K, ? extends K> keyTransformer,
            final Transformer<? super V, ? extends V> valueTransformer) {
        return new TransformedSortedMap<>(map, keyTransformer, valueTransformer, SortedMapRange.full(map.comparator()));
    }

    /**
     * Factory method to create a transforming sorted map that will transform
     * existing contents of the specified map.
     * <p>
     * If there are any elements already in the map being decorated, they
     * will be transformed by this method.
     * Contrast this with {@link #transformingSortedMap(SortedMap, Transformer, Transformer)}.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param keyTransformer  the transformer to use for key conversion, null means no transformation
     * @param valueTransformer  the transformer to use for value conversion, null means no transformation
     * @return a new transformed sorted map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> TransformedSortedMap<K, V> transformedSortedMap(final SortedMap<K, V> map,
            final Transformer<? super K, ? extends K> keyTransformer,
            final Transformer<? super V, ? extends V> valueTransformer) {

        final TransformedSortedMap<K, V> decorated =
                new TransformedSortedMap<>(map, keyTransformer, valueTransformer, SortedMapRange.full(map.comparator()));
        if (!map.isEmpty()) {
            final Map<K, V> transformed = decorated.transformMap(map);
            decorated.clear();
            decorated.decorated().putAll(transformed);  // avoids double transformation
        }
        return decorated;
    }

    /**
     * Constructor that wraps (not copies).
     * <p>
     * If there are any elements already in the collection being decorated, they
     * are NOT transformed.</p>
     *
     * @param map  the map to decorate, must not be null
     * @param keyTransformer  the predicate to validate the keys, null means no transformation
     * @param valueTransformer  the predicate to validate to values, null means no transformation
     * @throws NullPointerException if the map is null
     */
    protected TransformedSortedMap(final SortedMap<K, V> map,
            final Transformer<? super K, ? extends K> keyTransformer,
            final Transformer<? super V, ? extends V> valueTransformer,
            final SortedMapRange<K> keyRange) {
        super(map, keyTransformer, valueTransformer);
        this.keyRange = keyRange;
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    protected SortedMap<K, V> getSortedMap() {
        return (SortedMap<K, V>) map;
    }

    @Override
    public K firstKey() {
        return getSortedMap().firstKey();
    }

    @Override
    public K lastKey() {
        return getSortedMap().lastKey();
    }

    @Override
    public K nextKey(final K key) {
        return SortedMapUtils.nextKey(getSortedMap(), key);
    }

    @Override
    public K previousKey(final K key) {
        return SortedMapUtils.previousKey(getSortedMap(), key);
    }

    @Override
    public Comparator<? super K> comparator() {
        return getSortedMap().comparator();
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public TransformedSortedMap<K, V> subMap(final SortedMapRange<K> range) {
        return new TransformedSortedMap<>(range.applyToMap(getSortedMap()), keyTransformer, valueTransformer, range);
    }

    @Override
    public TransformedSortedMap<K, V> reversed() {
        return new TransformedSortedMap<>(getSortedMap().reversed(), keyTransformer, valueTransformer, keyRange.reversed());
    }

    @Override
    public SequencedSet<K> keySet() {
        return getSortedMap().sequencedKeySet();
    }

    @Override
    public SequencedSet<Entry<K, V>> entrySet() {
        return getSortedMap().sequencedEntrySet();
    }

    @Override
    public SequencedCollection<V> values() {
        return getSortedMap().sequencedValues();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return (OrderedMapIterator<K, V>) super.mapIterator();
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        if (map instanceof OrderedMap<K, V>) {
            return ((OrderedMap<K, V>) map).descendingMapIterator();
        } else {
            SortedMap<K, V> reverse = getSortedMap().reversed();
            if (reverse instanceof IterableGet) {
                final MapIterator<K, V> mapIterator = ((IterableGet<K, V>) reverse).mapIterator();
                if (mapIterator instanceof OrderedMapIterator<K, V>) {
                    return (OrderedMapIterator<K, V>) mapIterator;
                }
            }

            return SortedMapUtils.sortedMapIterator(reverse);

//            return getSortedMap().reversed().mapIterator();
        }
    }
}
