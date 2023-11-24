package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractMapViewSortedSetDecorator<E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
        extends AbstractMapViewSequencedSetDecorator<E, TDecorated, TSubSet>
        implements SortedRangedSet<E> {

    private final SortedMapRange<E> range;

    protected AbstractMapViewSortedSetDecorator(final TDecorated decorated, final SortedMapRange<E> range) {
        super(decorated);
        this.range = range;
    }

    @Override
    public SortedMapRange<E> getRange() {
        return range;
    }

    @Override
    protected TSubSet createReversed() {
        return decorateDerived((TDecorated) decorated.reversed(), range.reversed());
    }

    @Override
    public Comparator<? super E> comparator() {
        return decorated.comparator();
    }

    @Override
    public final TSubSet subSet(final SortedMapRange<E> range) {
        return decorateDerived(range.applyToSet(decorated()), range);
    }

}
