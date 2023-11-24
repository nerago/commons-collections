package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * OrderedMapIterator implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class NavigableMapOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    protected final NavigableMap<K, V> map;
    protected boolean forward;
    protected Iterator<Map.Entry<K, V>> forwardIterator;
    protected Iterator<Map.Entry<K, V>> backwardIterator;
    protected Map.Entry<K, V> current;

    public NavigableMapOrderedMapIterator(final NavigableMap<K, V> map) {
        this.map = map;
        reset();
    }

    @Override
    public void reset() {
        forward = true;
        forwardIterator = map.entrySet().iterator();
        backwardIterator = null;
    }

    private Iterator<Map.Entry<K, V>> getForwardIterator() {
        if (forwardIterator == null) {
            forwardIterator = map.tailMap(current.getKey(), true).entrySet().iterator();
        }
        return forwardIterator;
    }

    private Iterator<Map.Entry<K, V>> getBackwardIterator() {
        if (backwardIterator == null) {
            backwardIterator = map.headMap(current.getKey(), true).descendingMap().entrySet().iterator();
        }
        return backwardIterator;
    }

    @Override
    public boolean hasNext() {
        return getForwardIterator().hasNext();
    }

    @Override
    public K next() {
        current = getForwardIterator().next();
        forward = true;
        backwardIterator = null;
        return current.getKey();
    }

    @Override
    public boolean hasPrevious() {
        return getBackwardIterator().hasNext();
    }

    @Override
    public K previous() {
        current = getBackwardIterator().next();
        forward = false;
        forwardIterator = null;
        return current.getKey();
    }

    @Override
    public K getKey() {
        if (current == null) {
            throw new IllegalStateException();
        }
        return current.getKey();
    }

    @Override
    public V getValue() {
        if (current == null) {
            throw new IllegalStateException();
        }
        return current.getValue();
    }

    @Override
    public void remove() {
        if (current == null) {
            throw new IllegalStateException();
        }
        if (forward) {
            forwardIterator.remove();
        } else {
            backwardIterator.remove();
        }
        current = null;
    }

    @Override
    public V setValue(final V value) {
        if (current == null) {
            throw new IllegalStateException();
        }
        return current.setValue(value);
    }

    @Override
    public String toString() {
        if (current != null) {
            return "MapIterator[" + getKey() + "=" + getValue() + "]";
        }
        return "MapIterator[]";
    }

    public static class Descending<K, V> extends NavigableMapOrderedMapIterator<K, V> {
        public Descending(final NavigableMap<K, V> map) {
            super(map);
        }

        @Override
        public void reset() {
            forward = false;
            forwardIterator = null;
            backwardIterator = map.descendingMap().entrySet().iterator();
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
