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

import java.util.SortedMap;

public interface SortedRangedMap<K, V, TSubMap extends SortedRangedMap<K, V, ?>>
         extends SortedMap<K, V> {
    @Override
    default TSubMap subMap(final K fromKey, final K toKey) {
        return subMap(getKeyRange().subRange(fromKey, toKey));
    }

    @Override
    default TSubMap headMap(final K toKey) {
        return subMap(getKeyRange().head(toKey));
    }

    @Override
    default TSubMap tailMap(final K fromKey) {
        return subMap(getKeyRange().tail(fromKey));
    }

    TSubMap subMap(SortedMapRange<K> range);

    /**
     * Range of keys included in this map instance (i.e. full map or sub map)
     * @return key range
     */
    SortedMapRange<K> getKeyRange();
}
