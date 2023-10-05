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

import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("MissingJavadoc")
public interface IterableExtendedMap<K, V> extends IterableMap<K, V> {

    MapSpliterator<K, V> mapSpliterator();

    Iterator<Entry<K, V>> entryIterator();

    @Override
    V getOrDefault(Object key, V defaultValue);

    boolean containsEntry(Object key, Object value);

    @Override
    V put(K key, V value);

    @Override
    V putIfAbsent(K key, V value);

    @Override
    V replace(K key, V value);

    @Override
    boolean replace(K key, V oldValue, V newValue);

    @Override
    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    @Override
    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    @Override
    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    @Override
    V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);

    boolean removeAsBoolean(Object key);

    @Override
    boolean remove(Object key, Object value);

    boolean removeValueAsBoolean(Object value);

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    String toString();
}
