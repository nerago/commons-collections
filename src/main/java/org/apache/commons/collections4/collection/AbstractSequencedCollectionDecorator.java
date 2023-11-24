package org.apache.commons.collections4.collection;

import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.SequencedSet;

import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;

public abstract class AbstractSequencedCollectionDecorator<E>
        extends AbstractCollectionDecorator<E, SequencedCollection<E>>
        implements SequencedCommonsCollection<E> {

    private static final long serialVersionUID = 4858679099642326630L;
    private SequencedCommonsCollection<E> reversed;

    protected AbstractSequencedCollectionDecorator(final SequencedCollection<E> collection, final SequencedCommonsCollection<E> reversed) {
        super(collection);
        this.reversed = reversed;
    }

    public final SequencedCommonsCollection<E> reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }

    protected SequencedCommonsCollection<E> createReversed() {
        return new ReverseCollection<>(this);
    }

    @Override
    public Iterator<E> descendingIterator() {
        final SequencedCollection<E> decorated = decorated();
        if (decorated instanceof SequencedCommonsCollection<E>) {
            return ((SequencedCommonsCollection<E>) decorated).descendingIterator();
        } else {
            return decorated.reversed().iterator();
        }
    }

    @Override
    public E getFirst() {
        return decorated().getFirst();
    }

    @Override
    public E getLast() {
        return decorated().getLast();
    }

    @Override
    public void addFirst(final E e) {
        decorated().addFirst(e);
    }

    @Override
    public void addLast(final E e) {
        decorated().addLast(e);
    }

    @Override
    public E removeFirst() {
        return decorated().removeFirst();
    }

    @Override
    public E removeLast() {
        return decorated().removeLast();
    }

    public static class NullDecorator<E> extends AbstractSequencedCollectionDecorator<E> {
        protected NullDecorator(final SequencedCollection<E> collection) {
            super(collection, null);
        }
    }
}
