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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MutableBoolean;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.spliterators.MapSpliterator;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractIterableMapAlternate<K, V> extends AbstractIterableMap<K, V> {
    private static final long serialVersionUID = 4016005260054821088L;

    private transient Set<Entry<K, V>> entrySet;
    private transient Set<K> keySet;
    private transient Collection<V> values;

    protected abstract MapSpliterator<K, V> mapSpliterator();

    protected Iterator<Map.Entry<K, V>> entryIterator() {
        final MapIterator<K, V> mapIterator = mapIterator();
        return new TransformIterator<>(mapIterator, k -> new UnmodifiableMapEntry<>(k, mapIterator.getValue()));
    }

    @Override
    public abstract V getOrDefault(Object key, V defaultValue);

    @Override
    public final V get(final Object key) {
        return getOrDefault(key, null);
    }

    protected abstract boolean containsEntry(Object key, Object value);

    protected abstract V doPut(K key, V value, final boolean addIfAbsent, final boolean updateIfPresent);

    protected abstract V doPut(final K key,
                     final Function<? super K, ? extends V> absentFunc,
                     final BiFunction<? super K, ? super V, ? extends V> presentFunc,
                     final boolean saveNulls);

    @Override
    public final V put(final K key, final V value) {
        return doPut(key, value, true, true);
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        return doPut(key, value, true, false);
    }

    @Override
    public final V replace(final K key, final V value) {
        return doPut(key, value, false, true);
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        final MutableBoolean didUpdate = new MutableBoolean();
        doPut(key, null,
                (k, currentValue) -> {
                    if (Objects.equals(oldValue, currentValue)) {
                        didUpdate.flag = true;
                        return newValue;
                    } else {
                        return currentValue;
                    }
                }, true);
        return didUpdate.flag;
    }

    @Override
    public final V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        return doPut(key, mappingFunction, null, false);
    }

    @Override
    public final V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key, null, remappingFunction, false);
    }

    @Override
    public final V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                k -> remappingFunction.apply(k, null),
                remappingFunction, false);
    }

    @Override
    public final V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                k -> value,
                (k, v) -> remappingFunction.apply(v, value), false);
    }

    protected abstract boolean removeAsBoolean(Object key);

    @Override
    public abstract boolean remove(Object key, Object value);

    protected abstract boolean removeValueAsBoolean(Object value);

    @Override
    public int size() {
        return IteratorUtils.size(mapIterator());
    }

    @Override
    public boolean isEmpty() {
        return IteratorUtils.isEmpty(mapIterator());
    }

    @Override
    public void clear() {
        final MapIterator<K, V> iterator = mapIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    protected Set<K> createKeySet() {
        return new AbsIterMapKeySet();
    }

    protected Set<Map.Entry<K, V>> createEntrySet() {
        return new AbsIterMapEntrySet();
    }

    protected Collection<V> createValuesCollection() {
        return new AbsIterMapValues();
    }

    @Override
    public final Set<K> keySet() {
        if (keySet != null)
            return keySet;
        keySet = createKeySet();
        return keySet;
    }

    @Override
    public final Set<Map.Entry<K, V>> entrySet() {
        if (entrySet != null)
            return entrySet;
        entrySet = createEntrySet();
        return entrySet;
    }

    @Override
    public Collection<V> values() {
        if (values != null)
            return values;
        values = createValuesCollection();
        return values;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Map)) {
            return false;
        } else {
            return MapUtils.isEqualMap(mapIterator(), (Map<?, ?>) obj);
        }
    }

    @Override
    public int hashCode() {
        return MapUtils.hashCode(mapIterator());
    }

    @Override
    public String toString() {
        return MapUtils.toString(mapIterator());
    }

    protected final class AbsIterMapKeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return mapIterator();
        }

        @Override
        public Spliterator<K> spliterator() {
            return new TransformSpliterator<>(mapSpliterator(), Map.Entry::getKey);
        }

        @Override
        public int size() {
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractIterableMapAlternate.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractIterableMapAlternate.this.removeAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    protected final class AbsIterMapEntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return entryIterator();
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return mapSpliterator();
        }

        @Override
        public int size() {
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Entry<?, ?> entry = (Entry<?, ?>) obj;
            return AbstractIterableMapAlternate.this.containsEntry(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean remove(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Entry<?, ?> entry = (Entry<?, ?>) obj;
            return AbstractIterableMapAlternate.this.remove(entry.getKey(), entry.getValue());
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    protected final class AbsIterMapValues extends AbstractCollection<V> {
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
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractIterableMapAlternate.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractIterableMapAlternate.this.removeValueAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    protected static final class AbsIterMapValueIterator<V> implements Iterator<V> {
        private final MapIterator<?, V> mapIterator;

        public AbsIterMapValueIterator(final MapIterator<?, V> mapIterator) {
            this.mapIterator = mapIterator;
        }

        @Override
        public boolean hasNext() {
            return mapIterator.hasNext();
        }

        @Override
        public V next() {
            mapIterator.next();
            return mapIterator.getValue();
        }

        @Override
        public void remove() {
            mapIterator.remove();
        }
    }
}
