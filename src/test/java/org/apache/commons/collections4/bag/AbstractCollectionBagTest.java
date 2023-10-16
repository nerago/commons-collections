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
package org.apache.commons.collections4.bag;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.collection.AbstractCollectionTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractCollectionBagTest<T> extends AbstractCollectionTest<T> {
    @Override
    public abstract Bag<T> makeObject();

    @Test
    @SuppressWarnings("unchecked")
    public void testBagAdd() {
        if (!isAddSupported()) {
            return;
        }

        final Bag<T> bag = makeObject();
        assertTrue(bag.add((T) "A"));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(1, bag.getCount("A"), "Should have count of 1");
        assertTrue(bag.add((T) "A"));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(2, bag.getCount("A"), "Should have count of 2");
        assertTrue(bag.add((T) "B"));
        assertTrue(bag.contains("A"));
        assertTrue(bag.contains("B"));
    }

    @Test
    public void testBagAddNumbers() {
        if (!isAddSupported()) {
            return;
        }

        Bag<T> bag = makeObject();
        assertTrue(bag.add((T) "A", 1));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(1, bag.getCount("A"), "Should have count of 1");
        assertEquals(1, bag.size());

        bag = makeObject();
        assertTrue(bag.add((T) "A", 5));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(5, bag.getCount("A"), "Should have count of 5");
        assertEquals(5, bag.size());
        assertTrue(bag.add((T) "A", 2));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(7, bag.getCount("A"), "Should have count of 5");
        assertEquals(7, bag.size());
        assertFalse(bag.add((T) "A", -7));
        assertTrue(bag.contains("A"), "Should contain 'A'");
        assertEquals(7, bag.getCount("A"), "Should have count of 5");
        assertEquals(7, bag.size());

        assertFalse(bag.add((T) "Z", 0));
        assertFalse(bag.contains("Z"), "Should not contain 'Z'");
        assertEquals(0, bag.getCount("Z"), "Should have count of 0");
        assertEquals(7, bag.size());
        assertFalse(bag.add((T) "Z", -1));
        assertFalse(bag.contains("Z"), "Should not contain 'Z'");
        assertEquals(0, bag.getCount("Z"), "Should have count of 0");
        assertEquals(7, bag.size());
        assertFalse(bag.add((T) "Z", -9));
        assertFalse(bag.contains("Z"), "Should not contain 'Z'");
        assertEquals(0, bag.getCount("Z"), "Should have count of 0");
        assertEquals(7, bag.size());
    }

    @Test
    public void testBagRemoveNumbers() {
        if (!isAddSupported() || !isRemoveSupported()) {
            return;
        }

        Bag<T> bag = makeObject();
        bag.add((T) "A", 4);
        bag.add((T) "B", 3);
        bag.add((T) "C", 2);
        assertEquals(4, bag.getCount("A"));
        assertEquals(3, bag.getCount("B"));
        assertEquals(2, bag.getCount("C"));
        assertEquals(9, bag.size());

        assertTrue(bag.remove("A", 2));
        assertEquals(2, bag.getCount("A"));
        assertTrue(bag.contains("A"));
        assertTrue(bag.remove("A", 2));
        assertEquals(0, bag.getCount("A"));
        assertFalse(bag.contains("A"));
        assertEquals(5, bag.size());

        assertTrue(bag.remove("B", 2));
        assertEquals(1, bag.getCount("B"));
        assertTrue(bag.contains("B"));
        assertTrue(bag.remove("B", 2));
        assertEquals(0, bag.getCount("B"));
        assertFalse(bag.contains("B"));
        assertEquals(2, bag.size());

        assertFalse(bag.remove("C", 0));
        assertEquals(2, bag.getCount("C"));
        assertTrue(bag.contains("C"));
        assertFalse(bag.remove("C", -2));
        assertEquals(2, bag.getCount("C"));
        assertTrue(bag.contains("C"));
        assertEquals(2, bag.size());

        assertFalse(bag.remove("Z", 0));
        assertEquals(2, bag.size());
        assertTrue(bag.contains("C"));
        assertFalse(bag.contains("Z"));
        assertFalse(bag.remove("Z", 2));
        assertEquals(2, bag.size());
        assertTrue(bag.contains("C"));
        assertFalse(bag.contains("Z"));
    }
}
