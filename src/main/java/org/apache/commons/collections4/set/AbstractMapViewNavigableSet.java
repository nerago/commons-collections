package org.apache.commons.collections4.set;

import java.util.NavigableSet;

import org.apache.commons.collections4.NavigableRangedSet;

public abstract class AbstractMapViewNavigableSet<E> extends AbstractMapViewSortedSet<E> implements NavigableRangedSet<E> {
    @Override
    public final NavigableRangedSet<E> reversed() {
        return new ReverseNavigableView<>(this);
    }

    @Override
    public final E removeFirst() {
        return NavigableRangedSet.super.removeFirst();
    }

    @Override
    public final E removeLast() {
        return NavigableRangedSet.super.removeLast();
    }

    protected static class ReverseNavigableView<E>
            extends ReverseView<E, NavigableSet<E>, NavigableRangedSet<E>> 
            implements NavigableRangedSet<E> {
        private static final long serialVersionUID = 5385328200121138393L;

        public ReverseNavigableView(final NavigableSet<E> set) {
            super(set);
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
        public NavigableRangedSet<E> descendingSet() {
            return new AbstractNavigableSetDecorator.NullDecorator<>(decorated().descendingSet());
        }

        @Override
        public NavigableRangedSet<E> reversed() {
            return descendingSet();
        }
    }
}
