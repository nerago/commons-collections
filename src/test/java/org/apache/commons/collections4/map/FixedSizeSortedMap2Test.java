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

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.collection.IterationBehaviour;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extension of {@link AbstractSortedMapTest} for exercising the {@link FixedSizeSortedMap}
 * implementation, applying IterableMap tests.
 *
 * @since 3.0
 */
public class FixedSizeSortedMap2Test<K, V> extends AbstractIterableMapTest<K, V, IterableMap<K, V>> {

    @Override
    public IterableMap<K, V> makeObject() {
        return FixedSizeSortedMap.fixedSizeSortedMap(new TreeMap<K, V>());
    }

    @Override
    public IterableMap<K, V> makeFullMap() {
        final SortedMap<K, V> map = new TreeMap<>();
        addSampleMappings(map);
        return FixedSizeSortedMap.fixedSizeSortedMap(map);
    }

    @Override
    public boolean isSubMapViewsSerializable() {
        // TreeMap sub map views have a bug in deserialization.
        return false;
    }

    @Override
    public boolean isPutAddSupported() {
        return false;
    }

    @Override
    public boolean isRemoveSupported() {
        return false;
    }

    @Override
    public boolean isAllowNullKey() {
        return false; // need to override in this as opposed to primary test suite which has same override via SortedMapTests
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.OTHER_DECORATOR;
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.UNKNOWN;
    }

}
