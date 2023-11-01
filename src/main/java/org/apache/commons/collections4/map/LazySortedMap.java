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

import java.util.Comparator;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.SortedMap;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedMapUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.SortedMapOrderedMapIterator;

/**
 * Decorates another {@code SortedMap} to create objects in the map on demand.
 * <p>
 * When the {@link #get(Object)} method is called with a key that does not
 * exist in the map, the factory is used to create the object. The created
 * object will be added to the map using the requested key.
 * </p>
 * <p>
 * For instance:
 * </p>
 * <pre>
 * Factory&lt;Date&gt; factory = new Factory&lt;Date&gt;() {
 *     public Date create() {
 *         return new Date();
 *     }
 * }
 * SortedMap&lt;String, Date&gt; lazy =
 *     LazySortedMap.lazySortedMap(new HashMap&lt;String, Date&gt;(), factory);
 * Date date = lazy.get("NOW");
 * </pre>
 *
 * <p>
 * After the above code is executed, {@code date} will refer to
 * a new {@code Date} instance. Furthermore, that {@code Date}
 * instance is mapped to the "NOW" key in the map.
 * </p>
 * <p>
 * <strong>Note that LazySortedMap is not synchronized and is not thread-safe.</strong>
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
public class LazySortedMap<K, V> extends LazyMap<K, V> implements IterableSortedMap<K, V, LazySortedMap<K, V>> {

    /** Serialization version */
    private static final long serialVersionUID = 2715322183617658933L;
    private SortedMapRange<K> keyRange;

    /**
     * Factory method to create a lazily instantiated sorted map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @return a new lazy sorted map
     * @throws NullPointerException if map or factory is null
     * @since 4.0
     */
    public static <K, V> LazySortedMap<K, V> lazySortedMap(final SortedMap<K, V> map,
                                                           final Factory<? extends V> factory) {
        return new LazySortedMap<>(map, factory);
    }

    /**
     * Factory method to create a lazily instantiated sorted map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @return a new lazy sorted map
     * @throws NullPointerException if map or factory is null
     * @since 4.0
     */
    public static <K, V> LazySortedMap<K, V> lazySortedMap(final SortedMap<K, V> map,
                                                           final Transformer<? super K, ? extends V> factory) {
        return new LazySortedMap<>(map, factory);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @throws NullPointerException if map or factory is null
     */
    protected LazySortedMap(final SortedMap<K, V> map, final Factory<? extends V> factory) {
        super(map, factory);
        this.keyRange = SortedMapRange.full(map.comparator());
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @throws NullPointerException if map or factory is null
     */
    protected LazySortedMap(final SortedMap<K, V> map, final Transformer<? super K, ? extends V> factory) {
        super(map, factory);
        this.keyRange = SortedMapRange.full(map.comparator());
    }

    protected LazySortedMap(final SortedMap<K, V> map, final Transformer<? super K, ? extends V> factory, final SortedMapRange<K> keyRange) {
        super(map, factory);
        this.keyRange = keyRange;
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
    public K firstKey() {
        return getSortedMap().firstKey();
    }

    @Override
    public K lastKey() {
        return getSortedMap().lastKey();
    }

    @Override
    public K nextKey(final K key) {
        return SortedMapUtils.nextKey(getSortedMap(), key);
    }

    @Override
    public K previousKey(final K key) {
        return SortedMapUtils.previousKey(getSortedMap(), key);
    }

    @Override
    public Comparator<? super K> comparator() {
        return getSortedMap().comparator();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return SortedMapOrderedMapIterator.sortedMapIterator(getSortedMap());
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return null;
    }

    @Override
    public LazySortedMap<K, V> subMap(final SortedMapRange<K> range) {
        return new LazySortedMap<>(range.applyToMap(getSortedMap()), factory, range);
    }
    
    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public LazySortedMap<K, V> reversed() {
        return new LazySortedMap<>(getSortedMap().reversed(), factory);
    }

    @Override
    public SequencedSet<K> sequencedKeySet() {
        return null;
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
        return null;
    }

    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return null;
    }


}
