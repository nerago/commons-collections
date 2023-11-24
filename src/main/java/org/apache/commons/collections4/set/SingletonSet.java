package org.apache.commons.collections4.set;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.SingletonIterator;

public class SingletonSet<E>
        extends AbstractCommonsSortedSet<E, NavigableRangedSet<E>>
        implements NavigableRangedSet<E> {
    private E value;

    public SingletonSet(final E value) {
        this.value = value;
    }

    @Override
    public NavigableRangedSet<E> subSet(final SortedMapRange<E> range) {
        return range.contains(value) ? this : EmptySet.emptySet();
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
    public E lower(final E e) {
        return null;
    }

    @Override
    public E floor(final E e) {
        return Objects.equals(value, e) ? value : null;
    }

    @Override
    public E ceiling(final E e) {
        return Objects.equals(value, e) ? value : null;
    }

    @Override
    public E higher(final E e) {
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
    public boolean add(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
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
    protected NavigableRangedSet<E> createReversed() {
        return this;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        value = (E) in.readObject();
    }
}
