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

import java.util.Iterator;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapUtils;
import org.apache.commons.collections4.iterators.AbstractMapIteratorAdapter;
import org.apache.commons.collections4.iterators.MapIteratorToEntryAdapter;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.set.ReverseSequencedSet;

public abstract class AbstractEntrySetSequencedDecorator<K, V>
        extends AbstractEntrySetDecorator<K, V> implements SequencedCommonsSet<Map.Entry<K, V>> {
    private final SequencedMap<K, V> map;

    protected AbstractEntrySetSequencedDecorator(final SequencedMap<K, V> map) {
        super(map.sequencedEntrySet());
        this.map = map;
    }

    protected abstract Map.Entry<K, V> wrapEntry(final K key, final V value);

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return super.iterator();
    }

    @Override
    public Iterator<Map.Entry<K, V>> descendingIterator() {
        final OrderedMapIterator<K, V> mapIterator = SortedMapUtils.sortedMapIteratorDescending(map);
        return new MapIteratorWrappedEntryAdapter(mapIterator);
    }

    @Override
    public SequencedCommonsSet<Map.Entry<K, V>> reversed() {
        return new ReverseSequencedSet<>(this);
    }

    protected class MapIteratorWrappedEntryAdapter extends AbstractMapIteratorAdapter<K, V, Map.Entry<K, V>> {
        protected MapIteratorWrappedEntryAdapter(final MapIterator<K, V> iterator) {
            super(iterator);
        }

        @Override
        protected Map.Entry<K, V> transform(final K key, final V value) {
            return wrapEntry(key, value);
        }
    }

    public static class Wrapper<K, V> extends AbstractEntrySetSequencedDecorator<K, V> {
        private static final long serialVersionUID = 5401035603921603795L;

        private final Function<Map.Entry<K, V>, Map.Entry<K, V>> entryWrapper;
        private final BiFunction<K, V, Map.Entry<K, V>> keyValueWrapper;

        public Wrapper(final SequencedMap<K, V> map, final Function<Map.Entry<K, V>, Map.Entry<K, V>> entryWrapper,
                       final BiFunction<K, V, Map.Entry<K, V>> keyValueWrapper) {
            super(map);
            this.entryWrapper = entryWrapper;
            this.keyValueWrapper = keyValueWrapper;
        }

        public Wrapper(final SequencedMap<K, V> map, final BiFunction<K, V, Map.Entry<K, V>> keyValueWrapper) {
            super(map);
            this.entryWrapper = e -> keyValueWrapper.apply(e.getKey(), e.getValue());
            this.keyValueWrapper = keyValueWrapper;
        }

        public Wrapper(final SequencedMap<K, V> map, final Function<Map.Entry<K, V>, Map.Entry<K, V>> entryWrapper) {
            super(map);
            this.entryWrapper = entryWrapper;
            this.keyValueWrapper = (k, v) -> entryWrapper.apply(new UnmodifiableMapEntry<>(k ,v));
        }

        @Override
        protected Map.Entry<K, V> wrapEntry(final Map.Entry<K, V> entry) {
            return entryWrapper.apply(entry);
        }

        @Override
        protected Map.Entry<K, V> wrapEntry(final K key, final V value) {
            return keyValueWrapper.apply(key, value);
        }
    }
}
