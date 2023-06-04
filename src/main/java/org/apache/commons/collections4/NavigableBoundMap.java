package org.apache.commons.collections4;

import java.util.NavigableMap;

public interface NavigableBoundMap<K, V> extends NavigableMap<K, V>, SortedBoundMap<K, V> {
    @Override
    NavigableBoundMap<K, V> subMap(K fromKey, K toKey);

    @Override
    NavigableBoundMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    @Override
    NavigableBoundMap<K, V> headMap(K toKey);

    @Override
    NavigableBoundMap<K, V> headMap(K toKey, boolean inclusive);

    @Override
    NavigableBoundMap<K, V> tailMap(K fromKey);

    @Override
    NavigableBoundMap<K, V> tailMap(K fromKey, boolean inclusive);
}
