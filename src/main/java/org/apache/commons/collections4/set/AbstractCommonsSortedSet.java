package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractCommonsSortedSet<E, TSubSet extends SortedRangedSet<E>>
        extends AbstractCommonsSequencedSet<E, TSubSet> implements SortedRangedSet<E> {

    private static final long serialVersionUID = -1148032504181310936L;

    @Override
    protected TSubSet createReversed() {
        return (TSubSet) new SortedReverseView<>(this, this, getRange().reversed());
    }

    protected static class SortedReverseView<E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
            extends AbstractSortedSetDecorator<E, TDecorated, TSubSet> {
        private static final long serialVersionUID = -3785473145672064127L;

        protected SortedReverseView(final TDecorated set, final TSubSet reverse, final SortedMapRange<E> range) {
            super(set, reverse, range);
        }

        @Override
        protected TSubSet decorateDerived(final TDecorated subSet, final SortedMapRange<E> range) {
            return (TSubSet) new SortedReverseView<>(subSet, null, range);
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
