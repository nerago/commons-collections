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
package org.apache.commons.collections4.collection;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.TestUtils.assertUnorderedArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.Transformer;
import org.junit.jupiter.api.Test;

/**
 * Extension of {@link AbstractCollectionTest} for exercising the
 * {@link IndexedCollection} implementation.
 *
 * @since 4.0
 */
@SuppressWarnings("boxing")
public class IndexedCollectionTest extends AbstractCollectionTest<String> {

    protected Collection<String> decorateCollection(final Collection<String> collection) {
        return IndexedCollectionOriginal.nonUniqueIndexedCollection(collection, new LowerCaseTransformer());
    }

    private static final class LowerCaseTransformer implements Transformer<String, String>, Serializable {
        private static final long serialVersionUID = 809439581512072949L;

        @Override
        public String transform(final String input) {
            return input.toLowerCase();
        }
    }

    @Override
    public Collection<String> makeObject() {
        return decorateCollection(new ArrayList<>());
    }

    @Override
    public Collection<String> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    @Override
    public String[] getFullElements() {
        // Include some duplicates, some otherwise mapping to same key.
        // However, order matters to some of the base collection tests since this collection mildly breaks usual contract
        //   where Collection.contains uses indexed value (in this example case-insensitive match).
        return new String[] { "a", "c", "3", "i", "r", "s", "d", "f", "B", "D", "f" };
    }

    @Override
    public String[] getOtherElements() {
        return new String[] {"9", "v", "678", "87", "98", "q", "99", "98", "Q", "99"};
    }

    @Override
    public Collection<String> makeFullCollection() {
        return decorateCollection(new ArrayList<>(Arrays.asList(getFullElements())));
    }

    @Override
    public Collection<String> makeConfirmedFullCollection() {
        return new ArrayList<>(Arrays.asList(getFullElements()));
    }

    @Override
    public void verify() {
        super.verify();

        final IndexedCollectionInterface<String, String> coll = (IndexedCollectionInterface<String, String>) getCollection();
        // older version of non-unique indexed collection failed both of these asserts since remove cleared too much
        for (final String item : coll) {
            assertTrue(coll.contains(item));

            final String key = item.toLowerCase();
            assertTrue(coll.values(key).contains(item));
        }
    }

    @Test
    public void testNonUniqueGet() {
        final IndexedCollection<String, String> indexed =
                IndexedCollection.nonUniqueIndexedCollection(new ArrayList<>(), new LowerCaseTransformer());
        indexed.add("aa");
        indexed.add("aA");
        indexed.add("bB");
        indexed.add("bb");
        indexed.add("CC");

        assertNull(indexed.get("zz"), "should be null for non present");
        assertNull(indexed.get("aA"), "should be null for value that is an entry but not a valid key");
        assertTrue(Arrays.asList("aa", "aA").contains(indexed.get("aa")), "should return either value");
        assertTrue(Arrays.asList("bb", "bB").contains(indexed.get("bb")), "should return either value");
        assertEquals("CC", indexed.get("cc"), "should return either value");
    }

    @Test
    public void testNonUniqueValues() {
        final IndexedCollection<String, String> indexed =
                IndexedCollection.nonUniqueIndexedCollection(new ArrayList<>(), new LowerCaseTransformer());
        indexed.add("aa");
        indexed.add("aA");
        indexed.add("bB");
        indexed.add("bb");
        indexed.add("CC");

        assertNull(indexed.values("zz"), "should be null for non present");
        assertNull(indexed.values("aA"), "should be null for value that is an entry but not a valid key");
        assertUnorderedArrayEquals(new String[] { "aa", "aA" }, indexed.values("aa").toArray(),
                "values should return all mapped entries");
        assertUnorderedArrayEquals(new String[] { "bB", "bb" }, indexed.values("bb").toArray(),
                "values should return all mapped entries");
        assertUnorderedArrayEquals(new String[] { "CC" }, indexed.values("cc").toArray(), "values should return all mapped entries");
        // assertTrue(indexed.values("cc") instanceof Unmodifiable); // original violates this line
    }

    @Test
    public void testNonUniqueContains() {
        final IndexedCollection<String, String> indexed =
                IndexedCollection.nonUniqueIndexedCollection(new ArrayList<>(), new LowerCaseTransformer());
        indexed.add("aa");
        indexed.add("aA");
        indexed.add("CC");

        assertTrue(indexed.contains("aa"));
        assertTrue(indexed.contains("aA"));
        assertTrue(indexed.contains("Aa"));
        assertTrue(indexed.contains("AA"));
        assertTrue(indexed.contains("cc"));
        assertTrue(indexed.contains("CC"));
        assertFalse(indexed.contains("zz"));
    }

    @Test
    public void testNonUniqueRemove() {
        final IndexedCollection<String, String> indexed =
                IndexedCollection.nonUniqueIndexedCollection(new ArrayList<>(), new LowerCaseTransformer());
        indexed.add("aa");
        indexed.add("aA");

        // verify initial state
        assertEquals(2, indexed.size());
        assertFalse(indexed.isEmpty());
        assertNotNull(indexed.get("aa"));
        assertTrue(indexed.contains("aa"));
        //assertTrue(indexed.contains("aA"));
        assertUnorderedArrayEquals(new String[] { "aa", "aA" }, indexed.values("aa").toArray(),
                "values should return all mapped entries");

        // remove second
        assertTrue(indexed.remove("aA"));
        assertEquals(1, indexed.size());
        assertFalse(indexed.isEmpty());
        assertNotNull(indexed.get("aa")); // original fails here
        assertTrue(indexed.contains("aa"));
        //assertTrue(indexed.contains("aA")); // original contains just determines on key
        assertUnorderedArrayEquals(new String[] { "aa" }, indexed.values("aa").toArray(),
                "values should return all mapped entries");

        // try removing same again
        assertFalse(indexed.remove("aA"));
        assertEquals(1, indexed.size());
        assertFalse(indexed.isEmpty());
        assertNotNull(indexed.get("aa"));
        assertTrue(indexed.contains("aa"));
        //assertFalse(indexed.contains("aA"));
        assertUnorderedArrayEquals(new String[] { "aa" }, indexed.values("aa").toArray(),
                "values should return all mapped entries");

        // remove last
        assertTrue(indexed.remove("aa"));
        assertEquals(0, indexed.size());
        assertTrue(indexed.isEmpty());
        assertNull(indexed.get("aa"));
        assertFalse(indexed.contains("aa"));
        //assertFalse(indexed.contains("aA"));
        assertNull(indexed.values("aa"));
    }
}
