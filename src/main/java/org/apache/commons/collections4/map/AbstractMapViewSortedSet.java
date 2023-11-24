package org.apache.commons.collections4.map;

import java.util.Collection;

import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.set.AbstractCommonsSortedSet;

public abstract class AbstractMapViewSortedSet<E, TSubSet extends SortedRangedSet<E>>
        extends AbstractCommonsSortedSet<E, TSubSet> {

    @Override
    public final void addFirst(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void addLast(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

}
