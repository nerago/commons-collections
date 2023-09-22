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

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedExtendedBidiMap;
import org.apache.commons.collections4.map.AbstractIterableMapAlternate;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

public abstract class AbstractExtendedBidiMap<K, V> extends AbstractIterableMapAlternate<K, V> implements SortedExtendedBidiMap<K, V> {
    private static final long serialVersionUID = -9181666289732043651L;

    @Override
    public final K getKey(final Object value) {
        return getKeyOrDefault(value, null);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return (OrderedMapIterator<K, V>) super.mapIterator();
    }

    @Override
    protected Set<V> createValuesCollection() {
        return new AbsExMapValues();
    }

    @Override
    public Set<V> values() {
        return (Set<V>) super.values();
    }

    protected final class AbsExMapValues extends AbstractSet<V> {
        @Override
        public Iterator<V> iterator() {
            return new AbsIterMapValueIterator<>(mapIterator());
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> v);
        }

        @Override
        public int size() {
            return AbstractExtendedBidiMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractExtendedBidiMap.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractExtendedBidiMap.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractExtendedBidiMap.this.removeValueAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractExtendedBidiMap.this.clear();
        }
    }
}
