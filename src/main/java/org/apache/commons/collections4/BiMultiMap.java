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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public interface BiMultiMap<K, V> extends Serializable {
    // Query operations

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    boolean containsValue(V value);

    boolean containsMapping(K key, V value);

    Set<V> getValues(K key);

    Set<K> getKeys(V value);

    // Modification operations

    boolean add(K key, V value);

    boolean addAll(K key, Iterable<? extends V> values);

    boolean addAll(Iterable<? extends K> keys, V value);

    boolean addAll(Map<? extends K, ? extends V> map);

    boolean addAll(IterableMap<? extends K, ? extends V> map);

    boolean addAll(BiMultiMap<? extends K, ? extends V> map);

    boolean addAll(MultiValuedMap<? extends K, ? extends V> map);

    Set<V> removeKey(K key);

    Set<K> removeValue(V value);

    boolean removeAllKeys(Collection<K> collection);

    boolean removeAllValues(Collection<V> collection);

    boolean removeMapping(K key, V value);

    void clear();

    // Views

    Collection<Entry<K, V>> entrySet();

    Set<K> keySet();

    MultiSet<K> keyMultiSet();

    MultiValuedMap<V, K> keyMultiMap();

    Set<V> valueSet();

    MultiSet<V> valueMultiSet();

    MultiValuedMap<K, V> valueMultiMap();

    BiMultiMap<V, K> inverseBiMultiMap();

    // Iterators

    MapIterator<K, V> mapIteratorEntries();

    MapIterator<K, Set<V>> mapIteratorKeys();

    MapIterator<V, Set<K>> mapIteratorValues();

    MapSpliterator<K, V> mapSpliteratorEntries();

}
