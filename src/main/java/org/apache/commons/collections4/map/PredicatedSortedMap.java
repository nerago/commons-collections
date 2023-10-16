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
import java.util.SortedMap;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedMapUtils;

/**
 * Decorates another {@code SortedMap} to validate that additions
 * match a specified predicate.
 * <p>
 * This map exists to provide validation for the decorated map.
 * It is normally created to decorate an empty map.
 * If an object cannot be added to the map, an IllegalArgumentException is thrown.
 * </p>
 * <p>
 * One usage would be to ensure that no null keys are added to the map.
 * </p>
 * <pre>
 *   SortedMap map =
 *     PredicatedSortedMap.predicatedSortedMap(new TreeMap(),
 *                                             NotNullPredicate.notNullPredicate(),
 *                                             null);
 * </pre>
 * <p>
 * <strong>Note that PredicatedSortedMap is not synchronized and is not thread-safe.</strong>
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
public class PredicatedSortedMap<K, V> extends PredicatedMap<K, V, SortedMap<K, V>>
                                       implements IterableSortedMap<K, V, PredicatedSortedMap<K, V>> {

    /** Serialization version */
    private static final long serialVersionUID = 3359846175935304332L;
    private final SortedMapRange<K> keyRange;

    /**
     * Factory method to create a predicated (validating) sorted map.
     * <p>
     * If there are any elements already in the list being decorated, they
     * are validated.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param keyPredicate  the predicate to validate the keys, null means no check
     * @param valuePredicate  the predicate to validate to values, null means no check
     * @return a new predicated sorted map
     * @throws NullPointerException if the map is null
     * @since 4.0
     */
    public static <K, V> PredicatedSortedMap<K, V> predicatedSortedMap(final SortedMap<K, V> map,
            final Predicate<? super K> keyPredicate, final Predicate<? super V> valuePredicate) {
        return new PredicatedSortedMap<>(map, keyPredicate, valuePredicate, SortedMapRange.full(map.comparator()));
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param keyPredicate  the predicate to validate the keys, null means no check
     * @param valuePredicate  the predicate to validate to values, null means no check
     * @throws NullPointerException if the map is null
     */
    protected PredicatedSortedMap(final SortedMap<K, V> map, final Predicate<? super K> keyPredicate,
                                  final Predicate<? super V> valuePredicate, final SortedMapRange<K> keyRange) {
        super(map, keyPredicate, valuePredicate);
        this.keyRange = keyRange;
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
    public K nextKey(final K key) {
        return SortedMapUtils.nextKey(decorated(), key);
    }

    @Override
    public K previousKey(final K key) {
        return SortedMapUtils.previousKey(decorated(), key);
    }

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    @Override
    public PredicatedSortedMap<K, V> subMap(final SortedMapRange<K> range) {
        return new PredicatedSortedMap<>(range.applyToMap(decorated()), keyPredicate, valuePredicate, range);
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return (OrderedMapIterator<K, V>) super.mapIterator();
    }
}
