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

import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to an OrderedBidiMap via decoration.
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
public abstract class AbstractOrderedBidiMapDecorator<K, V,
            Decorated extends OrderedBidiMap<K, V, Decorated, DecoratedInverse>,
            DecoratedInverse extends OrderedBidiMap<V, K, DecoratedInverse, Decorated>,
            RegularMap extends AbstractOrderedBidiMapDecorator<K, V, Decorated, DecoratedInverse, RegularMap, InverseMap>,
            InverseMap extends AbstractOrderedBidiMapDecorator<V, K, DecoratedInverse, Decorated, InverseMap, RegularMap>>
        extends AbstractBidiMapDecorator<K, V, Decorated, DecoratedInverse, RegularMap, InverseMap>
        implements OrderedBidiMap<K, V, RegularMap, InverseMap> {

    private static final long serialVersionUID = 7010751296610809092L;

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    protected AbstractOrderedBidiMapDecorator(final Decorated map) {
        super(map);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return decorated().mapIterator();
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

}
