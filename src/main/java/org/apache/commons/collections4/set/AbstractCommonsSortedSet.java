package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractCommonsSortedSet<E, TSubSet extends SortedRangedSet<E, ?>>
        extends AbstractCommonsCollection<E>
        implements SortedRangedSet<E, TSubSet> {

    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof Set) {
            return SetUtils.isEqualSet(this, (Collection<?>) obj);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return SetUtils.hashCodeForSet(this);
    }

    @Override
    public final boolean removeAll(final Collection<?> coll) {
        boolean changed = false;
        if (coll.size() < size()) {
            for (final Object element : coll) {
                changed |= remove(element);
            }
        } else {
            removeIf(coll::contains);
        }
        return changed;
    }

    @Override
    public final boolean retainAll(final Collection<?> coll) {
        return removeIf(element -> !coll.contains(element));
    }

    @Override
    public boolean containsAll(final Collection<?> coll) {
        for (final Object element : coll) {
            if (!coll.contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final TSubSet subSet(final E fromElement, final E toElement) {
        return SortedRangedSet.super.subSet(fromElement, toElement);
    }

    @Override
    public final TSubSet headSet(final E toElement) {
        return SortedRangedSet.super.headSet(toElement);
    }

    @Override
    public final TSubSet tailSet(final E fromElement) {
        return SortedRangedSet.super.tailSet(fromElement);
    }
}
