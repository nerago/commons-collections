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
package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.spliterators.UnmodifiableSpliterator;

/**
 * Decorates another {@code SortedSet} to ensure it can't be altered.
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 * </p>
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <E> the type of the elements in this set
 * @since 3.0
 */
public final class UnmodifiableSortedSet<E>
        extends AbstractSortedSetDecorator<E, SortedSet<E>, UnmodifiableSortedSet<E>>
        implements Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = -725356885467962424L;

    /**
     * Factory method to create an unmodifiable set.
     *
     * @param <E> the element type
     * @param set  the set to decorate, must not be null
     * @return a new unmodifiable {@link SortedSet}
     * @throws NullPointerException if set is null
     * @since 4.0
     */
    public static <E> SortedSet<E> unmodifiableSortedSet(final SortedSet<E> set) {
        if (set instanceof Unmodifiable) {
            return set;
        }
        return new UnmodifiableSortedSet<>(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    private UnmodifiableSortedSet(final SortedSet<E> set) {
        super(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    private UnmodifiableSortedSet(final SortedSet<E> set, SortedMapRange range) {
        super(set, range);
    }

    @Override
    public Iterator<E> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
    }

    @Override
    public Spliterator<E> spliterator() {
        return new UnmodifiableSpliterator<>(decorated().spliterator());
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

    /**
     * @since 4.4
     */
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
    protected UnmodifiableSortedSet<E> decorateDerived(final SortedSet<E> subMap, final SortedMapRange<E> range) {
        return new UnmodifiableSortedSet<>(subMap, range);
    }
}
