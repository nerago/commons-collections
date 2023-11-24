package org.apache.commons.collections4.set;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.ToArrayUtils;

public class ReverseSortedSet <E, TDecorated extends SortedSet<E>, TSubSet extends SortedRangedSet<E>>
        extends AbstractSortedSetDecorator<E, TDecorated, TSubSet> {
    public ReverseSortedSet(final TDecorated set, final SortedMapRange<E> range) {
        super(set, range.reversed());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TSubSet decorateDerived(final TDecorated subSet, final SortedMapRange<E> range) {
        return (TSubSet) new ReverseSortedSet<>(subSet, range);
    }

    @Override
    public E removeFirst() {
        return super.removeFirst();
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
    protected TSubSet createReversed() {
        return (TSubSet) new NullSequencedDecorator<>(decorated());
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
}
