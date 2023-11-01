package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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

    @Override
    public final TSubSet reversed() {
        return new ReverseView();
    }

    protected class ReverseView implements SortedRangedSet<E, TSubSet> {

        @Override
        public SortedMapRange<E> getRange() {
            return null;
        }

        @Override
        public TSubSet subSet(SortedMapRange<E> range) {
            return null;
        }

        @Override
        public Comparator<? super E> comparator() {
            return null;
        }

        @Override
        public E first() {
            return null;
        }

        @Override
        public E last() {
            return null;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return null;
        }

        @Override
        public TSubSet reversed() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(E e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
