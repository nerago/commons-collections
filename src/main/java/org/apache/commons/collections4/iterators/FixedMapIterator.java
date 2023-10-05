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
package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.MapIterator;

import java.util.Objects;

/**
 * Decorates a map iterator such that it cannot be modified in ways that change collection size.
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 * @since X.X
 */
public class FixedMapIterator<K, V> extends AbstractMapIteratorDecorator<K, V> {

    /**
     * Decorates the specified iterator such that it cannot be modified in ways that change collection size.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param iterator  the iterator to decorate
     * @return a new unmodifiable map iterator
     * @throws NullPointerException if the iterator is null
     */
    public static <K, V> MapIterator<K, V> fixedMapIterator(
            final MapIterator<K, V> iterator) {
        Objects.requireNonNull(iterator, "iterator");
        if (iterator instanceof FixedMapIterator) {
            return iterator;
        }
        return new FixedMapIterator<>(iterator);
    }

    protected FixedMapIterator(final MapIterator<K, V> iterator) {
        super(iterator);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

}
