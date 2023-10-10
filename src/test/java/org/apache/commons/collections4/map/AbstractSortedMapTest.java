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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.collections4.BulkTest;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Abstract test class for {@link java.util.SortedMap} methods and contracts.
 */
public abstract class AbstractSortedMapTest<K, V> extends AbstractMapTest<K, V> {

    /**
     * Can't sort null keys.
     *
     * @return false
     */
    @Override
    public boolean isAllowNullKey() {
        return false;
    }

    protected boolean runSubMapTests() {
        return getSampleKeys().length > TestHeadMap.SUBSIZE
                && getSampleKeys().length > TestTailMap.SUBSIZE
                && getSampleKeys().length > TestSubMap.SUBSIZE;
    }

    /**
     * SortedMap uses TreeMap as its known comparison.
     *
     * @return a map that is known to be valid
     */
    @Override
    public SortedMap<K, V> makeConfirmedMap() {
        return new TreeMap<>();
    }

    @Test
    public void testComparator() {
//        SortedMap<K, V> sm = makeFullMap();
        // no tests I can think of
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract SortedMap<K, V> makeObject();

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> makeFullMap() {
        return (SortedMap<K, V>) super.makeFullMap();
    }

    @Test
    public void testFirstKey() {
        final SortedMap<K, V> sm = makeFullMap();
        assertSame(sm.keySet().iterator().next(), sm.firstKey());
    }

    @Test
    public void testLastKey() {
        final SortedMap<K, V> sm = makeFullMap();
        K obj = null;
        for (final K k : sm.keySet()) {
            obj = k;
        }
        assertSame(obj, sm.lastKey());
    }

    @TestFactory
    public DynamicNode subMapTests() {
        if (runSubMapTests()) {
            return DynamicContainer.dynamicContainer("subMapTests", Arrays.asList(
                    new TestHeadMap<>(this).getDynamicTests(),
                    new TestTailMap<>(this).getDynamicTests(),
                    new TestSubMap<>(this).getDynamicTests()
            ));
        } else {
            return DynamicContainer.dynamicContainer("subMapTests", Stream.empty());
        }
    }

    public abstract static class TestViewMap<K, V> extends AbstractSortedMapTest<K, V> {
        protected final AbstractMapTest<K, V> main;
        protected final List<K> subSortedKeys = new ArrayList<>();
        protected final List<V> subSortedValues = new ArrayList<>();
        protected final List<V> subSortedNewValues = new ArrayList<>();

        public TestViewMap(final AbstractMapTest<K, V> main) {
            this.main = main;
        }
        @Override
        public void resetEmpty() {
            // needed to init verify correctly
            main.resetEmpty();
            super.resetEmpty();
        }
        @Override
        public void resetFull() {
            // needed to init verify correctly
            main.resetFull();
            super.resetFull();
        }
        @Override
        public void verify() {
            // cross verify changes on view with changes on main map
            super.verify();
            main.verify();
        }
        @Override
        protected boolean runSubMapTests() {
            return false;
        }
        @Override
        @SuppressWarnings("unchecked")
        public K[] getSampleKeys() {
            return (K[]) subSortedKeys.toArray();
        }
        @Override
        @SuppressWarnings("unchecked")
        public V[] getSampleValues() {
            return (V[]) subSortedValues.toArray();
        }
        @Override
        @SuppressWarnings("unchecked")
        public V[] getNewSampleValues() {
            return (V[]) subSortedNewValues.toArray();
        }

        @Override
        public boolean isAllowNullKey() {
            return main.isAllowNullKey();
        }
        @Override
        public boolean isAllowNullValue() {
            return main.isAllowNullValue();
        }
        @Override
        public boolean isPutAddSupported() {
            return main.isPutAddSupported();
        }
        @Override
        public boolean isPutChangeSupported() {
            return main.isPutChangeSupported();
        }
        @Override
        public boolean isRemoveSupported() {
            return main.isRemoveSupported();
        }
        @Override
        public boolean isTestSerialization() {
            return false;
        }
//        public void testSimpleSerialization() throws Exception {
//            if (main.isSubMapViewsSerializable() == false) return;
//            super.testSimpleSerialization();
//        }
//        public void testSerializeDeserializeThenCompare() throws Exception {
//            if (main.isSubMapViewsSerializable() == false) return;
//            super.testSerializeDeserializeThenCompare();
//        }
//        public void testEmptyMapCompatibility() throws Exception {
//            if (main.isSubMapViewsSerializable() == false) return;
//            super.testEmptyMapCompatibility();
//        }
//        public void testFullMapCompatibility() throws Exception {
//            if (main.isSubMapViewsSerializable() == false) return;
//            super.testFullMapCompatibility();
//        }
    }

    public static class TestHeadMap<K, V> extends TestViewMap<K, V> {
        static final int SUBSIZE = 6;
        final K toKey;

        public TestHeadMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                this.subSortedKeys.add(entry.getKey());
                this.subSortedValues.add(entry.getValue());
            }
            this.toKey = this.subSortedKeys.get(SUBSIZE);
            this.subSortedKeys.subList(SUBSIZE, this.subSortedKeys.size()).clear();
            this.subSortedValues.subList(SUBSIZE, this.subSortedValues.size()).clear();
            this.subSortedNewValues.addAll(Arrays.asList(main.getNewSampleValues()).subList(0, SUBSIZE));
        }
        @Override
        public SortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return ((SortedMap<K, V>) main.makeObject()).headMap(toKey);
        }
        @Override
        public SortedMap<K, V> makeFullMap() {
            return ((SortedMap<K, V>) main.makeFullMap()).headMap(toKey);
        }

        @Test
        public void testHeadMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(toKey, subSortedValues.get(0)));
            verify();
        }
        @Override
        public String getCompatibilityVersion() {
            return main.getCompatibilityVersion() + ".HeadMapView";
        }

