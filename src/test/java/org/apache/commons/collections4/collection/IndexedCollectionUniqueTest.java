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

import org.apache.commons.collections4.Transformer;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Extension of {@link AbstractCollectionTest} for exercising the
 * {@link IndexedCollection} implementation.
 *
 * @since 4.0
 */
@SuppressWarnings("boxing")
public class IndexedCollectionUniqueTest extends AbstractCollectionTest<String> {

    private static IndexedCollection<Integer, String> decorateUniqueCollection(final Collection<String> collection) {
        return IndexedCollection.uniqueIndexedCollection(collection, new IntegerTransformer());
    }

    private static final class IntegerTransformer implements Transformer<String, Integer>, Serializable {
        private static final long serialVersionUID = 809439581555072949L;

        @Override
        public Integer transform(final String input) {
            return Integer.valueOf(input);
        }
    }

    @Override
    public IndexedCollection<Integer, String> makeObject() {
        return decorateUniqueCollection(new ArrayList<>());
    }

    @Override
    public Collection<String> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    @Override
    public String[] getFullElements() {
        return new String[] { "1", "3", "5", "7", "2", "4", "6" };
    }

    @Override
    public String[] getOtherElements() {
        return new String[] {"9", "88", "678", "87", "98", "78", "99"};
    }

    @Override
    public Collection<String> makeFullCollection() {
        return decorateUniqueCollection(new ArrayList<>(Arrays.asList(getFullElements())));
    }

    @Override
    public Collection<String> makeConfirmedFullCollection() {
        return new ArrayList<>(Arrays.asList(getFullElements()));
    }

    @Override
    public String getCompatibilityVersion() {
        return "4.Unique";
    }

    @Override
    public void verify() {
        super.verify();

        final IndexedCollection<Integer, String> coll = (IndexedCollection<Integer, String>) getCollection();
        for (final String item : getConfirmed()) {
            assertTrue(coll.contains(item));

            final Integer key = Integer.valueOf(item);
            assertTrue(coll.values(key).contains(item));
            assertEquals(1, coll.values(key).size());
        }
    }

    /** Can only do the first two parts of the regular test normally since the third would add duplicates */
    @Test
    @Override
    public void testCollectionAddAll() {
        resetEmpty();
        String[] elements = getFullElements();
        boolean r = getCollection().addAll(Arrays.asList(elements));
        getConfirmed().addAll(Arrays.asList(elements));
        verify();
        assertTrue(r, "Empty collection should change after addAll");
        for (final String element : elements) {
            assertTrue(getCollection().contains(element), "Collection should contain added element");
        }

        resetFull();
        int size = getCollection().size();
        elements = getOtherElements();
        r = getCollection().addAll(Arrays.asList(elements));
        getConfirmed().addAll(Arrays.asList(elements));
        verify();
        assertTrue(r, "Full collection should change after addAll");
        for (final String element : elements) {
            assertTrue(getCollection().contains(element),
                    "Full collection should contain added element");
        }
        assertEquals(size + elements.length, getCollection().size(), "Size should increase after addAll");

        resetFull();
        size = getCollection().size();
        assertThrows(IllegalArgumentException.class, () -> getCollection().addAll(Arrays.asList(getFullElements())));
        // prior version of indexedcollection failed verify here since didn't pre validate uniqueness before changing
        verify();
        assertEquals(size, getCollection().size(), "Size should not change if addAll throws");
    }

    @Test
    public void testAddedObjectsCanBeRetrievedByKey() throws Exception {
        final IndexedCollection<Integer, String> coll = makeObject();
        coll.add("12");
        coll.add("16");
        coll.add("1");
        coll.addAll(Arrays.asList("2", "3", "4"));

        assertEquals("12", coll.get(12));
        assertEquals("16", coll.get(16));
        assertEquals("1", coll.get(1));
        assertEquals("2", coll.get(2));
        assertEquals("3", coll.get(3));
        assertEquals("4", coll.get(4));
    }

    @Test
    public void testEnsureDuplicateObjectsCauseException() throws Exception {
        final Collection<String> coll = makeObject();

        coll.add("1");

        assertThrows(IllegalArgumentException.class, () -> coll.add("1"));
    }

    @Test
    public void testDecoratedCollectionIsIndexedOnCreation() throws Exception {
        final Collection<String> original = makeFullCollection();
        final IndexedCollection<Integer, String> indexed = decorateUniqueCollection(original);

        assertEquals("1", indexed.get(1));
        assertEquals("2", indexed.get(2));
        assertEquals("3", indexed.get(3));
    }

    @Test
    public void testReindexUpdatesIndexWhenDecoratedCollectionIsModifiedSeparately() throws Exception {
        final Collection<String> original = new ArrayList<>();
        final IndexedCollection<Integer, String> indexed = decorateUniqueCollection(original);

        original.add("1");
        original.add("2");
        original.add("3");

        assertNull(indexed.get(1));
        assertNull(indexed.get(2));
        assertNull(indexed.get(3));

        indexed.reindex();

        assertEquals("1", indexed.get(1));
        assertEquals("2", indexed.get(2));
        assertEquals("3", indexed.get(3));
    }
}
