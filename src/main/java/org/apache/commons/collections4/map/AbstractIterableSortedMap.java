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

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedSet;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.SequencedCommonsSet;
import org.apache.commons.collections4.SortedMapRange;

public abstract class AbstractIterableSortedMap<K, V,
            TSubMap extends IterableSortedMap<K, V, ?>,
            TKeySet extends SequencedCommonsSet<K>,
            TEntrySet extends SequencedCommonsSet<Map.Entry<K, V>>,
            TValueSet extends SequencedCommonsCollection<V>>
        extends AbstractIterableMapAlternate<K, V, TKeySet, TEntrySet, TValueSet>
        implements IterableSortedMap<K, V, TSubMap> {
    private static final long serialVersionUID = 8059244983988773629L;

    private TSubMap reversed;

    @Override
    public abstract OrderedMapIterator<K, V> mapIterator();

    @Override
    public abstract TSubMap subMap(final SortedMapRange<K> range);

    protected TSubMap createReversed() {
        return (TSubMap) new ReverseSortedMap<>(this);
    }

    @Override
    public TSubMap reversed() {
        if (reversed == null) {
            reversed = createReversed();
        }
        return reversed;
    }
}
