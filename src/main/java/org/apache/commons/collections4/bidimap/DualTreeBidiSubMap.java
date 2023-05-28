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
package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;

import java.util.*;

/**
 * Internal sub map view.
 */
class DualTreeBidiSubMap<K, V> extends AbstractDualTreeBidiMap<K, V> {
    protected DualTreeBidiSubMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseMap) {
        super(normalMap, reverseMap, inverseMap);
    }

    @Override
    protected AbstractDualTreeBidiMap<V, K> createInverse() {
        return new DualTreeBidiSubMapInverse<>(reverseMap(), normalMap(), this);
    }

    @Override
    protected AbstractDualTreeBidiMap<K, V> createSubMap(NavigableMap<K, V> normalMap) {
        return new DualTreeBidiSubMap<>(normalMap, reverseMap(),null);
    }

    @Override
    public Comparator<? super K> comparator() {
        return normalMap().comparator();
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return reverseMap().comparator();
    }

    @Override
    public void clear() {
        // override as default implementation uses reverseMap
        for (final Iterator<K> it = keySet().iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
    }

    @Override
    public V put(K key, V value) {
        if (normalMap().containsKey(key)) {
            V oldValue = normalMap().get(key);
            if (valueEquals(value, oldValue)) {
                return value;
            } else if (reverseMap().containsKey(value) && !keyEquals(reverseMap().get(value), key)) {
                throw new IllegalArgumentException(
                        "Cannot use put on sub map when the value being set is already in the map");
            }

            normalMap().put(key, value);
            reverseMap().remove(oldValue);
            reverseMap().put(value, key);
            return oldValue;
        } else {
            throw new IllegalArgumentException(
                    "Cannot use put on sub map unless the key is already in that map");
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        // override as default implementation uses reverseMap as a simple lookup
        // we need to also check it's in our filtered keys
        if (reverseMap().containsKey(value)) {
            K key = reverseMap().get(value);
            return normalMap().containsKey(key);
        }
        return false;
    }

    @Override
    public K getKey(Object value) {
        if (reverseMap().containsKey(value)) {
            K key = reverseMap().get(value);
            if (normalMap().containsKey(key)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public K removeValue(Object value) {
        if (reverseMap().containsKey(value)) {
            K key = reverseMap().get(value);
            if (normalMap().containsKey(key)) {
                normalMap().remove(key);
                reverseMap().remove(value);
                return key;
            }
        }
        return null;
    }

    @Override
    protected boolean removeValueViaCollection(V value) {
        if (reverseMap().containsKey(value)) {
            Object key = reverseMap().get(value);
            if (normalMap().containsKey(key)) {
                normalMap().remove(key);
                reverseMap().remove(value);
                return true;
            }
        }
        return false;
    }
}
