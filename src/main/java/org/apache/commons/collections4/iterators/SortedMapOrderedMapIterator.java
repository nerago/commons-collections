package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SortedMapOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    private final SortedMap<K, V> map;
    private boolean forward;
    private boolean currentValid;
    private Iterator<Map.Entry<K, V>> forwardIterator;
    private Map.Entry<K, V> current;
    private Map.Entry<K, V> previous;

    public static <K, V> OrderedMapIterator<K, V> sortedMapIterator(final SortedMap<K, V> map) {
        if (map instanceof NavigableMap) {
            return new NavigableMapOrderedMapIterator<>((NavigableMap<K, V>) map);
        } else if (map.size() < 10) {
            return new SortedMapListIterator<>(map);
        } else {
            return new SortedMapOrderedMapIterator<>(map);
        }
    }

    SortedMapOrderedMapIterator(final SortedMap<K, V> map) {
        this.map = map;
        reset();
    }

    @Override
    public void reset() {
        forwardIterator = map.entrySet().iterator();
        current = null;
        previous = null;
        forward = true;
        currentValid = false;
    }

    private Iterator<Map.Entry<K, V>> getForwardIterator() {
        if (forwardIterator == null) {
            if (current == null) {
                throw new IllegalStateException("unexpected null current on direction change");
            }
            forwardIterator = map.tailMap(current.getKey()).entrySet().iterator();
        }
        return forwardIterator;
    }

    private Map.Entry<K, V> getPreviousEntry() {
        if (previous != null) {
            return previous;
        } else if (current == null) {
            throw new IllegalStateException("unexpected null current on direction change");
        }

        previous = SortedMapUtils.previousEntry(map, current.getKey());
        return previous;
    }

    @Override
    public boolean hasNext() {
        return getForwardIterator().hasNext();
    }

    @Override
    public K next() {
        if (forward) {
            previous = current;
        } else {
            previous = null;
        }
        current = getForwardIterator().next();
        forward = true;
        currentValid = true;
        return current.getKey();
    }

    @Override
    public boolean hasPrevious() {
        return getPreviousEntry() != null;
    }

    @Override
    public K previous() {
        current = getPreviousEntry();
        forward = false;
        previous = null;
        currentValid = true;
        forwardIterator = null;
        return current.getKey();
    }

    @Override
    public K getKey() {
        if (!currentValid) {
            throw new IllegalStateException();
        }
        return current.getKey();
    }

    @Override
    public V getValue() {
        if (!currentValid) {
            throw new IllegalStateException();
        }
        return current.getValue();
    }

    @Override
    public void remove() {
        if (!currentValid) {
            throw new IllegalStateException();
        }
        if (forward) {
            forwardIterator.remove();
        } else {
            map.remove(current.getKey());
        }
    }

    @Override
    public V setValue(final V value) {
        if (currentValid) {
            return current.setValue(value);
        } else {
            throw new IllegalStateException();
        }
    }
}
