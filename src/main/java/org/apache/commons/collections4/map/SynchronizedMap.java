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


import org.apache.commons.collections4.bag.SynchronizedBag;
import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.apache.commons.collections4.multiset.SynchronizedMultiSet;
import org.apache.commons.collections4.set.SynchronizedSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Decorates another {@link Map} to synchronize its behavior
 * for a multithreaded environment.
 * <p>
 * Iterators must be manually synchronized:
 * </p>
 * <pre>
 * synchronized (map) {
 *   Iterator it = map.entrySet().iterator();
 *   // do stuff with iterator
 * }
 * </pre>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since X.X
 */
public class SynchronizedMap<K, V> extends AbstractIterableMap<K, V> implements Serializable {
    /** The map to decorate */
    protected final Map<K, V> map;
    /** The object to lock on, needed for views */
    protected final Object lock;

    /**
     * Factory method to create a synchronized map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map the map to decorate, must not be null
     * @return a new synchronized Map
     * @throws NullPointerException if map is null
     * @since 4.0
     */
    public static <K, V> SynchronizedMap<K, V> synchronizedMap(final Map<K, V> map) {
        return new SynchronizedMap<>(map);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    protected SynchronizedMap(final Map<K, V> map) {
        this.map = map;
        this.lock = this;
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param lock the lock to use, must not be null
     * @throws NullPointerException if map or lock is null
     */
    protected SynchronizedMap(final Map<K, V> map, final Object lock) {
        this.map = map;
        this.lock = Objects.requireNonNull(lock);
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    protected Map<K, V> decorated() {
        return map;
    }

    @Override
    public Set<K> keySet() {
        return new SynchronizedSet<>(decorated().keySet(), lock);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new SynchronizedEntrySet<>(decorated().entrySet(), lock);
    }

    @Override
    public Collection<V> values() {
        return new SynchronizedValues<>(decorated().values(), lock);
    }

    @Override
    public int size() {
        synchronized (lock) {
            return decorated().size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return decorated().isEmpty();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (lock) {
            return decorated().containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (lock) {
            return decorated().containsValue(value);
        }
    }

    @Override
    public V get(Object key) {
        synchronized (lock) {
            return decorated().get(key);
        }
    }

    @Override
    public V put(K key, V value) {
        synchronized (lock) {
            return decorated().put(key, value);
        }
    }

    @Override
    public V remove(Object key) {
        synchronized (lock) {
            return decorated().remove(key);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (lock) {
            decorated().putAll(m);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            decorated().clear();
        }
    }

    @Override
    public boolean equals(Object object) {
        synchronized (lock) {
            return object == this || decorated().equals(object);
        }
    }

    @Override
    public int hashCode() {
        synchronized (lock) {
            return decorated().hashCode();
        }
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        synchronized (lock) {
            return decorated().getOrDefault(key, defaultValue);
        }
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        synchronized (lock) {
            decorated().forEach(action);
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        synchronized (lock) {
            decorated().replaceAll(function);
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        synchronized (lock) {
            return decorated().putIfAbsent(key, value);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        synchronized (lock) {
            return decorated().remove(key, value);
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        synchronized (lock) {
            return decorated().replace(key, oldValue, newValue);
        }
    }

    @Override
    public V replace(K key, V value) {
        synchronized (lock) {
            return decorated().replace(key, value);
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        synchronized (lock) {
            return decorated().computeIfAbsent(key, mappingFunction);
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().computeIfPresent(key, remappingFunction);
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().compute(key, remappingFunction);
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().merge(key, value, remappingFunction);
        }
    }

    protected static class SynchronizedValues<V> extends SynchronizedCollection<V> {
        protected SynchronizedValues(Collection<V> values, Object lock) {
            super(values, lock);
        }
    }
}
