package org.apache.commons.collections4;

public interface NavigableExtendedBidiMap<K, V,
            SubMap extends NavigableExtendedBidiMap<K, V, SubMap, SubMap, ?>,
            RegularMap extends NavigableExtendedBidiMap<K, V, SubMap, RegularMap, InverseMap>,
            InverseMap extends NavigableExtendedBidiMap<V, K, ?, InverseMap, RegularMap>>
        extends SortedExtendedBidiMap<K, V, SubMap, RegularMap, InverseMap>,
                NavigableRangedMap<K, V, SubMap> {

}
