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

import java.util.Comparator;

import org.apache.commons.collections4.SortedExtendedBidiMap;
import org.apache.commons.collections4.SortedMapRange;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to a SortedBidiMap via decoration.
 * <p>
 * Methods are forwarded directly to the decorated map.
 * </p>
 * <p>
 * This implementation does not perform any special processing with the map views.
 * Instead it simply returns the inverse from the wrapped map. This may be
 * undesirable, for example if you are trying to write a validating implementation
 * it would provide a loophole around the validation.
 * But, you might want that loophole, so this class is kept simple.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public abstract class AbstractSortedBidiMapDecorator<K, V,
            Decorated extends SortedExtendedBidiMap<K, V, Decorated, Decorated, DecoratedInverse>,
            DecoratedInverse extends SortedExtendedBidiMap<V, K, DecoratedInverse, DecoratedInverse, Decorated>,
            SubMap extends AbstractSortedBidiMapDecorator<K, V, Decorated, DecoratedInverse, SubMap, SubMap, ?>,
            RegularMap extends AbstractSortedBidiMapDecorator<K, V, Decorated, DecoratedInverse, SubMap, RegularMap, InverseMap>,
            InverseMap extends AbstractSortedBidiMapDecorator<V, K, DecoratedInverse, Decorated, ?, InverseMap, RegularMap>>
        extends AbstractOrderedBidiMapDecorator<K, V, Decorated, DecoratedInverse, RegularMap, InverseMap>
        implements SortedExtendedBidiMap<K, V, SubMap, RegularMap, InverseMap> {

    private static final long serialVersionUID = -2025553015999206418L;

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    public AbstractSortedBidiMapDecorator(final Decorated map) {
        super(map);
    }

    protected abstract SubMap decorateDerived(final Decorated map);

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return decorated().valueComparator();
    }

    @Override
    public SubMap subMap(final SortedMapRange<K> range) {
        return decorateDerived(range.applyToMap(decorated()));
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return decorated().getKeyRange();
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return decorated().getValueRange();
    }
}
