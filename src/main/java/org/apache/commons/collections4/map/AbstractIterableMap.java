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

import org.apache.commons.collections4.IterableGet;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;

import java.util.Iterator;
import java.util.Map;

/**
 * Provide a basic {@link IterableMap} implementation.
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 4.0
 */
public abstract class AbstractIterableMap<K, V> implements IterableMap<K, V> {

    private static final long serialVersionUID = -1765018229420704832L;

    /**
     * {@inheritDoc}
     */
    @Override
    public MapIterator<K, V> mapIterator() {
        return new EntrySetToMapIteratorAdapter<>(entrySet());
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        if (mapToCopy instanceof IterableGet) {
            final IterableGet<? extends K, ? extends V> iterableMap = (IterableGet<? extends K, ? extends V>) mapToCopy;
            final MapIterator<? extends K, ? extends V> mapIterator = iterableMap.mapIterator();
            while (mapIterator.hasNext()) {
                final K key = mapIterator.next();
                final V value = mapIterator.getValue();
                put(key, value);
            }
        } else {
            final Iterator<? extends Entry<? extends K, ? extends V>> iterator = mapToCopy.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<? extends K, ? extends V> entry = iterator.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }
}
