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

import org.apache.commons.collections4.set.AbstractSetTest;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractNestedMapEntrySetTest<K, V> extends AbstractSetTest<Map.Entry<K, V>> {
    
    public abstract AbstractMapTest<K, V> outerTest();
    
    public abstract AbstractMapTest<K, V>.MapTest mapTest();
    
    public Map<K, V> getMap() {
        return mapTest().getMap();
    }

    public Map<K, V> getConfirmedMap() {
        return mapTest().getConfirmed();
    }
    
    // Have to implement manually; entrySet doesn't support addAll
    @Override
    public Map.Entry<K, V>[] getFullElements() {
        return getFullNonNullElements();
    }

    @Override
    public Map.Entry<K, V>[] getFullNonNullElements() {
        final K[] k = outerTest().getSampleKeys();
        final V[] v = outerTest().getSampleValues();
        return outerTest().makeEntryArray(k, v);
    }

    // Have to implement manually; entrySet doesn't support addAll
    @Override
    public Map.Entry<K, V>[] getOtherElements() {
        final K[] k = outerTest().getOtherKeys();
        final V[] v = outerTest().getOtherValues();
        return outerTest().makeEntryArray(k, v);
    }

    @Override
    public Set<Map.Entry<K, V>> makeObject() {
        return outerTest().makeObject().entrySet();
    }

    @Override
    public Set<Map.Entry<K, V>> makeFullCollection() {
        return outerTest().makeFullMap().entrySet();
    }

    @Override
    public boolean isAddSupported() {
        // Collection views don't support add operations.
        return false;
    }

    @Override
    public boolean isRemoveSupported() {
        // Entry set should only support remove if map does
        return outerTest().isRemoveSupported();
    }

    protected boolean isGetStructuralModify() {
        return outerTest().isGetStructuralModify();
    }

    @Override
    public boolean areEqualElementsDistinguishable() {
        return outerTest().areEqualElementsDistinguishable();
    }

    @Override
    public boolean isTestSerialization() {
        return false;
    }

    @Override
    public void resetFull() {
        mapTest().resetFull();
        setCollection(getMap().entrySet());
        setConfirmed(getConfirmedMap().entrySet());
    }

    @Override
    public void resetEmpty() {
        mapTest().resetEmpty();
        setCollection(getMap().entrySet());
        setConfirmed(getConfirmedMap().entrySet());
    }

    @Override
    protected int getIterationBehaviour() {
        return outerTest().getIterationBehaviour();
    }

    @Test
    public void testMapEntrySetIteratorEntry() {
        resetFull();
        int count = 0;
        for (final Map.Entry<K, V> entry : getCollection()) {
            assertTrue(getMap().containsKey(entry.getKey()));
            assertTrue(getMap().containsValue(entry.getValue()));
            if (!isGetStructuralModify()) {
                assertEquals(getMap().get(entry.getKey()), entry.getValue());
            }
            count++;
        }
        assertEquals(getCollection().size(), count);
    }

    @Test
    public void testMapEntrySetIteratorEntrySetValue() {
        final K key1 = outerTest().getSampleKeys()[0];
        final K key2 = outerTest().getSampleKeys().length == 1 ? outerTest().getSampleKeys()[0] : outerTest().getSampleKeys()[1];
        final V newValue1 = outerTest().getNewSampleValues()[0];
        final V newValue2 = outerTest().getNewSampleValues().length == 1 ? outerTest().getNewSampleValues()[0] : outerTest().getNewSampleValues()[1];

        resetFull();
        // explicitly get entries as sample values/keys are connected for some maps
        // such as BeanMap
        Iterator<Map.Entry<K, V>> it = getCollection().iterator();
        final Map.Entry<K, V> entry1 = getEntry(it, key1);
        it = this.getCollection().iterator();
        final Map.Entry<K, V> entry2 = getEntry(it, key2);
        Iterator<Map.Entry<K, V>> itConfirmed = getConfirmed().iterator();
        final Map.Entry<K, V> entryConfirmed1 = getEntry(itConfirmed, key1);
        itConfirmed = getConfirmed().iterator();
        final Map.Entry<K, V> entryConfirmed2 = getEntry(itConfirmed, key2);
        verify();

        if (!outerTest().isSetValueSupported()) {
            assertThrows(UnsupportedOperationException.class, () -> entry1.setValue(newValue1));
            return;
        }

        entry1.setValue(newValue1);
        entryConfirmed1.setValue(newValue1);
        assertEquals(newValue1, entry1.getValue());
        assertTrue(getMap().containsKey(entry1.getKey()));
        assertTrue(getMap().containsValue(newValue1));
        assertEquals(newValue1, getMap().get(entry1.getKey()));
        verify();

        entry1.setValue(newValue1);
        entryConfirmed1.setValue(newValue1);
        assertEquals(newValue1, entry1.getValue());
        assertTrue(getMap().containsKey(entry1.getKey()));
        assertTrue(getMap().containsValue(newValue1));
        assertEquals(newValue1, getMap().get(entry1.getKey()));
        verify();

        entry2.setValue(newValue2);
        entryConfirmed2.setValue(newValue2);
        assertEquals(newValue2, entry2.getValue());
        assertTrue(getMap().containsKey(entry2.getKey()));
        assertTrue(getMap().containsValue(newValue2));
        assertEquals(newValue2, getMap().get(entry2.getKey()));
        verify();
    }

    @Test
    public void testMapEntrySetIteratorEntrySetValueClonedKeysValues() throws Exception {
        K key1 = outerTest().getSampleKeys()[0];
        V newValue1 = outerTest().getNewSampleValues()[0];
        V newValue2 = outerTest().getNewSampleValues().length == 1 ? outerTest().getNewSampleValues()[0] : outerTest().getNewSampleValues()[1];

        resetFull();
        final Map<K, V> map = getMap();
        final Map<K, V> confirmed = getConfirmedMap();
        final Iterator<Map.Entry<K, V>> it = getCollection().iterator();
        final Map.Entry<K, V> entry1 = getEntry(it, key1);
        final Iterator<Map.Entry<K, V>> itConfirmed = getConfirmed().iterator();
        final Map.Entry<K, V> entryConfirmed1 = getEntry(itConfirmed, key1);

        if (outerTest().isSetValueSupported()) {
            // set new value using put
            key1 = cloneObject(key1);
            newValue1 = cloneObject(newValue1);
            map.put(key1, newValue1);
            confirmed.put(key1, newValue1);
            assertSame(newValue1, map.get(key1));
            verify();

            // set same value using setValue, should be noop
            newValue1 = cloneObject(newValue1);
            entry1.setValue(newValue1);
            entryConfirmed1.setValue(newValue1);
            assertSame(newValue1, map.get(key1));
            verify();

            // set another new value using put
            key1 = cloneObject(key1);
            newValue2 = cloneObject(newValue2);
            map.put(key1, newValue2);
            confirmed.put(key1, newValue2);
            assertSame(newValue2, map.get(key1));
            verify();

            // set back to first value using setValue
            newValue1 = cloneObject(newValue1);
            entry1.setValue(newValue1);
            entryConfirmed1.setValue(newValue1);
            assertSame(newValue1, map.get(key1));
            verify();
        } else {
            assertThrows(UnsupportedOperationException.class, () -> entry1.setValue(outerTest().getNewSampleValues()[0]));
        }
    }

    protected Map.Entry<K, V> getEntry(final Iterator<Map.Entry<K, V>> itConfirmed, final K key) {
        Map.Entry<K, V> entry = null;
        while (itConfirmed.hasNext()) {
            final Map.Entry<K, V> temp = itConfirmed.next();
            if (temp.getKey() == null) {
                if (key == null) {
                    entry = temp;
                    break;
                }
            } else if (temp.getKey().equals(key)) {
                entry = temp;
                break;
            }
        }
        assertNotNull(entry, "No matching entry in map for key '" + key + "'");
        return entry;
    }

    @Test
    public void testMapEntrySetRemoveNonMapEntry() {
        if (!isRemoveSupported()) {
            return;
        }
        resetFull();
        assertFalse(getCollection().remove(null));
        assertFalse(getCollection().remove(new Object()));
    }

    @Override
    public void verify() {
        super.verify();
        mapTest().verify();
    }
}
