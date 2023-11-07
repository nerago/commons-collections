package org.apache.commons.collections4.list;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.NavigableList;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.AbstractListIteratorDecorator;
import org.apache.commons.collections4.iterators.ReverseListIterator;

public class AutoSortingList<E>
        extends AbstractListDecorator<E, List<E>, NavigableList<E, ?>>
        implements NavigableList<E, NavigableList<E, ?>> {
    private static final long serialVersionUID = 3123908897367878525L;
    private Comparator<? super E> comparator;

    /**
     * Method that creates and wraps an ArrayList and uses Comparable objects natural sort order.
     * @param <E> list element type, must implement Comparable
     * @return a new empty auto sorted map
     */
    public static <E extends Comparable<? super E>> List<E> autoSortingList() {
        return new AutoSortingList<E>(new ArrayList<>(), Comparator.naturalOrder());
    }

    /**
     * Method that wraps given List and uses and uses Comparable objects natural sort order.
     * @param <E> list element type, must implement Comparable
     * @param list existing list, must not be null
     * @return a new auto sorted map wrapping parameter list
     */
    public static <E extends Comparable<? super E>> List<E> autoSortingList(final List<E> list) {
        return new AutoSortingList<>(list, Comparator.naturalOrder());
    }

    /**
     * Method that creates and wraps an ArrayList and orders by provided Comparator.
     * @param <E> list element type
     * @param comparator the comparator to define list order
     * @return a new empty auto sorted map
     */
    public static <E> List<E> autoSortingList(final Comparator<E> comparator) {
        return new AutoSortingList<>(new ArrayList<>(), comparator);
    }

    /**
     * Method that wraps a List and orders by provided Comparator.
     * @param <E> list element type
     * @param list the list to decorate, must not be null
     * @param comparator the comparator to define list order
     * @return a new auto sorted map wrapping parameter list
     */
    public static <E> List<E> autoSortingList(final List<E> list, final Comparator<E> comparator) {
        return new AutoSortingList<>(list, comparator);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param list the list to decorate, must not be null
     * @param comparator provide order to sort elements
     * @throws NullPointerException if list or comparator is null
     */
    protected AutoSortingList(final List<E> list, final Comparator<? super E> comparator) {
        super(list);
        this.comparator = Objects.requireNonNull(comparator, "comparator");
        redoSort();
    }

    @SuppressWarnings("unchecked")
    private static <E> E castElementNotNull(final Object target) {
        return (E) Objects.requireNonNull(target);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean add(final E object) {
        final int index = floorIndex(object);
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
    public void addFirst(final E object) {
        add(object);
    }

    @Override
    public void addLast(final E object) {
        add(object);
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
    public E getFirst() {
        return first();
    }

    @Override
    public E getLast() {
        return last();
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
    public boolean contains(final Object object) {
        return _indexOf(castElementNotNull(object)) != -1;
    }

    @Override
    public boolean containsAll(final Collection<?> coll) {
        for (final Object object : coll) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int lastIndexOf(Object object) {
        return super.lastIndexOf(object);
    }

    @Override
    public int indexOf(final Object target) {
        return _indexOf(castElementNotNull(target));
    }

    private int _indexOf(final E target) {
        final List<E> coll = decorated();
        int lo = 0, hi = coll.size() - 1;
        while (lo <= hi) {
            final int mid = lo + (hi - lo) / 2;
            final E check = coll.get(mid);
            final int cmp = comparator.compare(check, target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    @Override
    public E lower(final E target) {
        return getIfValid(lowerIndex(target));
    }

    @Override
    public E floor(final E target) {
        return getIfValid(floorIndex(target));
    }

    @Override
    public E ceiling(final E target) {
        return getIfValid(ceilingIndex(target));
    }

    @Override
    public E higher(final E target) {
        return getIfValid(higherIndex(target));
    }

    @Override
    public int lowerIndex(final E target) {
        Objects.requireNonNull(target);
        final List<E> coll = decorated();
        int lo = 0, hi = coll.size() - 1;
        int best = -1;
        while (lo <= hi) {
            final int mid = lo + (hi - lo) / 2;
            final E check = coll.get(mid);
            final int cmp = comparator.compare(check, target);
            if (cmp < 0) {
                best = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return best;
    }

    @Override
    public int floorIndex(final E target) {
        Objects.requireNonNull(target);
        final List<E> coll = decorated();
        int lo = 0, hi = coll.size() - 1;
        int best = -1;
        while (lo <= hi) {
            final int mid = lo + (hi - lo) / 2;
            final E check = coll.get(mid);
            final int cmp = comparator.compare(check, target);
            if (cmp <= 0) {
                best = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return best;
    }

    @Override
    public int ceilingIndex(final E target) {
        Objects.requireNonNull(target);
        final List<E> coll = decorated();
        int lo = 0, hi = coll.size() - 1;
        int best = -1;
        while (lo <= hi) {
            final int mid = lo + (hi - lo) / 2;
            final E check = coll.get(mid);
            final int cmp = comparator.compare(check, target);
            if (cmp >= 0) {
                best = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return best;
    }

    @Override
    public int higherIndex(final E target) {
        Objects.requireNonNull(target);
        final List<E> coll = decorated();
        int lo = 0, hi = coll.size() - 1;
        int best = -1;
        while (lo <= hi) {
            final int mid = lo + (hi - lo) / 2;
            final E check = coll.get(mid);
            final int cmp = comparator.compare(check, target);
            if (cmp > 0) {
                best = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        return best;
    }

    protected int rangeIndex(final E target, int min, int max, boolean hiIsTrue, boolean includeEqual) {
        // look how we need
        return -1;
    }

    @Override
    public E pollFirst() {
        final List<E> coll = decorated();
        if (!coll.isEmpty()) {
            return coll.remove(0);
        } else {
            return null;
        }
    }

    @Override
    public E pollLast() {
        final List<E> coll = decorated();
        final int size = coll.size();
        if (size > 0) {
            return coll.remove(size - 1);
        } else {
            return null;
        }
    }

    @Override
    public E removeFirst() {
        return pollFirst();
    }

    @Override
    public E removeLast() {
        return pollLast();
    }

    @Override
    public NavigableList<E, ?> subSet(final SortedMapRange<E> range) {
        return new AutoSortedSubList<>(this, range);
    }

    @Override
    protected NavigableList<E, ?> decorateSubList(final List<E> subList) {
        return new AutoSortedSubList<>(subList);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ReadOrRemoveListIterator<>(decorated().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ReadOrRemoveListIterator<>(decorated().listIterator(index));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReverseListIterator<>(decorated());
    }

    @Override
    protected NavigableList<E, ?> makeReverse() {
        return new ReversedNavigableList<>(this, getRange());
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        comparator = (Comparator<? super E>) in.readObject();
    }

    private E getIfValid(final int index) {
        final List<E> coll = decorated();
        if (index >= 0 && index < coll.size()) {
            return coll.get(index);
        } else {
            return null;
        }
    }

    // some are value ranged based, some more index range

    public static class AutoSortedSubList<E> extends AbstractSet<E> implements NavigableList<E, NavigableList<E, ?>> {
        private final AutoSortingList<E> parent;
        private final SortedMapRange<E> range;

        AutoSortedSubList(final AutoSortingList<E> parent, final SortedMapRange<E> range) {
            this.parent = parent;
            this.range = range;
        }

        @Override
        public SortedMapRange<E> getRange() {
            return range;
        }

        @Override
        public Comparator<? super E> comparator() {
            return parent.comparator;
        }

        @Override
        public Iterator<E> iterator() {
            return null; // TODO
        }

        @Override
        public Iterator<E> descendingIterator() {
            return null; // TODO
        }

        @Override
        public Spliterator<E> spliterator() {
            return null; // TODO
        }

        @Override
        public int size() {
            return 0; // TODO
        }

        @Override
        public boolean contains(final Object obj) {
            return range.contains(castElementNotNull(obj)) && parent.contains(obj);
        }

        @Override
        public boolean add(final E element) {
            if (range.contains(element)) {
                return parent.add(element);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean remove(final Object obj) {
            final E element = castElementNotNull(obj);
            if (range.contains(element)) {
                return parent.remove(element);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            return false;
        }

        @Override
        public E get(int index) {
            return null;
        }

        @Override
        public E set(int index, E element) {
            return null;
        }

        @Override
        public void add(int index, E element) {

        }
        @Override
        public E remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<E> listIterator() {
            return null;
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return null;
        }

        @Override
        public AutoSortedSubList<E> subSet(final SortedMapRange<E> range) {
            return new AutoSortedSubList<>(parent, range);
        }

        @Override
        public NavigableList<E, ?> descendingSet() {
            return new ReversedNavigableList<>(this, range);  // TODO
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
        public E lower(final E e) {
            return null;
        }

        @Override
        public E floor(final E e) {
            return null;
        }

        @Override
        public E ceiling(final E e) {
            return null;
        }

        @Override
        public E higher(final E e) {
            return null;
        }

        @Override
        public E pollFirst() {
            return null;
        }

        @Override
        public E pollLast() {
            return null;
        }

        @Override
        public int lowerIndex(E e) {
            return 0;
        }

        @Override
        public int floorIndex(E e) {
            return 0;
        }

        @Override
        public int ceilingIndex(E e) {
            return 0;
        }

        @Override
        public int higherIndex(E e) {
            return 0;
        }

        @Override
        public NavigableList<E, ?> subList(int fromIndex, int toIndex) {
            return null;
        }

        @Override
        public NavigableList<E, ?> reversed() {
            return null;
        }

        @Override
        public E getFirst() {
            return null;
        }

        @Override
        public E getLast() {
            return null;
        }

        @Override
        public void addFirst(E e) {

        }

        @Override
        public void addLast(E e) {

        }

        @Override
        public E removeFirst() {
            return null;
        }

        @Override
        public E removeLast() {
            return null;
        }
    }

    public static class AutoSortedSubListReverse<E> extends AutoSortedSubList<E> {
        AutoSortedSubListReverse(final AutoSortingList<E> parent, final SortedMapRange<E> range) {
            super(parent, range);
        }

        @Override
        public Spliterator<E> spliterator() {
            return null;
        }

        // or just wrap in reverseset
    }

    private static final class ReadOrRemoveListIterator<E> extends AbstractListIteratorDecorator<E> {
        private ReadOrRemoveListIterator(final ListIterator<E> decorated) {
            super(decorated);
        }

        /**
         * AutoSortingList can't support the normal contract of {@link ListIterator#add} since the inserted element would be out
         * of position for expected next/previous values as documented in that interface method.
         */
        @Override
        public void add(final E obj) {
            throw new UnsupportedOperationException();
        }

        /**
         * AutoSortingList could support a version of this {@link ListIterator#set} since the exact text of that interface method
         * doesn't fully specify how it should affect indexes and next/previous, however to avoid skipping elements after a
         * possible list reshuffle we'd may have to adjust {@link #nextIndex} and {@link #previousIndex} values on the fly
         * which could violate expectations.
         */
        @Override
        public void set(final E obj) {
            throw new UnsupportedOperationException();
        }

        // remove is allowed since removal in place can't mess up existing sort order
    }
}
