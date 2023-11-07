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

import java.util.Iterator;
import java.util.Map;
import java.util.SequencedSet;
import java.util.SortedSet;

import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.map.UnmodifiableSortedEntrySet;


/**
 * Decorates another {@code SortedSet} to provide additional behavior.
 * <p>
 * Methods are forwarded directly to the decorated set.
 * </p>
 *
 * @param <E> the type of the elements in the sorted set
 * @since 3.0
 */
public abstract class AbstractSequencedSetDecorator<E, TDecorated extends SequencedSet<E>, TSubSet extends SequencedCommonsSet<E>>
        extends AbstractSetDecorator<E, TDecorated>
        implements SequencedCommonsSet<E> {

    private static final long serialVersionUID = -363134107178725896L;

    private TSubSet reversed;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractSequencedSetDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    protected AbstractSequencedSetDecorator(final TDecorated set) {
        super(set);
    }

    protected AbstractSequencedSetDecorator(final TDecorated set, final TSubSet reverse) {
        super(set);
        this.reversed = reverse;
    }

    @Override
    public void addFirst(final E e) {
        decorated().addFirst(e);
    }

    @Override
    public void addLast(final E e) {
        decorated().addLast(e);
    }

    @Override
    public E getFirst() {
        return decorated().getFirst();
    }

    @Override
    public E getLast() {
        return decorated().getLast();
    }

    @Override
    public E removeFirst() {
        return decorated().removeFirst();
    }

    @Override
    public E removeLast() {
        return decorated().removeLast();
    }

    // some implementations can provide reversed via constructor so may not require
    protected TSubSet createReversed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final TSubSet reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }

    @Override
    public Iterator<E> descendingIterator() {
        final SequencedSet<E> decorated = decorated();
        if (decorated instanceof SequencedCommonsSet<E>) {
            return ((SequencedCommonsSet<E>) decorated).descendingIterator();
        } else {
            return decorated.reversed().iterator();
        }
    }
}
