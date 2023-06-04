package org.apache.commons.collections4;

import java.util.Comparator;

public final class SortedMapRange<K> {
    /** The key to start from, null if the beginning. */
    private final K fromKey;

    /** The key to end at, null if till the end. */
    private final K toKey;

    /** Whether or not the 'from' is inclusive. */
    private final boolean fromInclusive;

    /** Whether or not the 'to' is inclusive. */
    private final boolean toInclusive;

    /** Comparator which defines key order. */
    private final Comparator<? super K> comparator;

    private SortedMapRange(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, Comparator<? super K> comparator) {
        if (fromKey != null && toKey != null && comparator.compare(fromKey, toKey) > 0) {
            throw new IllegalArgumentException("fromKey > toKey");
        }

        this.fromKey = fromKey;
        this.fromInclusive = fromInclusive;
        this.toKey = toKey;
        this.toInclusive = toInclusive;
        this.comparator = comparator;
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

    public static <K> SortedMapRange<K> full(Comparator<? super K> comparator) {
        return new SortedMapRange<>(null, false, null, false, comparator);
    }

    public SortedMapRange<K> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        if (fromKey == null && toKey == null) {
            throw new IllegalArgumentException("SortedMapRange must have a from or to!");
        }
        return new SortedMapRange<>(fromKey, fromInclusive, toKey, toInclusive, comparator);
    }

    public SortedMapRange<K> tailMap(K fromKey, boolean fromInclusive) {
        if (fromKey == null) {
            throw new IllegalArgumentException("SortedMapRange must have a from!");
        }
        int cmp = comparator.compare(fromKey, this.fromKey);
        if (cmp < 0) {
            fromKey = this.fromKey;
            fromInclusive = this.fromInclusive;
        } else if (cmp == 0) {
            fromInclusive = !(fromInclusive & this.fromInclusive);
        }
        return new SortedMapRange<>(fromKey, fromInclusive, this.toKey, this.toInclusive, comparator);
    }

    public SortedMapRange<K> headMap(K toKey, boolean toInclusive) {
        if (toKey == null) {
            throw new IllegalArgumentException("SortedMapRange must have a to!");
        }
        int cmp = comparator.compare(toKey, this.toKey);
        if (cmp > 0) {
            toKey = this.toKey;
            toInclusive = this.toInclusive;
        } else if (cmp == 0) {
            toInclusive = !(toInclusive & this.toInclusive);
        }
        return new SortedMapRange<>(this.fromKey, this.fromInclusive, toKey, toInclusive, comparator);
    }

    public SortedMapRange<K> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    public SortedMapRange<K> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    public SortedMapRange<K> headMap(K toKey) {
        return headMap(toKey, false);
    }

    /**
     * Returns true if the provided key is greater than TO and less than FROM.
     */
    public boolean inRange(final K key) {
        final K fromKey = getFromKey();
        final K toKey = getToKey();

        return (fromKey == null || inFromRange(key, false)) && (toKey == null || inToRange(key, false));
    }

    /**
     * This form allows the high endpoint (as well as all legit keys).
     */
    public boolean inRange2(final K key) {
        final K fromKey = getFromKey();
        final K toKey = getToKey();

        return (fromKey == null || inFromRange(key, false)) && (toKey == null || inToRange(key, true));
    }

    /**
     * Returns true if the provided key is in the FROM range of the {@link SortedMapRange}.
     */
    public boolean inFromRange(final K key, final boolean forceInclusive) {
        final K fromKey = getFromKey();
        final boolean fromInclusive = isFromInclusive();

        final int ret = comparator.compare(key, fromKey);
        if (fromInclusive || forceInclusive) {
            return ret >= 0;
        }
        return ret > 0;
    }

    /**
     * Returns true if the provided key is in the TO range of the {@link SortedMapRange}.
     */
    public boolean inToRange(final K key, final boolean forceInclusive) {
        final K toKey = getToKey();
        final boolean toInclusive = isToInclusive();

        final int ret = comparator.compare(key, toKey);
        if (toInclusive || forceInclusive) {
            return ret <= 0;
        }
        return ret < 0;
    }

    public SortedMapRange<K> reversed() {
        return new SortedMapRange<>(toKey, toInclusive, fromKey, fromInclusive, comparator.reversed());
    }
}
