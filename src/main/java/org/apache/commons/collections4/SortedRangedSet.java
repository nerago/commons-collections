package org.apache.commons.collections4;

import java.util.SortedSet;

public interface SortedRangedSet<E>
        extends SortedSet<E>, SequencedCommonsSet<E> {
    /**
     * Range of elements included in this set instance (i.e. full set or sub set)
     *
     * @return set element range
     */
    SortedMapRange<E> getRange();

    SortedRangedSet<E> subSet(SortedMapRange<E> range);

    @Override
    @Deprecated
    default SortedRangedSet<E> subSet(final E fromElement, final E toElement) {
        return subSet(getRange().subRange(fromElement, toElement));
    }

    @Override
    @Deprecated
    default SortedRangedSet<E> headSet(final E toElement) {
        return subSet(getRange().head(toElement));
    }

    @Override
    @Deprecated
    default SortedRangedSet<E> tailSet(final E fromElement) {
        return subSet(getRange().tail(fromElement));
    }

    @Override
    SortedRangedSet<E> reversed();
}
