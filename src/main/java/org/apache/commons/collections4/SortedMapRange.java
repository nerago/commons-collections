package org.apache.commons.collections4;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;

public final class SortedMapRange<K> implements Serializable {
    private static final long serialVersionUID = 5904683499000042719L;

    /** The key to start from, null if the beginning. */
    private final K fromKey;

    /** The key to end at, null if till the end. */
    private final K toKey;

    /** Whether the 'from' is inclusive. */
    private final boolean fromInclusive;

    /** Whether the 'to' is inclusive. */
    private final boolean toInclusive;

    /** Comparator which defines key order. */
    private final Comparator<? super K> comparator;

    private SortedMapRange(final K fromKey, final boolean fromInclusive,
                           final K toKey, final boolean toInclusive,
                           final Comparator<? super K> comparator) {
        if (fromKey != null && toKey != null && comparator.compare(fromKey, toKey) > 0) {
            throw new IllegalArgumentException("fromKey > toKey");
        }

        this.fromKey = fromKey;
        this.fromInclusive = fromInclusive;
        this.toKey = toKey;
        this.toInclusive = toInclusive;
        this.comparator = comparator;
    }

    private SortedMapRange() {
        this.fromKey = null;
        this.fromInclusive = false;
        this.toKey = null;
        this.toInclusive = false;
        this.comparator = null;
    }

    /**
     * Returns the FROM Key.
     */
    public K getFromKey() {
        return fromKey;
    }

    /**
     * Whether or not the {@link #getFromKey()} is in the range.
     */
    public boolean isFromInclusive() {
        return fromInclusive;
    }

    public boolean hasFrom() {
        return fromKey != null;
    }

    /**
     * Returns the TO Key.
     */
    public K getToKey() {
        return toKey;
    }

    /**
     * Whether or not the {@link #getToKey()} is in the range.
     */
    public boolean isToInclusive() {
        return toInclusive;
    }

    public boolean hasTo() {
        return toKey != null;
    }

    @SuppressWarnings("unchecked")
    public static <K> SortedMapRange<K> full(final Comparator<? super K> comparator) {
        return new SortedMapRange<>(null, false, null, false,
                comparator != null ? comparator : (Comparator<? super K>) Comparator.naturalOrder());
    }

    public static <K> SortedMapRange<K> empty() {
        return new SortedMapRange<>();
    }

    public boolean isFull() {
        return fromKey == null && toKey == null && comparator != null;
    }

    public boolean isEmpty() {
        return fromKey == null && toKey == null && comparator == null;
    }

    public SortedMapRange<K> subRange(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
        if (fromKey == null && toKey == null) {
            throw new IllegalArgumentException("SortedMapRange sub must have a from or to");
        }
        return makeSubRange(fromKey, fromInclusive, toKey, toInclusive);
    }

    public SortedMapRange<K> tail(final K fromKey, final boolean fromInclusive) {
        if (fromKey == null) {
            throw new IllegalArgumentException("SortedMapRange tail must have a from");
        }
        return makeSubRange(fromKey, fromInclusive, null, false);
    }

    public SortedMapRange<K> head(final K toKey, final boolean toInclusive) {
        if (toKey == null) {
            throw new IllegalArgumentException("SortedMapRange head must have a to");
        }
        return makeSubRange(null, false, toKey, toInclusive);
    }

    public SortedMapRange<K> subRange(final K fromKey, final K toKey) {
        return subRange(fromKey, true, toKey, false);
    }

    public SortedMapRange<K> tail(final K fromKey) {
        return tail(fromKey, true);
    }

    public SortedMapRange<K> head(final K toKey) {
        return head(toKey, false);
    }

    private int compare(final K a, final K b) {
        assert a != null && b != null;
        return comparator.compare(a, b);
    }

    private SortedMapRange<K> makeSubRange(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
        if (isEmpty()) {
            throw new IllegalArgumentException("Can't take sub range of an empty range");
        }

        if (fromKey != null) {
            if (!inToRange(fromKey) || !rangeInFromRange(fromKey, fromInclusive)) {
                throw new IllegalArgumentException("FromKey is out of range: " + fromKey);
            }
        }

        if (toKey != null) {
            if (!inFromRange(toKey) || !rangeInToRange(toKey, toInclusive)) {
                throw new IllegalArgumentException("ToKey is out of range: " + toKey);
            }
        }

        return new SortedMapRange<>(fromKey, fromInclusive, toKey, toInclusive, comparator);
    }

    /**
     * Returns true if the provided key is greater than TO and less than FROM.
     */
    public boolean inRange(final K key) {
        if (isEmpty()) {
            return false;
        } else {
            return inFromRange(key) && inToRange(key);
        }
    }

    /**
     * Returns true if the provided key is in the FROM range of the {@link SortedMapRange}.
     */
    private boolean inFromRange(final K key) {
        if (fromKey == null) {
            return true;
        }
        final int ret = compare(key, fromKey);
        if (fromInclusive) {
            return ret >= 0;
        }
        return ret > 0;
    }

    private boolean rangeInFromRange(final K key, final boolean inclusive) {
        if (fromKey == null) {
            return true;
        }
        final int ret = compare(key, fromKey);
        if (ret == 0) {
            // matching keys is fine if we're inclusive, or they don't need inclusive
            return fromInclusive || !inclusive;
        }
        return ret > 0;
    }

    /**
     * Returns true if the provided key is in the TO range of the {@link SortedMapRange}.
     */
    private boolean inToRange(final K key) {
        if (toKey == null) {
            return true;
        }
        final int ret = compare(key, toKey);
        if (toInclusive) {
            return ret <= 0;
        }
        return ret < 0;
    }

    private boolean rangeInToRange(final K key, final boolean inclusive) {
        if (toKey == null) {
            return true;
        }
        final int ret = compare(key, toKey);
        if (ret == 0) {
            // matching keys is fine if we're inclusive, or they don't need inclusive
            return toInclusive || !inclusive;
        }
        return ret < 0;
    }

    public SortedMapRange<K> reversed() {
        if (isEmpty()) {
            return this;
        }
        return new SortedMapRange<>(toKey, toInclusive, fromKey, fromInclusive, comparator.reversed());
    }

    public K applyMapFindFirstKey(final NavigableMap<K, ?> map) {
        final Map.Entry<K, ?> entry;
        if (fromKey == null) {
            entry = map.firstEntry();
        } else {
            if (fromInclusive) {
                entry = map.ceilingEntry(fromKey);
            } else {
                entry = map.higherEntry(fromKey);
            }
        }

        final K key = entry != null ? entry.getKey() : null;
        if (key == null || !inRange(key)) {
            throw new NoSuchElementException();
        }
        return key;
    }
}
