package org.apache.commons.collections4;


import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Objects;
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
    public static <K, V> K previousKey(final SortedMap<K,V> map, final K key) {
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

    private SortedMapUtils() {
    }
}
