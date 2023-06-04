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

import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.collections4.comparators.ReverseComparator;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests.
 */
@SuppressWarnings("boxing")
public class DualTreeBidi2MapComparatorTest<K extends Comparable<K>, V extends Comparable<V>> extends AbstractSortedBidiMapTest<K, V> {

    public DualTreeBidi2MapComparatorTest() {
        super(DualTreeBidi2MapComparatorTest.class.getSimpleName());
    }

    @Override
    public DualTreeBidi2Map<K, V> makeObject() {
        return new DualTreeBidi2Map<>(
                new ReverseComparator<>(ComparableComparator.<K>comparableComparator()),
                new ReverseComparator<>(ComparableComparator.<V>comparableComparator()));
    }

    @Override
    public TreeMap<K, V> makeConfirmedMap() {
        return new TreeMap<>(new ReverseComparator<>(ComparableComparator.<K>comparableComparator()));
    }

    @Test
    public void testComparator() {
        resetEmpty();
        final SortedBidiMap<K, V> bidi = (SortedBidiMap<K, V>) map;
        assertNotNull(bidi.comparator());
        assertTrue(bidi.comparator() instanceof ReverseComparator);
    }

    @Test
    public void testComparator2() {
        final DualTreeBidi2Map<String, Integer> dtbm = new DualTreeBidi2Map<>(
                String.CASE_INSENSITIVE_ORDER, null);
        dtbm.put("two", 0);
        dtbm.put("one", 1);
        assertEquals("one", dtbm.firstKey());
        assertEquals("two", dtbm.lastKey());

    }

    @Test
    public void testSerializeDeserializeCheckComparator() throws Exception {
        final SortedBidiMap<?, ?> obj = makeObject();
        if (obj instanceof Serializable && isTestSerialization()) {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(obj);
            out.close();

            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            final Object dest = in.readObject();
            in.close();

            final SortedBidiMap<?, ?> bidi = (SortedBidiMap<?, ?>) dest;
            assertNotNull(obj.comparator());
            assertNotNull(bidi.comparator());
            assertTrue(bidi.comparator() instanceof ReverseComparator);
        }
    }

    private static class IntegerComparator implements Comparator<Integer>, Serializable{
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(final Integer o1, final Integer o2) {
            return o1.compareTo(o2);
        }
    }

    @Test
    public void testCollections364() throws Exception {
        final DualTreeBidi2Map<String, Integer> original = new DualTreeBidi2Map<>(
                String.CASE_INSENSITIVE_ORDER, new IntegerComparator());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        @SuppressWarnings("unchecked")
        final DualTreeBidi2Map<String, Integer> deserialized = (DualTreeBidi2Map<String, Integer>) in.readObject();
        in.close();

        assertNotNull(original.comparator());
        assertNotNull(deserialized.comparator());
        assertEquals(original.comparator().getClass(), deserialized.comparator().getClass());
        assertEquals(original.valueComparator().getClass(), deserialized.valueComparator().getClass());
    }

    @Test
    public void testSortOrder() throws Exception {
        final SortedBidiMap<K, V> sm = makeFullMap();

        // Sort by the comparator used in the makeEmptyBidiMap() method
        List<K> newSortedKeys = getAsList(getSampleKeys());
        newSortedKeys.sort(new ReverseComparator<>(ComparableComparator.<K>comparableComparator()));
        newSortedKeys = Collections.unmodifiableList(newSortedKeys);

        final Iterator<K> mapIter = sm.keySet().iterator();
        for (final K expectedKey : newSortedKeys) {
            final K mapKey = mapIter.next();
            assertNotNull(expectedKey, "key in sorted list may not be null");
            assertNotNull(mapKey, "key in map may not be null");
            assertEquals(expectedKey, mapKey, "key from sorted list and map must be equal");
        }
    }

    @Override
    public String getCompatibilityVersion() {
        return "4.Test2";
    }

    /**
     * Override to prevent infinite recursion of tests.
     */
    @Override
    public String[] ignoredTests() {
        final String recursiveTest = "DualTreeBidiMap2Test.bulkTestInverseMap.bulkTestInverseMap";
        return new String[] { recursiveTest };
    }


//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/DualTreeBidiMap.emptyCollection.version4.Test2.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/DualTreeBidiMap.fullCollection.version4.Test2.obj");
//    }
}
