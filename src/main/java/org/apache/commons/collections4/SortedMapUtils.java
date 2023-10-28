package org.apache.commons.collections4;


import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.SortedMap;

public final class SortedMapUtils {
    @SuppressWarnings("unchecked")
    public static <K, V> K nextKey(final SortedMap<K, V> map, final K key) throws IllegalArgumentException {
        if (key == null) {
            return null;
        }

        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).higherKey(key);
        } else if (map instanceof OrderedMap) {
            return ((OrderedMap<K, V>) map).nextKey(key);
        } else {
            final SortedMap<K, V> tail = map.tailMap(key);
            final Iterator<K> iter = tail.keySet().iterator();
            if (!iter.hasNext()) {
                return null;
            }

            final K next = iter.next();
            if (!Objects.equals(next, key)) { // todo original doesn't have this check
                return next;
            }

            if (iter.hasNext()) {
                return iter.next();
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> K previousKey(final SortedMap<K, V> map, final K key) {
        if (key == null) {
            return null;
        }

        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).lowerKey(key);
        } else if (map instanceof OrderedMap) {
            return ((OrderedMap<K, V>) map).previousKey(key);
        } else {
            final SortedMap<K, V> headMap = map.headMap(key);
            return headMap.isEmpty() ? null : headMap.lastKey();
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map.Entry<K, V> nextEntry(final SortedMap<K, V> map, final K key) throws IllegalArgumentException {
        if (key == null) {
            return null;
        }

        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).higherEntry(key);
        } else if (map instanceof OrderedMap) {
            final K nextKey = ((OrderedMap<K, V>) map).nextKey(key);
            return findEntry(map, nextKey);
        } else {
            final SortedMap<K, V> tail = map.tailMap(key);
            final Iterator<Map.Entry<K, V>> iter = tail.entrySet().iterator();
            if (!iter.hasNext()) {
                return null;
            }

            final Map.Entry<K, V> next = iter.next();
            if (!Objects.equals(next.getKey(), key)) {
                return next;
            }

            if (iter.hasNext()) {
                return iter.next();
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map.Entry<K, V> previousEntry(final SortedMap<K, V> map, final K key) {
        if (key == null) {
            return null;
        }

        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).lowerEntry(key);
        } else if (map instanceof OrderedMap) {
            final K prevKey = ((OrderedMap<K, V>) map).previousKey(key);
            return findEntry(map, key);
        } else {
            final SortedMap<K, V> headMap = map.headMap(key);
            if (headMap.isEmpty()) {
                return null;
            } else {
                return findEntry(map, headMap.lastKey());
            }
        }
    }

    public static <K, V> Map.Entry<K,V> findEntry(final SortedMap<K, V> map, final K key) {
        final Iterator<Map.Entry<K, V>> iterator = map.tailMap(key).entrySet().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    private SortedMapUtils() {
    }
}
