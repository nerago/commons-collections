package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.NavigableBoundMap;
import org.apache.commons.collections4.SortedMapRange;

import java.util.NavigableMap;
import java.util.SortedMap;

public class DualTreeBidi2MapSubValues<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapBase<K, V> {
    protected final DualTreeBidi2Map<K, V> parent;
    protected final SortedMapRange<K> range;

    DualTreeBidi2MapSubValues(NavigableMap<K, V> subKeyMap, SortedMapRange<K> range, DualTreeBidi2Map<K, V> parent) {
        super(subKeyMap, parent.valueMap);
        this.range = range;
        this.parent = parent;
    }

    @Override
    protected DualTreeBidi2Map<?, ?> primaryMap() {
        return parent;
    }

    @Override
    protected DualTreeBidi2MapBase<V, K> createInverse() {
        return null;
    }

    @Override
    protected NavigableBoundMap<K, V> wrapMap(SortedMap<K, V> map, SortedMapRange<K> range) {
        return null;
    }

    @Override
    protected void modified() {

    }

    @Override
    protected KeySet<K, V> createKeySet(boolean descending) {
        return null;
    }

    @Override
    protected ValueMapKeySet<K, V> createValueSet() {
        return null;
    }

    @Override
    protected EntrySet<K, V> createEntrySet() {
        return null;
    }
}
