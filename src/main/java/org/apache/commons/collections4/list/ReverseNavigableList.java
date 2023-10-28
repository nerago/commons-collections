package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.ListIterator;

import org.apache.commons.collections4.NavigableList;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.set.ReverseNavigableSet;

class ReverseNavigableList<E>
        extends ReverseNavigableSet<E, NavigableList<E, ?, ?>, AutoSortingList.AutoSortedSubSet<E>>
        implements NavigableList<E, AutoSortingList.AutoSortingSubList<E>, AutoSortingList.AutoSortedSubSet<E>> {
    public ReverseNavigableList(NavigableList<E, ?, ?> list, SortedMapRange<E> range) {
        super(list, range);
    }

    @Override
    public int lowerIndex(E e) {
        return decorated().higherIndex();
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
    public AutoSortingList.AutoSortingSubList<E> subList(int fromIndex, int toIndex) {
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

    @Override
    public AutoSortingList.AutoSortedSubSet<E> descendingSet() {
        return super.descendingSet();
    }

    @Override
    public AutoSortingList.AutoSortedSubSet<E> reversed() {
        return (NavigableList<E, AutoSortingList.AutoSortingSubList<E>, AutoSortingList.AutoSortedSubSet<E>>) decorated();
    }
}
