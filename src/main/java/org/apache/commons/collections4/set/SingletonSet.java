package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.SingletonIterator;

public class SingletonSet<E>
        extends AbstractCommonsSortedSet<E>
        implements NavigableRangedSet<E> {
    private final E value;

    public SingletonSet(final E value) {
        this.value = value;
    }

    @Override
    public NavigableRangedSet<E> subSet(final SortedMapRange range) {
        return range.inRange(value) ? this : EmptySet.emptySet();
    }

    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public E first() {
        return value;
    }

    @Override
    public E last() {
        return value;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(final Object obj) {
        return Objects.equals(value, obj);
    }

    @Override
    public E lower(E e) {
        return null;
    }

    @Override
    public E floor(E e) {
        return Objects.equals(value, e) ? value : null;
    }

    @Override
    public E ceiling(E e) {
        return Objects.equals(value, e) ? value : null;
    }

    @Override
    public E higher(E e) {
        return null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new SingletonIterator<>(value);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return iterator();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final SortedMapRange<E> getRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public NavigableRangedSet<E> descendingSet() {
        return this;
    }

    @Override
    public NavigableRangedSet<E> reversed() {
        return this;
    }
}
