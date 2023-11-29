package org.apache.commons.collections4;

public interface NavigableExtendedBidiMap<K, V,
            TSubMap extends NavigableExtendedBidiMap<K, V, ?, ?>,
        TInverseMap extends NavigableExtendedBidiMap<V, K, ?, ?>>
        extends SortedBidiMap<K, V, TSubMap, TInverseMap>,
                NavigableRangedMap<K, V, TSubMap> {

    TSubMap subMap(SortedMapRange<K> keyRange, SortedMapRange<V> valueRange);

    @Override
    default TSubMap subMap(final SortedMapRange<K> range) {
        return subMap(range, getValueRange());
    }
}
