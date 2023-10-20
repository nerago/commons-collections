package org.apache.commons.collections4.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public class AutoSortingList<E> extends AbstractListDecorator<E, List<E>, AutoSortingList<E>> implements SortedRangedSet<E, AutoSortingList<E>> {
    private static final long serialVersionUID = 3123908897367878525L;
    private Comparator<? super E> comparator;

    /**
     * Constructor that creates and wraps standard ArrayList.
     *
     * @param comparator provide order to sort elements
     * @throws NullPointerException if list is null
     */
    public AutoSortingList(final Comparator<? super E> comparator) {
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
    public AutoSortingList(final List<E> list, final Comparator<? super E> comparator) {
        super(list);
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private int findInsertIndex(final E add) {
        int lo = 0, hi = decorated().size() - 1;
        while (lo < hi) {
            final int mid = lo + (hi - lo) / 2;
            final E midElement = decorated().get(mid);
            final int cmp = comparator.compare(add, midElement);
            if (cmp < 0) {
                hi = mid - 1;
                // know: add < old_mid && hi <= old_mid
                // options: (add < hi <= old_mid) or (hi <= add < old_mid)
                // if higher than current hi
            } else if (cmp > 0) {
                lo = mid + 1;
                // know old_mid < add && old_mid <= lo
                // options: (old_mid < add <= lo) or (old_mid <= lo < add)
                // if lower than current lo, insert at current lo
            } else {
                return mid;
            }
        }

        if (lo == hi) {
            final E midElement = decorated().get(lo);
            final int cmp = comparator.compare(add, midElement);
            return cmp < 0 ? lo : lo + 1;
        } else {
            return lo;
        }

        /*
         * lo hi basics
         * diff  mid
         * 0     0     --..z    or    a
         * 1     0
         * 2     1
         * 3     1
         * 4     2
         * 5     2
         */


        /*
         * lo  hi  mid   if<then X      if>then X
         * 1   1   1     1..0    1      2..1    2
         * 1   2   1     1..0    1      2..2    23
         * 1   3   2     1..1    12     3..3    34
         * 1   4   2     1..1    12     3..4
         * 2   2   2     2..1    2      3..2    3
         * 2   3   2     2..1    2      3..3    34
         * 2   4   3     2..2    23     4..4
         * 3   3   3     3..2    3      4..3    4
         * 3   4   3     3..2           4..4    45
         * 4   4   4     4..3           5..4      0a1234  01a234  012a34  0123a4  01234a
         *
         */

    }

    @Override
    public boolean add(final E object) {
        final int index = findInsertIndex(object);
        decorated().add(index, object);
        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        boolean changed = false;
        for (final E item : coll) {
            add(item);
            changed = true;
        }
        return changed;
    }

    @Override
    public E set(final int index, final E newValue) {
        // remove then item at given index, then add the new value in sorted position
        final E oldValue = decorated().remove(index);
        add(newValue);
        return oldValue;
    }

    @Override
    public void add(final int index, final E object) {
        // ignore specified index
        add(object);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> coll) {
        // ignore specified index
        return addAll(coll);
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
        redoSort();
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        throw new UnsupportedOperationException("should never need to call sort, and different comparator couldn't be used anyway");
    }

    /**
     * Should only be called if underlying list modified directly.
     */
    public void redoSort() {
        decorated().sort(comparator);
    }

    /**
     * Range of elements included in this set instance (i.e. full set or sub set)
     *
     * @return set element range
     */
    @Override
    public SortedMapRange<E> getRange() {
        return SortedMapRange.full(comparator);
    }

    @Override
    public AutoSortingList<E> subSet(SortedMapRange<E> range) {
        return null;
    }

    @Override
    protected AutoSortingList<E> decorateSubList(List<E> subList) {
        return null;
    }


    @Override
    public ListIterator<E> listIterator() {
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }
}
