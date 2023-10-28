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

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;

import java.util.Iterator;
import java.util.NavigableSet;

/**
 * Decorates another {@code NavigableSet} to provide additional behavior.
 * <p>
 * Methods are forwarded directly to the decorated set.
 * </p>
 *
 * @param <E> the type of the elements in the navigable set
 * @since 4.1
 */
public abstract class AbstractNavigableSetDecorator<E, TDecorated extends NavigableSet<E>, TSubSet extends NavigableRangedSet<E, ?>>
        extends AbstractSortedSetDecorator<E, TDecorated, TSubSet>
        implements NavigableRangedSet<E, TSubSet> {

    /** Serialization version */
    private static final long serialVersionUID = 20150528L;

    private SortedMapRange<E> range;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected AbstractNavigableSetDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    protected AbstractNavigableSetDecorator(final TDecorated set, final SortedMapRange<E> range) {
        super(set);
        this.range = range;
    }

    @Override
    public SortedMapRange<E> getRange() {
        return range;
    }

    @Override
    public E lower(final E e) {
        return decorated().lower(e);
    }

    @Override
    public E floor(final E e) {
        return decorated().floor(e);
    }

    @Override
    public E ceiling(final E e) {
        return decorated().ceiling(e);
    }

    @Override
    public E higher(final E e) {
        return decorated().higher(e);
    }

    @Override
    public E pollFirst() {
        return decorated().pollFirst();
    }

    @Override
    public E pollLast() {
        return decorated().pollLast();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet descendingSet() {
        return decorateDerived((TDecorated) decorated().descendingSet(), range.reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return decorated().descendingIterator();
    }

    public static final class NullDecorator<E> extends AbstractNavigableSetDecorator<E, NavigableSet<E>, NullDecorator<E>> {
        private static final long serialVersionUID = -2806194974152145578L;


        /**
         * Constructor that wraps (not copies).
         *
         * @param set   the set to decorate, must not be null
         * @throws NullPointerException if set is null
         */
        public NullDecorator(final NavigableSet<E> set) {
            super(set, SortedMapRange.full(set.comparator()));
        }

        /**
         * Constructor that wraps (not copies).
         *
         * @param set   the set to decorate, must not be null
         * @param range sub-set range
         * @throws NullPointerException if set or range is null
         */
        public NullDecorator(final NavigableSet<E> set, final SortedMapRange<E> range) {
            super(set, range);
        }

        @Override
        protected NullDecorator<E> decorateDerived(final NavigableSet<E> subMap, final SortedMapRange<E> range) {
            return new NullDecorator<>(subMap, range);
        }
    }
}
