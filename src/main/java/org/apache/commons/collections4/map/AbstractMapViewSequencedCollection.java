package org.apache.commons.collections4.map;

import java.util.Collection;

import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.collection.AbstractSequencedCollection;

public abstract class AbstractMapViewSequencedCollection<V> extends AbstractSequencedCollection<V, SequencedCommonsCollection<V>> implements SequencedCommonsCollection<V> {
    protected AbstractMapViewSequencedCollection() {
    }

    protected AbstractMapViewSequencedCollection(final SequencedCommonsCollection<V> reversed) {
        super(reversed);
    }

    @Override
    public final void addFirst(final V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void addLast(final V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean add(final V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }
}
