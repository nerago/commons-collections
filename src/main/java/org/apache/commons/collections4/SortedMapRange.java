/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.collections4.map.EmptyMap;

/**
 * Represents the range of keys/values included in a given instance of sub collection.
 * @param <T> the element object type (usually a key)
 *
 * @see SortedMap#subMap(Object, Object)
 * @see NavigableMap#subMap(Object, boolean, Object, boolean)
 * @see java.util.SortedSet#subSet(Object, Object)
 * @see java.util.NavigableSet#subSet(Object, boolean, Object, boolean)
 */
public final class SortedMapRange<T> implements Serializable {
    private static final long serialVersionUID = 5904683499000042719L;

    /** The key to start from, null if the beginning. */
    private final T fromKey;

    /** The key to end at, null if till the end. */
    private final T toKey;

    /** Whether the 'from' is inclusive. */
    private final boolean fromInclusive;

    /** Whether the 'to' is inclusive. */
    private final boolean toInclusive;

    /** Comparator which defines key order. */
    private final Comparator<? super T> comparator;

    private SortedMapRange(final T fromKey, final boolean fromInclusive,
                           final T toKey, final boolean toInclusive,
                           final Comparator<? super T> comparator) {
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
    public T getFromKey() {
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
    public T getToKey() {
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

    public SortedMapRange<T> subRange(final T fromKey, final boolean fromInclusive, final T toKey, final boolean toInclusive) {
        if (fromKey == null && toKey == null) {
            throw new IllegalArgumentException("SortedMapRange sub must have a from or to");
        }
        return makeSubRange(fromKey, fromInclusive, toKey, toInclusive);
    }

    public SortedMapRange<T> tail(final T fromKey, final boolean fromInclusive) {
        if (fromKey == null) {
            throw new IllegalArgumentException("SortedMapRange tail must have a from");
        }
        return makeSubRange(fromKey, fromInclusive, null, false);
    }

    public SortedMapRange<T> head(final T toKey, final boolean toInclusive) {
        if (toKey == null) {
            throw new IllegalArgumentException("SortedMapRange head must have a to");
        }
        return makeSubRange(null, false, toKey, toInclusive);
    }

    public SortedMapRange<T> subRange(final T fromKey, final T toKey) {
        return subRange(fromKey, true, toKey, false);
    }

    public SortedMapRange<T> tail(final T fromKey) {
        return tail(fromKey, true);
    }

    public SortedMapRange<T> head(final T toKey) {
        return head(toKey, false);
    }

    private int compare(final T a, final T b) {
        assert a != null && b != null;
        return comparator.compare(a, b);
    }

    private SortedMapRange<T> makeSubRange(final T fromKey, final boolean fromInclusive, final T toKey, final boolean toInclusive) {
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
    public boolean inRange(final T key) {
        return inFromRange(key) && inToRange(key);
    }

    /**
     * Returns true if the provided key is in the FROM range of the {@link SortedMapRange}.
     */
    public boolean inFromRange(final T key) {
        if (isEmpty() || key == null) {
            return false;
        } else if (fromKey == null) {
            return true;
        }
        final int ret = compare(key, fromKey);
        if (fromInclusive) {
            return ret >= 0;
        }
        return ret > 0;
    }

    public boolean rangeInFromRange(final T key, final boolean inclusive) {
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
    public boolean inToRange(final T key) {
        if (isEmpty() || key == null) {
            return false;
        } else if (toKey == null) {
            return true;
        }
        final int ret = compare(key, toKey);
        if (toInclusive) {
            return ret <= 0;
        }
        return ret < 0;
    }

    private boolean rangeInToRange(final T key, final boolean inclusive) {
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

    public SortedMapRange<T> reversed() {
        if (isEmpty() || isFull()) {
            return this;
        }
        return new SortedMapRange<>(toKey, toInclusive, fromKey, fromInclusive, comparator.reversed());
    }

    public T applyMapFindFirstKey(final NavigableMap<T, ?> map) {
        final Map.Entry<T, ?> entry;
        if (fromKey == null) {
            entry = map.firstEntry();
        } else {
            if (fromInclusive) {
                entry = map.ceilingEntry(fromKey);
            } else {
                entry = map.higherEntry(fromKey);
            }
        }

        final T key = entry != null ? entry.getKey() : null;
        if (key == null || !inRange(key)) {
            throw new NoSuchElementException();
        }
        return key;
    }

    public <V> NavigableMap<T, V> applyToNavigableMap(final NavigableMap<T, V> map) {
        if (fromKey != null && toKey != null) {
            return map.subMap(fromKey, fromInclusive, toKey, toInclusive);
        } else if (fromKey != null) {
            return map.tailMap(fromKey, fromInclusive);
        } else if (toKey != null) {
            return map.headMap(toKey, toInclusive);
        } else if (isFull()) {
            return map;
        } else {
            return EmptyMap.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    public <V, M extends SortedMap<T, V>> M applyToMap(final M map) {
        if (fromKey != null && fromInclusive && toKey != null && !toInclusive) {
            return (M) map.subMap(fromKey, toKey);
        } else if (fromKey != null && fromInclusive && toKey == null) {
            return (M) map.tailMap(fromKey);
        } else if (fromKey == null && toKey != null && !toInclusive) {
            return (M) map.headMap(toKey);
        } else if (map instanceof NavigableMap) {
            return (M) applyToNavigableMap((NavigableMap<T, V>) map);
        } else if (isFull()) {
            return map;
        } else if (isEmpty()) {
            return (M) EmptyMap.emptyMap();
        } else {
            throw new IllegalArgumentException("range is not applicable to basic SortedMap");
        }
    }

    @SuppressWarnings("unchecked")
    public NavigableSet<T> applyToNavigableSet(final NavigableSet<T> set) {
        if (fromKey != null && toKey != null) {
            return set.subSet(fromKey, fromInclusive, toKey, toInclusive);
        } else if (fromKey != null) {
            return set.tailSet(fromKey, fromInclusive);
        } else if (toKey != null) {
            return set.headSet(toKey, toInclusive);
        } else if (isFull()) {
            return set;
        } else {
            return Collections.emptyNavigableSet();
        }
    }

    @SuppressWarnings("unchecked")
    public <S extends SortedSet<T>> S applyToSet(final S set) {
        if (fromKey != null && fromInclusive && toKey != null && !toInclusive) {
            return (S) set.subSet(fromKey, toKey);
        } else if (fromKey != null && fromInclusive && toKey == null) {
            return (S) set.tailSet(fromKey);
        } else if (fromKey == null && toKey != null && !toInclusive) {
            return (S) set.headSet(toKey);
        } else if (set instanceof NavigableSet) {
            return (S) applyToNavigableSet((NavigableSet<T>) set);
        } else if (isFull()) {
            return set;
        } else if (isEmpty()) {
            return (S) EmptyMap.emptyMap();
        } else {
            throw new IllegalArgumentException("range is not applicable to basic SortedSet");
        }
    }
}
