package org.apache.commons.collections4;

import java.util.List;
import java.util.Spliterator;

public interface NavigableList<E, TSubList extends NavigableList<E, ?>>
        extends NavigableRangedSet<E>, List<E> {
    /**
     * Returns the index of the last element in this list strictly less than the
     * given element -1 if there is no such location.
     *
     * @param e the value to compare to
     * @return the index of the last element strictly less than where the specified element
     *         would belong in this list. Range @{code 0..size-2}, or -1 if the list is empty.
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the list
     * @throws NullPointerException if the specified element is null
     */
    int lowerIndex(E e);

    /**
     * Returns the index of the last element in this list less than or equal to the given element,
     * or -1 if there is no such location.
     *
     * @param e the value to match
     * @return the index of the last element strictly less than where the specified element
     *         would belong in this list in range @{code 0..size-2}, or -1 if the list is empty.
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    int floorIndex(E e);

    /**
     * Returns the least element in this set greater than or equal to
     * the given element, or -1 if there is no such location.
     *
     * @param e the value to match
     * @return the least element greater than or equal to {@code e},
     * or {@code null} if there is no such element
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    int ceilingIndex(E e);

    /**
     * Returns the least element in this set strictly greater than the
     * given element, or {@code null} if there is no such location.
     *
     * @param e the value to match
     * @return the least element greater than {@code e},
     * or {@code null} if there is no such element
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    int higherIndex(E e);

    // override to give return type our preferred type
    @Override
    TSubList subList(int fromIndex, int toIndex);

    // override since super defaults might not take combined interface into account
    @Override
    TSubList reversed();

    @Override
    default TSubList descendingSet() {
        return reversed();
    }

    // override since super defaults might not take combined interface into account
    Spliterator<E> spliterator();

    @Override
    E getFirst();

    @Override
    E getLast();

    @Override
    void addFirst(final E e);

    @Override
    void addLast(final E e);

    @Override
    E removeFirst();

    @Override
    E removeLast();
}
