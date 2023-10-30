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
package org.apache.commons.collections4.map;

import java.util.SequencedCollection;
import java.util.SequencedSet;

import org.apache.commons.collections4.IterableExtendedMap;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;

public abstract class AbstractIterableSortedMap<K extends Comparable<K>, V>
        extends AbstractIterableMapAlternate<K, V>
        implements IterableSortedMap<K, V, AbstractIterableSortedMap<K, V>>, IterableExtendedMap<K, V> {
    private static final long serialVersionUID = 8059244983988773629L;

    private AbstractIterableSortedMap<K, V> reversed;

    @Override
    public abstract OrderedMapIterator<K, V> mapIterator();

    @Override
    public abstract AbstractIterableSortedMap<K, V> subMap(final SortedMapRange<K> range);

    // todo remove
    public final AbstractIterableSortedMap<K, V> subMap(final K fromKey, final K toKey) {
        return IterableSortedMap.super.subMap(fromKey, toKey);
    }

    public final AbstractIterableSortedMap<K, V> headMap(final K toKey) {
        return IterableSortedMap.super.headMap(toKey);
    }

    public final AbstractIterableSortedMap<K, V> tailMap(final K fromKey) {
        return IterableSortedMap.super.tailMap(fromKey);
    }

    @Override
    public final SequencedSet<K> keySet() {
        return sequencedKeySet();
    }

    @Override
    public final SequencedSet<Entry<K, V>> entrySet() {
        return sequencedEntrySet();
    }

    @Override
    public final SequencedCollection<V> values() {
        return sequencedValues();
    }

    protected abstract AbstractIterableSortedMap<K, V> createReversed();

    @Override
    public AbstractIterableSortedMap<K, V> reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }
}
