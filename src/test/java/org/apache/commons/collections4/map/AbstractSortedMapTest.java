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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

    /**
     * SortedMap uses TreeMap as its known comparison.
     *
     * @return a map that is known to be valid
     */
    @Override
    public SortedMap<K, V> makeConfirmedMap() {
        return new TreeMap<>();
    }

    @Override
    public abstract SortedMap<K, V> makeObject();

    @Override
    public SortedMap<K, V> makeFullMap() {
        return (SortedMap<K, V>) super.makeFullMap();
    }

    @Override
    public MapTest makeMapTest() {
        return new MapTest();
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    @Nested
    public class MapTest extends AbstractMapTest<K, V>.MapTest {
        @Override
        public SortedMap<K, V> getMap() {
            return (SortedMap<K, V>) super.getMap();
        }

        @Override
        public SortedMap<K, V> getConfirmed() {
            return (SortedMap<K, V>) super.getConfirmed();
        }

        @Override
        protected SortedMap<K, V> makeFullMap() {
            return (SortedMap<K, V>) super.makeFullMap();
        }

        @Override
        public SortedMap<K, V> makeObject() {
            return (SortedMap<K, V>) super.makeObject();
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
    }

    public abstract class TestViewMapBase extends MapTest {
        protected final List<K> subKeys = new ArrayList<>();
        protected final List<V> subValues = new ArrayList<>();
        protected final List<V> subNewValues = new ArrayList<>();

        protected MapTest mainTest;

        @BeforeEach
        protected void prepareTest() {
            mainTest = makeMapTest();
        }

        @Override
        public void resetEmpty() {
            // needed to init verify correctly
            mainTest.resetEmpty();
            super.resetEmpty();
        }

        @Override
        public void resetFull() {
            // needed to init verify correctly
            mainTest.resetFull();
            super.resetFull();
        }

        @Override
        public void verify() {
            // cross verify changes on view with changes on main map
            super.verify();
            mainTest.verify();
        }
        @Override
        @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
        public K[] getSampleKeys() {
            return (K[]) subKeys.toArray();
        }

        @Override
        @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
        public V[] getSampleValues() {
            return (V[]) subValues.toArray();
        }

        @Override
        @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
        public V[] getNewSampleValues() {
            return (V[]) subNewValues.toArray();
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }
    }

    @Nested
    public class TestHeadMap extends TestViewMapBase {
        static final int SUBSIZE = 6;
        K toKey;

        @BeforeEach
        protected void prepareRange() {
            final SortedMap<K, V> sm = mainTest.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                subKeys.add(entry.getKey());
                subValues.add(entry.getValue());
            }
            toKey = subKeys.get(SUBSIZE);
            subKeys.subList(SUBSIZE, subKeys.size()).clear();
            subValues.subList(SUBSIZE, subValues.size()).clear();
            subNewValues.addAll(Arrays.asList(mainTest.getNewSampleValues()).subList(0, SUBSIZE));
        }

        @Override
        public SortedMap<K, V> makeObject() {
            return mainTest.makeObject().headMap(toKey);
        }

        @Override
        public SortedMap<K, V> makeFullMap() {
            return mainTest.makeFullMap().headMap(toKey);
        }

        @Test
        public void testHeadMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(toKey, subValues.get(0)));
            verify();
        }

        @Override
        public String getCompatibilityVersion() {
            return mainTest.getCompatibilityVersion() + ".HeadMapView";
        }
    }

    @Nested
    public class TestTailMap extends TestViewMapBase {
        static final int SUBSIZE = 6;
        K fromKey;
        K invalidKey;

        @BeforeEach
        protected void prepareRange() {
            final SortedMap<K, V> sm = mainTest.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                subKeys.add(entry.getKey());
                subValues.add(entry.getValue());
            }
            fromKey = subKeys.get(subKeys.size() - SUBSIZE);
            invalidKey = subKeys.get(subKeys.size() - SUBSIZE - 1);
            subKeys.subList(0, subKeys.size() - SUBSIZE).clear();
            subValues.subList(0, subValues.size() - SUBSIZE).clear();
            subNewValues.addAll(Arrays.asList(mainTest.getNewSampleValues()).subList(0, SUBSIZE));
        }
        
        @Override
        public SortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return mainTest.makeObject().tailMap(fromKey);
        }
        
        @Override
        public SortedMap<K, V> makeFullMap() {
            return mainTest.makeFullMap().tailMap(fromKey);
        }

        @Test
        public void testTailMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(invalidKey, subValues.get(0)));
            verify();
        }
        @Override
        public String getCompatibilityVersion() {
            return mainTest.getCompatibilityVersion() + ".TailMapView";
        }
    }

    @Nested
    public class TestSubMap extends TestViewMapBase {
        static final int SUBSIZE = 3;
        K fromKey;
        K toKey;

        @BeforeEach
        protected void prepareRange() {
            final SortedMap<K, V> sm = mainTest.makeFullMap();
            for (final Entry<K, V> entry : sm.entrySet()) {
                subKeys.add(entry.getKey());
                subValues.add(entry.getValue());
            }

            fromKey = subKeys.get(SUBSIZE);
            toKey = subKeys.get(subKeys.size() - SUBSIZE);

            subKeys.subList(0, SUBSIZE).clear();
            subKeys.subList(subKeys.size() - SUBSIZE, subKeys.size()).clear();

            subValues.subList(0, SUBSIZE).clear();
            subValues.subList(subValues.size() - SUBSIZE, subValues.size()).clear();

            subNewValues.addAll(Arrays.asList(mainTest.getNewSampleValues()).subList(
                SUBSIZE, mainTest.getNewSampleValues().length - SUBSIZE));
        }

        @Override
        public SortedMap<K, V> makeObject() {
            // done this way so toKey is correctly set in the returned map
            return mainTest.makeObject().subMap(fromKey, toKey);
        }

        @Override
        public SortedMap<K, V> makeFullMap() {
            return mainTest.makeFullMap().subMap(fromKey, toKey);
        }

        @Test
        public void testSubMapOutOfRange() {
            if (!isPutAddSupported()) {
                return;
            }
            resetEmpty();
            assertThrows(IllegalArgumentException.class, () -> getMap().put(toKey, subValues.get(0)));
            verify();
        }

        @Override
        public String getCompatibilityVersion() {
            return mainTest.getCompatibilityVersion() + ".SubMapView";
        }
    }
}
