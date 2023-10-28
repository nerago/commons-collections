package org.apache.commons.collections4.set;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.ToArrayUtils;

public class ReverseNavigableSet<E, TDecorated extends NavigableSet<E>, TSubSet extends NavigableRangedSet<E, ?>>
        extends AbstractNavigableSetDecorator<E, TDecorated, TSubSet> {
    public ReverseNavigableSet(final TDecorated set, final SortedMapRange<E> range) {
        super(set, range.reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TSubSet decorateDerived(final TDecorated subMap, final SortedMapRange<E> range) {
        return (TSubSet) new ReverseNavigableSet<>(subMap, range);
    }

    @Override
    public E lower(final E e) {
        return super.higher(e);
    }

    @Override
    public E floor(final E e) {
        return super.ceiling(e);
    }

    @Override
    public E ceiling(final E e) {
        return super.floor(e);
    }

    @Override
    public E higher(final E e) {
        return super.lower(e);
    }

    @Override
    public E pollFirst() {
        return super.pollLast();
    }

    @Override
    public E pollLast() {
        return super.pollFirst();
    }

    @Override
    public E first() {
        return super.last();
    }

    @Override
    public E last() {
        return super.first();
    }

    @Override
    public Object[] toArray() {
        return ToArrayUtils.fromIteratorAndSize(super.descendingIterator(), size());
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return ToArrayUtils.fromIteratorAndSize(super.descendingIterator(), size(), array);
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        descendingIterator().forEachRemaining(action);
    }

    @Override
    public Iterator<E> iterator() {
        return super.descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return super.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet reversed() {
        return (TSubSet) new NullDecorator<>(decorated());
    }

    /**
     * dumb iterator version, best we can do without knowing the internals of the decorated
     */
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(descendingIterator(), size(), Spliterator.DISTINCT | Spliterator.SIZED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Comparator<? super E> comparator() {
        final Comparator<? super E> parent = super.comparator();
        if (parent != null) {
            return parent.reversed();
        } else {
            return (Comparator<? super E>) Comparator.reverseOrder();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet descendingSet() {
        return (TSubSet) new NullDecorator<>(decorated());
    }
}

