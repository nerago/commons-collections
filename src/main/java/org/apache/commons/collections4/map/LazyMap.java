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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.FactoryTransformer;

/**
 * Decorates another {@code Map} to create objects in the map on demand.
 * <p>
 * When the {@link #get(Object)} method is called with a key that does not
 * exist in the map, the factory is used to create the object. The created
 * object will be added to the map using the requested key.
 * </p>
 * <p>
 * For instance:
 * </p>
 * <pre>
 * Factory&lt;Date&gt; factory = new Factory&lt;Date&gt;() {
 *     public Date create() {
 *         return new Date();
 *     }
 * }
 * Map&lt;String, Date&gt; lazy = LazyMap.lazyMap(new HashMap&lt;String, Date&gt;(), factory);
 * Date date = lazy.get("NOW");
 * </pre>
 *
 * <p>
 * After the above code is executed, {@code date} will refer to
 * a new {@code Date} instance. Furthermore, that {@code Date}
 * instance is mapped to the "NOW" key in the map.
 * </p>
 * <p>
 * <strong>Note that LazyMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 * </p>
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class LazyMap<K, V> extends AbstractMapDecorator<K, V, Map<K, V>> {

    /** Serialization version */
    private static final long serialVersionUID = 7990956402564206740L;

    /** The factory to use to construct elements */
    protected Transformer<? super K, ? extends V> factory;

    /**
     * Factory method to create a lazily instantiated map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @return a new lazy map
     * @throws NullPointerException if map or factory is null
     * @since 4.0
     */
    public static <K, V> LazyMap<K, V> lazyMap(final Map<K, V> map, final Factory<? extends V> factory) {
        return new LazyMap<>(map, factory);
    }

    /**
     * Factory method to create a lazily instantiated map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @return a new lazy map
     * @throws NullPointerException if map or factory is null
     * @since 4.0
     */
    public static <V, K> LazyMap<K, V> lazyMap(final Map<K, V> map, final Transformer<? super K, ? extends V> factory) {
        return new LazyMap<>(map, factory);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @throws NullPointerException if map or factory is null
     */
    protected LazyMap(final Map<K, V> map, final Factory<? extends V> factory) {
        super(map);
        this.factory = FactoryTransformer.factoryTransformer(Objects.requireNonNull(factory, "factory"));
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @param factory  the factory to use, must not be null
     * @throws NullPointerException if map or factory is null
     */
    protected LazyMap(final Map<K, V> map, final Transformer<? super K, ? extends V> factory) {
        super(map);
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     * @since 3.1
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(factory);
        super.writeExternal(out);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     * @since 3.1
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        factory = (Transformer<? super K, ? extends V>) in.readObject();
        super.readExternal(in);
    }

    @Override
    public V get(final Object key) {
        // create value for key if key is not currently in the map
        if (!map.containsKey(key)) {
            @SuppressWarnings("unchecked")
            final K castKey = (K) key;
            final V value = factory.transform(castKey);
            map.put(castKey, value);
            return value;
        }
        return map.get(key);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        // pretend every key is contained so never use the default
        return get(key);
    }

    @Override
    public V putIfAbsent(final K key, final V ignoreCallerValue) {
        final V mapValue = map.get(key);
        if (mapValue != null) {
            return mapValue;
        } else {
            final V value = factory.transform(key);
            map.put(key, value);
            return value;
        }
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final V mapValue = map.get(key);
        if (mapValue != null) {
            return mapValue;
        } else {
            final V value = factory.transform(key);
            map.put(key, value);
            return value;
        }
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        final V oldMapValue = map.get(key);
        if (oldMapValue == null && map.containsKey(key)) {
            return null;
        } else if (oldMapValue != null) {
            final V newValue = remappingFunction.apply(key, oldMapValue);
            if (newValue != null) {
                map.put(key, newValue);
            } else {
                map.remove(key);
            }
            return newValue;
        } else {
            final V factoryValue = factory.transform(key);
            final V newValue = remappingFunction.apply(key, factoryValue);
            if (newValue != null) {
                map.put(key, newValue);
            }
            return newValue;
        }
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        final V oldMapValue = map.get(key);
        if (oldMapValue != null) {
            final V newValue = remappingFunction.apply(key, oldMapValue);
            if (newValue != null) {
                map.put(key, newValue);
            } else {
                map.remove(key);
            }
            return newValue;
        } else {
            final V factoryValue = factory.transform(key);
            final V newValue = remappingFunction.apply(key, factoryValue);
            if (newValue != null) {
                map.put(key, newValue);
            }
            return newValue;
        }
    }

    @Override
    public V merge(final K key, final V paramValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(paramValue);

        final V oldMapValue = map.get(key);
        if (oldMapValue != null || map.containsKey(key)) {
            final V initialValue = oldMapValue != null ? oldMapValue : factory.transform(key);
            final V newValue = remappingFunction.apply(initialValue, paramValue);
            if (newValue != null) {
                map.put(key, newValue);
            } else {
                map.remove(key);
            }
            return newValue;
        } else {
            final V factoryValue = factory.transform(key);
            final V newValue = remappingFunction.apply(factoryValue, paramValue);
            if (newValue != null) {
                map.put(key, newValue);
            }
            return newValue;
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        if (map.remove(key, value)) {
            return true;
        } else {
            @SuppressWarnings("unchecked")
            final V factoryValue = factory.transform((K) key);
            return Objects.equals(factoryValue, value);
        }
    }

    @Override
    public V replace(final K key, final V value) {
        final boolean wasContained = map.containsKey(key);
        final V oldValue = map.put(key, value);
        return wasContained ? oldValue : factory.transform(key);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final V mapValue = map.get(key);
        final V currentValue = (mapValue != null || map.containsKey(key))
                               ? mapValue
                               : factory.transform(key);
        if (Objects.equals(currentValue, oldValue)) {
            map.put(key, newValue);
            return true;
        }
        return false;
    }

    // no need to wrap keySet, entrySet or values as they are views of
    // existing map entries - you can't do a map-style get on them.
}
