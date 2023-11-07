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

import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.BoundedCollection;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.spliterators.UnmodifiableSpliterator;

/**
 * {@link UnmodifiableSequencedCollection} decorates another
 * {@link BoundedCollection} to ensure it can't be altered.
 * <p>
 * If a BoundedCollection is first wrapped in some other collection decorator,
 * such as synchronized or predicated, the BoundedCollection methods are no
 * longer accessible.
 * The factory on this class will attempt to retrieve the bounded nature by
 * examining the package scope variables.
 * </p>
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 * </p>
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <E> the type of elements in this collection
 * @since X.X
 */
public final class UnmodifiableSequencedCollection<E> extends AbstractSequencedCollectionDecorator<E> implements Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = -7112672385450340330L;

    /**
     * Factory method to create an unmodifiable bounded collection.
     *
     * @param <E> the type of the elements in the collection
     * @param coll  the {@code BoundedCollection} to decorate, must not be null
     * @return a new unmodifiable bounded collection
     * @throws NullPointerException if {@code coll} is {@code null}
     * @since 4.0
     */
    public static <E> SequencedCollection<E> unmodifiableSequencedCollection(final SequencedCollection<E> coll) {
        if (coll instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final SequencedCollection<E> tmpColl = (SequencedCollection<E>) coll;
            return tmpColl;
        }
        return new UnmodifiableSequencedCollection<>(coll);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param coll  the collection to decorate, must not be null
     * @throws NullPointerException if coll is null
     */
    private UnmodifiableSequencedCollection(final SequencedCollection<E> coll) {
        super(coll);
    }

    @Override
    public Iterator<E> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return UnmodifiableIterator.unmodifiableIterator(decorated().reversed().iterator());
    }

    @Override
    public Spliterator<E> spliterator() {
        return new UnmodifiableSpliterator<>(decorated().spliterator());
    }

    @Override
    public SequencedCommonsCollection<E> reversed() {
        return new UnmodifiableSequencedCollection<>(decorated().reversed());
    }

    @Override
    public boolean add(final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
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
    public boolean removeIf(final Predicate<? super E> filter) {
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

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException();
    }
}
