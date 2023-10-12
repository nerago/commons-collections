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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.MapIterator;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests.
 */
public class Flat3MapTest<K, V> extends AbstractIterableMapTest<K, V> {

    private static final Integer ONE = Integer.valueOf(1);
    private static final Integer TWO = Integer.valueOf(2);
    private static final Integer THREE = Integer.valueOf(3);
    private static final String TEN = "10";
    private static final String TWENTY = "20";
    private static final String THIRTY = "30";

    @Override
    public Flat3Map<K, V> makeObject() {
        return new Flat3Map<>();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEquals1() {
        final Flat3Map<K, V> map1 = makeObject();
        map1.put((K) "a", (V) "testA");
        map1.put((K) "b", (V) "testB");
        final Flat3Map<K, V> map2 = makeObject();
        map2.put((K) "a", (V) "testB");
        map2.put((K) "b", (V) "testA");
        assertFalse(map1.equals(map2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEquals2() {
        final Flat3Map<K, V> map1 = makeObject();
        map1.put((K) "a", (V) "testA");
        map1.put((K) "b", (V) "testB");
        final Flat3Map<K, V> map2 = makeObject();
        map2.put((K) "a", (V) "testB");
        map2.put((K) "c", (V) "testA");
        assertFalse(map1.equals(map2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testClone2() {
        final Flat3Map<K, V> map = makeObject();
        assertEquals(0, map.size());
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        assertEquals(2, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertSame(TEN, map.get(ONE));
        assertSame(TWENTY, map.get(TWO));

        // clone works (size = 2)
        final Flat3Map<K, V> cloned = map.clone();
        assertEquals(2, cloned.size());
        assertTrue(cloned.containsKey(ONE));
        assertTrue(cloned.containsKey(TWO));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));

        // change original doesn't change clone
        map.put((K) TEN, (V) ONE);
        map.put((K) TWENTY, (V) TWO);
        assertEquals(4, map.size());
        assertEquals(2, cloned.size());
        assertTrue(cloned.containsKey(ONE));
        assertTrue(cloned.containsKey(TWO));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testClone4() {
        final Flat3Map<K, V> map = makeObject();
        assertEquals(0, map.size());
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) TEN, (V) ONE);
        map.put((K) TWENTY, (V) TWO);

        // clone works (size = 4)
        final Flat3Map<K, V> cloned = map.clone();
        assertEquals(4, map.size());
        assertEquals(4, cloned.size());
        assertTrue(cloned.containsKey(ONE));
        assertTrue(cloned.containsKey(TWO));
        assertTrue(cloned.containsKey(TEN));
        assertTrue(cloned.containsKey(TWENTY));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
        assertSame(ONE, cloned.get(TEN));
        assertSame(TWO, cloned.get(TWENTY));

        // change original doesn't change clone
        map.clear();
        assertEquals(0, map.size());
        assertEquals(4, cloned.size());
        assertTrue(cloned.containsKey(ONE));
        assertTrue(cloned.containsKey(TWO));
        assertTrue(cloned.containsKey(TEN));
        assertTrue(cloned.containsKey(TWENTY));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
        assertSame(ONE, cloned.get(TEN));
        assertSame(TWO, cloned.get(TWENTY));
    }

    @Test
    public void testSerialisation0() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        final byte[] bytes = bout.toByteArray();
        out.close();
        final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        final ObjectInputStream in = new ObjectInputStream(bin);
        final Flat3Map<?, ?> ser = (Flat3Map<?, ?>) in.readObject();
        in.close();
        assertEquals(0, map.size());
        assertEquals(0, ser.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialisation2() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        final byte[] bytes = bout.toByteArray();
        out.close();
        final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        final ObjectInputStream in = new ObjectInputStream(bin);
        final Flat3Map<?, ?> ser = (Flat3Map<?, ?>) in.readObject();
        in.close();
        assertEquals(2, map.size());
        assertEquals(2, ser.size());
        assertTrue(ser.containsKey(ONE));
        assertTrue(ser.containsKey(TWO));
        assertEquals(TEN, ser.get(ONE));
        assertEquals(TWENTY, ser.get(TWO));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialisation4() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) TEN, (V) ONE);
        map.put((K) TWENTY, (V) TWO);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        final byte[] bytes = bout.toByteArray();
        out.close();
        final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        final ObjectInputStream in = new ObjectInputStream(bin);
        final Flat3Map<?, ?> ser = (Flat3Map<?, ?>) in.readObject();
        in.close();
        assertEquals(4, map.size());
        assertEquals(4, ser.size());
        assertTrue(ser.containsKey(ONE));
        assertTrue(ser.containsKey(TWO));
        assertTrue(ser.containsKey(TEN));
        assertTrue(ser.containsKey(TWENTY));
        assertEquals(TEN, ser.get(ONE));
        assertEquals(TWENTY, ser.get(TWO));
        assertEquals(ONE, ser.get(TEN));
        assertEquals(TWO, ser.get(TWENTY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEntryIteratorSetValue1() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        final Map.Entry<K, V> entry = it.next();
        entry.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals("NewValue", map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEntryIteratorSetValue2() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        it.next();
        final Map.Entry<K, V> entry = it.next();
        entry.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals("NewValue", map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEntryIteratorSetValue3() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        it.next();
        it.next();
        final Map.Entry<K, V> entry = it.next();
        entry.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals("NewValue", map.get(THREE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapIteratorSetValue1() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final MapIterator<K, V> it = map.mapIterator();
        it.next();
        it.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals("NewValue", map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapIteratorSetValue2() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final MapIterator<K, V> it = map.mapIterator();
        it.next();
        it.next();
        it.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals("NewValue", map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapIteratorSetValue3() throws Exception {
        final Flat3Map<K, V> map = makeObject();
        map.put((K) ONE, (V) TEN);
        map.put((K) TWO, (V) TWENTY);
        map.put((K) THREE, (V) THIRTY);

        final MapIterator<K, V> it = map.mapIterator();
        it.next();
        it.next();
        it.next();
        it.setValue((V) "NewValue");
        assertEquals(3, map.size());
        assertTrue(map.containsKey(ONE));
        assertTrue(map.containsKey(TWO));
        assertTrue(map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals("NewValue", map.get(THREE));
    }

    @Test
    public void testEntrySet() {
        // Sanity check
        putAndRemove(new LinkedHashMap<>());
        // Actual test
        putAndRemove(new Flat3Map<>());
    }

    private void putAndRemove(final Map<K, V> map) {
        map.put((K) "A", (V) "one");
        map.put((K) "B", (V) "two");
        map.put((K) "C", (V) "three");
        final Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();

        final Map.Entry<K, V> mapEntry1 = it.next();
        final Map.Entry<K, V> mapEntry2 = it.next();
        final Map.Entry<K, V> mapEntry3 = it.next();
        it.remove();
        assertEquals(2, map.size());
        assertEquals("one", map.get("A"));
        assertEquals("two", map.get("B"));
        assertNull(map.get("C"));
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/Flat3Map.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/Flat3Map.fullCollection.version4.obj");
//    }

    @Test
    public void testCollections261() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        m.put( Integer.valueOf(1), Integer.valueOf(1) );
        m.put( Integer.valueOf(0), Integer.valueOf(0) );
        assertEquals( Integer.valueOf(1), m.remove( Integer.valueOf(1) ) );
        assertEquals( Integer.valueOf(0), m.remove( Integer.valueOf(0) ) );

        m.put( Integer.valueOf(2), Integer.valueOf(2) );
        m.put( Integer.valueOf(1), Integer.valueOf(1) );
        m.put( Integer.valueOf(0), Integer.valueOf(0) );
        assertEquals( Integer.valueOf(2), m.remove( Integer.valueOf(2) ) );
        assertEquals( Integer.valueOf(1), m.remove( Integer.valueOf(1) ) );
        assertEquals( Integer.valueOf(0), m.remove( Integer.valueOf(0) ) );
    }

    @Test
    public void testToString() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final String string0 = m.toString();
        assertNotNull(string0);
        m.put( Integer.valueOf(1), Integer.valueOf(1) );
        final String string1 = m.toString();
        assertNotNull(string1);
        assertNotSame(string0, string1);
        m.put( Integer.valueOf(0), Integer.valueOf(0) );
        final String string2 = m.toString();
        assertNotNull(string2);
        assertNotSame(string0, string2);
        assertNotSame(string1, string2);
        m.put( Integer.valueOf(2), Integer.valueOf(2) );
        final String string3 = m.toString();
        assertNotNull(string3);
        assertNotSame(string0, string3);
        assertNotSame(string1, string3);
        assertNotSame(string2, string3);
    }

    @Test
    public void testRemove1() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        // object is not existing
        Object obj = m.remove(44);
        assertNull(obj);

        m.put(ONE, ONE);
        obj = m.remove(ONE);
        assertSame(ONE, obj);
        assertEquals(0, m.size());

        // after removal, be no longer there
        obj = m.get(ONE);
        assertNull(obj);

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(ONE);
        assertSame(ONE, obj);

        obj = m.get(ONE);
        assertNull(obj);
        obj = m.get(TWO);
        assertSame(TWO, obj);
    }

    @Test
    public void testRemove2() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(ONE);
        assertSame(ONE, obj);

        obj = m.get(ONE);
        assertNull(obj);
        obj = m.get(TWO);
        assertSame(TWO, obj);
        obj = m.get(THREE);
        assertSame(THREE, obj);
    }

    @Test
    public void testRemove3() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(TWO);
        assertSame(TWO, obj);

        obj = m.get(ONE);
        assertSame(ONE, obj);
        obj = m.get(TWO);
        assertNull(obj);
        obj = m.get(THREE);
        assertSame(THREE, obj);
    }

    @Test
    public void testRemove4() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(THREE);
        assertSame(THREE, obj);

        obj = m.get(ONE);
        assertSame(ONE, obj);
        obj = m.get(TWO);
        assertSame(TWO, obj);
        obj = m.get(THREE);
        assertNull(obj);
    }

    @Test
    public void testRemove5() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(null, ONE);

        obj = m.remove(null);
        assertSame(ONE, obj);

        obj = m.get(null);
        assertNull(obj);
    }

    @Test
    public void testRemove6() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(ONE, ONE);
        m.put(null, TWO);

        obj = m.remove(null);
        assertSame(TWO, obj);

        obj = m.get(ONE);
        assertSame(ONE, obj);
        obj = m.get(null);
        assertNull(obj);
    }

    @Test
    public void testRemove7() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(null, ONE);
        m.put(TWO, TWO);

        obj = m.remove(null);
        assertSame(ONE, obj);

        obj = m.get(null);
        assertNull(obj);
        obj = m.get(TWO);
        assertSame(TWO, obj);
    }

    @Test
    public void testRemove8() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(null, THREE);

        obj = m.remove(null);
        assertSame(THREE, obj);

        obj = m.get(ONE);
        assertSame(ONE, obj);
        obj = m.get(TWO);
        assertSame(TWO, obj);
        obj = m.get(null);
        assertNull(obj);
    }

    @Test
    public void testRemove9() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);

        obj = m.remove(null);
        assertNull(obj);
    }

    @Test
    public void testRemove10() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);

        obj = m.remove(null);
        assertNull(obj);
    }

