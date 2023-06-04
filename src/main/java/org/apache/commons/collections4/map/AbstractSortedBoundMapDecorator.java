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

import java.util.SortedMap;

import org.apache.commons.collections4.SortedBoundMap;
import org.apache.commons.collections4.SortedMapRange;


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
public abstract class AbstractSortedBoundMapDecorator<K, V>
        extends AbstractSortedMapDecorator<K, V>
        implements SortedBoundMap<K, V> {

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected AbstractSortedBoundMapDecorator() {
    }

    public AbstractSortedBoundMapDecorator(SortedBoundMap<K, V> map) {
        super(map);
    }

    @Override
    protected SortedBoundMap<K, V> decorated() {
        return (SortedBoundMap<K, V>) super.decorated();
    }

    @Override
    protected SortedMap<K, V> wrapMap(SortedMap<K, V> map) {
        throw new IllegalArgumentException();
    }

    protected abstract SortedBoundMap<K, V> wrapMap(SortedMap<K, V> map, SortedMapRange<K> range);

    @Override
    public SortedMapRange<K> getKeyRange() {
        return decorated().getKeyRange();
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return decorated().getValueRange();
    }

    @Override
    public SortedBoundMap<K, V> subMap(K fromKey, K toKey) {
        return wrapMap(decorated().subMap(fromKey, toKey), getKeyRange().sub(fromKey, toKey));
    }

    @Override
    public SortedBoundMap<K, V> headMap(K toKey) {
        return wrapMap(decorated().headMap(toKey), decorated().getKeyRange().head(toKey));
    }

    @Override
    public SortedBoundMap<K, V> tailMap(K fromKey) {
        return wrapMap(decorated().tailMap(fromKey), decorated().getKeyRange().tail(fromKey));
    }
}
