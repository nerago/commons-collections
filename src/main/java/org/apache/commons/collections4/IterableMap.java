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

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Defines a map that can be iterated directly without needing to create an entry set.
 * <p>
 * A map iterator is an efficient way of iterating over maps.
 * There is no need to access the entry set or use Map Entry objects.
 * </p>
 * <pre>
 * IterableMap&lt;String,Integer&gt; map = new HashedMap&lt;String,Integer&gt;();
 * MapIterator&lt;String,Integer&gt; it = map.mapIterator();
 * while (it.hasNext()) {
 *   String key = it.next();
 *   Integer value = it.getValue();
 *   it.setValue(value + 1);
 * }
 * </pre>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 *
 * @since 3.0
 */
public interface IterableMap<K, V> extends Map<K, V>, Put<K, V>, IterableGet<K, V> {
    /***
     * {@inheritDoc}
     * <p>
     * Overridden in IterableMap to use mapIterator and avoid creating entrySet unnecessarily.
     */
    @Override
    default void forEach(final BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        final MapIterator<K, V> iterator = mapIterator();
        while (iterator.hasNext()) {
            final K key;
            final V value;
            try {
                key = iterator.next();
                value = iterator.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(key, value);
        }
    }

    /***
     * {@inheritDoc}
     * <p>
     * Overridden in IterableMap to use mapIterator and avoid creating entrySet unnecessarily.
     */
    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        final MapIterator<K, V> iterator = mapIterator();
        while (iterator.hasNext()) {
            final K key;
            final V value;
            try {
                key = iterator.next();
                value = iterator.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            final V newValue = function.apply(key, value);

            try {
                iterator.setValue(newValue);
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    // TODO avoid this default, would be nice to integrate better
    @Override
    default boolean containsMapping(final Object key, final Object value) {
        return MapUtils.containsMapping(this, key, value);
    }

    @Override
    default boolean removeMapping(final Object key, final Object value) {
        return remove(key, value);
    }
}
