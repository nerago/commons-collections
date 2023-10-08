package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.map.EntrySetToMapIteratorAdapter;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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

    protected SortedMapListIterator(final SortedMap<K,V> map) {
        super(map.entrySet());
    }

    @Override
    public synchronized void reset() {
        super.reset();
        iterator = new ListIteratorWrapper<>(iterator);
    }

    @Override
    public boolean hasPrevious() {
        return ((ListIterator<Map.Entry<K, V>>) iterator).hasPrevious();
    }

    @Override
    public K previous() {
        entry = ((ListIterator<Map.Entry<K, V>>) iterator).previous();
        return getKey();
    }

    @Override
    public int nextIndex() {
        return 0;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Override
    public void set(K k) {

    }

    @Override
    public void add(K k) {

    }
}
