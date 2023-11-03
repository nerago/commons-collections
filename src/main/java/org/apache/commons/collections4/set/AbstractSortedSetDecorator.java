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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

/**
 * Decorates another {@code SortedSet} to provide additional behavior.
 * <p>
 * Methods are forwarded directly to the decorated set.
 * </p>
 *
 * @param <E> the type of the elements in the sorted set
 * @since 3.0
 */
public abstract class AbstractSortedSetDecorator<E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
        extends AbstractSequencedSetDecorator<E, TDecorated, TSubSet>
        implements SortedRangedSet<E> {

    /** Serialization version */
    private static final long serialVersionUID = -3462240946294214398L;

    /** The range of this sub-set */
    transient SortedMapRange<E> range;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractSortedSetDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    protected AbstractSortedSetDecorator(final TDecorated set) {
        super(set);
        this.range = SortedMapRange.full(set.comparator());
    }

    protected AbstractSortedSetDecorator(final TDecorated set, final SortedMapRange<E> range) {
        super(set);
        this.range = range;
    }

    @Override
    public E first() {
        return decorated().first();
    }

    @Override
    public E last() {
        return decorated().last();
    }

    @Override
    public Comparator<? super E> comparator() {
        return decorated().comparator();
    }

    @Override
    public SortedMapRange<E> getRange() {
        return range;
    }

    @Override
    protected final TSubSet decorateReverse(final TDecorated subMap) {
        return decorateDerived(subMap, range.reversed());
    }

    protected abstract TSubSet decorateDerived(final TDecorated subMap, final SortedMapRange<E> range);

    @Override
    public Iterator<E> descendingIterator() {
        if (decorated() instanceof SequencedCommonsCollection) {
            return ((SequencedCommonsCollection<E>) decorated()).descendingIterator();
        } else {
            return reversed().iterator();
        }
    }

    @Override
    public final TSubSet subSet(final SortedMapRange<E> range) {
        return decorateDerived(range.applyToSet(decorated()), range);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet reversed() {
        return decorateDerived((TDecorated) decorated().reversed(), range.reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        range = (SortedMapRange<E>) in.readObject();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(range);
    }
}
