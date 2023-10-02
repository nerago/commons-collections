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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.collection.AbstractCollectionTest;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.collections4.set.AbstractSetTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Abstract test class for {@link java.util.Map} methods and contracts.
 * <p>
 * The forces at work here are similar to those in {@link AbstractCollectionTest}.
 * If your class implements the full Map interface, including optional
 * operations, simply extend this class, and implement the
 * {@link #makeObject()} method.
 * <p>
 * On the other hand, if your map implementation is weird, you may have to
 * override one or more of the other protected methods.  They're described
 * below.
 * <p>
 * <b>Entry Population Methods</b>
 * <p>
 * Override these methods if your map requires special entries:
 *
 * <ul>
 * <li>{@link #getSampleKeys()}
 * <li>{@link #getSampleValues()}
 * <li>{@link #getNewSampleValues()}
 * <li>{@link #getOtherKeys()}
 * <li>{@link #getOtherValues()}
 * </ul>
 *
 * <b>Indicate Map Behaviour</b>
 * <p>
 * Override these if your map makes specific behavior guarantees:
 * <ul>
 * <li>{@link #getIterationBehaviour()}</li>
 * </ul>
 *
 * <b>Supported Operation Methods</b>
 * <p>
 * Override these methods if your map doesn't support certain operations:
 *
 * <ul>
 * <li> {@link #isPutAddSupported()}
 * <li> {@link #isPutChangeSupported()}
 * <li> {@link #isSetValueSupported()}
 * <li> {@link #isRemoveSupported()}
 * <li> {@link #isGetStructuralModify()}
 * <li> {@link #isAllowDuplicateValues()}
 * <li> {@link #isAllowNullKey()}
 * <li> {@link #isAllowNullValue()}
 * </ul>
 *
 * <b>Fixture Methods</b>
 * <p>
 * For tests on modification operations (puts and removes), fixtures are used
 * to verify that that operation results in correct state for the map and its
 * collection views.  Basically, the modification is performed against your
 * map implementation, and an identical modification is performed against
 * a <I>confirmed</I> map implementation.  A confirmed map implementation is
 * something like <Code>java.util.HashMap</Code>, which is known to conform
 * exactly to the {@link Map} contract.  After the modification takes place
 * on both your map implementation and the confirmed map implementation, the
 * two maps are compared to see if their state is identical.  The comparison
 * also compares the collection views to make sure they're still the same.<P>
 *
 * The upshot of all that is that <I>any</I> test that modifies the map in
 * <I>any</I> way will verify that <I>all</I> of the map's state is still
 * correct, including the state of its collection views.  So for instance
 * if a key is removed by the map's key set's iterator, then the entry set
 * is checked to make sure the key/value pair no longer appears.<P>
 *
 * The {@link MapTest#map} field holds an instance of your collection implementation.
 * And the {@link MapTest#confirmed} field holds
 * an instance of the confirmed collection implementation.  The
 * {@link MapTest#resetEmpty()} and {@link MapTest#resetFull()} methods set these fields to
 * empty or full maps, so that tests can proceed from a known state.<P>
 *
 * After a modification operation to both {@link MapTest#map} and {@link MapTest#confirmed},
 * the {@link MapTest#verify()} method is invoked to compare the results.  The
 * {@link MapTest#verify} method calls separate methods to verify the map and its three
 * collection views ({@link MapTest#verifyMap}, {@link MapTest#verifyEntrySet},
 * {@link MapTest#verifyKeySet}, and {@link MapTest#verifyValues}).  You may want to override
 * one of the verification methods to perform additional verifications.  For
 * instance, TestDoubleOrderedMap would want override its
 * {@link MapTest#verifyValues()} method to verify that the values are unique and in
 * ascending order.<P>
 *
 * <b>Other Notes</b>
 * <p>
 * If your {@link Map} fails one of these tests by design, you may still use
 * this base set of cases.  Simply override the test case (method) your map
 * fails and/or the methods that define the assumptions used by the test
 * cases.  For example, if your map does not allow duplicate values, override
 * {@link #isAllowDuplicateValues()} and have it return {@code false}
 */
public abstract class AbstractMapTest<K, V> extends AbstractObjectTest {

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * support the {@code put} and {@code putAll} operations
     * adding new mappings.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support put adding.
     */
    public boolean isPutAddSupported() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * support the {@code put} and {@code putAll} operations
     * changing existing mappings.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support put changing.
     */
    public boolean isPutChangeSupported() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * support the {@code setValue} operation on entrySet entries.
     * <p>
     * Default implementation returns isPutChangeSupported().
     * Override if your collection class does not support setValue but does
     * support put changing.
     */
    public boolean isSetValueSupported() {
        return isPutChangeSupported();
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * support the {@code remove} and {@code clear} operations.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support removal operations.
     */
    public boolean isRemoveSupported() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * can cause structural modification on a get(). The example is LRUMap.
     * <p>
     * Default implementation returns false.
     * Override if your map class structurally modifies on get.
     */
    public boolean isGetStructuralModify() {
        return false;
    }

    /**
     * Returns whether the sub map views of SortedMap are serializable.
     * If the class being tested is based around a TreeMap then you should
     * override and return false as TreeMap has a bug in deserialization.
     *
     * @return false
     */
    public boolean isSubMapViewsSerializable() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * supports null keys.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support null keys.
     */
    public boolean isAllowNullKey() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * supports null values.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support null values.
     */
    public boolean isAllowNullValue() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * supports duplicate values.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support duplicate values.
     */
    public boolean isAllowDuplicateValues() {
        return true;
    }

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * provide fail-fast behavior on their various iterators.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support fast failure.
     */
    public boolean isFailFastExpected() {
        return true;
    }

    public boolean areEqualElementsDistinguishable() {
        return false;
    }

    /**
     *  Returns the set of keys in the mappings used to test the map.  This
     *  method must return an array with the same length as {@link
     *  #getSampleValues()} and all array elements must be different. The
     *  default implementation constructs a set of String keys, and includes a
     *  single null key if {@link #isAllowNullKey()} returns {@code true}.
     */
    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public K[] getSampleKeys() {
        final Object[] result = {
            "blah", "foo", "bar", "baz", "tmp", "gosh", "golly", "gee",
            "hello", "goodbye", "we'll", "see", "you", "all", "again",
            "key",
            "key2",
            isAllowNullKey() ? null : "nonnullkey"
        };
        return (K[]) result;
    }

    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public K[] getOtherKeys() {
        return (K[]) getOtherNonNullStringElements();
    }

    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public V[] getOtherValues() {
        return (V[]) getOtherNonNullStringElements();
    }

    /**
     * Returns a list of string elements suitable for return by
     * {@link #getOtherKeys()} or {@link #getOtherValues}.
     *
     * <p>Override getOtherElements to return the results of this method if your
     * collection does not support heterogeneous elements or the null element.
     * </p>
     */
    public Object[] getOtherNonNullStringElements() {
        return new Object[] {
            "For", "then", "despite", /* of */"space", "I", "would", "be", "brought",
            "From", "limits", "far", "remote", "where", "thou", "dost", "stay"
        };
    }

    /**
     * Returns the set of values in the mappings used to test the map.  This
     * method must return an array with the same length as
     * {@link #getSampleKeys()}.  The default implementation constructs a set of
     * String values and includes a single null value if
     * {@link #isAllowNullValue()} returns {@code true}, and includes
     * two values that are the same if {@link #isAllowDuplicateValues()} returns
     * {@code true}.
     */
    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public V[] getSampleValues() {
        final Object[] result = {
            "blahv", "foov", "barv", "bazv", "tmpv", "goshv", "gollyv", "geev",
            "hellov", "goodbyev", "we'llv", "seev", "youv", "allv", "againv",
            isAllowNullValue() ? null : "nonnullvalue",
            "value",
            isAllowDuplicateValues() ? "value" : "value2",
        };
        return (V[]) result;
    }

    /**
     * Returns a set of values that can be used to replace the values
     * returned from {@link #getSampleValues()}.  This method must return an
     * array with the same length as {@link #getSampleValues()}.  The values
     * returned from this method should not be the same as those returned from
     * {@link #getSampleValues()}.  The default implementation constructs a
     * set of String values and includes a single null value if
     * {@link #isAllowNullValue()} returns {@code true}, and includes two values
     * that are the same if {@link #isAllowDuplicateValues()} returns
     * {@code true}.
     */
    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public V[] getNewSampleValues() {
        final Object[] result = {
            isAllowNullValue() && isAllowDuplicateValues() ? null : "newnonnullvalue",
            "newvalue",
            isAllowDuplicateValues() ? "newvalue" : "newvalue2",
            "newblahv", "newfoov", "newbarv", "newbazv", "newtmpv", "newgoshv",
            "newgollyv", "newgeev", "newhellov", "newgoodbyev", "newwe'llv",
            "newseev", "newyouv", "newallv", "newagainv",
        };
        return (V[]) result;
    }

    /**
     *  Helper method to add all the mappings described by
     * {@link #getSampleKeys()} and {@link #getSampleValues()}.
     */
    public void addSampleMappings(final Map<? super K, ? super V> m) {

        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();

        for (int i = 0; i < keys.length; i++) {
            try {
                m.put(keys[i], values[i]);
            } catch (final NullPointerException exception) {
                assertTrue(keys[i] == null || values[i] == null,
                        "NullPointerException only allowed to be thrown " +
                                "if either the key or value is null.");

                assertTrue(keys[i] == null || !isAllowNullKey(),
                        "NullPointerException on null key, but " +
                                "isAllowNullKey is not overridden to return false.");

                assertTrue(values[i] == null || !isAllowNullValue(),
                        "NullPointerException on null value, but " +
                                "isAllowNullValue is not overridden to return false.");

                fail("Unknown reason for NullPointer.");
            }
        }
        assertEquals(keys.length, m.size(),
                "size must reflect number of mappings added.");
    }

    /**
     * Utility methods to create an array of Map.Entry objects
     * out of the given key and value arrays.<P>
     *
     * @param keys    the array of keys
     * @param values  the array of values
     * @return an array of Map.Entry of those keys to those values
     */
    @SuppressWarnings("unchecked")
    public Map.Entry<K, V>[] makeEntryArray(final K[] keys, final V[] values) {
        final Map.Entry<K, V>[] result = new Map.Entry[keys.length];
        for (int i = 0; i < keys.length; i++) {
            final Map<K, V> map = makeConfirmedMap();
            map.put(keys[i], values[i]);
            result[i] = map.entrySet().iterator().next();
        }
        return result;
    }

    /**
     * Return a new, empty {@link Map} to be used for testing.
     *
     * @return the map to be tested
     */
    @Override
    public abstract Map<K, V> makeObject();

    /**
     * Return a new, populated map.  The mappings in the map should match the
     * keys and values returned from {@link #getSampleKeys()} and
     * {@link #getSampleValues()}.  The default implementation uses makeEmptyMap()
     * and calls {@link #addSampleMappings} to add all the mappings to the
     * map.
     *
     * @return the map to be tested
     */
    public Map<K, V> makeFullMap() {
        final Map<K, V> m = makeObject();
        addSampleMappings(m);
        return m;
    }

    /**
     * Override to return a map other than HashMap as the confirmed map.
     *
     * @return a map that is known to be valid
     */
    public Map<K, V> makeConfirmedMap() {
        return new HashMap<>();
    }

    /**
     * Creates a new Map Entry that is independent of the first and the map.
     */
    public static <K, V> Map.Entry<K, V> cloneMapEntry(final Map.Entry<K, V> entry) {
        final HashMap<K, V> map = new HashMap<>();
        map.put(entry.getKey(), entry.getValue());
        return map.entrySet().iterator().next();
    }

    /**
     * Gets the compatibility version, needed for package access.
     */
    @Override
    public String getCompatibilityVersion() {
        return super.getCompatibilityVersion();
    }

    /**
     * Test to ensure the test setup is working properly.  This method checks
     * to ensure that the getSampleKeys and getSampleValues methods are
     * returning results that look appropriate.  That is, they both return a
     * non-null array of equal length.  The keys array must not have any
     * duplicate values, and may only contain a (single) null key if
     * isNullKeySupported() returns true.  The values array must only have a null
     * value if useNullValue() is true and may only have duplicate values if
     * isAllowDuplicateValues() returns true.
     */
    @Test
    public void testSampleMappings() {
        final Object[] keys = getSampleKeys();
        final Object[] values = getSampleValues();
        final Object[] newValues = getNewSampleValues();

        assertNotNull(keys, "failure in test: Must have keys returned from " +
                "getSampleKeys.");

        assertNotNull(values, "failure in test: Must have values returned from " +
                "getSampleValues.");

        // verify keys and values have equivalent lengths (in case getSampleX are
        // overridden)
        assertEquals(keys.length, values.length, "failure in test: not the same number of sample " +
                "keys and values.");

        assertEquals(values.length, newValues.length,
                "failure in test: not the same number of values and new values.");

        // verify there aren't duplicate keys, and check values
        for (int i = 0; i < keys.length - 1; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertTrue(keys[i] != null || keys[j] != null,
                        "failure in test: duplicate null keys.");
                assertTrue(keys[i] == null || keys[j] == null || !keys[i].equals(keys[j]) && !keys[j].equals(keys[i]),
                        "failure in test: duplicate non-null key.");
            }
            assertTrue(keys[i] != null || isAllowNullKey(),
                    "failure in test: found null key, but isNullKeySupported " + "is false.");
            assertTrue(values[i] != null || isAllowNullValue(),
                    "failure in test: found null value, but isNullValueSupported " + "is false.");
            assertTrue(newValues[i] != null || isAllowNullValue(),
                    "failure in test: found null new value, but isNullValueSupported " + "is false.");
            assertTrue(values[i] != newValues[i] && (values[i] == null || !values[i].equals(newValues[i])),
                    "failure in test: values should not be the same as new value");
        }
    }

    /**
     * Return a flag specifying the iteration behavior of the collection.
     * This is used to change the assertions used by specific tests.
     * The default implementation returns 0 which indicates ordered iteration behavior.
     *
     * @return the iteration behavior
     * @see AbstractCollectionTest#UNORDERED
     */
    protected int getIterationBehaviour(){
        return 0;
    }

    // tests begin here.  Each test adds a little bit of tested functionality.
    // Many methods assume previous methods passed.  That is, they do not
    // exhaustively recheck things that have already been checked in a previous
    // test methods.

    /**
     * Test to ensure that makeEmptyMap and makeFull returns a new non-null
     * map with each invocation.
     */
    @Test
    public void testMakeMap() {
        final Map<K, V> em = makeObject();
        assertNotNull(em, "failure in test: makeEmptyMap must return a non-null map.");

        final Map<K, V> em2 = makeObject();
        assertNotNull(em, "failure in test: makeEmptyMap must return a non-null map.");

        assertNotSame(em, em2, "failure in test: makeEmptyMap must return a new map " +
                "with each invocation.");

        final Map<K, V> fm = makeFullMap();
        assertNotNull(fm, "failure in test: makeFullMap must return a non-null map.");

        final Map<K, V> fm2 = makeFullMap();
        assertNotNull(fm2, "failure in test: makeFullMap must return a non-null map.");

        assertNotSame(fm, fm2, "failure in test: makeFullMap must return a new map " +
                "with each invocation.");
    }

    public MapTest makeMapTest() {
        return new MapTest();
    }

    @Nested
    public class MapTest extends AbstractNestedMapTest<K, V> {
        @Override
        public AbstractMapTest<K, V> outerTest() {
            return AbstractMapTest.this;
        }
    }

    /**
     * Bulk test {@link Map#entrySet()}.  This method runs through all of
     * the tests in {@link AbstractSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.
     */
    @Nested
    public class TestMapEntrySet extends AbstractNestedMapEntrySetTest<K, V> {
        @Override
        public AbstractMapTest<K, V> outerTest() {
            return AbstractMapTest.this;
        }
    }

    /**
     * Bulk test {@link Map#keySet()}.  This method runs through all of
     * the tests in {@link AbstractSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.
     */
    @Nested
    public class TestMapKeySet extends AbstractNestedMapKeySetTest<K, V> {
        @Override
        public AbstractMapTest<K, V> outerTest() {
            return AbstractMapTest.this;
        }
    }

    /**
     * Bulk test {@link Map#values()}.  This method runs through all of
     * the tests in {@link AbstractCollectionTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.
     */
    @Nested
    public class TestMapValues extends AbstractNestedMapValuesTest<K, V> {
        @Override
        public AbstractMapTest<K, V> outerTest() {
            return AbstractMapTest.this;
        }
    }

}
