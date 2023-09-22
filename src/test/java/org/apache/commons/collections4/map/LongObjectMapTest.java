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

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongObjectMapTest extends AbstractObjectTest {
    public LongObjectMapTest() {
        super("LongObjectMapTest");
    }

    @Override
    public LongObjectMap<String> makeObject() {
        return new LongObjectMap<>();
    }

    protected LongObjectMap<String> map;

    protected Map<Long, String> confirmed;

    private static final long[] testKeys = new long[] {
            1L, 7L, 11L, 12L, 13L, 17L, 21L, 22L,
            -30L, -31L, -32L, -33L, 34L, 35L, 36L,
            Long.MAX_VALUE,
            Long.MIN_VALUE,
            0L };

    private static final String[] testValues = {
            "blahv", "foov", "barv", "bazv", "tmpv", "goshv", "gollyv", "geev",
            "hellov", "goodbyev", "we'llv", "seev", "youv", "allv", "againv",
            null, "value", "value",
    };

    private static final long[] otherKeys = new long[] {
        123L, 456L, 789L, 111L, 222L, 333L, 444L, 555L,
                -123L, -456L, -789L, -111L, -222L, -333L, -444L, -555L
    };

    void resetEmpty() {
        map = new LongObjectMap<>();
        confirmed = new HashMap<>();
    }

    void resetFull() {
        map = new LongObjectMap<>();
        confirmed = new HashMap<>();
        for (int i = 0; i < testKeys.length; ++i) {
            map.put(testKeys[i], testValues[i]);
            confirmed.put(testKeys[i], testValues[i]);
        }
    }

    void verify() {
        assertTrue(MapUtils.isEqualMap(map.mapIterator(), confirmed));
    }

    @Test
    void size() {
        resetEmpty();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        verify();

        resetFull();
        assertFalse(map.isEmpty());
        assertEquals(confirmed.size(), map.size());
        verify();
    }

    @Test
    void get() {
        resetEmpty();
        for (int i = 0; i < testKeys.length; ++i) {
            assertNull(map.get(testKeys[i]));
            assertFalse(map.containsKey(testKeys[i]));
            assertFalse(map.containsValue(testValues[i]));
        }
        verify();

        resetFull();
        for (int i = 0; i < testKeys.length; ++i) {
            assertEquals(testValues[i], map.get(testKeys[i]));
            assertTrue(map.containsKey(testKeys[i]));
            assertTrue(map.containsValue(testValues[i]));
        }
        for (long otherKey : otherKeys) {
            assertNull(map.get(otherKey));
        }
        verify();
    }

    @Test
    void getOrDefault() {
        final String defaultValue = "abc";

        resetEmpty();
        for (long testKey : testKeys) {
            assertEquals(defaultValue, map.getOrDefault(testKey, defaultValue));
        }
        verify();

        resetFull();
        for (int i = 0; i < testKeys.length; ++i) {
            assertEquals(testValues[i], map.getOrDefault(testKeys[i], defaultValue));
        }
        for (long otherKey : otherKeys) {
            assertEquals(defaultValue, map.getOrDefault(otherKey, defaultValue));
        }
        verify();
    }

    @Test
    void put() {
    }

    @Test
    void putIfAbsent() {
    }

    @Test
    void replace() {
    }

    @Test
    void testReplace() {
    }

    @Test
    void putAll() {
    }

    @Test
    void testPutAll() {
    }

    @Test
    void removeKey() {
    }

    @Test
    void remove() {
    }

    @Test
    void testRemove() {
    }

    @Test
    void clear() {
    }

    @Test
    void toKeyArray() {
    }

    @Test
    void forEach() {
    }

    @Test
    void containsEntry() {
    }

    @Test
    void testContainsEntry() {
    }

    @Test
    void removeEntry() {
    }

    @Test
    void testContainsEntry1() {
    }

    @Test
    void testRemoveEntry() {
    }

    @Test
    void keySet() {
    }

    @Nested
    public class CheckMapAdapter extends AbstractMapTest<Long, String> {
        public CheckMapAdapter() {
            super("CheckMapAdapter");
        }

        @Override
        public boolean isCopyConstructorCheckable() {
            return false;
        }

        @Override
        public boolean isFailFastExpected() {
            return false;
        }

        @Override
        public boolean isAllowNullKey() {
            return false;
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }

        @Override
        protected IterationBehaviour getIterationBehaviour() {
            return IterationBehaviour.CONSISTENT_SEQUENCE_UNTIL_MODIFY;
        }

        @Override
        public Long[] getSampleKeys() {
            return new Long[] {
                    1L, 7L, 11L, 12L, 13L, 17L, 21L, 22L,
                    -30L, -31L, -32L, -33L, 34L, 35L, 36L,
                    Long.MAX_VALUE,
                    Long.MIN_VALUE,
                    0L
            };
        }

        @Override
        public Long[] getOtherKeys() {
            return new Long[] {
                    123L, 456L, 789L, 111L, 222L, 333L, 444L, 555L,
                    -123L, -456L, -789L, -111L, -222L, -333L, -444L, -555L
            };
        }

        @Override
        public Map<Long, String> makeObject() {
            return LongObjectMapTest.this.makeObject().asMap();
        }
    }
}
