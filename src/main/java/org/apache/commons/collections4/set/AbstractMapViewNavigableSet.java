package org.apache.commons.collections4.set;

import java.util.NavigableSet;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;

public abstract class AbstractMapViewNavigableSet<E> extends AbstractMapViewSortedSet<E> implements NavigableRangedSet<E> {
    @Override
    public final NavigableRangedSet<E> reversed() {
        return new ReverseNavigableView<>(this);
    }

    @Override
    public final E removeFirst() {
        final E element = pollFirst();
        if (element != null) {
            return element;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final E removeLast() {
        final E element = pollLast();
        if (element != null) {
            return element;
        } else {
            throw new NoSuchElementException();
        }
    }

    protected static class ReverseNavigableView<E>
            extends ReverseView<E, NavigableSet<E>, NavigableRangedSet<E>> 
            implements NavigableRangedSet<E> {
        private static final long serialVersionUID = 5385328200121138393L;

        protected ReverseNavigableView(final NavigableRangedSet<E> set) {
            super(set, set, set.getRange());
        }

        protected ReverseNavigableView(final NavigableSet<E> set, final SortedMapRange<E> range) {
            super(set, null, range);
        }

        @Override
        protected NavigableRangedSet<E> decorateDerived(final NavigableSet<E> subSet, final SortedMapRange<E> range) {
            return new ReverseNavigableView(subSet, range);
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

        @Override
        public E pollFirst() {
            return decorated().pollLast();
        }

        @Override
        public E pollLast() {
            return decorated().pollFirst();
        }

        @Override
        public final E removeFirst() {
            final E element = pollFirst();
            if (element != null) {
                return element;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public final E removeLast() {
            final E element = pollLast();
            if (element != null) {
                return element;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public NavigableRangedSet<E> descendingSet() {
            return reversed();
        }
    }
}
