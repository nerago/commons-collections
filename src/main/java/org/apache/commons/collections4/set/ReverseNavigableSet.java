package org.apache.commons.collections4.set;

import java.util.NavigableSet;

import org.apache.commons.collections4.SortedMapRange;

public class ReverseNavigableSet<E> extends AbstractNavigableSetDecorator<E, NavigableSet<E>, ReverseNavigableSet<E>> {
    public ReverseNavigableSet(final NavigableSet<E> set, final SortedMapRange<E> range) {
        super(set, range);
    }

    @Override
    protected ReverseNavigableSet<E> decorateDerived(final NavigableSet<E> subMap, final SortedMapRange<E> range) {
        return new ReverseNavigableSet<>(subMap, range);
    }

}
