package org.apache.commons.collections4.map;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;

import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class NavigableMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    protected final NavigableMap<K, V> map;
    protected Map.Entry<K, V> lastReturnedNode;
    /**
     * The next node to be returned by the iterator.
     */
    protected Map.Entry<K, V> nextNode;
    /**
     * The previous node in the sequence returned by the iterator.
     */
    protected Map.Entry<K, V> previousNode;

    /**
     * Create a new AbstractNavigableMapDecorator.NavigableMapIterator.
     *
     * @param map the map to iterate
     */
    protected NavigableMapIterator(final NavigableMap<K, V> map) {
        this.map = map;
    }

    @Override
    public synchronized void reset() {
        previousNode = null;
        lastReturnedNode = null;
        nextNode = map.firstEntry();
    }

    @Override
    public boolean hasNext() {
        return nextNode != null;
    }

    @Override
    public K next() {
        Map.Entry<K, V> current = nextNode;
        if (current == null)
            throw new NoSuchElementException();
        K key = current.getKey();
        previousNode = current;
        lastReturnedNode = current;
        nextNode = map.higherEntry(key);
        return key;
    }

    @Override
    public boolean hasPrevious() {
        return previousNode != null;
    }

    @Override
    public K previous() {
        Map.Entry<K, V> current = previousNode;
        if (current == null)
            throw new NoSuchElementException();
        K key = current.getKey();
        previousNode = map.lowerEntry(key);
        lastReturnedNode = current;
        nextNode = current;
        return key;
    }

    protected synchronized Map.Entry<K, V> current() {
        if (lastReturnedNode == null) {
            throw new IllegalStateException();
        }
        return lastReturnedNode;
    }

    @Override
    public K getKey() {
        return current().getKey();
    }

    @Override
    public V getValue() {
        return current().getValue();
    }

    @Override
    public void remove() {
        Map.Entry<K, V> current = current();
        map.remove(current.getKey());
        lastReturnedNode = null;
    }

    @Override
    public V setValue(V value) {
        Map.Entry<K, V> current = current();
        current.setValue(value);
        return map.put(current.getKey(), current.getValue());
    }
}
