package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections4.NavigableList;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.set.ReverseNavigableSet;

public class ReversedList<E>
        extends AbstractListDecorator<E, List<E>, List<E>> {
    private static final long serialVersionUID = -3297287219489077581L;

    ReversedList(final List<E> list) {
        super(list);
    }

    private int convertIndex(final int index) {
        return size() - index - 1;
    }

    @Override
    public E get(final int index) {
        return decorated().get(convertIndex(index));
    }

    @Override
    public E set(final int index, final E element) {
        return decorated().set(convertIndex(index), element);
    }

    @Override
    public void add(final int index, final E element) {
        decorated().add(convertIndex(index), element);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> coll) {
        // TODO coll needs reversing before adding?
        return decorated().addAll(convertIndex(index), coll);
    }

    @Override
    public E remove(final int index) {
        return decorated().remove(convertIndex(index));
    }

    @Override
    public int indexOf(final Object o) {
        return convertIndex(decorated().indexOf(o));
    }

    @Override
    public int lastIndexOf(final Object o) {
        return convertIndex(decorated().lastIndexOf(o));
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ReversedIndexListIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ReversedIndexListIterator(index);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return super.subList(convertIndex(toIndex), convertIndex(fromIndex));
    }

    @Override
    protected List<E> decorateSubList(final List<E> subList) {
        return new ReversedList<>(subList);
    }

    @Override
    protected List<E> makeReverse() {
        return decorated();
    }

    private class ReversedIndexListIterator implements ListIterator<E> {
        /** The list iterator being wrapped. */
        private final ListIterator<E> iterator;
        /** Flag to indicate if updating is possible at the moment. */
        private boolean validForUpdate = true;

        /**
         * Constructor that wraps a list iterator.
         *
         * @param startIndex start index from user's perspective
         */

        public ReversedIndexListIterator(final int startIndex) {
            iterator = decorated().listIterator(decorated().size() - startIndex);
        }

        /**
         * Checks whether there is another element.
         *
         * @return true if there is another element
         */
        @Override
        public boolean hasNext() {
            return iterator.hasPrevious();
        }

        /**
         * Gets the next element.
         * The next element is the previous in the list.
         *
         * @return the next element in the iterator
         */
        @Override
        public E next() {
            final E obj = iterator.previous();
            validForUpdate = true;
            return obj;
        }

        /**
         * Gets the index of the next element.
         *
         * @return the index of the next element in the iterator
         */
        @Override
        public int nextIndex() {
            return convertIndex(iterator.previousIndex());
        }

        /**
         * Checks whether there is a previous element.
         *
         * @return true if there is a previous element
         */
        @Override
        public boolean hasPrevious() {
            return iterator.hasNext();
        }

        /**
         * Gets the previous element.
         * The next element is the previous in the list.
         *
         * @return the previous element in the iterator
         */
        @Override
        public E previous() {
            final E obj = iterator.next();
            validForUpdate = true;
            return obj;
        }

        /**
         * Gets the index of the previous element.
         *
         * @return the index of the previous element in the iterator
         */
        @Override
        public int previousIndex() {
            return convertIndex(iterator.nextIndex());
        }

        /**
         * Removes the last returned element.
         *
         * @throws UnsupportedOperationException if the list is unmodifiable
         * @throws IllegalStateException if there is no element to remove
         */
        @Override
        public void remove() {
            if (!validForUpdate) {
                throw new IllegalStateException("Cannot remove from list until next() or previous() called");
            }
            iterator.remove();
        }

        /**
         * Replaces the last returned element.
         *
         * @param obj  the object to set
         * @throws UnsupportedOperationException if the list is unmodifiable
         * @throws IllegalStateException if the iterator is not in a valid state for set
         */
        @Override
        public void set(final E obj) {
            if (!validForUpdate) {
                throw new IllegalStateException("Cannot set to list until next() or previous() called");
            }
            iterator.set(obj);
        }

        /**
         * Adds a new element to the list between the next and previous elements.
         *
         * @param obj  the object to add
         * @throws UnsupportedOperationException if the list is unmodifiable
         * @throws IllegalStateException if the iterator is not in a valid state for set
         */
        @Override
        public void add(final E obj) {
            // the validForUpdate flag is needed as the necessary previous()
            // method call re-enables remove and add
            if (!validForUpdate) {
                throw new IllegalStateException("Cannot add to list until next() or previous() called");
            }
            validForUpdate = false;
            iterator.add(obj);
            iterator.previous();
        }
    }
}
