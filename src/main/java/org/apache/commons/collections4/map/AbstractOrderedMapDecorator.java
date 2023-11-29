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

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SequencedCommonsCollection;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to an OrderedMap via decoration.
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
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public abstract class AbstractOrderedMapDecorator<K, V,
            TDecorated extends OrderedMap<K, V>,
            TSubMap extends OrderedMap<K, V>,
            TKeySet extends SequencedSet<K>,
            TEntrySet extends SequencedSet<Map.Entry<K, V>>,
            TValueSet extends SequencedCommonsCollection<V>>
        extends AbstractMapDecorator<K, V, TDecorated, TKeySet, TEntrySet, TValueSet>
        implements OrderedMap<K, V> {

    private static final long serialVersionUID = 6964783574989279065L;
    private transient TSubMap reverse;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractOrderedMapDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public AbstractOrderedMapDecorator(final TDecorated map) {
        super(map);
    }

    protected AbstractOrderedMapDecorator(final TDecorated map, final TSubMap reverse) {
        super(map);
        this.reverse = reverse;
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
        return decorated().nextKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return decorated().previousKey(key);
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
    public V putFirst(final K k, final V v) {
        return decorated().putFirst(k, v);
    }

    @Override
    public V putLast(final K k, final V v) {
        return decorated().putLast(k, v);
    }

    @Override
    public TKeySet keySet() {
        return (TKeySet) decorated().sequencedKeySet();
    }

    @Override
    public TEntrySet entrySet() {
        return (TEntrySet) decorated().sequencedEntrySet();
    }

    @Override
    public TValueSet values() {
        return (TValueSet) decorated().sequencedValues();
    }

    @Override
    public final TSubMap reversed() {
        if (reverse == null) {
            reverse = createReverse();
        }
        return reverse;
    }

    protected abstract TSubMap createReverse();

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return decorated().mapIterator();
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return decorated().descendingMapIterator();
    }
}
