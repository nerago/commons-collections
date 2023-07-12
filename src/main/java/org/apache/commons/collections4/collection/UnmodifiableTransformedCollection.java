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
import org.apache.commons.collections4.Unmodifiable;
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
public class UnmodifiableTransformedCollection<S, I> implements Collection<I>, Serializable, Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = 1708768617060537720L;

    /** The transformer to use */
    protected final Transformer<? super S, ? extends I> transform;

    protected final Collection<S> collection;

    public static <S, I> Collection<I> transformingCollection(final Collection<S> coll,
                                                              final Transformer<? super S, ? extends I> transform) {
        return new UnmodifiableTransformedCollection<>(coll, transform);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param collection  the collection to decorate, must not be null
     * @param transform the transformer to use for conversion of outputs, must not be null
     * @throws NullPointerException if collection or transformer is null
     */
    protected UnmodifiableTransformedCollection(final Collection<S> collection,
                                                final Transformer<? super S, ? extends I> transform) {
        this.collection = Objects.requireNonNull(collection);
        this.transform = Objects.requireNonNull(transform, "transform");
    }

    /**
     * Original wrapped collection.
     */
    public Collection<S> decorated() {
        return collection;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * UnmodifiableTransformedCollection has inefficient implementation, subclasses should override.
     */
    @Override
    public boolean contains(Object obj) {
        for (S stored : collection) {
            I external = transform.transform(stored);
            if (Objects.equals(external, obj))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> coll) {
        for (final Object item : coll) {
            if (!contains(item))
                return false;
        }
        return true;
    }

    @Override
    public void forEach(Consumer<? super I> action) {
        collection.forEach(s -> action.accept(transform.transform(s)));
    }

    @Override
    public Iterator<I> iterator() {
        return new TransformIterator<>(collection.iterator(), transform);
    }

    @Override
    public Spliterator<I> spliterator() {
        return new TransformSpliterator<>(collection.spliterator(), transform);
    }

    @Override
    public Object[] toArray() {
        final Object[] result = new Object[collection.size()];
        int i = 0;
        for (Iterator<S> it = collection.iterator(); it.hasNext(); i++) {
            result[i] = transform.transform(it.next());
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
            result[i] = transform.transform(it.next());
        }

        if (result.length > size) {
            result[size] = null;
        }
        return (T[]) result;
    }

    @Override
    public boolean add(final I object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends I> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(final Predicate<? super I> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
}
