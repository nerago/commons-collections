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

import org.apache.commons.collections4.IterableExtendedMap;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;

public abstract class AbstractIterableSortedMap<K extends Comparable<K>, V>
        extends AbstractIterableMapAlternate<K, V>
        implements IterableSortedMap<K, V>, IterableExtendedMap<K, V> {
    private static final long serialVersionUID = 8059244983988773629L;

    @Override
    public abstract OrderedMapIterator<K, V> mapIterator();

    @Override
    public abstract IterableSortedMap<K, V> subMap(final SortedMapRange<K> range);

    public final IterableSortedMap<K, V> subMap(final K fromKey, final K toKey) {
        return subMap(getKeyRange().subRange(fromKey, toKey));
    }

    public final IterableSortedMap<K, V> headMap(final K toKey) {
        return subMap(getKeyRange().head(toKey));
    }

    public final IterableSortedMap<K, V> tailMap(final K fromKey) {
        return subMap(getKeyRange().tail(fromKey));
    }
}
