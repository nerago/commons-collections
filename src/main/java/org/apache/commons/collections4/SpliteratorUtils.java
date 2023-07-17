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
package org.apache.commons.collections4;

import org.apache.commons.collections4.iterators.*;
import org.apache.commons.collections4.spliterators.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides static utility methods and decorators for {@link Spliterator}
 * instances. The implementations are provided in the iterators subpackage.
 *
 * @since 2.1
 */
public class SpliteratorUtils {
    // validation is done in this class in certain cases because the
    // public classes allow invalid states

    /**
     * An iterator over no elements.
     */
    @SuppressWarnings("rawtypes")
    public static final ResettableIterator EMPTY_ITERATOR = EmptyIterator.RESETTABLE_INSTANCE;

    /**
     * A list iterator over no elements.
     */
    @SuppressWarnings("rawtypes")
    public static final ResettableListIterator EMPTY_LIST_ITERATOR = EmptyListIterator.RESETTABLE_INSTANCE;

    /**
     * An ordered iterator over no elements.
     */
    @SuppressWarnings("rawtypes")
    public static final OrderedIterator EMPTY_ORDERED_ITERATOR = EmptyOrderedIterator.INSTANCE;

    /**
     * A map iterator over no elements.
     */
    @SuppressWarnings("rawtypes")
    public static final MapIterator EMPTY_MAP_ITERATOR = EmptyMapIterator.INSTANCE;

    /**
     * An ordered map iterator over no elements.
     */
    @SuppressWarnings("rawtypes")
    public static final OrderedMapIterator EMPTY_ORDERED_MAP_ITERATOR = EmptyOrderedMapIterator.INSTANCE;
    /**
     * Default delimiter used to delimit elements while converting an Iterator
     * to its String representation.
     */
    private static final String DEFAULT_TOSTRING_DELIMITER = ", ";

    /**
     * Don't allow instances.
     */
    private SpliteratorUtils() {}

    /**
     * Gets an empty ordered map iterator.
     * <p>
     * This iterator is a valid map iterator object that will iterate
     * over nothing.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a map iterator over nothing
     */
    public static <K, V> MapSpliterator<K, V> emptyMapSpliterator() {
        return EmptyMapSpliterator.emptyMapSpliterator();
    }

    // Singleton
    /**
     * Gets a singleton iterator.
     * <p>
     * This iterator is a valid iterator object that will iterate over
     * the specified object.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param key   the single key to supply
     * @param value the single value to supply
     * @return a decorated spliterator for the object
     */
    public static <K, V> MapSpliterator<K, V> singletonMapSpliterator(final K key, final V value) {
        return new SingletonMapSpliterator<>(key, value);
    }

    /**
     * Gets a MapSpliterator for a standard map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the existing map instance
     * @return a decorated spliterator for the object
     */
    public static <K, V> MapSpliterator<K, V> mapSpliterator(final Map<K, V> map) {
        return new EntrySetSpliterator<>(map);
    }

    /**
     * Gets a MapSpliterator for a standard map entry set.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param entrySet the existing entry set
     * @return a decorated spliterator for the object
     */
    public static <K, V> MapSpliterator<K, V> mapSpliterator(final Collection<Map.Entry<K,V>> entrySet) {
        return new EntrySetSpliterator<>(entrySet);
    }

    /**
     * Gets a MapSpliterator for a standard map entry spliterator.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param spliterator the existing spliterator
     * @return a decorated spliterator for the object
     */
    public static <K, V> MapSpliterator<K, V> mapSpliterator(final Spliterator<Map.Entry<K,V>> spliterator) {
        return new EntrySetSpliterator<>(spliterator);
    }

    /**
     * Gets an immutable version of a {@link MapIterator}. The returned object
     * will always throw an {@link UnsupportedOperationException} for {@link Map.Entry#setValue(Object)} method.
     *
     * @param <K>            the key type
     * @param <V>            the value type
     * @param mapSpliterator the spliterator to make immutable
     * @return an immutable version of the spliterator
     */
    public static <K, V> MapSpliterator<K, V> unmodifiableMapSpliterator(final Spliterator<Map.Entry<K,V>> mapSpliterator) {
        return UnmodifiableMapSpliterator.unmodifiableMapSpliterator(mapSpliterator);
    }

