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
package org.apache.commons.collections4.bidimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedExtendedBidiMap;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIterator;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;
import org.apache.commons.collections4.set.UnmodifiableSet;

/**
 * Decorates another {@link SortedBidiMap} to ensure it can't be altered.
 * <p>
 * Attempts to modify it will result in an {@link UnsupportedOperationException}.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public final class UnmodifiableSortedBidiMap<K, V>
        extends AbstractSortedBidiMapDecorator<K, V> implements Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = 3795490687011556072L;

    /** The inverse unmodifiable map */
    private transient UnmodifiableSortedBidiMap<V, K> inverse;

    /**
     * Factory method to create an unmodifiable map.
     * <p>
     * If the map passed in is already unmodifiable, it is returned.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map  the map to decorate, must not be null
     * @return an unmodifiable SortedBidiMap
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> SortedExtendedBidiMap<K, V> unmodifiableSortedBidiMap(final SortedExtendedBidiMap<K, ? extends V> map) {
        if (map instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final SortedExtendedBidiMap<K, V> tmpMap = (SortedExtendedBidiMap<K, V>) map;
            return tmpMap;
        }
        return new UnmodifiableSortedBidiMap<>(map);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableSortedBidiMap(final SortedExtendedBidiMap<K, ? extends V> map) {
        super((SortedExtendedBidiMap<K, V>) map);
    }

    @Override
    protected SortedExtendedBidiMap<K, V> decorateDerived(final SortedExtendedBidiMap<K, V> map) {
        return new UnmodifiableSortedBidiMap<>(map);
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
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> set = super.entrySet();
        return UnmodifiableEntrySet.unmodifiableEntrySet(set);
    }

    @Override
    public Set<K> keySet() {
        final Set<K> set = super.keySet();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override
    public Set<V> values() {
        final Set<V> set = super.values();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override
    public K removeValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        final OrderedMapIterator<K, V> it = decorated().mapIterator();
        return UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it);
    }

    @Override
    public SortedExtendedBidiMap<V, K> inverseBidiMap() {
        if (inverse == null) {
            inverse = new UnmodifiableSortedBidiMap<>(decorated().inverseBidiMap());
            inverse.inverse = this;
        }
        return inverse;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(decorated());
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setMap((Map<K, V>) in.readObject());
    }
}
