package org.apache.commons.collections4;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

public interface SequencedCommonsMap<K, V>
        extends SequencedMap<K, V> {
    @Override
    SequencedCommonsMap<K, V> reversed();

    @Override
    default SequencedSet<K> sequencedKeySet() {
        return keySet();
    }

    @Override
    default SequencedSet<Map.Entry<K, V>> sequencedEntrySet() {
        return entrySet();
    }

    @Override
    default SequencedCollection<V> sequencedValues() {
        return values();
    }

    @Override
    SequencedSet<K> keySet();

    @Override
    SequencedSet<Map.Entry<K, V>> entrySet();

    @Override
    SequencedCollection<V> values() ;
}
