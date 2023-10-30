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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;
import org.apache.commons.collections4.set.UnmodifiableSet;

/**
 * Decorates another {@link BidiMap} to ensure it can't be altered.
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public final class UnmodifiableBidiMap<K, V>
        extends AbstractBidiMapDecorator<K, V,
            BidiMap<K, V, ?>,
            BidiMap<V, K, ?>,
            UnmodifiableBidiMap<V, K>,
            Set<K>, Set<Map.Entry<K, V>>, Set<V>>
        implements Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = -3563384200742281975L;

    /**
     * Factory method to create an unmodifiable map.
     * <p>
     * If the map passed in is already unmodifiable, it is returned.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map  the map to decorate, must not be null
     * @return an unmodifiable BidiMap
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> BidiMap<K, V, ?> unmodifiableBidiMap(final BidiMap<K, V, ?> map) {
        if (map instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final BidiMap<K, V, ?> tmpMap = (BidiMap<K, V, ?>) map;
            return tmpMap;
        }
        return new UnmodifiableBidiMap<>(map);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableBidiMap(final BidiMap<K, V, ?> map) {
        super(map);
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
    public MapIterator<K, V> mapIterator() {
        final MapIterator<K, V> it = decorated().mapIterator();
        return UnmodifiableMapIterator.unmodifiableMapIterator(it);
    }

    @Override
    protected UnmodifiableBidiMap<V, K> decorateInverse(final BidiMap<V, K, ?> inverse) {
        return new UnmodifiableBidiMap<>(inverse);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(decorated());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        setMap((BidiMap<K, V, ?>) in.readObject());
    }

}
