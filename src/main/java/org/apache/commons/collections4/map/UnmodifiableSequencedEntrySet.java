package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.set.AbstractSetDecorator;
import org.apache.commons.collections4.spliterators.UnmodifiableMapSpliterator;

public final class UnmodifiableSequencedEntrySet<K, V>
        extends AbstractSetDecorator<Map.Entry<K, V>, SequencedSet<Map.Entry<K, V>>>
        implements SequencedCommonsSet<Map.Entry<K, V>>, Unmodifiable {

    private static final long serialVersionUID = -3669591917001393091L;
    private UnmodifiableSequencedEntrySet<K, V> reversed;

    /**
     * Factory method to create an unmodifiable set of Map Entry objects.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param set the set to decorate, must not be null
     * @return a new unmodifiable entry set
     * @throws NullPointerException if set is null
     * @since X.X
     */
    public static <K, V> SequencedSet<Map.Entry<K, V>> unmodifiableEntrySet(final SequencedSet<Map.Entry<K, V>> set) {
        if (set instanceof Unmodifiable) {
            return set;
        }
        return new UnmodifiableSequencedEntrySet<>(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    private UnmodifiableSequencedEntrySet(final SequencedSet<Map.Entry<K, V>> set) {
        super(set);
    }

    private Map.Entry<K, V> wrapEntry(final Map.Entry<K, V> entry) {
        return new UnmodifiableMapEntry<>(entry);
    }

    @Override
    public void forEach(final Consumer<? super Map.Entry<K, V>> action) {
        super.forEach(entry -> action.accept(wrapEntry(entry)));
    }

    @Override
    public boolean add(final Map.Entry<K, V> object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends Map.Entry<K, V>> coll) {
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
    public boolean removeIf(final Predicate<? super Map.Entry<K, V>> filter) {
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

    @Override
    public SequencedCommonsSet<Map.Entry<K, V>> reversed() {
        if (reversed == null) {
            reversed = new UnmodifiableSequencedEntrySet<>(decorated().reversed());
        }
        return reversed;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new UnmodifiableEntrySetIterator(decorated().iterator());
    }

    @Override
    public Iterator<Map.Entry<K, V>> descendingIterator() {
        if (decorated() instanceof SequencedCommonsSet<Map.Entry<K,V>>) {
            return ((SequencedCommonsSet<Map.Entry<K,V>>) decorated()).descendingIterator();
        } else {
            return reversed().iterator();
        }
    }

    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return UnmodifiableMapSpliterator.unmodifiableMapSpliterator(decorated().spliterator());
    }

    @Override
    public Object[] toArray() {
        return ToArrayUtils.fromEntryCollectionUnmodifiable(decorated());
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return ToArrayUtils.fromEntryCollectionUnmodifiable(decorated(), array);
    }

    /**
     * Implementation of an entry set iterator.
     */
    private class UnmodifiableEntrySetIterator extends AbstractIteratorDecorator<Map.Entry<K, V>> {

        protected UnmodifiableEntrySetIterator(final Iterator<Map.Entry<K, V>> iterator) {
            super(iterator);
        }

        @Override
        public Map.Entry<K, V> next() {
            return new UnmodifiableMapEntry<>(getIterator().next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
