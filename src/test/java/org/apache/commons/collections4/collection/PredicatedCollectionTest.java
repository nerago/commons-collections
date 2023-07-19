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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.TruePredicate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extension of {@link AbstractCollectionTest} for exercising the
 * {@link PredicatedCollection} implementation.
 *
 * @since 3.0
 */
public class PredicatedCollectionTest<E> extends AbstractCollectionTest<E> {

    public PredicatedCollectionTest() {
        super(PredicatedCollectionTest.class.getSimpleName());
    }

    protected Predicate<E> truePredicate = TruePredicate.<E>truePredicate();

    protected Collection<E> decorateCollection(
                final Collection<E> collection, final Predicate<E> predicate) {
        return PredicatedCollection.predicatedCollection(collection, predicate);
    }

    @Override
    public Collection<E> makeObject() {
        return decorateCollection(new ArrayList<E>(), truePredicate);
    }

    @Override
    public Collection<E> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E[] getFullElements() {
        return (E[]) new Object[] { "1", "3", "5", "7", "2", "4", "6" };
    }

    @Override
    public Collection<E> makeFullCollection() {
        final List<E> list = new ArrayList<>(Arrays.asList(getFullElements()));
        return decorateCollection(list, truePredicate);
    }

    @Override
    public Collection<E> makeConfirmedFullCollection() {
        return new ArrayList<>(Arrays.asList(getFullElements()));
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.PREDICATED;
    }

    protected Predicate<E> testPredicate =
        o -> o instanceof String;

    public Collection<E> makeTestCollection() {
        return decorateCollection(new ArrayList<E>(), testPredicate);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalAdd() {
        final Collection<E> c = makeTestCollection();
        final Integer i = 3;

        assertThrows(IllegalArgumentException.class, () -> c.add((E) i), "Integer should fail string predicate.");

        assertFalse(c.contains(i), "Collection shouldn't contain illegal element");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalAddAll() {
        final Collection<E> c = makeTestCollection();
        final List<E> elements = new ArrayList<>();
        elements.add((E) "one");
        elements.add((E) "two");
        elements.add((E) Integer.valueOf(3));
        elements.add((E) "four");

        assertThrows(IllegalArgumentException.class, () -> c.addAll(elements), "Integer should fail string predicate.");

        assertFalse(c.contains("one"), "Collection shouldn't contain illegal element");
        assertFalse(c.contains("two"), "Collection shouldn't contain illegal element");
        assertFalse(c.contains(3), "Collection shouldn't contain illegal element");
        assertFalse(c.contains("four"), "Collection shouldn't contain illegal element");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateOnCreate() {
        final List<E> elements1 = new ArrayList<>();
        elements1.add((E) "one");
        elements1.add((E) Integer.valueOf(2));
        assertThrows(IllegalArgumentException.class, () -> decorateCollection(elements1, testPredicate));

        final List<E> elements2 = new ArrayList<>();
        elements2.add((E) "one");
        elements2.add((E) "two");
        final Collection<E> c2 = decorateCollection(elements2, testPredicate);
        assertEquals(2, c2.size());
        assertTrue(c2.contains("one"));
        assertTrue(c2.contains("two"));

        final List<E> elements3 = new ArrayList<>();
        final Collection<E> c3 = decorateCollection(elements3, testPredicate);
        assertEquals(0, c3.size());
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/PredicatedCollection.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/PredicatedCollection.fullCollection.version4.obj");
//    }

}