    @Test
    public void testRemove11() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(null);
        assertNull(obj);
    }

    @Test
    public void testRemove12() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);

        obj = m.remove(42);
        assertNull(obj);
    }

    @Test
    public void testRemove13() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);

        obj = m.remove(42);
        assertNull(obj);
    }

    @Test
    public void testNewInstance1() {
        final Map<Integer, Integer> orig = new HashMap<>();
        orig.put(ONE, ONE);
        orig.put(TWO, TWO);

        final Flat3Map<Integer, Integer> m = new Flat3Map<>(orig);

        assertEquals(orig, m);
        assertEquals(2, m.size());
    }

    @Test
    public void testGet1() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(null, ONE);
        obj = m.get(null);
        assertSame(ONE, obj);
    }

    @Test
    public void testGet2() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(null, TWO);
        obj = m.get(null);
        assertSame(TWO, obj);
    }

    @Test
    public void testGet3() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();
        final Object obj;

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(null, THREE);
        obj = m.get(null);
        assertSame(THREE, obj);
    }

    @Test
    public void testContainsKey1() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(null, THREE);
        final boolean contains = m.containsKey(null);
        assertTrue(contains);
    }

    @Test
    public void testContainsKey2() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(null, TWO);
        final boolean contains = m.containsKey(null);
        assertTrue(contains);
    }

    @Test
    public void testContainsKey3() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(null, ONE);
        final boolean contains = m.containsKey(null);
        assertTrue(contains);
    }

    @Test
    public void testContainsValue1() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, null);
        final boolean contains = m.containsValue(null);
        assertTrue(contains);
    }

    @Test
    public void testContainsValue2() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, null);
        final boolean contains = m.containsValue(null);
        assertTrue(contains);
    }

    @Test
    public void testContainsValue3() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, null);
        final boolean contains = m.containsValue(null);
        assertTrue(contains);
    }

    @Test
    public void testPut1() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(null, THREE);
        final Object old = m.put(null, ONE);
        assertEquals(THREE, old);
        assertEquals(ONE, m.get(null));
    }

    @Test
    public void testPut2() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(null, THREE);
        final Object old = m.put(null, ONE);
        assertEquals(THREE, old);
        assertEquals(ONE, m.get(null));
    }

    @Test
    public void testPut3() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(null, THREE);
        final Object old = m.put(null, ONE);
        assertEquals(THREE, old);
        assertNull(m.get(ONE));
    }

    @Test
    public void testPut4() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, TWO);
        m.put(THREE, THREE);
        final Object old = m.put(THREE, ONE);
        assertEquals(THREE, old);
        assertEquals(ONE, m.get(THREE));
    }

    @Test
    public void testPut5() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, ONE);
        m.put(TWO, THREE);
        final Object old = m.put(TWO, ONE);
        assertEquals(THREE, old);
        assertEquals(ONE, m.get(TWO));
    }

    @Test
    public void testPut6() {
        final Flat3Map<Integer, Integer> m = new Flat3Map<>();

        m.put(ONE, THREE);
        final Object old = m.put(ONE, ONE);
        assertEquals(THREE, old);
        assertEquals(ONE, m.get(ONE));
    }
}
