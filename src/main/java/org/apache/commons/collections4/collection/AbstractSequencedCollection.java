package org.apache.commons.collections4.collection;

import java.util.Iterator;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsCollection;

public abstract class AbstractSequencedCollection<E, TSubSet extends SequencedCommonsCollection<E>>
        extends AbstractCommonsCollection<E>
        implements SequencedCommonsCollection<E> {

    private TSubSet reversed;

    protected AbstractSequencedCollection() {
    }

    protected AbstractSequencedCollection(final TSubSet reversed) {
        this.reversed = reversed;
    }

    public abstract Iterator<E> descendingIterator();

    public final TSubSet reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }

    protected TSubSet createReversed() {
        return (TSubSet) new ReverseCollection<>(this);
    }
}
