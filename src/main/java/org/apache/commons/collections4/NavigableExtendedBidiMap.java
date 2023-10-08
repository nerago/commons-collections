package org.apache.commons.collections4;

public interface NavigableExtendedBidiMap<K, V,
            RegularMap extends NavigableExtendedBidiMap<K, V, RegularMap, InverseMap>,
            InverseMap  extends NavigableExtendedBidiMap<V, K, InverseMap, RegularMap>>
        extends SortedExtendedBidiMap<K, V, RegularMap, InverseMap>,
                NavigableRangedMap<K, V, RegularMap> {

}
