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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.BoundedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.iterators.FixedOrderedMapIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

/**
 * Decorates another {@code SortedMap} to fix the size blocking add/remove.
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
 * <strong>Note that FixedSizeSortedMap is not synchronized and is not thread-safe.</strong>
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
public class FixedSizeSortedMap<K, V>
        extends AbstractSortedMapDecorator<K, V, SortedMap<K, V>, FixedSizeSortedMap<K, V>>
        implements BoundedMap<K, V> {

    /** Serialization version */
    private static final long serialVersionUID = 3126019624511683653L;

    /**
     * Factory method to create a fixed size sorted map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @return a new fixed size sorted map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> FixedSizeSortedMap<K, V> fixedSizeSortedMap(final SortedMap<K, V> map) {
        return new FixedSizeSortedMap<>(map, SortedMapRange.full(map.comparator()));
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    protected FixedSizeSortedMap(final SortedMap<K, V> map, final SortedMapRange<K> keyRange) {
        super(map, keyRange);
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    protected SortedMap<K, V> getSortedMap() {
        return (SortedMap<K, V>) map;
    }

    @Override
    protected FixedSizeSortedMap<K, V> decorateDerived(final SortedMap<K, V> subMap, final SortedMapRange<K> keyRange) {
        return new FixedSizeSortedMap<>(subMap, keyRange);
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(map);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked") // (1) should only fail if input stream is incorrect
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        map = (SortedMap<K, V>) in.readObject(); // (1)
    }

    @Override
    public V put(final K key, final V value) {
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_NEW_KEY);
        }
        return map.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        if (!CollectionUtils.isSubCollection(mapToCopy.keySet(), keySet())) {
            throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_NEW_KEY);
        }
        map.putAll(mapToCopy);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
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
                throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_REMOVE_COMPUTE);
            }
        } else {
            throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_NEW_KEY);
        }
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException(FixedSizeMap.EXCEPTION_FIXED);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(FixedSizeMap.EXCEPTION_FIXED);
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException(FixedSizeMap.EXCEPTION_FIXED);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException(FixedSizeMap.EXCEPTION_FIXED);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return FixedOrderedMapIterator.fixedOrderedMapIterator(super.mapIterator());
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return UnmodifiableSet.unmodifiableSet(map.entrySet());
    }

    @Override
    public Set<K> keySet() {
        return UnmodifiableSet.unmodifiableSet(map.keySet());
    }

    @Override
    public Collection<V> values() {
        return UnmodifiableCollection.unmodifiableCollection(map.values());
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public int maxSize() {
        return size();
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
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
                throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_REMOVE_COMPUTE);
            }
        } else {
            return null;
        }
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            final V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_REMOVE_COMPUTE);
            }
        } else {
            final V newValue = remappingFunction.apply(key, null);
            if (newValue != null) {
                throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_NEW_KEY);
            }
            return null;
        }
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        if (map.containsKey(key)) {
            final V oldValue = get(key);
            final V newValue = remappingFunction.apply(oldValue, value);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_REMOVE_COMPUTE);
            }
        } else {
            throw new IllegalArgumentException(FixedSizeMap.EXCEPTION_NEW_KEY);
        }
    }
}
