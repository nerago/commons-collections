package org.apache.commons.collections4.map;

import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedMap;

public class ReverseSortedMap<K, V>
        extends AbstractSortedMapDecorator<K, V,
            SortedMap<K, V>, IterableSortedMap<K, V, ?>,
            SequencedCommonsSet<K>, SequencedCommonsSet<Map.Entry<K, V>>, SequencedCommonsCollection<V>> {
    public ReverseSortedMap(final SortedRangedMap<K, V, ?> map) {
        super(map);
    }

    @Override
    protected IterableSortedMap<K, V, ?> decorateDerived(SortedMap<K, V> subMap, SortedMapRange<K> keyRange) {
        return null;
    }
}
