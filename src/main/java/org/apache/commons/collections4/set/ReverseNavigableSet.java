package org.apache.commons.collections4.set;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.ToArrayUtils;

public class ReverseNavigableSet<E, TDecorated extends NavigableSet<E>, TSubSet extends NavigableRangedSet<E>>
        extends ReverseSortedSet<E, TDecorated, TSubSet>
        implements NavigableRangedSet<E> {
    public ReverseNavigableSet(final TDecorated set, final SortedMapRange<E> range) {
        super(set, range.reversed());
    }

    @Override
    public E pollFirst() {
        return decorated().pollLast();
    }

    @Override
    public E pollLast() {
        return decorated().pollFirst();
    }

    @Override
    public E lower(final E e) {
        return decorated().higher(e);
    }

    @Override
    public E floor(final E e) {
        return decorated().ceiling(e);
    }

    @Override
    public E ceiling(final E e) {
        return decorated().floor(e);
    }

    @Override
    public E higher(final E e) {
        return decorated().lower(e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet descendingSet() {
        return (TSubSet) new AbstractNavigableSetDecorator.NullDecorator<>(decorated());
    }

}

