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
package org.apache.commons.collections4;


import java.util.SequencedCollection;
import java.util.SequencedSet;

/**
 * Defines a map that maintains order and allows both forward and backward
 * iteration through that order.
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 *
 * @since 3.0
 */
public interface OrderedMap<K, V, TSubMap extends SequencedCommonsMap<K, V, ?>>
        extends IterableMap<K, V>, SequencedCommonsMap<K, V, TSubMap> {

    /**
     * Obtains an {@code OrderedMapIterator} over the map.
     * <p>
     * An ordered map iterator is an efficient way of iterating over maps
     * in both directions.
     *
     * @return a map iterator
     */
    @Override
    OrderedMapIterator<K, V> mapIterator();

    /**
     * Obtains an {@code OrderedMapIterator} over the map but which starts at the end.
     * Swaps meaning of next/previous so that algorithms can still just use next to process whole collection.
     * <p>
     * An ordered map iterator is an efficient way of iterating over maps in both directions.
     *
     * @return a map iterator
     * @see java.util.NavigableSet#descendingIterator
     */
    OrderedMapIterator<K, V> descendingMapIterator();

    /**
     * Gets the first key currently in this map.
     *
     * @return the first key currently in this map
     * @throws java.util.NoSuchElementException if this map is empty
     */
    default K firstKey() {
        return sequencedKeySet().getFirst();
    }

    /**
     * Gets the last key currently in this map.
     *
     * @return the last key currently in this map
     * @throws java.util.NoSuchElementException if this map is empty
     */
    default K lastKey() {
        return sequencedKeySet().getLast();
    }

    /**
     * Gets the next key after the one specified.
     * Result undefined if !containsKey(key).
     *
     * @param key  the key to search for next from
     * @return the next key, null if no match or at end
     */
    K nextKey(K key);

    /**
     * Gets the previous key before the one specified.
     * Result undefined if !containsKey(key).
     *
     * @param key  the key to search for previous from
     * @return the previous key, null if no match or at start
     */
    K previousKey(K key);

    @Override
    default SequencedSet<K> keySet() {
        return SequencedCommonsMap.super.keySet();
    }

    @Override
    default SequencedSet<Entry<K, V>> entrySet() {
        return SequencedCommonsMap.super.entrySet();
    }

    @Override
    default SequencedCollection<V> values() {
        return SequencedCommonsMap.super.values();
    }
}
