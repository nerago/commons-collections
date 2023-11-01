package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.map.EntrySetToMapIteratorAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Supplier;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SortedMapListIterator<K, V> extends EntrySetToMapIteratorAdapter<K, V>
        implements OrderedMapIterator<K, V>, ListIterator<K> {
    /**
     * Create a new SortedMapListIterator.
     *
     * @param entrySet the entrySet to iterate
     */
    protected SortedMapListIterator(final Set<Map.Entry<K, V>> entrySet) {
        super(entrySet);
    }

    protected SortedMapListIterator(final SortedMap<K, V> map) {
        super(map.entrySet());
    }

    protected SortedMapListIterator(final Supplier<Iterator<Map.Entry<K, V>>> supplier) {
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
