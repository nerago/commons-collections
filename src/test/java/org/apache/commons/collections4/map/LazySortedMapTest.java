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

import static org.apache.commons.collections4.map.LazySortedMap.lazySortedMap;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Extension of {@link LazyMapTest} for exercising the
 * {@link LazySortedMap} implementation.
 *
 * @since 3.0
 */
@SuppressWarnings("boxing")
public class LazySortedMapTest<K, V> extends AbstractSortedMapTest<K, V> {

    private static class ReverseStringComparator implements Comparator<String> {

        @Override
        public int compare(final String arg0, final String arg1) {
            return arg1.compareTo(arg0);
        }

    }

    private static final int FACTORY = 42;
    private static final Factory<Integer> defaultFactory = FactoryUtils.constantFactory(FACTORY);

    protected final Comparator<String> reverseStringComparator = new ReverseStringComparator();

    public LazySortedMapTest() {
        super("");
    }

    @Override
    @SuppressWarnings("unchecked")
    public LazySortedMap<K, V> makeObject() {
        return lazySortedMap(new TreeMap<>(), (Factory<V>) defaultFactory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getMissingEntryGetExpectValue() {
        return (V) (Integer) FACTORY;
    }

    @Override
    public boolean isAllowNullKey() {
        return false;
    }

    // expect different behaviour in Lazy maps, will test via LazyMapTestsNested
    @Override
    public boolean isTestFunctionalMethods() {
        return false;
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.OTHER_DECORATOR;
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.FULLY_SORTED;
    }

    @Override
    public boolean isGetStructuralModify() {
        return true;
    }

    @Test
    public void mapGetLazy() {
        Map<Integer, Number> map = lazySortedMap(new TreeMap<>(), defaultFactory);
        assertEquals(0, map.size());
        final Number i1 = map.get(5);
        assertEquals(FACTORY, i1);
        assertEquals(1, map.size());

        map = lazySortedMap(new TreeMap<>(), FactoryUtils.nullFactory());
        final Number o = map.get(5);
        assertNull(o);
        assertEquals(1, map.size());

    }

    @Test
    public void testSortOrder() {
        final SortedMap<String, Number> map = lazySortedMap(new TreeMap<>(), defaultFactory);
        map.put("A",  5);
        map.get("B"); // Entry with value "One" created
        map.put("C", 8);
        assertEquals("A", map.firstKey(), "First key should be A");
        assertEquals("C", map.lastKey(), "Last key should be C");
        assertEquals("B", map.tailMap("B").firstKey(),
                "First key in tail map should be B");
        assertEquals("B", map.headMap("C").lastKey(),
                "Last key in head map should be B");
        assertEquals("B", map.subMap("A", "C").lastKey(),
                "Last key in submap should be B");

        final Comparator<?> c = map.comparator();
        assertNull(c, "natural order, so comparator should be null");
    }

    @Test
    public void testReverseSortOrder() {
        final SortedMap<String, Number> map = lazySortedMap(new ConcurrentSkipListMap<String, Number>(reverseStringComparator), defaultFactory);
        map.put("A", 5);
        map.get("B"); // Entry with value "One" created
        map.put("C", 8);
        assertEquals("A", map.lastKey(), "Last key should be A");
        assertEquals("C", map.firstKey(), "First key should be C");
        assertEquals("B", map.tailMap("B").firstKey(),
                "First key in tail map should be B");
        assertEquals("B", map.headMap("A").lastKey(),
                "Last key in head map should be B");
        assertEquals("B", map.subMap("C", "A").lastKey(),
                "Last key in submap should be B");

        final Comparator<?> c = map.comparator();
        assertSame(c, reverseStringComparator, "natural order, so comparator should be null");
    }

    @Test
    public void testTransformerDecorate() {
        final Transformer<Object, Integer> transformer = TransformerUtils.asTransformer(defaultFactory);
        final SortedMap<Integer, Number> map = lazySortedMap(new TreeMap<Integer, Number>(), transformer);
        assertTrue(map instanceof LazySortedMap);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> lazySortedMap(new TreeMap<Integer, Number>(), (Transformer<Integer, Number>) null),
                        "Expecting NullPointerException for null transformer"),
                () -> assertThrows(NullPointerException.class, () -> lazySortedMap((SortedMap<Integer, Number>) null, transformer),
                        "Expecting NullPointerException for null map")
        );
    }

