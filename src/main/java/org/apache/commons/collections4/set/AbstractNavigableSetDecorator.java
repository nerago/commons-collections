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

import org.apache.commons.collections4.NavigableBoundSet;
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
public abstract class AbstractNavigableSetDecorator<E>
        extends AbstractSortedSetDecorator<E>
        implements NavigableBoundSet<E> {

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
    protected AbstractNavigableSetDecorator(final NavigableSet<E> set, final SortedMapRange<E> range) {
        super(set);
        this.range = range;
    }

    /**
     * Gets the set being decorated.
     *
     * @return the decorated set
     */
    @Override
    protected NavigableSet<E> decorated() {
        return (NavigableSet<E>) super.decorated();
    }

    protected abstract NavigableBoundSet<E> decorateDerived(NavigableSet<E> subSet, SortedMapRange<E> range);

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

    @Override
    public NavigableBoundSet<E> descendingSet() {
        return decorateDerived(decorated().descendingSet(), getRange().reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return decorated().descendingIterator();
    }

    @Override
    public NavigableBoundSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement,
                                       final boolean toInclusive) {
        return decorateDerived(decorated().subSet(fromElement, fromInclusive, toElement, toInclusive),
                getRange().subRange(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public NavigableBoundSet<E> headSet(final E toElement, final boolean inclusive) {
        return decorateDerived(decorated().headSet(toElement, inclusive), getRange().head(toElement, inclusive));
    }

    @Override
    public NavigableBoundSet<E> tailSet(final E fromElement, final boolean inclusive) {
        return decorateDerived(decorated().tailSet(fromElement, inclusive), getRange().tail(fromElement, inclusive));
    }

    @Override
    public NavigableBoundSet<E> subSet(final E fromElement, final E toElement) {
        return NavigableBoundSet.super.subSet(fromElement, toElement);
    }

    @Override
    public NavigableBoundSet<E> headSet(final E toElement) {
        return NavigableBoundSet.super.headSet(toElement);
    }

    @Override
    public NavigableBoundSet<E> tailSet(final E fromElement) {
        return NavigableBoundSet.super.tailSet(fromElement);
    }
}
