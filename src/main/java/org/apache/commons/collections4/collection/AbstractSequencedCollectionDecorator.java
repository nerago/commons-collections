package org.apache.commons.collections4.collection;

import java.util.SequencedCollection;

import org.apache.commons.collections4.SequencedCommonsCollection;

public abstract class AbstractSequencedCollectionDecorator<E>
        extends AbstractCollectionDecorator<E, SequencedCollection<E>>
        implements SequencedCommonsCollection<E> {
    protected AbstractSequencedCollectionDecorator(final SequencedCollection<E> collection) {
        super(collection);
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
    public void addFirst(E e) {
        decorated().addFirst(e);
    }

    @Override
    public void addLast(E e) {
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

    @Override
    public abstract SequencedCollection<E> reversed();
}
