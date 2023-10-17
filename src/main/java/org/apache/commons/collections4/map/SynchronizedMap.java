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


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.apache.commons.collections4.set.SynchronizedSet;

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
public class SynchronizedMap<K, V> extends AbstractIterableMap<K, V> {
    /** The map to decorate */
    protected Map<K, V> map;
    /** The object to lock on, needed for views */
    protected Object lock;

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
    public boolean containsKey(final Object key) {
        synchronized (lock) {
            return decorated().containsKey(key);
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        synchronized (lock) {
            return decorated().containsValue(value);
        }
    }

    @Override
    public V get(final Object key) {
        synchronized (lock) {
            return decorated().get(key);
        }
    }

    @Override
    public V put(final K key, final V value) {
        synchronized (lock) {
            return decorated().put(key, value);
        }
    }

    @Override
    public V remove(final Object key) {
        synchronized (lock) {
            return decorated().remove(key);
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
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
    public boolean equals(final Object object) {
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
    public V getOrDefault(final Object key, final V defaultValue) {
        synchronized (lock) {
            return decorated().getOrDefault(key, defaultValue);
        }
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        synchronized (lock) {
            decorated().forEach(action);
        }
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        synchronized (lock) {
            decorated().replaceAll(function);
        }
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        synchronized (lock) {
            return decorated().putIfAbsent(key, value);
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        synchronized (lock) {
            return decorated().remove(key, value);
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        synchronized (lock) {
            return decorated().replace(key, oldValue, newValue);
        }
    }

    @Override
    public V replace(final K key, final V value) {
        synchronized (lock) {
            return decorated().replace(key, value);
        }
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        synchronized (lock) {
            return decorated().computeIfAbsent(key, mappingFunction);
        }
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().computeIfPresent(key, remappingFunction);
        }
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().compute(key, remappingFunction);
        }
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        synchronized (lock) {
            return decorated().merge(key, value, remappingFunction);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(map);
        out.writeObject(lock);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        map = (Map<K, V>) in.readObject();
        lock = in.readObject();
    }

    protected static class SynchronizedValues<V> extends SynchronizedCollection<V> {
        protected SynchronizedValues(final Collection<V> values, final Object lock) {
            super(values, lock);
        }
    }
}
