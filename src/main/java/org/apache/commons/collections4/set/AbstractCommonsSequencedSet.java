package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;

public abstract class AbstractCommonsSequencedSet<E, TSubSet extends SequencedCommonsSet<E>>
        extends AbstractCommonsCollection<E>
        implements SequencedCommonsSet<E> {

    private TSubSet reversed;

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
    public abstract Iterator<E> descendingIterator();

    @Override
    public final TSubSet reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }

    protected TSubSet createReversed() {
        return (TSubSet) new SequencedReverseView<>(this, this);
    }

    protected static class SequencedReverseView<E>
            extends AbstractSequencedSetDecorator<E, SequencedCommonsSet<E>, SequencedCommonsSet<E>> {
        private static final long serialVersionUID = -3785473145672064127L;

        protected SequencedReverseView(final SequencedCommonsSet<E> set, final SequencedCommonsSet<E> reverse) {
            super(set, reverse);
        }

        @Override
        public Iterator<E> iterator() {
            return decorated().descendingIterator();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return decorated().iterator();
        }

        @Override
        public boolean addAll(final Collection<? extends E> coll) {
            return super.addAll(CollectionUtils.reversedCollection(coll));
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
    }
}
