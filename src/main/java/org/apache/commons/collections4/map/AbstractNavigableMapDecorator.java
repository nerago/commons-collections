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

import org.apache.commons.collections4.NavigableBoundMap;
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
public abstract class AbstractNavigableMapDecorator<K, V> extends AbstractSortedMapDecorator<K, V> implements
        NavigableBoundMap<K, V> {

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
    public AbstractNavigableMapDecorator(final NavigableMap<K, V> map) {
        super(map);
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    @Override
    protected NavigableMap<K, V> decorated() {
        return (NavigableMap<K, V>) super.decorated();
    }


    @Override
    protected SortedMap<K, V> wrapMap(SortedMap<K, V> map) {
        throw new IllegalArgumentException();
    }

    protected abstract NavigableBoundMap<K, V> wrapMap(SortedMap<K, V> map, SortedMapRange<K> range);

    @Override
    public K nextKey(K key) {
        return decorated().higherKey(key);
    }

    @Override
    public K previousKey(K key) {
        return decorated().lowerKey(key);
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return decorated().lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return decorated().lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return decorated().floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return decorated().floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return decorated().ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return decorated().ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return decorated().higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
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
    
    @Override
    public NavigableMap<K, V> descendingMap() {
        return wrapMap(decorated().descendingMap(), getKeyRange().reversed());
    }
    
    @Override
    public NavigableBoundMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return wrapMap(decorated().subMap(fromKey, fromInclusive, toKey, toInclusive),
                getKeyRange().sub(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    public NavigableBoundMap<K, V> headMap(K toKey, boolean inclusive) {
        return wrapMap(decorated().headMap(toKey, inclusive), getKeyRange().head(toKey, inclusive));
    }

    @Override
    public NavigableBoundMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return wrapMap(decorated().tailMap(fromKey, inclusive), getKeyRange().tail(fromKey, inclusive));
    }

    @Override
    public NavigableBoundMap<K, V> subMap(K fromKey, K toKey) {
        return wrapMap(decorated().subMap(fromKey, true, toKey, false),
                getKeyRange().sub(fromKey, true, toKey, false));
    }

    @Override
    public NavigableBoundMap<K, V> headMap(K toKey) {
        return wrapMap(decorated().headMap(toKey, false), getKeyRange().head(toKey, false));
    }

    @Override
    public NavigableBoundMap<K, V> tailMap(K fromKey) {
        return wrapMap(decorated().tailMap(fromKey, true), getKeyRange().tail(fromKey, true));
    }
}