//        public void testCreate() throws Exception {
//            Map map = makeEmptyMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/FixedSizeSortedMap.emptyCollection.version3.1.HeadMapView.obj");
//            map = makeFullMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/FixedSizeSortedMap.fullCollection.version3.1.HeadMapView.obj");
//        }
    }

    public static class TestTailMap<K, V> extends TestViewMap<K, V> {
        static final int SUBSIZE = 6;
        final K fromKey;
        final K invalidKey;

        public TestTailMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                this.subSortedKeys.add(entry.getKey());
                this.subSortedValues.add(entry.getValue());
            }
            this.fromKey = this.subSortedKeys.get(this.subSortedKeys.size() - SUBSIZE);
            this.invalidKey = this.subSortedKeys.get(this.subSortedKeys.size() - SUBSIZE - 1);
            this.subSortedKeys.subList(0, this.subSortedKeys.size() - SUBSIZE).clear();
            this.subSortedValues.subList(0, this.subSortedValues.size() - SUBSIZE).clear();
            this.subSortedNewValues.addAll(Arrays.asList(main.getNewSampleValues()).subList(0, SUBSIZE));
        }
        @Override
        public SortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return ((SortedMap<K, V>) main.makeObject()).tailMap(fromKey);
        }
        @Override
        public SortedMap<K, V> makeFullMap() {
            return ((SortedMap<K, V>) main.makeFullMap()).tailMap(fromKey);
        }

        @Test
        public void testTailMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(invalidKey, subSortedValues.get(0)));
            verify();
        }
        @Override
        public String getCompatibilityVersion() {
            return main.getCompatibilityVersion() + ".TailMapView";
        }

//        public void testCreate() throws Exception {
//            Map map = makeEmptyMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/FixedSizeSortedMap.emptyCollection.version3.1.TailMapView.obj");
//            map = makeFullMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/FixedSizeSortedMap.fullCollection.version3.1.TailMapView.obj");
//        }
    }

    public static class TestSubMap<K, V> extends TestViewMap<K, V> {
        static final int SUBSIZE = 3;
        final K fromKey;
        final K toKey;

        public TestSubMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                this.subSortedKeys.add(entry.getKey());
                this.subSortedValues.add(entry.getValue());
            }
            this.fromKey = this.subSortedKeys.get(SUBSIZE);
            this.toKey = this.subSortedKeys.get(this.subSortedKeys.size() - SUBSIZE);

            this.subSortedKeys.subList(0, SUBSIZE).clear();
            this.subSortedKeys.subList(this.subSortedKeys.size() - SUBSIZE, this.subSortedKeys.size()).clear();

            this.subSortedValues.subList(0, SUBSIZE).clear();
            this.subSortedValues.subList(this.subSortedValues.size() - SUBSIZE, this.subSortedValues.size()).clear();

            this.subSortedNewValues.addAll(Arrays.asList(main.getNewSampleValues()).subList(
                SUBSIZE, this.main.getNewSampleValues().length - SUBSIZE));
        }

        @Override
        public SortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return ((SortedMap<K, V>) main.makeObject()).subMap(fromKey, toKey);
        }
        @Override
        public SortedMap<K, V> makeFullMap() {
            return ((SortedMap<K, V>) main.makeFullMap()).subMap(fromKey, toKey);
        }

        @Test
        public void testSubMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(toKey, subSortedValues.get(0)));
            verify();
        }
        @Override
        public String getCompatibilityVersion() {
            return main.getCompatibilityVersion() + ".SubMapView";
        }

//        public void testCreate() throws Exception {
//            Map map = makeEmptyMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/TransformedSortedMap.emptyCollection.version3.1.SubMapView.obj");
//            map = makeFullMap();
//            writeExternalFormToDisk(
//                (java.io.Serializable) map,
//                "D:/dev/collections/data/test/TransformedSortedMap.fullCollection.version3.1.SubMapView.obj");
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getMap() {
        return (SortedMap<K, V>) super.getMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<K, V> getConfirmed() {
        return (SortedMap<K, V>) super.getConfirmed();
    }

}