    public static <I, O> Spliterator<O> transformedSpliterator(final Spliterator<? extends I> spliterator,
            final Transformer<? super I, ? extends O> transformer) {

        Objects.requireNonNull(spliterator, "iterator");
        Objects.requireNonNull(transformer, "transformer");
        return new TransformSpliterator<>(spliterator, transformer);
    }

    private static class Holder<X> {
        X value;
    }

    /**
     * Returns the {@code index}-th value in {@link Spliterator}, throwing
     * {@code IndexOutOfBoundsException} if there is no such element.
     * <p>
     * The Iterator is advanced to {@code index} (or to the end, if
     * {@code index} exceeds the number of entries) as a side effect of this method.
     *
     * @param <E> the type of object in the {@link Iterator}
     * @param spliterator  the iterator to get a value from
     * @param index  the index to get
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @since X.X
     */
    public static <E> E get(final Spliterator<E> spliterator, final int index) {
        int i = index;
        CollectionUtils.checkIndexBounds(i);
        final Holder<E> holder = new Holder<>();
        while (spliterator.tryAdvance(e -> holder.value = e)) {
            i--;
            if (i == -1) {
                return holder.value;
            }
        }
        throw new IndexOutOfBoundsException("Entry does not exist: " + i);
    }

    /**
     * Returns the {@code index}-th value in {@link Iterator}, throwing
     * {@code IndexOutOfBoundsException} if there is no such element.
     * <p>
     * The Iterator is advanced to {@code index} (or to the end, if
     * {@code index} exceeds the number of entries) as a side effect of this method.
     *
     * @param <E> the type of object in the {@link Iterator}
     * @param stream  the stream to get a value from
     * @param index   the index to get
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @since X.X
     */
    public static <E> E get(final Stream<E> stream, final int index) {
        return stream.skip(index)
                     .findFirst()
                     .orElseThrow(() -> new IndexOutOfBoundsException("Entry does not exist: " + index));
    }

    /**
     * Shortcut for {@code get(iterator, 0)}.
     * <p>
     * Returns the {@code first} value in {@link Spliterator}, throwing
     * {@code IndexOutOfBoundsException} if there is no such element.
     * </p>
     * <p>
     * The Spliterator is advanced to {@code 0} (or to the end, if
     * {@code 0} exceeds the number of entries) as a side effect of this method.
     * </p>
     * @param <E> the type of object in the {@link Spliterator}
     * @param spliterator the spliterator to get a value from
     * @return the first object
     * @throws IndexOutOfBoundsException if the request is invalid
     * @since X.X
     */
    public static <E> E first(final Spliterator<E> spliterator) {
        final Holder<E> holder = new Holder<>();
        if (spliterator.tryAdvance(e -> holder.value = e)) {
            return holder.value;
        }
        throw new IndexOutOfBoundsException("Entry does not exist: 0");
    }

    /**
     * Shortcut for {@code get(iterator, 0)}.
     * <p>
     * Returns the {@code first} value in {@link Spliterator}, throwing
     * {@code IndexOutOfBoundsException} if there is no such element.
     * </p>
     * <p>
     * The Iterator is advanced to {@code 0} (or to the end, if
     * {@code 0} exceeds the number of entries) as a side effect of this method.
     * </p>
     * @param <E> the type of object in the {@link Iterator}
     * @param stream the spliterator to get a value from
     * @return the first object
     * @throws IndexOutOfBoundsException if the request is invalid
     * @since X.X
     */
    public static <E> E first(final Stream<E> stream) {
        return stream.findFirst()
                     .orElseThrow(() -> new IndexOutOfBoundsException("Entry does not exist: 0"));
    }

    /**
     * Returns the number of elements contained in the given spliterator.
     * <p>
     * A {@code null} or empty spliterator returns {@code 0}.
     *
     * @param spliterator the spliterator to check, may be null
     * @return the number of elements contained in the stream
     * @since X.X
     */
    public static long size(final Spliterator<?> spliterator) {
        if (spliterator == null)
            return 0;
        else if (spliterator.hasCharacteristics(Spliterator.SIZED))
            return spliterator.estimateSize();
        else
            return StreamSupport.stream(spliterator, false).count();
    }

    /**
     * Returns the number of elements contained in the given spliterator.
     * <p>
     * A {@code null} or empty stream returns {@code 0}.
     *
     * @param stream the stream to check, may be null
     * @return the number of elements remaining in the stream
     * @since X.X
     */
    public static long size(final Stream<?> stream) {
        if (stream == null)
            return 0;
        return stream.count();
    }
}
