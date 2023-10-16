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

import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.AbstractMapDecorator;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to a BidiMap via decoration.
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
public abstract class AbstractBidiMapDecorator<K, V, Decorated extends BidiMap<K, V, ?>,
                                                     DecoratedInverse extends BidiMap<V, K, ?>,
                                                     InverseMap extends AbstractBidiMapDecorator<V, K, ?, ?, ?>>
        extends AbstractMapDecorator<K, V, Decorated>
        implements BidiMap<K, V, InverseMap> {

    private static final long serialVersionUID = -3483039813600794480L;
    private InverseMap inverse;

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    protected AbstractBidiMapDecorator(final Decorated map) {
        super(map);
    }

    @Override
    public MapIterator<K, V> mapIterator() {
        return decorated().mapIterator();
    }

    @Override
    public K getKey(final Object value) {
        return decorated().getKey(value);
    }

    @Override
    public K getKeyOrDefault(final Object value, final K defaultKey) {
        return decorated().getKeyOrDefault(value, defaultKey);
    }

    @Override
    public K removeValue(final Object value) {
        return decorated().removeValue(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final InverseMap inverseBidiMap() {
        if (inverse == null) {
            inverse = decorateInverse((DecoratedInverse) decorated().inverseBidiMap());
        }
        return inverse;
    }

    protected abstract InverseMap decorateInverse(DecoratedInverse inverse);

    @Override
    public Set<V> values() {
        return decorated().values();
    }

}
