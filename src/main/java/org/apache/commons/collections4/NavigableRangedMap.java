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

import java.util.NavigableMap;

public interface NavigableRangedMap<K, V, SubMap extends NavigableRangedMap<K, V, ?>>
        extends NavigableMap<K, V>, IterableSortedMap<K, V, SubMap> {
    @Override
    default SubMap subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
        return subMap(getKeyRange().subRange(fromKey, fromInclusive, toKey, toInclusive));
    }

    @Override
    default SubMap headMap(final K toKey, final boolean inclusive) {
        return subMap(getKeyRange().head(toKey, inclusive));
    }

    @Override
    default SubMap tailMap(final K fromKey, final boolean inclusive) {
        return subMap(getKeyRange().tail(fromKey, inclusive));
    }

    @Override
    default SubMap subMap(final K fromKey, final K toKey) {
        return IterableSortedMap.super.subMap(fromKey, toKey);
    }

    @Override
    default SubMap headMap(final K toKey) {
        return IterableSortedMap.super.headMap(toKey);
    }

    @Override
    default SubMap tailMap(final K fromKey) {
        return IterableSortedMap.super.tailMap(fromKey);
    }
}
