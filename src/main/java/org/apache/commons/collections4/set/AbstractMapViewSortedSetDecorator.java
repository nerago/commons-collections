package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractMapViewSortedSetDecorator<E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
        extends AbstractMapViewSortedSet<E> {

    /** The collection being decorated */
    private final TDecorated decorated;
    private final SortedMapRange<E> range;

    protected AbstractMapViewSortedSetDecorator(final TDecorated decorated, final SortedMapRange<E> range) {
        this.decorated = decorated;
        this.range = range;
    }

    protected TDecorated decorated() {
        return decorated;
    }

    protected abstract TSubSet decorateDerived(final TDecorated subMap, final SortedMapRange<E> range);

    @Override
    public SortedMapRange<E> getRange() {
        return range;
    }

    @Override
    public Comparator<? super E> comparator() {
        return decorated.comparator();
    }

    @Override
    public final TSubSet subSet(final SortedMapRange<E> range) {
        return decorateDerived(range.applyToSet(decorated()), range);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SortedRangedSet<E> reversed() {
        return decorateDerived((TDecorated) decorated.reversed(), range.reversed());
    }

    @Override
    public E first() {
        return decorated.first();
    }

    @Override
    public E last() {
        return decorated.last();
    }

    @Override
    public Spliterator<E> spliterator() {
        return decorated.spliterator();
    }

    @Override
    public E getFirst() {
        return decorated.getFirst();
    }

    @Override
    public E getLast() {
        return decorated.getLast();
    }

    @Override
    public E removeFirst() {
        return decorated.removeFirst();
    }

    @Override
    public E removeLast() {
        return decorated.removeLast();
    }

    @Override
    public int size() {
        return decorated.size();
    }

    @Override
    public boolean isEmpty() {
        return decorated.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return decorated.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return decorated.iterator();
    }

    @Override
    public Object[] toArray() {
        return decorated.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return decorated.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return decorated.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return decorated.containsAll(c);
    }

    @Override
    public void clear() {
        decorated.clear();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return decorated.removeIf(filter);
    }
}
