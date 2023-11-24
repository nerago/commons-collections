package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractMapViewSequencedSetDecorator<E, TDecorated extends SequencedSet<E>, TSubSet extends SequencedCommonsSet<E>>
        extends AbstractMapViewSequencedSet<E, TSubSet> {
    /**
     * The collection being decorated
     */
    protected final TDecorated decorated;

    public AbstractMapViewSequencedSetDecorator(final TDecorated decorated) {
        this.decorated = decorated;
    }

    protected TDecorated decorated() {
        return decorated;
    }

    protected abstract TSubSet decorateDerived(final TDecorated subMap, final SortedMapRange<E> range);

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
