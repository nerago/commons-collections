package org.apache.commons.collections4;

import java.util.NavigableMap;

public interface NavigableBoundMap<K, V> extends NavigableMap<K, V>, IterableSortedMap<K, V> {
    @Override
    NavigableBoundMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    @Override
    NavigableBoundMap<K, V> headMap(K toKey, boolean inclusive);

    @Override
    NavigableBoundMap<K, V> tailMap(K fromKey, boolean inclusive);

    @Override
    default NavigableBoundMap<K, V> subMap(final K fromKey, final K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    default NavigableBoundMap<K, V> headMap(final K toKey) {
        return headMap(toKey, false);
    }

    @Override
    default NavigableBoundMap<K, V> tailMap(final K fromKey) {
        return tailMap(fromKey, true);
    }
}
