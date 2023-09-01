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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.BulkTest;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.iterators.AbstractMapIteratorTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Abstract test class for {@link OrderedBidiMap} methods and contracts.
 */
public abstract class AbstractOrderedBidiMapTest<K, V> extends AbstractBidiMapTest<K, V> {

    public AbstractOrderedBidiMapTest(final String testName) {
        super(testName);
    }

    public AbstractOrderedBidiMapTest() {
    }

    @Test
    public void testFirstKey() {
        resetEmpty();
        OrderedBidiMap<K, V> bidi = getMap();

        final OrderedBidiMap<K, V> finalBidi = bidi;
        assertThrows(NoSuchElementException.class, () -> finalBidi.firstKey());

        resetFull();
        bidi = getMap();
        final K confirmedFirst = confirmed.keySet().iterator().next();
        assertEquals(confirmedFirst, bidi.firstKey());
    }

    @Test
    public void testLastKey() {
        resetEmpty();
        OrderedBidiMap<K, V> bidi = getMap();

        final OrderedBidiMap<K, V> finalBidi = bidi;
        assertThrows(NoSuchElementException.class, () -> finalBidi.lastKey());

        resetFull();
        bidi = getMap();
        K confirmedLast = null;
        for (final Iterator<K> it = confirmed.keySet().iterator(); it.hasNext();) {
            confirmedLast = it.next();
        }
        assertEquals(confirmedLast, bidi.lastKey());
    }

    @Test
    public void testNextKey() {
        resetEmpty();
        OrderedBidiMap<K, V> bidi = (OrderedBidiMap<K, V>) map;
        assertNull(bidi.nextKey(getOtherKeys()[0]));
        if (!isAllowNullKey()) {
            try {
                assertNull(bidi.nextKey(null)); // this is allowed too
            } catch (final NullPointerException ex) {}
        } else {
            assertNull(bidi.nextKey(null));
        }

        resetFull();
        bidi = (OrderedBidiMap<K, V>) map;
        final Iterator<K> it = confirmed.keySet().iterator();
        K confirmedLast = it.next();
        while (it.hasNext()) {
            final K confirmedObject = it.next();
            assertEquals(confirmedObject, bidi.nextKey(confirmedLast));
            confirmedLast = confirmedObject;
        }
        assertNull(bidi.nextKey(confirmedLast));

        if (!isAllowNullKey()) {
            final OrderedBidiMap<K, V> finalBidi = bidi;
            assertThrows(NullPointerException.class, () -> finalBidi.nextKey(null));

        } else {
            assertNull(bidi.nextKey(null));
        }
    }

    @Test
    public void testPreviousKey() {
        resetEmpty();
        OrderedBidiMap<K, V> bidi = getMap();
        assertNull(bidi.previousKey(getOtherKeys()[0]));
        if (!isAllowNullKey()) {
            try {
                assertNull(bidi.previousKey(null)); // this is allowed too
            } catch (final NullPointerException ex) {}
        } else {
            assertNull(bidi.previousKey(null));
        }

        resetFull();
        bidi = getMap();
        final List<K> list = new ArrayList<>(confirmed.keySet());
        Collections.reverse(list);
        final Iterator<K> it = list.iterator();
        K confirmedLast = it.next();
        while (it.hasNext()) {
            final K confirmedObject = it.next();
            assertEquals(confirmedObject, bidi.previousKey(confirmedLast));
            confirmedLast = confirmedObject;
        }
        assertNull(bidi.previousKey(confirmedLast));

        if (!isAllowNullKey()) {
            final OrderedBidiMap<K, V> finalBidi = bidi;
            assertThrows(NullPointerException.class, () -> finalBidi.previousKey(null));

        } else {
            assertNull(bidi.previousKey(null));
        }
    }

