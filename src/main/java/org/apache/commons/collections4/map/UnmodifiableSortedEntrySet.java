package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableEntrySetIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.set.AbstractSortedSetDecorator;
import org.apache.commons.collections4.spliterators.UnmodifiableMapSpliterator;

public class UnmodifiableSortedEntrySet<K, V>
        extends AbstractSortedSetDecorator<Map.Entry<K, V>, SortedSet<Map.Entry<K, V>>, SortedRangedSet<Map.Entry<K, V>>>
        implements Unmodifiable {

    private static final long serialVersionUID = -3669591917001393091L;

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
    public static <K, V> SortedRangedSet<Map.Entry<K, V>> unmodifiableEntrySet(final SortedSet<Map.Entry<K, V>> set) {
        if (set instanceof Unmodifiable && set instanceof SortedRangedSet) {
            return (SortedRangedSet<Map.Entry<K, V>>) set;
        }
        return new UnmodifiableSortedEntrySet<>(set);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    private UnmodifiableSortedEntrySet(final SortedSet<Map.Entry<K, V>> set) {
        super(set);
    }

    public UnmodifiableSortedEntrySet(final SortedSet<Map.Entry<K, V>> set, final SortedMapRange<Map.Entry<K, V>> range) {
        super(set, range);
    }

    @Override
    protected SortedRangedSet<Map.Entry<K, V>> decorateDerived(SortedSet<Map.Entry<K, V>> subSet, SortedMapRange<Map.Entry<K, V>> range) {
        return new UnmodifiableSortedEntrySet(subSet, range);
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
    public Iterator<Map.Entry<K, V>> iterator() {
        return new UnmodifiableEntrySetIterator<>(decorated().iterator());
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

}
