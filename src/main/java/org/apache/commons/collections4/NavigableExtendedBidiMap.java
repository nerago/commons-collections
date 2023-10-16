package org.apache.commons.collections4;

public interface NavigableExtendedBidiMap<K, V,
            SubMap extends NavigableExtendedBidiMap<K, V, ?, ?>,
        InverseMap extends NavigableExtendedBidiMap<V, K, ?, ?>>
        extends SortedBidiMap<K, V, SubMap, InverseMap>,
                NavigableRangedMap<K, V, SubMap> {

}
