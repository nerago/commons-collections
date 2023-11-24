package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SortedMapOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    protected final SortedMap<K, V> map;
    protected boolean forward;
    protected boolean currentValid;
    protected Iterator<Map.Entry<K, V>> forwardIterator;
    protected Map.Entry<K, V> current;
    protected Map.Entry<K, V> previous;

    public SortedMapOrderedMapIterator(final SortedMap<K, V> map) {
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

    private static class Descending<K, V> extends SortedMapOrderedMapIterator<K, V> {
        private Descending(final SortedMap<K, V> map) {
            super(map);
        }

        @Override
        public void reset() {
            forwardIterator = null;
            current = null;
            previous = map.lastEntry();
            forward = false;
            currentValid = false;
        }

        @Override
        public boolean hasNext() {
            return super.hasPrevious();
        }

        @Override
        public K next() {
            return super.previous();
        }

        @Override
        public boolean hasPrevious() {
            return super.hasNext();
        }

        @Override
        public K previous() {
            return super.next();
        }
    }
}
