package org.apache.commons.collections4;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

public interface SequencedCommonsMap<K, V>
        extends SequencedMap<K, V> {
    @Override
    SequencedCommonsMap reversed();

    @Override
    SequencedSet<K> sequencedKeySet();

    @Override
    SequencedCollection<V> sequencedValues();

    @Override
    SequencedSet<Map.Entry<K, V>> sequencedEntrySet();

    @Override
    default SequencedSet<K> keySet() {
        return sequencedKeySet();
    }

    @Override
    default SequencedCollection<V> values() {
        return sequencedValues();
    }

    @Override
    default SequencedSet<Map.Entry<K, V>> entrySet() {
        return sequencedEntrySet();
    }
}
