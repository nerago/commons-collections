package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractCommonsSortedSet<E>
        extends AbstractCommonsCollection<E>
        implements SortedRangedSet<E> {

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
        if (coll.isEmpty()) {
            return false;
        } else if (coll.size() < size()) {
            boolean changed = false;
            for (final Object element : coll) {
                changed |= remove(element);
            }
            return changed;
        } else {
            return removeIf(coll::contains);
        }
    }

    @Override
    public final boolean retainAll(final Collection<?> coll) {
        if (isEmpty()) {
            return false;
        } else if (coll.isEmpty()) {
            clear();
            return true;
        } else {
            return removeIf(element -> !coll.contains(element));
        }
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
    public abstract Iterator<E> descendingIterator();

    @Override
    public SortedRangedSet<E> reversed() {
        return new ReverseView<>(this);
    }

    protected static class ReverseView<E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
            extends AbstractSortedSetDecorator<E, TDecorated, TSubSet> {
        private static final long serialVersionUID = -3785473145672064127L;

        public ReverseView(final TDecorated set) {
            super(set);
        }

        @Override
        protected TSubSet decorateDerived(final TDecorated subMap, final SortedMapRange<E> range) {
            return (TSubSet) new ReverseView<>(subMap);
        }

        @Override
        public Iterator<E> iterator() {
            if (decorated() instanceof SequencedCommonsSet<?>) {
                return ((SequencedCommonsSet<E>) decorated()).descendingIterator();
            } else {
                return decorated().reversed().iterator();
            }
        }

        @Override
        public Iterator<E> descendingIterator() {
            return decorated().iterator();
        }

        @Override
        public boolean addAll(Collection<? extends E> coll) {
            return super.addAll(CollectionUtils.reversedCollection(coll));
        }

        @Override
        public void addFirst(E e) {
            super.addLast(e);
        }

        @Override
        public void addLast(E e) {
            super.addFirst(e);
        }

        @Override
        public E getFirst() {
            return super.getLast();
        }

        @Override
        public E getLast() {
            return super.getFirst();
        }

        @Override
        public E removeFirst() {
            return super.removeLast();
        }

        @Override
        public E removeLast() {
            return super.removeFirst();
        }

        @Override
        public E first() {
            return super.last();
        }

        @Override
        public E last() {
            return super.first();
        }

        @Override
        public Comparator<? super E> comparator() {
            return ComparatorUtils.reversedComparator(decorated().comparator());
        }
    }
}