    @Test
    public void testMapIteratorDirectional() {
        resetEmpty();
        OrderedMapIterator<K, V> mapIterator1 = getMap().mapIterator();
        assertFalse(mapIterator1.hasNext());
        assertFalse(mapIterator1.hasPrevious());
        assertThrows(NoSuchElementException.class, mapIterator1::next);
        assertThrows(NoSuchElementException.class, mapIterator1::previous);

        resetFull();
        Object[] keys = getMap().keySet().toArray(new Object[0]);
        K key;
        OrderedMapIterator<K, V> mapIterator2 = getMap().mapIterator();
        // initial state before first element
        assertTrue(mapIterator2.hasNext());
        assertFalse(mapIterator2.hasPrevious());
        assertThrows(NoSuchElementException.class, mapIterator2::previous);
        // first element
        key = mapIterator2.next();
        assertEquals(key, mapIterator2.getKey());
        assertEquals(keys[0], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        // middle elements
        for (int i = 1; i < keys.length - 1; ++i) {
            key = mapIterator2.next();
            assertEquals(key, mapIterator2.getKey());
            assertEquals(keys[i], key);
            assertTrue(mapIterator2.hasNext());
            assertTrue(mapIterator2.hasPrevious());
        }
        // final element
        key = mapIterator2.next();
        assertEquals(keys[keys.length - 1], key);
        assertFalse(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        assertThrows(NoSuchElementException.class, mapIterator2::next);
        // iterate backwards
        for (int i = keys.length - 1; i > 0; --i) {
            key = mapIterator2.previous();
            assertEquals(key, mapIterator2.getKey());
            assertEquals(keys[i], key);
            assertTrue(mapIterator2.hasNext());
            assertTrue(mapIterator2.hasPrevious());
        }
        // first element
        key = mapIterator2.previous();
        assertEquals(key, mapIterator2.getKey());
        assertEquals(keys[0], key);
        assertTrue(mapIterator2.hasNext());
        assertFalse(mapIterator2.hasPrevious());
        assertThrows(NoSuchElementException.class, mapIterator2::previous);
        // switching direction
        key = mapIterator2.next();
        assertEquals(key, mapIterator2.getKey());
        assertEquals(keys[0], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        key = mapIterator2.next();
        assertEquals(key, mapIterator2.getKey());
        assertEquals(keys[1], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        key = mapIterator2.next();
        assertEquals(key, mapIterator2.getKey());
        assertEquals(keys[2], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        key = mapIterator2.previous();
        assertEquals(keys[2], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        key = mapIterator2.next();
        assertEquals(keys[2], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
        key = mapIterator2.next();
        assertEquals(keys[3], key);
        assertTrue(mapIterator2.hasNext());
        assertTrue(mapIterator2.hasPrevious());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedBidiMap<K, V> getMap() {
        return (OrderedBidiMap<K, V>) super.getMap();
    }

    @Nested
    public class TestBidiOrderedMapIterator extends AbstractMapIteratorTest<K, V> {

        public TestBidiOrderedMapIterator() {
            super("TestBidiOrderedMapIterator");
        }

        @Override
        public V[] addSetValues() {
            return AbstractOrderedBidiMapTest.this.getNewSampleValues();
        }

        @Override
        public boolean supportsRemove() {
            return AbstractOrderedBidiMapTest.this.isRemoveSupported();
        }

        @Override
        public boolean supportsSetValue() {
            return AbstractOrderedBidiMapTest.this.isSetValueSupported();
        }

        @Override
        public MapIterator<K, V> makeEmptyIterator() {
            resetEmpty();
            return AbstractOrderedBidiMapTest.this.getMap().mapIterator();
        }

        @Override
        public MapIterator<K, V> makeObject() {
            resetFull();
            return AbstractOrderedBidiMapTest.this.getMap().mapIterator();
        }

        @Override
        public Map<K, V> getMap() {
            // assumes makeFullMapIterator() called first
            return AbstractOrderedBidiMapTest.this.map;
        }

        @Override
        public Map<K, V> getConfirmedMap() {
            // assumes makeFullMapIterator() called first
            return AbstractOrderedBidiMapTest.this.confirmed;
        }

        @Override
        public void verify() {
            super.verify();
            AbstractOrderedBidiMapTest.this.verify();
        }

    }

}
