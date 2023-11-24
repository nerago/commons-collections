package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.collection.AbstractSequencedCollection;

public abstract class AbstractCommonsSequencedSet<E, TSubSet extends SequencedCommonsSet<E>>
        extends AbstractSequencedCollection<E, TSubSet>
        implements SequencedCommonsSet<E> {

    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof Set) {
            return SetUtils.isEqualSet(this, (Collection<?>) obj);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return SetUtils.hashCodeForSet(this);
    }

    @Override
    protected TSubSet createReversed() {
        return (TSubSet) new ReverseSequencedSet<>(this);
    }
}
