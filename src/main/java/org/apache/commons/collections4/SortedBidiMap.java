/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4;

import java.util.Comparator;
import java.util.Map;

public interface SortedBidiMap<K, V,
            TSubMap extends SortedBidiMap<K, V, ?, ?>,
        TInverseMap extends SortedBidiMap<V, K, ?, ?>>
        extends OrderedBidiMap<K, V, TSubMap, TInverseMap>,
                IterableSortedMap<K, V, TSubMap> {

    default V firstValue() {
        return sequencedValues().getFirst();
    }

    default V lastValue() {
        return sequencedValues().getLast();
    }

    /**
     * Get the comparator used for the values in the value-to-key map aspect.
     * @return Comparator&lt;? super V&gt;
     */
    Comparator<? super V> valueComparator();

    SortedMapRange<V> getValueRange();

    @Override
    SortedRangedSet<K> keySet();

    @Override
    SortedRangedSet<Entry<K, V>> entrySet();

    @Override
    SortedRangedSet<V> values();

    @Override
    default SortedRangedSet<K> sequencedKeySet() {
        return keySet();
    }

    @Override
    default SortedRangedSet<Entry<K, V>> sequencedEntrySet() {
        return entrySet();
    }

    @Override
    default SortedRangedSet<V> sequencedValues() {
        return values();
    }
}
