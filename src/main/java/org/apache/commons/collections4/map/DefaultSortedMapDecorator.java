package org.apache.commons.collections4.map;

import org.apache.commons.collections4.SortedBoundMap;
import org.apache.commons.collections4.SortedMapRange;

import java.util.SortedMap;

public class DefaultSortedMapDecorator<K, V> extends AbstractSortedMapDecorator<K, V> {
    public DefaultSortedMapDecorator(SortedMap<K, V> sortedMap) {
        super(sortedMap);
    }

    @Override
    protected SortedMap<K, V> wrapMap(SortedMap<K, V> map) {
        return new DefaultSortedMapDecorator<>(map);
    }
}
