package org.apache.commons.collections4;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedSet;

public interface NavigableBoundSet<E> extends NavigableSet<E> {
    SortedMapRange<E> getRange();

    @Override
    NavigableBoundSet<E> descendingSet();

    @Override
    NavigableBoundSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

    @Override
    NavigableBoundSet<E> headSet(E toElement, boolean inclusive);

    @Override
    NavigableBoundSet<E> tailSet(E fromElement, boolean inclusive);

    @Override
    default NavigableBoundSet<E> subSet(final E fromElement, final E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    default NavigableBoundSet<E> headSet(final E toElement) {
        return headSet(toElement, false);
    }

    @Override
    default NavigableBoundSet<E> tailSet(final E fromElement) {
        return tailSet(fromElement, true);
    }
}
