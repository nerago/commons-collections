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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.SortedBidiMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Abstract test class for {@link SortedBidiMap} methods and contracts.
 */
public abstract class AbstractSortedBidiMapTest<K extends Comparable<K>, V extends Comparable<V>, TMap extends SortedBidiMap<K, V, ?, ?>>
        extends AbstractOrderedBidiMapTest<K, V, TMap> {

    protected List<K> sortedKeys;
    protected List<V> sortedValues = new ArrayList<>();
    protected List<V> sortedNewValues = new ArrayList<>();

    public AbstractSortedBidiMapTest() {
        sortedKeys = getAsList(getSampleKeys());
        sortedKeys.sort(null);
        sortedKeys = Collections.unmodifiableList(sortedKeys);

        final Map<K, V> map = new TreeMap<>();
        addSampleMappings(map);

        sortedValues.addAll(map.values());
        sortedValues = Collections.unmodifiableList(sortedValues);

        sortedNewValues.addAll(getAsList(getNewSampleValues()));
    }

    @Override
    public boolean isAllowNullKey() {
        return false;
    }

    @Override
    public boolean isAllowNullValue() {
        return false;
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return null;
    }

    @Override
    public SortedMap<K, V> makeConfirmedMap() {
        return new TreeMap<>();
    }

    @Test
    public void testBidiHeadMapContains() {
        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K toKey = it.next();
        final K second = it.next();
        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);

        final SortedMap<K, V> head = sm.headMap(toKey);
        assertEquals(1, head.size());
        assertTrue(sm.containsKey(first));
        assertTrue(head.containsKey(first));
        assertTrue(sm.containsValue(firstValue));
        assertTrue(head.containsValue(firstValue));
        assertTrue(sm.containsKey(second));
        assertFalse(head.containsKey(second));
        assertTrue(sm.containsValue(secondValue));
        assertFalse(head.containsValue(secondValue));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBidiHeadMapPut() {
        if (!isPutAddSupported() || !isPutChangeSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        int size = sm.size();
        final K first = it.next();
        final K toKey = it.next();
        final K second = it.next();
        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);
        final V firstNewValue = sortedNewValues.get(0);
        final V secondNewValue = sortedNewValues.get(1);
        final V thirdNewValue = sortedNewValues.get(2);

        // normal working put
        final SortedMap<K, V> head = sm.headMap(toKey);
        assertEquals(1, head.size());
        assertTrue(sm.containsKey(first));
        assertTrue(head.containsKey(first));
        assertTrue(sm.containsValue(firstValue));
        assertTrue(head.containsValue(firstValue));
        assertFalse(head.containsValue(firstNewValue));
        assertEquals(firstValue, sm.get(first));
        assertEquals(firstValue, head.get(first));
        assertEquals(firstValue, head.put(first, firstNewValue)); // put
        assertEquals(1, head.size());
        assertTrue(sm.containsKey(first));
        assertTrue(head.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(head.containsValue(firstValue));
        assertTrue(sm.containsValue(firstNewValue));
        assertTrue(head.containsValue(firstNewValue));
        assertEquals(firstNewValue, sm.get(first));
        assertEquals(firstNewValue, head.get(first));

        // can't put outside submap range
        // note that this part can already pass but depends on exact sequence in existing put
        //  interacting with internal TreeMap validation on the range of the map
        assertEquals(1, head.size());
        assertTrue(sm.containsKey(second));
        assertFalse(head.containsKey(second));
        assertTrue(sm.containsValue(secondValue));
        assertFalse(sm.containsValue(secondNewValue));
        assertFalse(head.containsValue(secondValue));
        assertFalse(head.containsValue(secondNewValue));
        assertEquals(secondValue, sm.get(second));
        assertNull(head.get(second));
        assertThrows(IllegalArgumentException.class, () -> head.put(second, secondNewValue));
        assertEquals(1, head.size());
        assertTrue(sm.containsKey(second));
        assertFalse(head.containsKey(second));
        assertFalse(sm.containsValue(secondNewValue));
        assertTrue(sm.containsValue(secondValue));
        assertFalse(head.containsValue(secondValue));
        assertFalse(head.containsValue(secondNewValue));
        assertEquals(secondValue, sm.get(second));
        assertNull(head.get(second));

        // can't put in submap range if key doesn't already exist
        K possibleBetween = (K) (first + "a");
        if (sm.comparator() != null && sm.comparator().compare(first, possibleBetween) > 0)
            possibleBetween = (K) (toKey + "a");
        final K between = possibleBetween;
        assertEquals(1, head.size());
        assertFalse(sm.containsKey(between));
        assertFalse(head.containsKey(between));
        assertFalse(sm.containsValue(thirdNewValue));
        assertFalse(head.containsValue(thirdNewValue));
        assertNull(sm.get(between));
        assertNull(head.get(between));
        assertThrows(IllegalArgumentException.class, () -> head.put(between, thirdNewValue));
        assertEquals(1, head.size());
        assertFalse(sm.containsKey(between));
        assertFalse(head.containsKey(between));
        assertFalse(sm.containsValue(thirdNewValue));
        assertFalse(head.containsValue(thirdNewValue));
        assertNull(sm.get(between));
        assertNull(head.get(between));

        // can't put if value exists in map elsewhere (same restriction as setValue)
        assertTrue(sm.containsKey(first));
        assertTrue(head.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertFalse(head.containsKey(second));
        assertTrue(sm.containsValue(firstNewValue));
        assertTrue(sm.containsValue(secondValue));
        assertTrue(head.containsValue(firstNewValue));
        assertFalse(head.containsValue(secondValue));
        assertEquals(firstNewValue, sm.get(first));
        assertEquals(secondValue, sm.get(second));
        assertEquals(firstNewValue, head.get(first));
        assertNull(head.get(second));
        assertThrows(IllegalArgumentException.class, () -> head.put(first, secondValue));
        assertTrue(sm.containsKey(first));
        assertTrue(head.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertFalse(head.containsKey(second));
        assertTrue(sm.containsValue(firstNewValue));
        assertTrue(sm.containsValue(secondValue));
        assertTrue(head.containsValue(firstNewValue));
        assertFalse(head.containsValue(secondValue));
        assertEquals(firstNewValue, sm.get(first));
        assertEquals(secondValue, sm.get(second));
        assertEquals(firstNewValue, head.get(first));
        assertNull(head.get(second));
        assertEquals(1, head.size());
        assertEquals(size, sm.size());
        assertEquals(size, sm.inverseBidiMap().size());
    }

    @Test
    public void testBidiClearByHeadMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);
        final V toKeyValue = sm.get(toKey);

        final SortedMap<K, V> sub = sm.headMap(toKey);
        final int size = sm.size();
        assertEquals(2, sub.size());
        sub.clear();
        assertEquals(0, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());

        assertFalse(sm.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));

        assertTrue(sm.containsKey(toKey));
        assertTrue(sm.containsValue(toKeyValue));
        assertTrue(sm.inverseBidiMap().containsKey(toKeyValue));
        assertTrue(sm.inverseBidiMap().containsValue(toKey));
        assertFalse(sub.containsKey(toKey));
        assertFalse(sub.containsValue(toKeyValue));
    }

    @Test
    public void testBidiRemoveByHeadMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();
        final V toKeyValue = sm.get(toKey);
        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.headMap(toKey);
        assertEquals(2, sub.size());
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(sm.containsValue(firstValue));
        assertTrue(sm.containsValue(secondValue));
        assertTrue(sm.containsValue(toKeyValue));
        assertTrue(sub.containsValue(firstValue));
        assertTrue(sub.containsValue(secondValue));
        assertFalse(sub.containsValue(toKeyValue));

        assertEquals(firstValue, sub.remove(first));
        assertEquals(1, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        assertEquals(secondValue, sub.remove(second));
        assertEquals(0, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));

        assertTrue(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));
        assertNull(sub.remove(toKey)); // should ignore out of range
        assertTrue(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));
        assertTrue(sm.containsValue(toKeyValue));
        assertFalse(sub.containsValue(toKeyValue));
        assertEquals(toKeyValue, sm.remove(toKey)); // okay on main map
        assertFalse(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));

        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.containsValue(toKeyValue));
        assertFalse(sub.containsValue(firstValue));
        assertFalse(sub.containsValue(secondValue));
        assertFalse(sub.containsValue(toKeyValue));
    }

    @Test
    public void testBidiRemoveByHeadMapKeys() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.headMap(toKey);
        assertEquals(2, sub.size());
        assertTrue(sm.keySet().contains(first));
        assertTrue(sub.keySet().contains(first));
        assertTrue(sm.keySet().contains(second));
        assertTrue(sub.keySet().contains(second));

        final V firstValue = sub.get(first);
        assertTrue(sub.keySet().remove(first));
        assertEquals(1, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.keySet().contains(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().keySet().contains(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.keySet().contains(first));
        assertFalse(sub.containsValue(firstValue));

        final V secondValue = sub.get(second);
        assertTrue(sub.keySet().remove(second));
        assertEquals(0, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.keySet().contains(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().keySet().contains(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.keySet().contains(second));
        assertFalse(sub.containsValue(secondValue));

        assertTrue(sm.keySet().contains(toKey));
        assertFalse(sub.keySet().contains(toKey));
        assertFalse(sub.keySet().remove(toKey)); // should ignore out of range
        assertTrue(sm.keySet().contains(toKey));
        assertFalse(sub.keySet().contains(toKey));
        assertTrue(sm.keySet().remove(toKey)); // okay on main map
        assertFalse(sm.keySet().contains(toKey));
        assertFalse(sub.keySet().contains(toKey));
    }

    @Test
    public void testBidiRemoveByHeadMapValues() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();
        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);
        final V toKeyValue = sm.get(toKey);

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.headMap(toKey);
        assertEquals(2, sub.size());
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(sm.values().contains(firstValue));
        assertTrue(sm.values().contains(secondValue));
        assertTrue(sm.values().contains(toKeyValue));
        assertTrue(sub.values().contains(firstValue));
        assertTrue(sub.values().contains(secondValue));
        assertFalse(sub.values().contains(toKeyValue));

        assertTrue(sub.values().remove(firstValue));
        assertEquals(1, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(first));
        assertFalse(sm.values().contains(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().values().contains(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.values().contains(firstValue));

        assertTrue(sub.values().remove(secondValue));
        assertEquals(0, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(second));
        assertFalse(sm.values().contains(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().values().contains(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.values().contains(secondValue));

        assertTrue(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));
        assertFalse(sub.values().remove(toKeyValue)); // should ignore out of range
        assertTrue(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));
        assertTrue(sm.values().contains(toKeyValue));
        assertFalse(sub.values().contains(toKeyValue));
        assertTrue(sm.values().remove(toKeyValue)); // okay on main map
        assertFalse(sm.containsKey(toKey));
        assertFalse(sub.containsKey(toKey));

        assertFalse(sm.values().contains(firstValue));
        assertFalse(sm.values().contains(secondValue));
        assertFalse(sm.values().contains(toKeyValue));
        assertFalse(sub.values().contains(firstValue));
        assertFalse(sub.values().contains(secondValue));
        assertFalse(sub.values().contains(toKeyValue));
    }

    @Test
    public void testBidiRemoveByHeadMapEntrySet() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.headMap(toKey);
        final Set<Map.Entry<K, V>> set = sub.entrySet();
        assertEquals(2, sub.size());
        assertEquals(2, set.size());

        final Iterator<Map.Entry<K, V>> it2 = set.iterator();
        final Map.Entry<K, V> firstEntry = cloneMapEntry(it2.next());
        final Map.Entry<K, V> secondEntry = cloneMapEntry(it2.next());
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(set.contains(firstEntry));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(set.contains(secondEntry));

        set.remove(firstEntry);
        assertEquals(1, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(firstEntry.getKey()));
        assertFalse(sm.containsValue(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(firstEntry.getKey()));
        assertFalse(sub.containsKey(firstEntry.getKey()));
        assertFalse(sub.containsValue(firstEntry.getValue()));
        assertFalse(set.contains(firstEntry));

        set.remove(secondEntry);
        assertEquals(0, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(secondEntry.getKey()));
        assertFalse(sm.containsValue(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(secondEntry.getKey()));
        assertFalse(sub.containsKey(secondEntry.getKey()));
        assertFalse(sub.containsValue(secondEntry.getValue()));
        assertFalse(set.contains(secondEntry));
    }

    @Test
    public void testBidiTailMapContains() {
        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K fromKey = it.next();
        final K second = it.next();
        final V firstValue = sm.get(first);
        final V fromKeyValue = sm.get(fromKey);
        final V secondValue = sm.get(second);

        final SortedMap<K, V> sub = sm.tailMap(fromKey);
        assertEquals(sm.size() - 1, sub.size());
        assertTrue(sm.containsKey(first));
        assertFalse(sub.containsKey(first));
        assertTrue(sm.containsValue(firstValue));
        assertFalse(sub.containsValue(firstValue));
        assertTrue(sm.containsKey(fromKey));
        assertTrue(sub.containsKey(fromKey));
        assertTrue(sm.containsValue(fromKeyValue));
        assertTrue(sub.containsValue(fromKeyValue));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(sm.containsValue(secondValue));
        assertTrue(sub.containsValue(secondValue));
    }

    @Test
    public void testBidiClearByTailMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        it.next();
        final K first = it.next();
        final K fromKey = it.next();
        final K second = it.next();

        final V firstValue = sm.get(first);
        final V fromKeyValue = sm.get(fromKey);
        final V secondValue = sm.get(second);

        final SortedMap<K, V> sub = sm.tailMap(fromKey);
        final int size = sm.size();
        assertEquals(size - 3, sub.size());
        sub.clear();
        assertEquals(0, sub.size());
        assertEquals(3, sm.size());
        assertEquals(3, sm.inverseBidiMap().size());

        assertTrue(sm.containsKey(first));
        assertTrue(sm.containsValue(firstValue));
        assertTrue(sm.inverseBidiMap().containsKey(firstValue));
        assertTrue(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        assertFalse(sm.containsKey(fromKey));
        assertFalse(sm.containsValue(fromKeyValue));
        assertFalse(sm.inverseBidiMap().containsKey(fromKeyValue));
        assertFalse(sm.inverseBidiMap().containsValue(fromKey));
        assertFalse(sub.containsKey(fromKey));
        assertFalse(sub.containsValue(fromKeyValue));

        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));
    }

    @Test
    public void testBidiRemoveByTailMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        it.next();
        final K fromKey = it.next();
        final K first = it.next();
        final K second = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.tailMap(fromKey);
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));

        final Object firstValue = sub.remove(first);
        assertEquals(size - 3, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        final Object secondValue = sub.remove(second);
        assertEquals(size - 4, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));
    }

    @Test
    public void testBidiRemoveByTailMapEntrySet() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        it.next();
        final K fromKey = it.next();
        final K first = it.next();
        final K second = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.tailMap(fromKey);
        final Set<Map.Entry<K, V>> set = sub.entrySet();
        final Iterator<Map.Entry<K, V>> it2 = set.iterator();
        it2.next();
        final Map.Entry<K, V> firstEntry = cloneMapEntry(it2.next());
        final Map.Entry<K, V> secondEntry = cloneMapEntry(it2.next());
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(set.contains(firstEntry));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(set.contains(secondEntry));

        set.remove(firstEntry);
        assertEquals(size - 3, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(firstEntry.getKey()));
        assertFalse(sm.containsValue(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(firstEntry.getKey()));
        assertFalse(sub.containsKey(firstEntry.getKey()));
        assertFalse(sub.containsValue(firstEntry.getValue()));
        assertFalse(set.contains(firstEntry));

        set.remove(secondEntry);
        assertEquals(size - 4, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(secondEntry.getKey()));
        assertFalse(sm.containsValue(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(secondEntry.getKey()));
        assertFalse(sub.containsKey(secondEntry.getKey()));
        assertFalse(sub.containsValue(secondEntry.getValue()));
        assertFalse(set.contains(secondEntry));
    }

    @Test
    public void testBidiSubMapContains() {
        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        final K first = it.next();
        final K fromKey = it.next();
        final K second = it.next();
        final K toKey = it.next();
        final K third = it.next();
        final V firstValue = sm.get(first);
        final V fromKeyValue = sm.get(fromKey);
        final V secondValue = sm.get(second);
        final V thirdValue = sm.get(third);

        final SortedMap<K, V> sub = sm.subMap(fromKey, toKey);
        assertEquals(2, sub.size());
        assertTrue(sm.containsKey(first));
        assertFalse(sub.containsKey(first));
        assertTrue(sm.containsValue(firstValue));
        assertFalse(sub.containsValue(firstValue));
        assertTrue(sm.containsKey(fromKey));
        assertTrue(sub.containsKey(fromKey));
        assertTrue(sm.containsValue(fromKeyValue));
        assertTrue(sub.containsValue(fromKeyValue));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(sm.containsValue(secondValue));
        assertTrue(sub.containsValue(secondValue));
        assertTrue(sm.containsKey(third));
        assertFalse(sub.containsKey(third));
        assertTrue(sm.containsValue(thirdValue));
        assertFalse(sub.containsValue(thirdValue));
    }

    @Test
    public void testBidiClearBySubMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        final K fromKey = it.next();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final V fromKeyValue = sm.get(fromKey);
        final V firstValue = sm.get(first);
        final V secondValue = sm.get(second);
        final V toKeyValue = sm.get(toKey);

        final SortedMap<K, V> sub = sm.subMap(fromKey, toKey);
        final int size = sm.size();
        assertEquals(3, sub.size());
        sub.clear();
        assertEquals(0, sub.size());
        assertEquals(size - 3, sm.size());
        assertEquals(size - 3, sm.inverseBidiMap().size());

        assertFalse(sm.containsKey(fromKey));
        assertFalse(sm.containsValue(fromKeyValue));
        assertFalse(sm.inverseBidiMap().containsKey(fromKeyValue));
        assertFalse(sm.inverseBidiMap().containsValue(fromKey));
        assertFalse(sub.containsKey(fromKey));
        assertFalse(sub.containsValue(fromKeyValue));

        assertFalse(sm.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));

        assertTrue(sm.containsKey(toKey));
        assertTrue(sm.containsValue(toKeyValue));
        assertTrue(sm.inverseBidiMap().containsKey(toKeyValue));
        assertTrue(sm.inverseBidiMap().containsValue(toKey));
        assertFalse(sub.containsKey(toKey));
        assertFalse(sub.containsValue(toKeyValue));
    }

    @Test
    public void testBidiRemoveBySubMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        it.next();
        final K fromKey = it.next();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.subMap(fromKey, toKey);
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));

        final V firstValue = sub.remove(first);
        assertEquals(2, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(first));
        assertFalse(sm.containsValue(firstValue));
        assertFalse(sm.inverseBidiMap().containsKey(firstValue));
        assertFalse(sm.inverseBidiMap().containsValue(first));
        assertFalse(sub.containsKey(first));
        assertFalse(sub.containsValue(firstValue));

        final V secondValue = sub.remove(second);
        assertEquals(1, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(second));
        assertFalse(sm.containsValue(secondValue));
        assertFalse(sm.inverseBidiMap().containsKey(secondValue));
        assertFalse(sm.inverseBidiMap().containsValue(second));
        assertFalse(sub.containsKey(second));
        assertFalse(sub.containsValue(secondValue));
    }

    @Test
    public void testBidiRemoveBySubMapEntrySet() {
        if (!isRemoveSupported()) {
            return;
        }

        // extra test as other tests get complex
        final SortedBidiMap<K, V, ?, ?> sm = makeFullMap();
        final Iterator<K> it = sm.keySet().iterator();
        it.next();
        it.next();
        final K fromKey = it.next();
        final K first = it.next();
        final K second = it.next();
        final K toKey = it.next();

        final int size = sm.size();
        final SortedMap<K, V> sub = sm.subMap(fromKey, toKey);
        final Set<Map.Entry<K, V>> set = sub.entrySet();
        assertEquals(3, set.size());
        final Iterator<Map.Entry<K, V>> it2 = set.iterator();
        it2.next();
        final Map.Entry<K, V> firstEntry = cloneMapEntry(it2.next());
        final Map.Entry<K, V> secondEntry = cloneMapEntry(it2.next());
        assertTrue(sm.containsKey(first));
        assertTrue(sub.containsKey(first));
        assertTrue(set.contains(firstEntry));
        assertTrue(sm.containsKey(second));
        assertTrue(sub.containsKey(second));
        assertTrue(set.contains(secondEntry));

        set.remove(firstEntry);
        assertEquals(2, sub.size());
        assertEquals(size - 1, sm.size());
        assertEquals(size - 1, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(firstEntry.getKey()));
        assertFalse(sm.containsValue(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(firstEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(firstEntry.getKey()));
        assertFalse(sub.containsKey(firstEntry.getKey()));
        assertFalse(sub.containsValue(firstEntry.getValue()));
        assertFalse(set.contains(firstEntry));

        set.remove(secondEntry);
        assertEquals(1, sub.size());
        assertEquals(size - 2, sm.size());
        assertEquals(size - 2, sm.inverseBidiMap().size());
        assertFalse(sm.containsKey(secondEntry.getKey()));
        assertFalse(sm.containsValue(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsKey(secondEntry.getValue()));
        assertFalse(sm.inverseBidiMap().containsValue(secondEntry.getKey()));
        assertFalse(sub.containsKey(secondEntry.getKey()));
        assertFalse(sub.containsValue(secondEntry.getValue()));
        assertFalse(set.contains(secondEntry));
    }
}
