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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.BoundedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.iterators.EntrySetMapIterator;
import org.apache.commons.collections4.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

import javax.management.openmbean.OpenDataException;

/**
 * Decorates another {@code Map} to fix the size, preventing add/remove.
 * <p>
 * Any action that would change the size of the map is disallowed.
 * The put method is allowed to change the value associated with an existing
 * key however.
 * </p>
 * <p>
 * If trying to remove or clear the map, an UnsupportedOperationException is
 * thrown. If trying to put a new mapping into the map, an
 * IllegalArgumentException is thrown. This is because the put method can
 * succeed if the mapping's key already exists in the map, so the put method
 * is not always unsupported.
 * </p>
 * <p>
 * <strong>Note that FixedSizeMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class may throw
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
public class FixedSizeMap<K, V>
        extends AbstractMapDecorator<K, V>
        implements BoundedMap<K, V>, Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 7450927208116179316L;

    /**
     * Factory method to create a fixed size map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new fixed size map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> FixedSizeMap<K, V> fixedSizeMap(final Map<K, V> map) {
        return new FixedSizeMap<>(map);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    protected FixedSizeMap(final Map<K, V> map) {
        super(map);
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     * @since 3.1
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(map);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     * @since 3.1
     */
    @SuppressWarnings("unchecked") // (1) should only fail if input stream is incorrect
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        map = (Map<K, V>) in.readObject(); // (1)
    }

    @Override
    public V put(final K key, final V value) {
        if (!map.containsKey(key)) {
            throwFixedSizeNewKey();
        }
        return map.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        for (final K key : mapToCopy.keySet()) {
            if (!containsKey(key)) {
                throwFixedSizeNewKey();
            }
        }
        map.putAll(mapToCopy);
    }

    @Override
    public void clear() {
        throwFixedSize();
    }

    @Override
    public V remove(final Object key) {
        return throwFixedSize();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> set = map.entrySet();
        // unmodifiable set will still allow modification via Map.Entry objects
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override
    public Set<K> keySet() {
        final Set<K> set = map.keySet();
        return UnmodifiableSet.unmodifiableSet(set);
    }

    @Override
    public Collection<V> values() {
        final Collection<V> coll = map.values();
        return UnmodifiableCollection.unmodifiableCollection(coll);
    }

    @Override
    public MapIterator<K, V> mapIterator() {
        // TODO add to fixes branch
        return UnmodifiableMapIterator.unmodifiableMapIterator(new EntrySetToMapIteratorAdapter<>(entrySet()));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return throwFixedSize();
    }

    @Override
    public boolean remove(Object key, Object value) {
        return throwFixedSize();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            if (oldValue != null) {
                return oldValue;
            }

            final V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                return throwFixedSizeComputeRemove();
            }
        } else {
            return throwFixedSizeNewKey();
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            if (oldValue == null) {
                return null;
            }

            final V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                return throwFixedSizeComputeRemove();
            }
        } else {
            return null;
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            final V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                return throwFixedSizeComputeRemove();
            }
        } else {
            final V newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
                throwFixedSizeNewKey();
            }
            return null;
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            final V newValue = remappingFunction.apply(oldValue, value);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                return throwFixedSizeComputeRemove();
            }
        } else {
            return throwFixedSizeNewKey();
        }
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public int maxSize() {
        return size();
    }

    private static <R> R throwFixedSize() {
        throw new UnsupportedOperationException("Map is fixed size");
    }

    private static <R> R throwFixedSizeNewKey() {
        throw new IllegalArgumentException("Cannot put new key/value pair - Map is fixed size");
    }

    private static <R> R throwFixedSizeComputeRemove() {
        throw new IllegalArgumentException("Cannot action compute removal - Map is fixed size");
    }
}
