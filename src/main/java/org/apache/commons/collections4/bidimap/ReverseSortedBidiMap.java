package org.apache.commons.collections4.bidimap;

import java.util.Map;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.SortedRangedSet;

public class ReverseSortedBidiMap<K, V>
        extends AbstractSortedBidiMapDecorator<K, V,
            SortedBidiMap<K, V, ?, ?>, SortedBidiMap<V, K, ?, ?>,
            SortedBidiMap<K, V, ?, ?>, SortedBidiMap<V, K, ?, ?>,
            SortedRangedSet<K>, SortedRangedSet<Map.Entry<K, V>>, SortedRangedSet<V>> {

    /**
     * Constructor that wraps (not copies).
     *
     * @param map     the map to decorate, must not be null
     * @throws NullPointerException if the collection is null
     */
    public ReverseSortedBidiMap(SortedBidiMap<K, V, ?, ?> map) {
        super(map, map);
    }

    @Override
    protected SortedBidiMap<K, V, ?, ?> createReverse() {
        return decorated();
    }

    @Override
    protected SortedBidiMap<K, V, ?, ?> decorateDerived(final SortedBidiMap<K, V, ?, ?> map) {
        return null;
    }

    @Override
    protected SortedBidiMap<V, K, ?, ?> decorateInverse(final SortedBidiMap<V, K, ?, ?> vkSortedBidiMap) {
        return null;
    }
}
