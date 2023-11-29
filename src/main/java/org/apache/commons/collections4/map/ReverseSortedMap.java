package org.apache.commons.collections4.map;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedMap;

public class ReverseSortedMap<K, V>
        extends AbstractSortedMapDecorator<K, V,
            SortedMap<K, V>, IterableSortedMap<K, V, ?>,
            SequencedCommonsSet<K>, SequencedCommonsSet<Map.Entry<K, V>>, SequencedCommonsCollection<V>> {
    public ReverseSortedMap(final SortedRangedMap<K, V, ?> map) {
        super(map, map.getKeyRange());
    }

    public ReverseSortedMap(final SortedMap<K, V> map, final SortedMapRange<K> keyRange) {
        super(map, keyRange);
    }

    @Override
    public K firstKey() {
        return super.lastKey();
    }

    @Override
    public K lastKey() {
        return super.firstKey();
    }

    @Override
    public K nextKey(final K key) {
        return super.previousKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return super.nextKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return super.lastEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return super.firstEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return super.pollLastEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return super.pollFirstEntry();
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return super.getKeyRange().reversed();
    }

    @Override
    public Comparator<? super K> comparator() {
        return Collections.reverseOrder(super.comparator());
    }

    @Override
    public IterableSortedMap<K, V, ?> subMap(final SortedMapRange<K> range) {
        return (IterableSortedMap<K, V, ?>) range.reversed().applyToMap(decorated());
    }

    @Override
    public SequencedCommonsSet<K> keySet() {
        return SetUtils.reversedSet(decorated().sequencedKeySet());
    }

    @Override
    public SequencedCommonsSet<Entry<K, V>> entrySet() {
        return SetUtils.reversedSet(decorated().sequencedEntrySet());
    }

    @Override
    public SequencedCommonsCollection<V> values() {
        return CollectionUtils.reversedCollection(decorated().sequencedValues());
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return super.descendingMapIterator();
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return super.mapIterator();
    }

    @Override
    protected IterableSortedMap<K, V, ?> decorateDerived(final SortedMap<K, V> subMap, final SortedMapRange<K> keyRange) {
        return new ReverseSortedMap<>(subMap, keyRange);
    }
}
