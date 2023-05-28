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
    /**
     * Constructor.
     *
     * @param parent the parent bidi map
     * @param sub    the subMap sorted map
     */
    protected DualTreeBidiSubMap(final DualTreeBidiMap<K, V> parent, final NavigableMap<K, V> sub) {
        // use the normalMap as the filtered map, but reverseMap as the full map
        // this forces values to be checked more carefully
        super(sub, parent.reverseMap());
    }

    protected DualTreeBidiSubMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseMap) {
        super(normalMap, reverseMap, inverseMap);
    }

    @Override
    protected DualTreeBidiSubMap<V, K> createInverse() {
        return new DualTreeBidiSubMap<>(reverseMap(), normalMap(), this);
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
    public V put(K key, V value) {
        if (normalMap().containsKey(key)) {
            reverseMap().remove(normalMap().get(key));
        }
        if (reverseMap().containsKey(value)) {
            normalMap().remove(reverseMap().get(value));
        }
        final V obj = normalMap().put(key, value);
        reverseMap().put(value, key);
        return obj;
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
    protected V setValueViaCollection(K key, V value) {
        return super.setValueViaCollection(key, value);
    }

    @Override
    protected boolean removeViaCollection(K key) {
        return super.removeViaCollection(key);
    }

    @Override
    public boolean remove(Object key, Object expectedValue) {
        return super.remove(key, expectedValue);
    }

    @Override
    public V remove(Object key) {
        return super.remove(key);
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
