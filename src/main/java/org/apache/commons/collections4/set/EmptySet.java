package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.spliterators.EmptyMapSpliterator;

public final class EmptySet<E> implements NavigableRangedSet<E> {
    @SuppressWarnings("rawtypes")
    private static final EmptySet instance = new EmptySet();

    @SuppressWarnings("unchecked")
    public static <E> EmptySet<E> emptySet() {
        return (EmptySet<E>) instance;
    }

    @Override
    public NavigableRangedSet<E> reversed() {
        return this;
    }

    @Override
    public NavigableRangedSet<E> descendingSet() {
        return this;
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E getFirst() {
        throw new NoSuchElementException();
    }

    @Override
    public E getLast() {
        throw new NoSuchElementException();
    }

    @Override
    public E removeFirst() {
        throw new NoSuchElementException();
    }

    @Override
    public E removeLast() {
        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(final Object o) {
        return false;
    }

    @Override
    public E lower(E e) {
        return null;
    }

    @Override
    public E floor(E e) {
        return null;
    }

    @Override
    public E ceiling(E e) {
        return null;
    }

    @Override
    public E higher(E e) {
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
    public Iterator<E> iterator() {
        return EmptyIterator.emptyIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return iterator();
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
    }

    @Override
    public Object[] toArray() {
        return ToArrayUtils.EMPTY_ARRAY;
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        if (array.length > 0) {
            array[0] = null;
        }
        return array;
    }

    @Override
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public E first() {
        return null;
    }

    @Override
    public E last() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return (Spliterator<E>) EmptyMapSpliterator.emptyMapSpliterator();
    }

    @Override
    public SortedMapRange<E> getRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public NavigableRangedSet<E> subSet(final SortedMapRange<E> range) {
        return this;
    }
}
