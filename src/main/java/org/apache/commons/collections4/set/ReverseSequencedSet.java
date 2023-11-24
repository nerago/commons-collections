package org.apache.commons.collections4.set;

import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.ToArrayUtils;

public final class ReverseSequencedSet<E>
        extends AbstractSequencedSetDecorator<E, SequencedSet<E>, SequencedCommonsSet<E>> {

    public ReverseSequencedSet(final SequencedSet<E> set) {
        super(set);
    }

    public ReverseSequencedSet(final SequencedCommonsSet<E> set) {
        super(set, set);
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
        return super.descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return super.iterator();
    }

    @Override
    protected SequencedCommonsSet<E> createReversed() {
        final SequencedSet<E> decorated = decorated();
        if (decorated instanceof SequencedCommonsSet<E>) {
            return (SequencedCommonsSet<E>) decorated;
        } else {
            return new NullSequencedDecorator<>(decorated);
        }
    }

    /**
     * dumb iterator version, best we can do without knowing the internals of the decorated
     */
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.DISTINCT | Spliterator.SIZED);
    }
}

