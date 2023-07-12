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
package org.apache.commons.collections4.collection;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.iterators.TransformSpliterator;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Decorates another {@link Collection} to transform objects that are added or retrieved.
 * Provides a view over existing collection with new derived type or content.
 * <p>
 * All methods are affected by this class and act as a collection of the transformed type.
 * Requires transform methods in both directions between stored and interface types.
 * </p>
 * <p>
 * See also {@link TransformedCollection}.
 * </p>
 *
 * @param <S> the type of the elements stored in the collection
 * @param <I> the type of the elements external collection interface for the transformed collection
 * @since X.X
 */
public class DualTransformedCollection<I, S> implements Collection<I>, Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 4488366779197789058L;

    /** The transformer to use */
    protected final Transformer<? super I, ? extends S> interfaceToStorage;
    protected final Transformer<? super S, ? extends I> storageToInterface;

    protected final Collection<S> collection;

    public static <S, I> Collection<I> transformingCollection(final Collection<S> coll,
                                                              final Transformer<? super I, ? extends S> interfaceToStorage,
                                                              final Transformer<? super S, ? extends I> storageToInterface) {
        return new DualTransformedCollection<>(coll, interfaceToStorage, storageToInterface);
    }

    public static <S, I> Collection<I> transformedCollection(final Collection<I> input,
                                                             final Collection<S> collection,
                                                             final Transformer<? super I, ? extends S> interfaceToStorage,
                                                             final Transformer<? super S, ? extends I> storageToInterface) {
        for (final I value : input) {
            collection.add(interfaceToStorage.transform(value));
        }
        return new DualTransformedCollection<>(collection, interfaceToStorage, storageToInterface);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param collection  the collection to decorate, must not be null
     * @param interfaceToStorage  the transformer to use for conversion of inputs, must not be null
     * @param storageToInterface the transformer to use for conversion of outputs, must not be null
     * @throws NullPointerException if collection or transformer is null
     */
    protected DualTransformedCollection(final Collection<S> collection,
                                        final Transformer<? super I, ? extends S> interfaceToStorage,
                                        final Transformer<? super S, ? extends I> storageToInterface) {
        this.collection = Objects.requireNonNull(collection);
        this.interfaceToStorage = Objects.requireNonNull(interfaceToStorage, "interfaceToStored");
        this.storageToInterface = Objects.requireNonNull(storageToInterface, "storedToInterface");
    }

    /**
     * Original wrapped collection.
     */
    public Collection<S> decorated() {
        return collection;
    }

    @SuppressWarnings("unchecked")
    private List<S> transformCollection(Collection<?> c) {
        final List<S> list = new ArrayList<>(c.size());
        for (final Object item : c) {
            list.add(interfaceToStorage.transform((I) item));
        }
        return list;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return collection.contains(interfaceToStorage.transform((I) o));
    }

    @Override
    public boolean add(I o) {
        return collection.add(interfaceToStorage.transform(o));
    }

    @Override
    public boolean addAll(Collection<? extends I> c) {
        final List<S> list = transformCollection(c);
        return collection.addAll(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return collection.remove(interfaceToStorage.transform((I) o));
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        final List<S> list = transformCollection(c);
        return collection.containsAll(list);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        final List<S> list = transformCollection(c);
        return collection.removeAll(list);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        final List<S> list = transformCollection(c);
        return collection.retainAll(list);
    }


    @Override
    public void forEach(Consumer<? super I> action) {
        collection.forEach(s -> action.accept(storageToInterface.transform(s)));
    }

    @Override
    public boolean removeIf(Predicate<? super I> filter) {
        return collection.removeIf(s -> filter.test(storageToInterface.transform(s)));
    }

    @Override
    public Iterator<I> iterator() {
        return new TransformIterator<>(collection.iterator(), storageToInterface);
    }

    @Override
    public Spliterator<I> spliterator() {
        return new TransformSpliterator<>(collection.spliterator(), storageToInterface);
    }

    @Override
    public Object[] toArray() {
        final Object[] result = new Object[collection.size()];
        int i = 0;
        for (Iterator<S> it = collection.iterator(); it.hasNext(); i++) {
            result[i] = storageToInterface.transform(it.next());
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        final int size = collection.size();
        final Object[] result;
        if (array.length >= size) {
            result = array;
        } else {
            result = (Object[]) Array.newInstance(array.getClass().getComponentType(), size);
        }

        int i = 0;
        for (Iterator<S> it = collection.iterator(); it.hasNext(); i++) {
            result[i] = storageToInterface.transform(it.next());
        }

        if (result.length > size) {
            result[size] = null;
        }
        return (T[]) result;
    }
}
