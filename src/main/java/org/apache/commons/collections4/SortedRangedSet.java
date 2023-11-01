package org.apache.commons.collections4;

import java.util.SortedSet;

public interface SortedRangedSet<E, TSubSet extends SortedRangedSet<E, ?>>
        extends SortedSet<E>, SequencedCommonsSet<E> {
    /**
     * Range of elements included in this set instance (i.e. full set or sub set)
     *
     * @return set element range
     */
    SortedMapRange<E> getRange();

    TSubSet subSet(SortedMapRange<E> range);

    @Override
    default TSubSet subSet(final E fromElement, final E toElement) {
        return subSet(getRange().subRange(fromElement, toElement));
    }

    @Override
    default TSubSet headSet(final E toElement) {
        return subSet(getRange().head(toElement));
    }

    @Override
    default TSubSet tailSet(final E fromElement) {
        return subSet(getRange().tail(fromElement));
    }

    @Override
    TSubSet reversed();
}
