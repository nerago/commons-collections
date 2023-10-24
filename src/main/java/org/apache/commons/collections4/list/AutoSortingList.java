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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.commons.collections4.NavigableList;
import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.AbstractListIteratorDecorator;
import org.apache.commons.collections4.iterators.ReverseListIterator;

public class AutoSortingList<E>
        extends AbstractListDecorator<E, List<E>, AutoSortingList.AutoSortingSubList<E>>
        implements NavigableList<E, AutoSortingList.AutoSortingSubList<E>, AutoSortingList.AutoSortedSubSet<E>> {
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
    public AutoSortedSubSet<E> subSet(final SortedMapRange<E> range) {
        return new AutoSortedSubSet<>(this, range);
    }

    @Override
    protected AutoSortingSubList<E> decorateSubList(final List<E> subList) {
        return new AutoSortingSubList<>(subList);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new PartlyModifiableListIterator<>(decorated().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new PartlyModifiableListIterator<>(decorated().listIterator(index));
    }

    @Override
    public AutoSortedSubSet<E> descendingSet() {
        return new AutoSortedSubSetReverse<>(this, getRange().reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReverseListIterator<>(decorated());
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

    public static class AutoSortedSubSet<E> extends AbstractSet<E> implements NavigableRangedSet<E, AutoSortedSubSet<E>> {
        private final AutoSortingList<E> parent;
        private final SortedMapRange<E> range;

        AutoSortedSubSet(final AutoSortingList<E> parent, final SortedMapRange<E> range) {
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
        public int size() {
            return 0; // TODO
        }

        @Override
        public boolean contains(final Object obj) {
            return range.inRange(castElementNotNull(obj)) && parent.contains(obj);
        }

        @Override
        public boolean add(final E element) {
            if (range.inRange(element)) {
                return parent.add(element);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean remove(final Object obj) {
            final E element = castElementNotNull(obj);
            if (range.inRange(element)) {
                return parent.remove(element);
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Returns the first (lowest) element currently in this set.
         *
         * @return the first (lowest) element currently in this set
         * @throws NoSuchElementException if this set is empty
         */
        @Override
        public E first() {
            return null;
        }

        /**
         * Returns the last (highest) element currently in this set.
         *
         * @return the last (highest) element currently in this set
         * @throws NoSuchElementException if this set is empty
         */
        @Override
        public E last() {
            return null;
        }

        /**
         * Retrieves and removes the first (lowest) element,
         * or returns {@code null} if this set is empty.
         *
         * @return the first element, or {@code null} if this set is empty
         */
        @Override
        public E pollFirst() {
            return null;
        }

        /**
         * Retrieves and removes the last (highest) element,
         * or returns {@code null} if this set is empty.
         *
         * @return the last element, or {@code null} if this set is empty
         */
        @Override
        public E pollLast() {
            return null;
        }

        /**
         * Returns the greatest element in this set strictly less than the
         * given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the greatest element less than {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E lower(E e) {
            return null;
        }

        /**
         * Returns the greatest element in this set less than or equal to
         * the given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the greatest element less than or equal to {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E floor(E e) {
            return null;
        }

        /**
         * Returns the least element in this set greater than or equal to
         * the given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the least element greater than or equal to {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E ceiling(E e) {
            return null;
        }

        /**
         * Returns the least element in this set strictly greater than the
         * given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the least element greater than {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E higher(E e) {
            return null;
        }

        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection
         */
        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public AutoSortedSubSet<E> subSet(final SortedMapRange<E> range) {
            return new AutoSortedSubSet<>(parent, range);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over this collection, checking each
         * element returned by the iterator in turn to see if it's contained
         * in the specified collection.  If it's not so contained, it's removed
         * from this collection with the iterator's <tt>remove</tt> method.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by the
         * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
         * and this collection contains one or more elements not present in the
         * specified collection.
         *
         * @param c
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException            {@inheritDoc}
         * @throws NullPointerException          {@inheritDoc}
         * @see #remove(Object)
         * @see #contains(Object)
         */
        @Override
        public boolean retainAll(Collection<?> c) {
            return super.retainAll(c);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over this collection, removing each
         * element using the <tt>Iterator.remove</tt> operation.  Most
         * implementations will probably choose to override this method for
         * efficiency.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by this
         * collection's <tt>iterator</tt> method does not implement the
         * <tt>remove</tt> method and this collection is non-empty.
         *
         * @throws UnsupportedOperationException {@inheritDoc}
         */
        @Override
        public void clear() {
            super.clear();
        }

        /**
         * Returns a string representation of this collection.  The string
         * representation consists of a list of the collection's elements in the
         * order they are returned by its iterator, enclosed in square brackets
         * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
         * <tt>", "</tt> (comma and space).  Elements are converted to strings as
         * by {@link String#valueOf(Object)}.
         *
         * @return a string representation of this collection
         */
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public AutoSortedSubSet<E> descendingSet() {
            return null;
        }

        /**
         * Returns an iterator over the elements in this set, in descending order.
         * Equivalent in effect to {@code descendingSet().iterator()}.
         *
         * @return an iterator over the elements in this set, in descending order
         */
        @Override
        public Iterator<E> descendingIterator() {
            return null;
        }

        /**
         * Removes all of the elements of this collection that satisfy the given
         * predicate.  Errors or runtime exceptions thrown during iteration or by
         * the predicate are relayed to the caller.
         *
         * @param filter a predicate which returns {@code true} for elements to be
         *               removed
         * @return {@code true} if any elements were removed
         * @throws NullPointerException          if the specified filter is null
         * @throws UnsupportedOperationException if elements cannot be removed
         *                                       from this collection.  Implementations may throw this exception if a
         *                                       matching element cannot be removed or if, in general, removal is not
         *                                       supported.
         * @implSpec The default implementation traverses all elements of the collection using
         * its {@link #iterator}.  Each matching element is removed using
         * {@link Iterator#remove()}.  If the collection's iterator does not
         * support removal then an {@code UnsupportedOperationException} will be
         * thrown on the first matching element.
         * @since 1.8
         */
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return super.removeIf(filter);
        }

        /**
         * Returns a sequential {@code Stream} with this collection as its source.
         *
         * <p>This method should be overridden when the {@link #spliterator()}
         * method cannot return a spliterator that is {@code IMMUTABLE},
         * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
         * for details.)
         *
         * @return a sequential {@code Stream} over the elements in this collection
         * @implSpec The default implementation creates a sequential {@code Stream} from the
         * collection's {@code Spliterator}.
         * @since 1.8
         */
        @Override
        public Stream<E> stream() {
            return super.stream();
        }

        /**
         * Returns a possibly parallel {@code Stream} with this collection as its
         * source.  It is allowable for this method to return a sequential stream.
         *
         * <p>This method should be overridden when the {@link #spliterator()}
         * method cannot return a spliterator that is {@code IMMUTABLE},
         * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
         * for details.)
         *
         * @return a possibly parallel {@code Stream} over the elements in this
         * collection
         * @implSpec The default implementation creates a parallel {@code Stream} from the
         * collection's {@code Spliterator}.
         * @since 1.8
         */
        @Override
        public Stream<E> parallelStream() {
            return super.parallelStream();
        }

        /**
         * Performs the given action for each element of the {@code Iterable}
         * until all elements have been processed or the action throws an
         * exception.  Unless otherwise specified by the implementing class,
         * actions are performed in the order of iteration (if an iteration order
         * is specified).  Exceptions thrown by the action are relayed to the
         * caller.
         *
         * @param action The action to be performed for each element
         * @throws NullPointerException if the specified action is null
         * @implSpec <p>The default implementation behaves as if:
         * <pre>{@code
         *     for (T t : this)
         *         action.accept(t);
         * }</pre>
         * @since 1.8
         */
        @Override
        public void forEach(Consumer<? super E> action) {
            super.forEach(action);
        }
    }

    public static class AutoSortedSubSetReverse<E> extends AutoSortedSubSet<E> {
        AutoSortedSubSetReverse(final AutoSortingList<E> parent, final SortedMapRange<E> range) {
            super(parent, range);
        }

        /**
         * Returns the greatest element in this set strictly less than the
         * given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the greatest element less than {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E lower(E e) {
            return null;
        }

        /**
         * Returns the greatest element in this set less than or equal to
         * the given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the greatest element less than or equal to {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E floor(E e) {
            return null;
        }

        /**
         * Returns the least element in this set greater than or equal to
         * the given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the least element greater than or equal to {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E ceiling(E e) {
            return null;
        }

        /**
         * Returns the least element in this set strictly greater than the
         * given element, or {@code null} if there is no such element.
         *
         * @param e the value to match
         * @return the least element greater than {@code e},
         * or {@code null} if there is no such element
         * @throws ClassCastException   if the specified element cannot be
         *                              compared with the elements currently in the set
         * @throws NullPointerException if the specified element is null
         *                              and this set does not permit null elements
         */
        @Override
        public E higher(E e) {
            return null;
        }

        /**
         * Retrieves and removes the first (lowest) element,
         * or returns {@code null} if this set is empty.
         *
         * @return the first element, or {@code null} if this set is empty
         */
        @Override
        public E pollFirst() {
            return null;
        }

        /**
         * Retrieves and removes the last (highest) element,
         * or returns {@code null} if this set is empty.
         *
         * @return the last element, or {@code null} if this set is empty
         */
        @Override
        public E pollLast() {
            return null;
        }

        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection
         */
        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
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
        public AutoSortedSubSet<E> subSet(SortedMapRange<E> range) {
            return null;
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
            return null;
        }

        /**
         * Returns the first (lowest) element currently in this set.
         *
         * @return the first (lowest) element currently in this set
         * @throws NoSuchElementException if this set is empty
         */
        @Override
        public E first() {
            return null;
        }

        /**
         * Returns the last (highest) element currently in this set.
         *
         * @return the last (highest) element currently in this set
         * @throws NoSuchElementException if this set is empty
         */
        @Override
        public E last() {
            return null;
        }

        /**
         * Compares the specified object with this set for equality.  Returns
         * <tt>true</tt> if the given object is also a set, the two sets have
         * the same size, and every member of the given set is contained in
         * this set.  This ensures that the <tt>equals</tt> method works
         * properly across different implementations of the <tt>Set</tt>
         * interface.<p>
         * <p>
         * This implementation first checks if the specified object is this
         * set; if so it returns <tt>true</tt>.  Then, it checks if the
         * specified object is a set whose size is identical to the size of
         * this set; if not, it returns false.  If so, it returns
         * <tt>containsAll((Collection) o)</tt>.
         *
         * @param o object to be compared for equality with this set
         * @return <tt>true</tt> if the specified object is equal to this set
         */
        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        /**
         * Returns the hash code value for this set.  The hash code of a set is
         * defined to be the sum of the hash codes of the elements in the set,
         * where the hash code of a <tt>null</tt> element is defined to be zero.
         * This ensures that <tt>s1.equals(s2)</tt> implies that
         * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
         * and <tt>s2</tt>, as required by the general contract of
         * {@link Object#hashCode}.
         *
         * <p>This implementation iterates over the set, calling the
         * <tt>hashCode</tt> method on each element in the set, and adding up
         * the results.
         *
         * @return the hash code value for this set
         * @see Object#equals(Object)
         * @see Set#equals(Object)
         */
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Removes from this set all of its elements that are contained in the
         * specified collection (optional operation).  If the specified
         * collection is also a set, this operation effectively modifies this
         * set so that its value is the <i>asymmetric set difference</i> of
         * the two sets.
         *
         * <p>This implementation determines which is the smaller of this set
         * and the specified collection, by invoking the <tt>size</tt>
         * method on each.  If this set has fewer elements, then the
         * implementation iterates over this set, checking each element
         * returned by the iterator in turn to see if it is contained in
         * the specified collection.  If it is so contained, it is removed
         * from this set with the iterator's <tt>remove</tt> method.  If
         * the specified collection has fewer elements, then the
         * implementation iterates over the specified collection, removing
         * from this set each element returned by the iterator, using this
         * set's <tt>remove</tt> method.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by the
         * <tt>iterator</tt> method does not implement the <tt>remove</tt> method.
         *
         * @param c collection containing elements to be removed from this set
         * @return <tt>true</tt> if this set changed as a result of the call
         * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
         *                                       is not supported by this set
         * @throws ClassCastException            if the class of an element of this set
         *                                       is incompatible with the specified collection
         *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException          if this set contains a null element and the
         *                                       specified collection does not permit null elements
         *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
         *                                       or if the specified collection is null
         * @see #remove(Object)
         * @see #contains(Object)
         */
        @Override
        public boolean removeAll(Collection<?> c) {
            return super.removeAll(c);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation returns <tt>size() == 0</tt>.
         */
        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over the elements in the collection,
         * checking each element in turn for equality with the specified element.
         *
         * @param o
         * @throws ClassCastException   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        public boolean contains(Object o) {
            return super.contains(o);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation returns an array containing all the elements
         * returned by this collection's iterator, in the same order, stored in
         * consecutive elements of the array, starting with index {@code 0}.
         * The length of the returned array is equal to the number of elements
         * returned by the iterator, even if the size of this collection changes
         * during iteration, as might happen if the collection permits
         * concurrent modification during iteration.  The {@code size} method is
         * called only as an optimization hint; the correct result is returned
         * even if the iterator returns a different number of elements.
         *
         * <p>This method is equivalent to:
         *
         * <pre> {@code
         * List<E> list = new ArrayList<E>(size());
         * for (E e : this)
         *     list.add(e);
         * return list.toArray();
         * }</pre>
         */
        @Override
        public Object[] toArray() {
            return super.toArray();
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation returns an array containing all the elements
         * returned by this collection's iterator in the same order, stored in
         * consecutive elements of the array, starting with index {@code 0}.
         * If the number of elements returned by the iterator is too large to
         * fit into the specified array, then the elements are returned in a
         * newly allocated array with length equal to the number of elements
         * returned by the iterator, even if the size of this collection
         * changes during iteration, as might happen if the collection permits
         * concurrent modification during iteration.  The {@code size} method is
         * called only as an optimization hint; the correct result is returned
         * even if the iterator returns a different number of elements.
         *
         * <p>This method is equivalent to:
         *
         * <pre> {@code
         * List<E> list = new ArrayList<E>(size());
         * for (E e : this)
         *     list.add(e);
         * return list.toArray(a);
         * }</pre>
         *
         * @param a
         * @throws ArrayStoreException  {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        public <T> T[] toArray(T[] a) {
            return super.toArray(a);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation always throws an
         * <tt>UnsupportedOperationException</tt>.
         *
         * @param e
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException            {@inheritDoc}
         * @throws NullPointerException          {@inheritDoc}
         * @throws IllegalArgumentException      {@inheritDoc}
         * @throws IllegalStateException         {@inheritDoc}
         */
        @Override
        public boolean add(E e) {
            return super.add(e);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over the collection looking for the
         * specified element.  If it finds the element, it removes the element
         * from the collection using the iterator's remove method.
         *
         * <p>Note that this implementation throws an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by this
         * collection's iterator method does not implement the <tt>remove</tt>
         * method and this collection contains the specified object.
         *
         * @param o
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException            {@inheritDoc}
         * @throws NullPointerException          {@inheritDoc}
         */
        @Override
        public boolean remove(Object o) {
            return super.remove(o);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over the specified collection,
         * checking each element returned by the iterator in turn to see
         * if it's contained in this collection.  If all elements are so
         * contained <tt>true</tt> is returned, otherwise <tt>false</tt>.
         *
         * @param c
         * @throws ClassCastException   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         * @see #contains(Object)
         */
        @Override
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over the specified collection, and adds
         * each object returned by the iterator to this collection, in turn.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is
         * overridden (assuming the specified collection is non-empty).
         *
         * @param c
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException            {@inheritDoc}
         * @throws NullPointerException          {@inheritDoc}
         * @throws IllegalArgumentException      {@inheritDoc}
         * @throws IllegalStateException         {@inheritDoc}
         * @see #add(Object)
         */
        @Override
        public boolean addAll(Collection<? extends E> c) {
            return super.addAll(c);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over this collection, checking each
         * element returned by the iterator in turn to see if it's contained
         * in the specified collection.  If it's not so contained, it's removed
         * from this collection with the iterator's <tt>remove</tt> method.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by the
         * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
         * and this collection contains one or more elements not present in the
         * specified collection.
         *
         * @param c
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException            {@inheritDoc}
         * @throws NullPointerException          {@inheritDoc}
         * @see #remove(Object)
         * @see #contains(Object)
         */
        @Override
        public boolean retainAll(Collection<?> c) {
            return super.retainAll(c);
        }

        /**
         * {@inheritDoc}
         *
         * <p>This implementation iterates over this collection, removing each
         * element using the <tt>Iterator.remove</tt> operation.  Most
         * implementations will probably choose to override this method for
         * efficiency.
         *
         * <p>Note that this implementation will throw an
         * <tt>UnsupportedOperationException</tt> if the iterator returned by this
         * collection's <tt>iterator</tt> method does not implement the
         * <tt>remove</tt> method and this collection is non-empty.
         *
         * @throws UnsupportedOperationException {@inheritDoc}
         */
        @Override
        public void clear() {
            super.clear();
        }

        /**
         * Returns a string representation of this collection.  The string
         * representation consists of a list of the collection's elements in the
         * order they are returned by its iterator, enclosed in square brackets
         * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
         * <tt>", "</tt> (comma and space).  Elements are converted to strings as
         * by {@link String#valueOf(Object)}.
         *
         * @return a string representation of this collection
         */
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public AutoSortedSubSet<E> descendingSet() {
            return null;
        }

        /**
         * Returns an iterator over the elements in this set, in descending order.
         * Equivalent in effect to {@code descendingSet().iterator()}.
         *
         * @return an iterator over the elements in this set, in descending order
         */
        @Override
        public Iterator<E> descendingIterator() {
            return null;
        }

        /**
         * Removes all of the elements of this collection that satisfy the given
         * predicate.  Errors or runtime exceptions thrown during iteration or by
         * the predicate are relayed to the caller.
         *
         * @param filter a predicate which returns {@code true} for elements to be
         *               removed
         * @return {@code true} if any elements were removed
         * @throws NullPointerException          if the specified filter is null
         * @throws UnsupportedOperationException if elements cannot be removed
         *                                       from this collection.  Implementations may throw this exception if a
         *                                       matching element cannot be removed or if, in general, removal is not
         *                                       supported.
         * @implSpec The default implementation traverses all elements of the collection using
         * its {@link #iterator}.  Each matching element is removed using
         * {@link Iterator#remove()}.  If the collection's iterator does not
         * support removal then an {@code UnsupportedOperationException} will be
         * thrown on the first matching element.
         * @since 1.8
         */
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return super.removeIf(filter);
        }

        /**
         * Returns a sequential {@code Stream} with this collection as its source.
         *
         * <p>This method should be overridden when the {@link #spliterator()}
         * method cannot return a spliterator that is {@code IMMUTABLE},
         * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
         * for details.)
         *
         * @return a sequential {@code Stream} over the elements in this collection
         * @implSpec The default implementation creates a sequential {@code Stream} from the
         * collection's {@code Spliterator}.
         * @since 1.8
         */
        @Override
        public Stream<E> stream() {
            return super.stream();
        }

        /**
         * Returns a possibly parallel {@code Stream} with this collection as its
         * source.  It is allowable for this method to return a sequential stream.
         *
         * <p>This method should be overridden when the {@link #spliterator()}
         * method cannot return a spliterator that is {@code IMMUTABLE},
         * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
         * for details.)
         *
         * @return a possibly parallel {@code Stream} over the elements in this
         * collection
         * @implSpec The default implementation creates a parallel {@code Stream} from the
         * collection's {@code Spliterator}.
         * @since 1.8
         */
        @Override
        public Stream<E> parallelStream() {
            return super.parallelStream();
        }

        /**
         * Performs the given action for each element of the {@code Iterable}
         * until all elements have been processed or the action throws an
         * exception.  Unless otherwise specified by the implementing class,
         * actions are performed in the order of iteration (if an iteration order
         * is specified).  Exceptions thrown by the action are relayed to the
         * caller.
         *
         * @param action The action to be performed for each element
         * @throws NullPointerException if the specified action is null
         * @implSpec <p>The default implementation behaves as if:
         * <pre>{@code
         *     for (T t : this)
         *         action.accept(t);
         * }</pre>
         * @since 1.8
         */
        @Override
        public void forEach(Consumer<? super E> action) {
            super.forEach(action);
        }
    }

    public static class AutoSortingSubList<E> extends AbstractListDecorator<E, List<E>, AutoSortingSubList<E>> {

        public AutoSortingSubList(List<E> subList) {
        }

        @Override
        protected AutoSortingSubList<E> decorateSubList(List<E> subList) {
            return null;
        }

        /**
         * Replaces each element of this list with the result of applying the
         * operator to that element.  Errors or runtime exceptions thrown by
         * the operator are relayed to the caller.
         *
         * @param operator the operator to apply to each element
         * @throws UnsupportedOperationException if this list is unmodifiable.
         *                                       Implementations may throw this exception if an element
         *                                       cannot be replaced or if, in general, modification is not
         *                                       supported
         * @throws NullPointerException          if the specified operator is null or
         *                                       if the operator result is a null value and this list does
         *                                       not permit null elements
         *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
         * @implSpec The default implementation is equivalent to, for this {@code list}:
         * <pre>{@code
         *     final ListIterator<E> li = list.listIterator();
         *     while (li.hasNext()) {
         *         li.set(operator.apply(li.next()));
         *     }
         * }</pre>
         * <p>
         * If the list's list-iterator does not support the {@code set} operation
         * then an {@code UnsupportedOperationException} will be thrown when
         * replacing the first element.
         * @since 1.8
         */
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            super.replaceAll(operator);
        }

        /**
         * Sorts this list according to the order induced by the specified
         * {@link Comparator}.
         *
         * <p>All elements in this list must be <i>mutually comparable</i> using the
         * specified comparator (that is, {@code c.compare(e1, e2)} must not throw
         * a {@code ClassCastException} for any elements {@code e1} and {@code e2}
         * in the list).
         *
         * <p>If the specified comparator is {@code null} then all elements in this
         * list must implement the {@link Comparable} interface and the elements'
         * {@linkplain Comparable natural ordering} should be used.
         *
         * <p>This list must be modifiable, but need not be resizable.
         *
         * @param c the {@code Comparator} used to compare list elements.
         *          A {@code null} value indicates that the elements'
         *          {@linkplain Comparable natural ordering} should be used
         * @throws ClassCastException            if the list contains elements that are not
         *                                       <i>mutually comparable</i> using the specified comparator
         * @throws UnsupportedOperationException if the list's list-iterator does
         *                                       not support the {@code set} operation
         * @throws IllegalArgumentException      (<a href="Collection.html#optional-restrictions">optional</a>)
         *                                       if the comparator is found to violate the {@link Comparator}
         *                                       contract
         * @implSpec The default implementation obtains an array containing all elements in
         * this list, sorts the array, and iterates over this list resetting each
         * element from the corresponding position in the array. (This avoids the
         * n<sup>2</sup> log(n) performance that would result from attempting
         * to sort a linked list in place.)
         * @implNote This implementation is a stable, adaptive, iterative mergesort that
         * requires far fewer than n lg(n) comparisons when the input array is
         * partially sorted, while offering the performance of a traditional
         * mergesort when the input array is randomly ordered.  If the input array
         * is nearly sorted, the implementation requires approximately n
         * comparisons.  Temporary storage requirements vary from a small constant
         * for nearly sorted input arrays to n/2 object references for randomly
         * ordered input arrays.
         *
         * <p>The implementation takes equal advantage of ascending and
         * descending order in its input array, and can take advantage of
         * ascending and descending order in different parts of the same
         * input array.  It is well-suited to merging two or more sorted arrays:
         * simply concatenate the arrays and sort the resulting array.
         *
         * <p>The implementation was adapted from Tim Peters's list sort for Python
         * (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
         * TimSort</a>).  It uses techniques from Peter McIlroy's "Optimistic
         * Sorting and Information Theoretic Complexity", in Proceedings of the
         * Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
         * January 1993.
         * @since 1.8
         */
        @Override
        public void sort(Comparator<? super E> c) {
            super.sort(c);
        }
    }

    private static final class PartlyModifiableListIterator<E> extends AbstractListIteratorDecorator<E> {
        private E lastResult;

        private PartlyModifiableListIterator(final ListIterator<E> decorated) {
            super(decorated);
        }

        @Override
        public E next() {
            return lastResult = super.next();
        }

        @Override
        public E previous() {
            return lastResult = super.previous();
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
