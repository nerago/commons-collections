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
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedExtendedBidiMap;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedMapUtils;
import org.apache.commons.collections4.iterators.SortedMapOrderedMapIterator;

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
public abstract class AbstractSortedMapDecorator<K, V,
                Decorated extends SortedMap<K, V>,
                SubMap extends IterableSortedMap<K, V, SubMap>>
        extends AbstractMapDecorator<K, V, Decorated>
        implements IterableSortedMap<K, V, SubMap> {

    private static final long serialVersionUID = 4710068155190191469L;

    /** The map to decorate */
    transient SortedMapRange<K> keyRange;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractSortedMapDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public AbstractSortedMapDecorator(final Decorated map) {
        super(map);
        this.keyRange = SortedMapRange.full(map.comparator());
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map      the map to decorate, must not be null
     * @param keyRange the range of keys accepted by the map
     * @throws NullPointerException if the map is null
     */
    public AbstractSortedMapDecorator(final Decorated map, final SortedMapRange<K> keyRange) {
        super(map);
        this.keyRange = Objects.requireNonNull(keyRange);
    }

    protected abstract SubMap decorateDerived(final Decorated subMap, final SortedMapRange<K> keyRange);

    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
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
    public SubMap subMap(final SortedMapRange<K> range) {
        return decorateDerived(range.applyToMap(decorated()), range);
    }

    @Override
    public K previousKey(final K key) {
        return SortedMapUtils.previousKey(decorated(), key);
    }

    @Override
    public K nextKey(final K key) {
        return SortedMapUtils.nextKey(this, key);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return SortedMapOrderedMapIterator.sortedMapIterator(decorated());
    }

}