    // expect different behaviour in Lazy maps, will test via LazyMapTestsNested
    @Test
    @Disabled
    @Override
    public void testMapGetOrDefault() {
        super.testMapGetOrDefault();
    }
    @Test
    @Disabled
    @Override
    public void testMapReplace2() {
        super.testMapReplace2();
    }

    @Nested
    public class LazyMapTestsNested extends LazyMapTest<K, V> {
        @Override
        public IterableMap<K, V> makeObject() {
            return LazySortedMapTest.this.makeObject();
        }

        @Override
        public boolean isAllowNullKey() {
            return false;
        }

        @Override
        protected IterationBehaviour getIterationBehaviour() {
            return IterationBehaviour.FULLY_SORTED;
        }
    }

    @Override
    @TestFactory
    public DynamicNode subMapTests() {
        if (runSubMapTests()) {
            return DynamicContainer.dynamicContainer("subMapTests", Arrays.asList(
                    new TestLazyHeadMap(this).getDynamicTests(),
                    new TestLazyTailMap(this).getDynamicTests(),
                    new TestLazySubMap(this).getDynamicTests()
            ));
        } else {
            return DynamicContainer.dynamicContainer("subMapTests", Stream.empty());
        }
    }

    public class TestLazyHeadMap extends TestLazyViewMap<K, V> {
        static final int SUBSIZE = 6;
        final K toKey;

        public TestLazyHeadMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Map.Entry<K, V> entry : sm.entrySet()) {
                this.subSortedKeys.add(entry.getKey());
                this.subSortedValues.add(entry.getValue());
            }
            this.toKey = this.subSortedKeys.get(SUBSIZE);
            this.subSortedKeys.subList(SUBSIZE, this.subSortedKeys.size()).clear();
            this.subSortedValues.subList(SUBSIZE, this.subSortedValues.size()).clear();
            this.subSortedNewValues.addAll(Arrays.asList(main.getNewSampleValues()).subList(0, SUBSIZE));
        }
        @Override
        public LazySortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return (LazySortedMap<K, V>) ((LazySortedMap<K, V>) main.makeObject()).headMap(toKey);
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
    }

    public class TestLazyTailMap extends TestLazyViewMap<K, V> {
        static final int SUBSIZE = 6;
        final K fromKey;
        final K invalidKey;

        public TestLazyTailMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Map.Entry<K, V> entry : sm.entrySet()) {
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
        public LazySortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return (LazySortedMap<K, V>) ((SortedMap<K, V>) main.makeObject()).tailMap(fromKey);
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
    }

    public class TestLazySubMap extends TestLazyViewMap<K, V> {
        static final int SUBSIZE = 3;
        final K fromKey;
        final K toKey;

        public TestLazySubMap(final AbstractMapTest<K, V> main) {
            super(main);
            final Map<K, V> sm = main.makeFullMap();
            for (final Map.Entry<K, V> entry : sm.entrySet()) {
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
        public LazySortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return (LazySortedMap<K, V>) ((SortedMap<K, V>) main.makeObject()).subMap(fromKey, toKey);
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
    }

    public abstract static class TestLazyViewMap<K, V> extends LazySortedMapTest<K, V> {
        protected final AbstractMapTest<K, V> main;
        protected final List<K> subSortedKeys = new ArrayList<>();
        protected final List<V> subSortedValues = new ArrayList<>();
        protected final List<V> subSortedNewValues = new ArrayList<>();

        public TestLazyViewMap(final AbstractMapTest<K, V> main) {
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
        public boolean isFailFastFunctionalExpected() {
            return main.isFailFastFunctionalExpected();
        }
        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }
        @Override
        protected boolean runSubMapTests() {
            return false;
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }
    }


    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/LazySortedMap.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/LazySortedMap.fullCollection.version4.obj");
//    }

}
