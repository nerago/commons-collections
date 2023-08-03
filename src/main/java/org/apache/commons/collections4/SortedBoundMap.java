package org.apache.commons.collections4;

import java.util.*;

public interface SortedBoundMap<K, V> extends SortedMap<K, V> {
    @Override
    SortedBoundMap<K, V> subMap(K fromKey, K toKey);

    @Override
    SortedBoundMap<K, V> headMap(K toKey);

    @Override
    SortedBoundMap<K, V> tailMap(K fromKey);

    SortedMapRange<? super K> getKeyRange();
}
