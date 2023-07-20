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

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.Transformer;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extension of {@link AbstractCollectionTest} for exercising the {@link DualTransformedCollection}
 * implementation.
 *
 * @since X.X
 */
public class DualTransformedCollectionTest extends AbstractCollectionTest<Integer> {

    private static class StringToInteger implements Transformer<String, Integer>, Serializable {
        private static final long serialVersionUID = 6210576099579022361L;

        @Override
        public Integer transform(final String input) {
            return Integer.valueOf(input);
        }
    }

    private static class IntegerToString implements Transformer<Integer, String>, Serializable {
        private static final long serialVersionUID = 3091292970818955357L;

        @Override
        public String transform(final Integer input) {
            return String.valueOf(input);
        }
    }

    public static final Transformer<String, Integer> STRING_TO_INTEGER_TRANSFORMER = new StringToInteger();
    public static final Transformer<Integer, String> INTEGER_TO_STRING_TRANSFORMER = new IntegerToString();

    public DualTransformedCollectionTest() {
        super(TransformedCollectionTest.class.getSimpleName());
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.TRANSFORM;
    }

    @Override
    public Collection<Integer> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Integer> makeConfirmedFullCollection() {
        return new ArrayList<>(Arrays.asList(getFullElements()));
    }

    @Override
    public Collection<Integer> makeObject() {
        final List<String> list = new ArrayList<>();
        return DualTransformedCollection.transformingCollection(list, INTEGER_TO_STRING_TRANSFORMER, STRING_TO_INTEGER_TRANSFORMER);
    }

    @Override
    public Collection<Integer> makeFullCollection() {
        final List<Integer> input = Arrays.asList(getFullElements());
        final List<String> list = new ArrayList<>();
        return DualTransformedCollection.transformedCollection(input, list, INTEGER_TO_STRING_TRANSFORMER, STRING_TO_INTEGER_TRANSFORMER);
    }

    public String[] getFullElementsInternal() { return new String[]{"1", "3", "5", "7", "2", "4", "6"}; }

    @Override
    public Integer[] getFullElements() {
        return new Integer[]{1, 3, 5, 7, 2, 4, 6};
    }

    @Override
    public Integer[] getOtherElements() {
        return new Integer[]{9, 88, 678, 87, 98, 78, 99};
    }

    protected Collection<String> getInternal(Collection<Integer> coll) {
        return ((DualTransformedCollection<Integer, String>)coll).decorated();
    }

    @Test
    public void testTransformedCollection() {
        final Collection<Integer> coll = makeObject();
        assertEquals(0, coll.size());
        final Integer[] elements = getFullElements();
        final String[] internalElements = getFullElementsInternal();
        for (int i = 0; i < elements.length; i++) {
            final Integer e = elements[i];
            final String s = internalElements[i];
            coll.add(elements[i]);
            assertEquals(i + 1, coll.size());
            assertTrue(coll.contains(e));
            assertFalseOrThrows(ClassCastException.class, () -> coll.contains(s));
            assertFalseOrThrows(ClassCastException.class, () -> getInternal(coll).contains(e));
            assertTrue(getInternal(coll).contains(s));
        }

        assertTrue(coll.remove(elements[0]));
        assertFalseOrThrows(ClassCastException.class, () -> coll.remove(internalElements[1]));
    }

    @Test
    public void testTransformedCollection_decorateTransform() {
        final Collection<Integer> inputCollection = new ArrayList<>();
        final Integer[] elements = getFullElements();
        final String[] internalElements = getFullElementsInternal();
        Collections.addAll(inputCollection, elements);
        Collection<String> storedCollection = new ArrayList<>();
        final DualTransformedCollection<Integer, String> collection =
                (DualTransformedCollection<Integer, String>) DualTransformedCollection.transformedCollection(
                inputCollection, storedCollection, INTEGER_TO_STRING_TRANSFORMER, STRING_TO_INTEGER_TRANSFORMER);
        assertEquals(elements.length, collection.size());
        for (final Object element : elements) {
            final Object str = element.toString();
            assertTrue(collection.contains(element));
            assertFalseOrThrows(ClassCastException.class, () -> collection.contains(str));
            assertFalseOrThrows(ClassCastException.class, () -> collection.decorated().contains(element));
            assertTrue(collection.decorated().contains(str));
        }

        assertTrue(collection.remove(elements[0]));
        assertFalseOrThrows(ClassCastException.class, () -> collection.remove(internalElements[1]));
    }

    private void assertFalseOrThrows(Class<?> expectedType, Callable<Boolean> executable) {
        try {
            boolean result = executable.call();
            assertFalse(result);
            return;
        }
        catch (Throwable actualException) {
            if (expectedType.isInstance(actualException)) {
                return;
            } else {
                fail("Expected exception " + expectedType + " to be thrown, but " + actualException.getClass() + " was thrown.");
            }
        }
        fail("Expected exception " + expectedType + " to be thrown, but nothing was thrown.");
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }
}
