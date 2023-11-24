package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.map.EntrySetToMapIteratorAdapter;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class WrappedListOrderedMapIterator<K, V> extends EntrySetToMapIteratorAdapter<K, V>
        implements OrderedMapIterator<K, V>, ListIterator<K> {
    /**
     * Create a new WrappedListOrderedMapIterator.
     *
     * @param entrySet the entrySet to iterate
     */
    public WrappedListOrderedMapIterator(final Set<Map.Entry<K, V>> entrySet) {
        super(entrySet::iterator);
    }

    public WrappedListOrderedMapIterator(final Map<K, V> map) {
        super(() -> map.entrySet().iterator());
    }

    public WrappedListOrderedMapIterator(final Supplier<Iterator<Map.Entry<K, V>>> supplier) {
        super(supplier);
    }

    @Override
    public synchronized void reset() {
        iterator = new ListIteratorWrapper<>(supplier.get());
        entry = null;
    }

    private ListIterator<Map.Entry<K, V>> getListIterator() {
        return (ListIterator<Map.Entry<K, V>>) iterator;
    }

    @Override
    public boolean hasPrevious() {
        return getListIterator().hasPrevious();
    }

    @Override
    public K previous() {
        entry = getListIterator().previous();
        return getKey();
    }

    @Override
    public int nextIndex() {
        return getListIterator().nextIndex();
    }

    @Override
    public int previousIndex() {
        return getListIterator().previousIndex();
    }

    @Override
    public void set(final K k) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final K k) {
        throw new UnsupportedOperationException();
    }
}
