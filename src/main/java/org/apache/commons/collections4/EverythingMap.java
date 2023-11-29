package org.apache.commons.collections4;


public interface EverythingMap<K, V> extends
        IterableExtendedMap<K, V>,
        Trie<K, V, EverythingMap<K, V>>,
        BoundedMap<K, V>,
        NavigableExtendedBidiMap<K, V, EverythingMap<K, V>, EverythingMap<V, K>> {
}
