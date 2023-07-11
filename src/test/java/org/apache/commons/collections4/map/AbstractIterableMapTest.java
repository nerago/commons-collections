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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.apache.commons.collections4.iterators.AbstractMapIteratorTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract test class for {@link IterableMap} methods and contracts.
 */
public abstract class AbstractIterableMapTest<K, V> extends AbstractMapTest<K, V> {

    /**
     * JUnit constructor.
     *
     * @param testName  the test name
     */
    public AbstractIterableMapTest(final String testName) {
        super(testName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IterableMap<K, V> makeObject();

    /**
     * {@inheritDoc}
     */
    @Override
    public IterableMap<K, V> makeFullMap() {
        return (IterableMap<K, V>) super.makeFullMap();
    }

    @Test
    public void testFailFastEntrySet() {
        if (!isRemoveSupported()) {
            return;
        }
        if (!isFailFastExpected()) {
            return;
        }
        resetFull();
        Iterator<Map.Entry<K, V>> it = getMap().entrySet().iterator();
        final Map.Entry<K, V> val = it.next();
        getMap().remove(val.getKey());
        final Iterator<Map.Entry<K, V>> finalIt0 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt0.next());

        resetFull();
        it = getMap().entrySet().iterator();
        it.next();
        getMap().clear();
        final Iterator<Map.Entry<K, V>> finalIt1 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt1.next());

        resetFull();
        it = getMap().entrySet().iterator();
        it.next();
        getMap().put(getOtherKeys()[0], getOtherValues()[0]);
        final Iterator<Map.Entry<K, V>> finalIt2 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt2.next());
    }

    @Test
    public void testFailFastKeySet() {
        if (!isRemoveSupported()) {
            return;
        }
        if (!isFailFastExpected()) {
            return;
        }
        resetFull();
        Iterator<K> it = getMap().keySet().iterator();
        final K val = it.next();
        getMap().remove(val);
        final Iterator<K> finalIt0 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt0.next());

        resetFull();
        it = getMap().keySet().iterator();
        it.next();
        getMap().clear();
        final Iterator<K> finalIt1 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt1.next());

        resetFull();
        it = getMap().keySet().iterator();
        it.next();
        getMap().put(getOtherKeys()[0], getOtherValues()[0]);
        final Iterator<K> finalIt2 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt2.next());
    }

    @Test
    public void testFailFastValues() {
        if (!isRemoveSupported()) {
            return;
        }
        if (!isFailFastExpected()) {
            return;
        }
        resetFull();
        Iterator<V> it = getMap().values().iterator();
        it.next();
        getMap().remove(getMap().keySet().iterator().next());
        final Iterator<V> finalIt0 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt0.next());

        resetFull();
        it = getMap().values().iterator();
        it.next();
        getMap().clear();
        final Iterator<V> finalIt1 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt1.next());

        resetFull();
        it = getMap().values().iterator();
        it.next();
        getMap().put(getOtherKeys()[0], getOtherValues()[0]);
        final Iterator<V> finalIt2 = it;
        assertThrows(ConcurrentModificationException.class, () -> finalIt2.next());
    }

    @Test
    public void testCompareMapIterators() {
        if (getIterationBehaviour() == IterationBehaviour.UNORDERED)
            return;

        resetEmpty();
        MapIterator<K, V> mapIterator = getMap().mapIterator();
        Iterator<K> keyIterator = getMap().keySet().iterator();
        Spliterator<K> keySpliterator = getMap().keySet().spliterator();
        Iterator<Map.Entry<K, V>> entryIterator = getMap().entrySet().iterator();
        Spliterator<Map.Entry<K, V>> entrySpliterator = getMap().entrySet().spliterator();
        Iterator<V> valueIterator = getMap().values().iterator();
        Spliterator<V> valueSpliterator = getMap().values().spliterator();
        assertFalse(mapIterator.hasNext());
        assertFalse(keyIterator.hasNext());
        assertFalse(entryIterator.hasNext());
        assertFalse(valueIterator.hasNext());
        assertFalse(entrySpliterator.tryAdvance(x -> {
        }));
        assertFalse(keySpliterator.tryAdvance(x -> {
        }));
        assertFalse(valueSpliterator.tryAdvance(x -> {
        }));

        resetFull();
        mapIterator = getMap().mapIterator();
        keyIterator = getMap().keySet().iterator();
        keySpliterator = getMap().keySet().spliterator();
        entryIterator = getMap().entrySet().iterator();
        entrySpliterator = getMap().entrySet().spliterator();
        valueIterator = getMap().values().iterator();
        valueSpliterator = getMap().values().spliterator();

        while (mapIterator.hasNext()) {
            assertTrue(keyIterator.hasNext());
            assertTrue(entryIterator.hasNext());
            assertTrue(valueIterator.hasNext());

            K key = mapIterator.next();
            V value = mapIterator.getValue();
            Map.Entry<K, V> entry = entryIterator.next();
            Map.Entry<K, V> entry2 = advanceSpliterator(entrySpliterator);
            assertEquals(key, mapIterator.getKey());
            assertEquals(key, keyIterator.next());
            assertEquals(key, entry.getKey());
            assertEquals(key, entry2.getKey());
            assertEquals(key, advanceSpliterator(keySpliterator));
            assertEquals(value, valueIterator.next());
            assertEquals(value, entry.getValue());
            assertEquals(value, entry2.getValue());
            assertEquals(value, advanceSpliterator(valueSpliterator));
        }
        assertFalse(keyIterator.hasNext());
        assertFalse(entryIterator.hasNext());
        assertFalse(valueIterator.hasNext());
        cannotAdvanceSpliterator(entrySpliterator);
        cannotAdvanceSpliterator(keySpliterator);
        cannotAdvanceSpliterator(valueSpliterator);
    }

    @SuppressWarnings("unchecked")
    private <T> T advanceSpliterator(Spliterator<T> spliterator) {
        final Object[] result = new Object[1];
        assertTrue(spliterator.tryAdvance(x -> result[0] = x));
        return (T) result[0];
    }

    private <T> void cannotAdvanceSpliterator(Spliterator<T> spliterator) {
        final Object[] result = new Object[1];
        assertFalse(spliterator.tryAdvance(x -> result[0] = x));
        assertNull(result[0]);
    }

    @Nested
    public class InnerTestMapIterator extends AbstractMapIteratorTest<K, V> {
        public InnerTestMapIterator() {
            super("InnerTestMapIterator");
        }

        @Override
        public V[] addSetValues() {
            return AbstractIterableMapTest.this.getNewSampleValues();
        }

        @Override
        public boolean supportsRemove() {
            return AbstractIterableMapTest.this.isRemoveSupported();
        }

        @Override
        public boolean isGetStructuralModify() {
            return AbstractIterableMapTest.this.isGetStructuralModify();
        }

        @Override
        public boolean supportsSetValue() {
            return AbstractIterableMapTest.this.isSetValueSupported();
        }

        @Override
        public MapIterator<K, V> makeEmptyIterator() {
            resetEmpty();
            return AbstractIterableMapTest.this.getMap().mapIterator();
        }

        @Override
        public MapIterator<K, V> makeObject() {
            resetFull();
            return AbstractIterableMapTest.this.getMap().mapIterator();
        }

        @Override
        public Map<K, V> getMap() {
            // assumes makeFullMapIterator() called first
            return AbstractIterableMapTest.this.getMap();
        }

        @Override
        public Map<K, V> getConfirmedMap() {
            // assumes makeFullMapIterator() called first
            return AbstractIterableMapTest.this.getConfirmed();
        }

        @Override
        public void verify() {
            super.verify();
            AbstractIterableMapTest.this.verify();
        }
    }

//  public void testCreate() throws Exception {
//      resetEmpty();
//      writeExternalFormToDisk((Serializable) map, "D:/dev/collections/data/test/HashedMap.emptyCollection.version3.obj");
//      resetFull();
//      writeExternalFormToDisk((Serializable) map, "D:/dev/collections/data/test/HashedMap.fullCollection.version3.obj");
//  }

    /**
     * {@inheritDoc}
     */
    @Override
    public IterableMap<K, V> getMap() {
        return (IterableMap<K, V>) super.getMap();
    }

}
