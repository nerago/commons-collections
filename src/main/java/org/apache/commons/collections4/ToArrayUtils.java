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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

public final class ToArrayUtils {
    public static final Object[] EMPTY_ARRAY = new Object[0];

    private ToArrayUtils() {
    }

    /**
     * Gets an array based on an iterator.
     * <p>
     * As the wrapped Iterator is traversed, an ArrayList of its values is
     * created. At the end, this is converted to an array.
     *
     * @param iterator the iterator to use, not null
     * @return an array of the iterator contents
     * @throws NullPointerException if iterator parameter is null
     */
    public static Object[] fromIteratorUnknownSize(final Iterator<?> iterator) {
        Objects.requireNonNull(iterator, "iterator");
        final List<?> list = IteratorUtils.toList(iterator, 100);
        return list.toArray();
    }

    /**
     * Gets an array based on an iterator.
     * <p>
     * As the wrapped Iterator is traversed, an ArrayList of its values is
     * created. At the end, this is converted to an array.
     *
     * @param <E>        the element type
     * @param iterator   the iterator to use, not null
     * @param arrayClass the class of array to create
     * @return an array of the iterator contents
     * @throws NullPointerException if iterator parameter or arrayClass is null
     * @throws ArrayStoreException  if the arrayClass is invalid
     */
    public static <E> E[] fromIteratorUnknownSize(final Iterator<? extends E> iterator, final Class<E> arrayClass) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(arrayClass, "arrayClass");
        final List<E> list = IteratorUtils.toList(iterator, 100);
        @SuppressWarnings("unchecked") final E[] array = (E[]) Array.newInstance(arrayClass, list.size());
        return list.toArray(array);
    }

    public static <E, T> T[] fromIteratorUnknownSize(final Iterator<E> iterator, final T[] array) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(array, "array");
        final List<E> list = IteratorUtils.toList(iterator, 100);
        return list.toArray(array);
    }

    /**
     * Gets an array based on an iterator and expected size.
     * <p>
     * Can directly build target array given known size, should be used in place of {@link #fromIteratorUnknownSize(Iterator)} where possible.
     *
     * @param iterator the iterator to use, not null
     * @param size     the exact size of collection reflected by iterator
     * @return an array of the iterator contents
     * @throws NullPointerException if iterator parameter is null
     */
    public static Object[] fromIteratorAndSize(final Iterator<?> iterator, final int size) {
        final Object[] result = new Object[size];
        for (int i = 0; iterator.hasNext(); i++) {
            result[i] = iterator.next();
        }
        return result;
    }

    /**
     * Gets an array based on an iterator and expected size.
     * <p>
     * Can directly build target array given known size, should be used in place of {@link #fromIteratorUnknownSize(Iterator, Class)} where possible.
     *
     * @param iterator the iterator to use, not null
     * @param size     the exact size of collection reflected by iterator
     * @return an array of the iterator contents
     * @throws NullPointerException if iterator parameter is null
     */
    public static <E, T> T[] fromIteratorAndSize(final Iterator<E> iterator, final int size, final T[] array) {
        return fromFunction(a -> fillArrayAssumingSized(iterator, a), size, array);
    }

    public static Object[] fromFunction(final Consumer<Object[]> fillContent, final int size) {
        final Object[] result = new Object[size];
        fillContent.accept(result);
        return result;
    }

    public static <T> T[] fromFunction(final Consumer<T[]> fillContent, final int size, final T[] array) {
        if (array.length == size) {
            fillContent.accept(array);
            return array;
        } else if (array.length > size) {
            fillContent.accept(array);
            Arrays.fill(array, size, array.length, null);
            return array;
        } else {
            @SuppressWarnings("unchecked") // safe as both are of type T
            final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
            fillContent.accept(newArray);
            return newArray;
        }
    }

    public static <T, E> void fillArrayAssumingSized(final Iterator<E> iterator, final T[] array) {
        for (int i = 0; iterator.hasNext(); i++) {
            @SuppressWarnings("unchecked") final T value = (T) iterator.next();
            array[i] = value;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Object[] transformed(final Supplier<Object[]> nestedToArray, final Function<T, T> transformer) {
        final Object[] result = nestedToArray.get();
        transformInPlace((T[]) result, transformer);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <E, T> T[] transformed(final Function<T[], T[]> nestedToArray, final Function<E, E> transformer, final T[] outputArray) {
        if (outputArray.length == 0) {
            final T[] result = nestedToArray.apply(outputArray);
            transformInPlace(result, (Function<T, T>) transformer);
            return result;
        }

        // we must create a new array to handle multithreaded situations
        // where another thread could access data before we decorate it
        final T[] emptyForType = (T[]) Array.newInstance(outputArray.getClass().getComponentType(), 0);
        final T[] temp = nestedToArray.apply(emptyForType);

        if (temp.length > outputArray.length) {
            transformInPlace(temp, (Function<T, T>) transformer);
            return temp;
        }

        transformWithCopy(temp, outputArray, (Function<T, T>) transformer);
        return outputArray;
    }

    private static <T> void transformInPlace(final T[] array, final Function<T, T> transformer) {
        for (int i = 0; i < array.length; i++) {
            array[i] = transformer.apply(array[i]);
        }
    }

    private static <T> void transformWithCopy(final T[] src, final T[] dest, final Function<T, T> transformer) {
        int i = 0;
        while (i < src.length) {
            dest[i] = transformer.apply(src[i]);
            i++;
        }
        while (i < dest.length) {
            dest[i] = null;
            i++;
        }
    }

    public static <V, K> Object[] fromMapIteratorUnmodifiable(final MapIterator<K, V> mapIterator, final int size) {
        final Object[] array = new Object[size];
        for (int i = 0; i < size && mapIterator.hasNext(); i++) {
            array[i] = new UnmodifiableMapEntry<>(mapIterator.next(), mapIterator.getValue());
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <K, V, T> T[] fromMapIteratorUnmodifiable(final MapIterator<K, V> mapIterator, final int size, final T[] array) {
        final T[] result = array.length <= size ? array
                : (T[]) Array.newInstance(array.getClass().getComponentType(), size);
        for (int i = 0; i < size && mapIterator.hasNext(); i++) {
            result[i] = (T) new UnmodifiableMapEntry<>(mapIterator.next(), mapIterator.getValue());
        }
        if (result.length > size) {
            result[size] = null;
        }
        return array;
    }

    public static <K, V> Object[] fromEntryCollectionUnmodifiable(final Collection<Map.Entry<K, V>> collection) {
        return transformed(collection::toArray, (Map.Entry<K, V> e) -> new UnmodifiableMapEntry<>(e));
    }

    public static <K, V, T> T[] fromEntryCollectionUnmodifiable(final Collection<Map.Entry<K, V>> collection, final T[] array) {
        return transformed(collection::toArray, (Map.Entry<K, V> e) -> new UnmodifiableMapEntry<>(e), array);
    }
}
