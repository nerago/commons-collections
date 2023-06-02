package org.apache.commons.collections4.bidimap;

import java.util.NavigableMap;

public class DualTreeBidi2MapImprovedSubKeys<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapImproved<K, V> {
    public DualTreeBidi2MapImprovedSubKeys(NavigableMap<K, V> subKeyMap, DualTreeBidi2MapImproved<K, V> parent) {
        super(subKeyMap, parent.valueMap);
    }

    public DualTreeBidi2MapImprovedSubKeys() {
    }
}
