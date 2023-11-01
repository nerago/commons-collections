package org.apache.commons.collections4.set;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.NavigableRangedSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.ToArrayUtils;

public class ReverseSequencedSet<E, TDecorated extends SequencedSet<E>, TSubSet extends SequencedSet<E>>
        extends AbstractSequencedSetDecorator<E, TDecorated, TSubSet> {
    private final SequencedSet<E> reverse;

    public ReverseSequencedSet(final TDecorated set) {
        super(set);
        reverse = set.reversed();
    }

    @Override
    public void addFirst(E e) {
        super.addLast(e);
    }

    @Override
    public void addLast(E e) {
        super.addFirst(e);
    }

    @Override
    public E getFirst() {
        return super.getLast();
    }

    @Override
    public E getLast() {
        return super.getFirst();
    }

    @Override
    public E removeFirst() {
        return super.removeLast();
    }

    @Override
    public E removeLast() {
        return super.removeFirst();
    }

    @Override
    public Object[] toArray() {
        return ToArrayUtils.fromIteratorAndSize(iterator(), size());
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return ToArrayUtils.fromIteratorAndSize(iterator(), size(), array);
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        iterator().forEachRemaining(action);
    }

    @Override
    public Iterator<E> iterator() {
        return reverse.iterator();
    }

//    @Override
    public Iterator<E> descendingIterator() {
        return decorated().iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubSet reversed() {
        return (TSubSet) decorated();
    }

    @Override
    protected TSubSet decorateReverse(final TDecorated subMap) {
        return (TSubSet) subMap;
    }

    /**
     * dumb iterator version, best we can do without knowing the internals of the decorated
     */
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT | Spliterator.SIZED);
    }
}

