package org.apache.commons.collections4.map;

import org.apache.commons.collections4.SortedBoundMap;
import org.apache.commons.collections4.SortedMapRange;

import java.util.SortedMap;

public class DefaultSortedMapDecorator<K, V> extends AbstractSortedMapDecorator<K, V> {
    private final SortedMapRange<K> range;

    public DefaultSortedMapDecorator(SortedMap<K, V> sortedMap, SortedMapRange<K> range) {
        super(sortedMap);
        this.range = SortedMapRange.full(sortedMap.comparator());
    }

    @Override
    protected SortedBoundMap<K, V> wrapMap(SortedMap<K, V> map, SortedMapRange<K> range) {
        return null;
    }

    @Override
    public SortedMapRange<K> getMapRange() {
        return range;
    }
}
