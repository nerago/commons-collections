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
import org.apache.commons.collections4.TransformerUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extension of {@link AbstractCollectionTest} for exercising the {@link DualTransformedCollection}
 * implementation.
 *
 * @since X.X
 */
public class DualTransformedCollectionTest extends AbstractCollectionTest<Integer> {

    @Override
    public boolean isCopyConstructorSupported() {
        return false;
    }

    private static class StringToInteger implements Transformer<String, Integer> {
        @Override
        public Integer transform(final String input) {
            return Integer.valueOf(input);
        }
    }

    private static class IntegerToString implements Transformer<Integer, String> {
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
    public Collection<Integer> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    @Override
    public Collection<Integer> makeConfirmedFullCollection() {
        return new ArrayList<>(Arrays.asList(getFullElements()));
    }

    @Override
    public Collection<Integer> makeObject() {
        return DualTransformedCollection.transformingCollection(new ArrayList<>(), NOOP_TRANSFORMER);
    }

    @Override
    public Collection<Integer> makeFullCollection() {
        final List<Object> list = new ArrayList<>(Arrays.asList(getFullElements()));
        return DualTransformedCollection.transformingCollection(list, NOOP_TRANSFORMER);
    }

    @Override
    public Integer[] getFullElements() {
        return new Object[]{"1", "3", "5", "7", "2", "4", "6"};
    }

    @Override
    public Integer[] getOtherElements() {
        return new Object[]{"9", "88", "678", "87", "98", "78", "99"};
    }

    @Test
    public void testTransformedCollection() {
        final Collection<Object> coll = DualTransformedCollection.transformingCollection(new ArrayList<>(), STRING_TO_INTEGER_TRANSFORMER);
        assertEquals(0, coll.size());
        final Object[] elements = getFullElements();
        for (int i = 0; i < elements.length; i++) {
            coll.add(elements[i]);
            assertEquals(i + 1, coll.size());
            assertTrue(coll.contains(Integer.valueOf((String) elements[i])));
            assertFalse(coll.contains(elements[i]));
        }

        assertTrue(coll.remove(Integer.valueOf((String) elements[0])));
    }

    @Test
    public void testTransformedCollection_decorateTransform() {
        final Collection<Object> originalCollection = new ArrayList<>();
        final Object[] elements = getFullElements();
        Collections.addAll(originalCollection, elements);
        final Collection<Object> collection = DualTransformedCollection.transformedCollection(originalCollection, TransformedCollectionTest.STRING_TO_INTEGER_TRANSFORMER);
        assertEquals(elements.length, collection.size());
        for (final Object element : elements) {
            assertTrue(collection.contains(Integer.valueOf((String) element)));
            assertFalse(collection.contains(element));
        }

        assertFalse(collection.remove(elements[0]));
        assertTrue(collection.remove(Integer.valueOf((String) elements[0])));
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }
}
