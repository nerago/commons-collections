package org.apache.commons.collections4.set;

import java.util.Collection;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractMapViewSortedSet<E, TSubSet extends SortedRangedSet<E, ?>>
        extends AbstractCommonsSortedSet<E, TSubSet> {

    @Override
    public final void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

}
