package org.apache.commons.collections4.collection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;

import org.apache.commons.collections4.SequencedCommonsCollection;

public class ReverseCollection<E> extends AbstractSequencedCollectionDecorator<E> {
    public ReverseCollection(final SequencedCollection<E> collection) {
        super(collection, collection instanceof SequencedCommonsCollection<E> ? (SequencedCommonsCollection<E>) collection : null);
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
    protected SequencedCommonsCollection<E> createReversed() {
        return new AbstractSequencedCollectionDecorator.NullDecorator<>(decorated());
    }
}
