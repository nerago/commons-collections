package org.apache.commons.collections4.iterators;

import java.util.Map;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

public class MapIteratorToEntryAdapter<K, V> extends AbstractMapIteratorAdapter<K, V, Map.Entry<K, V>> {
    public MapIteratorToEntryAdapter(final MapIterator<K, V> iterator) {
        super(iterator);
    }

    @Override
    protected Map.Entry<K, V> transform(final K key, final V value) {
        return new UnmodifiableMapEntry<>(key, value);
    }
}
