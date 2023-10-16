package org.apache.commons.collections4;


public interface EverythingMap<K, V> extends IterableExtendedMap<K, V>,
        NavigableRangedMap<K, V, EverythingMap<K, V>>,
        SortedBidiMap<K, V, EverythingMap<K, V>, EverythingMap<V, K>>,
        Trie<K, V, EverythingMap<K, V>>,
        BoundedMap<K, V> {
}
