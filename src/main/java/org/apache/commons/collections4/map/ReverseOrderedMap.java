package org.apache.commons.collections4.map;


import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.set.ReverseSequencedSet;

// see java.util.ReverseOrderSortedMapView
public class ReverseOrderedMap<K, V>
        extends AbstractOrderedMapDecorator<K, V, OrderedMap<K, V>, OrderedMap<K, V>,
                                            SequencedSet<K>, SequencedSet<Map.Entry<K, V>>, SequencedCommonsCollection<V>> {
    /**
     * Constructor only used in deserialization, do not use otherwise.
     *
     * @since X.X
     */
    protected ReverseOrderedMap() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public ReverseOrderedMap(final OrderedMap<K, V> map) {
        super(map, map);
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
    public V putFirst(final K k, final V v) {
        return super.putLast(k, v);
    }

    @Override
    public V putLast(final K k, final V v) {
        return super.putFirst(k, v);
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
    protected OrderedMap<K, V> createReverse() {
        return decorated();
    }
}
