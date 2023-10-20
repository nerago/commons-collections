package org.apache.commons.collections4.list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public class SortedList<E> extends AbstractListDecorator<E, List<E>, SortedList<E>> implements SortedRangedSet<E, SortedList<E>> {
    private Comparator<? super E> comparator;

    /**
     * Constructor that creates and wraps standard ArrayList.
     *
     * @param comparator provide order to sort elements
     * @throws NullPointerException if list is null
     */
    public SortedList(final Comparator<? super E> comparator) {
        super(new ArrayList<>());
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param list the list to decorate, must not be null
     * @param comparator provide order to sort elements
     * @throws NullPointerException if list is null
     */
    public SortedList(final List<E> list, final Comparator<? super E> comparator) {
        super(list);
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    /**
     * Returns the comparator used to order the elements in this set,
     * or <tt>null</tt> if this set uses the {@linkplain Comparable
     * natural ordering} of its elements.
     *
     * @return the comparator used to order the elements in this set,
     * or <tt>null</tt> if this set uses the natural ordering
     * of its elements
     */
    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        if (!decorated().isEmpty()) {
            return decorated().get(0);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
        if (!decorated().isEmpty()) {
            return decorated().get(decorated().size() - 1);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        decorated().replaceAll(operator);
        resortList();
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        throw new UnsupportedOperationException("should never need to call sort, and different comparator couldn't be used anyway");
    }

    /**
     * Should only be called if underlying list modified directly.
     */
    public void resortList() {
        decorated().sort(comparator);
    }



    /**
     * Range of elements included in this set instance (i.e. full set or sub set)
     *
     * @return set element range
     */
    @Override
    public SortedMapRange<E> getRange() {
        return null;
    }

    @Override
    public SortedList<E> subSet(SortedMapRange<E> range) {
        return null;
    }

    @Override
    protected SortedList<E> decorateSubList(List<E> subList) {
        return null;
    }
}
