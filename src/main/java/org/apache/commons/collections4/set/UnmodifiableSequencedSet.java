package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedSet;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.spliterators.UnmodifiableSpliterator;

public class UnmodifiableSequencedSet<E> extends AbstractSequencedSetDecorator<E, SequencedSet<E>, SequencedCommonsSet<E>> {
    private static final long serialVersionUID = -3247548273544146332L;

    /**
     * Factory method to create an unmodifiable set.
     *
     * @param <E> the element type
     * @param set  the set to decorate, must not be null
     * @return a new unmodifiable {@link SortedSet}
     * @throws NullPointerException if set is null
     */
    public static <E> SequencedCommonsSet<E> unmodifiableSequencedSet(final SequencedSet<E> set) {
        if (set instanceof Unmodifiable && set instanceof SequencedCommonsSet<E>) {
            return (SequencedCommonsSet<E>) set;
        }
        return new UnmodifiableSequencedSet<>(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    private UnmodifiableSequencedSet(final SequencedSet<E> set) {
        super(set);
    }

    @Override
    protected SequencedCommonsSet<E> createReversed() {
        return new UnmodifiableSequencedSet<>(decorated().reversed());
    }

    @Override
    public Iterator<E> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(super.iterator());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return UnmodifiableIterator.unmodifiableIterator(super.descendingIterator());
    }

    @Override
    public Spliterator<E> spliterator() {
        return new UnmodifiableSpliterator<>(decorated().spliterator());
    }

    @Override
    public boolean add(final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
}