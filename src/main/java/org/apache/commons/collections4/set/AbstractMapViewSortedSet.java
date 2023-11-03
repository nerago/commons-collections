package org.apache.commons.collections4.set;

import java.util.Collection;

import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractMapViewSortedSet<E>
        extends AbstractCommonsSortedSet<E> {

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
