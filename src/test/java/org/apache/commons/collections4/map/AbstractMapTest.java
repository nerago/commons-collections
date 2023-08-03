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

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.collection.AbstractCollectionTest;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.collections4.set.AbstractSetTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
 * The {@link #map} field holds an instance of your collection implementation.
 * The {@link #entrySet}, {@link #keySet} and {@link #values} fields hold
 * that map's collection views.  And the {@link #confirmed} field holds
 * an instance of the confirmed collection implementation.  The
 * {@link #resetEmpty()} and {@link #resetFull()} methods set these fields to
 * empty or full maps, so that tests can proceed from a known state.<P>
 *
 * After a modification operation to both {@link #map} and {@link #confirmed},
 * the {@link #verify()} method is invoked to compare the results.  The
 * {@link #verify} method calls separate methods to verify the map and its three
 * collection views ({@link #verifyMap}, {@link #verifyEntrySet},
 * {@link #verifyKeySet}, and {@link #verifyValues}).  You may want to override
 * one of the verification methods to perform additional verifications.  For
 * instance, TestDoubleOrderedMap would want override its
 * {@link #verifyValues()} method to verify that the values are unique and in
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
@SuppressWarnings("unchecked")
public abstract class AbstractMapTest<K, V> extends AbstractObjectTest {

    /**
     * JDK1.2 has bugs in null handling of Maps, especially HashMap.Entry.toString
     * This avoids nulls for JDK1.2
     */
    private static final boolean JDK12;
    static {
        final String str = System.getProperty("java.version");
        JDK12 = str.startsWith("1.2");
    }

    // These instance variables are initialized with the reset method.
    // Tests for map methods that alter the map (put, putAll, remove)
    // first call reset() to create the map and its views; then perform
    // the modification on the map; perform the same modification on the
    // confirmed; and then call verify() to ensure that the map is equal
    // to the confirmed, that the already-constructed collection views
    // are still equal to the confirmed's collection views.

    /** Map created by reset(). */
    protected Map<K, V> map;

    /** Entry set of map created by reset(). */
    protected Set<Map.Entry<K, V>> entrySet;

    /** Key set of map created by reset(). */
    protected Set<K> keySet;

    /** Values collection of map created by reset(). */
    protected Collection<V> values;

    /** HashMap created by reset(). */
    protected Map<K, V> confirmed;

    /**
     * JUnit constructor.
     *
     * @param testName  the test name
     */
    public AbstractMapTest(final String testName) {
        super(testName);
    }

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
     * support the {@code setValue} operation on entrySet entries
     * in toArray results and foreach functions.
     * <p>
     * Default implementation returns isSetValueSupported().
     * Override if your collection class does support setValue in some cases
     * but not where generally unneeded.
     */
    public boolean isSetValueInArraySupported() {
        return isSetValueSupported();
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
     * support the {@code remove} operation on values collection.
     * <p>
     * Default implementation returns isRemoveSupported.
     * Override if your collection class does not support removal operations.
     */
    public boolean isSpecificValueRemoveSupported() {
        return isRemoveSupported();
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

    /**
     * Returns true if the maps produced by
     * {@link #makeObject()} and {@link #makeFullMap()}
     * provide fail-fast behavior on newer functional interfaces.
     * <p>
     * Default implementation returns isFailFastExpected.
     * Override if your collection class does not support fast failure.
     */
    public boolean isFailFastFunctionalExpected() {
        return isFailFastExpected();
    }

    public boolean isTestFunctionalMethods() {
        return true;
    }

    public boolean areEqualElementsIndistinguishable() {
        return false;
    }

    /**
     * Does toString on the map return a value similar to "standard" JRE collections.
     * Since there's no specification can override freely if not relevant for some collection.
     * <p>
     * Default implementation returns true.
     * Override if your collection class does not support toString or has different format.
     */
    public boolean isToStringLikeCommonMaps() {
        return true;
    }

    public abstract CollectionCommonsRole collectionRole();

    public boolean isCopyConstructorCheckable() {
        return collectionRole() == CollectionCommonsRole.CONCRETE;
    }

    /**
     *  Returns the set of keys in the mappings used to test the map.  This
     *  method must return an array with the same length as {@link
     *  #getSampleValues()} and all array elements must be different. The
     *  default implementation constructs a set of String keys, and includes a
     *  single null key if {@link #isAllowNullKey()} returns {@code true}.
     */
    @SuppressWarnings("unchecked")
    public K[] getSampleKeys() {
        final Object[] result = {
            "blah", "foo", "bar", "baz", "tmp", "gosh", "golly", "gee",
            "hello", "goodbye", "we'll", "see", "you", "all", "again",
            "key",
            "key2",
            isAllowNullKey() && !JDK12 ? null : "nonnullkey"
        };
        return (K[]) result;
    }

    @SuppressWarnings("unchecked")
    public K[] getOtherKeys() {
        return (K[]) getOtherNonNullStringElements();
    }

    @SuppressWarnings("unchecked")
    public V[] getOtherValues() {
        return (V[]) getOtherNonNullStringElements();
    }

    @SuppressWarnings("unchecked")
    protected <E> List<E> getAsList(final Object[] o) {
        final ArrayList<E> result = new ArrayList<>();
        for (final Object element : o) {
            result.add((E) element);
        }
        return result;
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
    @SuppressWarnings("unchecked")
    public V[] getSampleValues() {
        final Object[] result = {
            "blahv", "foov", "barv", "bazv", "tmpv", "goshv", "gollyv", "geev",
            "hellov", "goodbyev", "we'llv", "seev", "youv", "allv", "againv",
            isAllowNullValue() && !JDK12 ? null : "nonnullvalue",
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
    @SuppressWarnings("unchecked")
    public V[] getNewSampleValues() {
        final Object[] result = {
            isAllowNullValue() && !JDK12 && isAllowDuplicateValues() ? null : "newnonnullvalue",
            "newvalue",
            isAllowDuplicateValues() ? "newvalue" : "newvalue2",
            "newblahv", "newfoov", "newbarv", "newbazv", "newtmpv", "newgoshv",
            "newgollyv", "newgeev", "newhellov", "newgoodbyev", "newwe'llv",
            "newseev", "newyouv", "newallv", "newagainv",
        };
        return (V[]) result;
    }

    public V getMissingEntryGetExpectValue() {
        return null;
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

    private void addSampleMappingsUnchecked(final Map<K, V> m) {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();
        for (int i = 0; i < keys.length; i++) {
            m.put(keys[i], values[i]);
        }
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

    @SuppressWarnings("unchecked")
    protected Map<K, V> makeObjectCopy(Map<K,V> map) {
        return (Map<K, V>) makeObjectCopy(map, Map.class);
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
     *
     * @return the iteration behavior
     * @see IterationBehaviour
     */
    protected abstract IterationBehaviour getIterationBehaviour();

    @Test
    public void testCollectionCheckRolesBasics() throws Exception {
        checkRoleBasics(makeObject(), collectionRole(), isTestSerialization());
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

    /**
     * Tests Map.isEmpty()
     */
    @Test
    public void testMapIsEmpty() {
        resetEmpty();
        assertTrue(getMap().isEmpty(), "Map.isEmpty() should return true with an empty map");
        verify();

        resetFull();
        assertFalse(getMap().isEmpty(), "Map.isEmpty() should return false with a non-empty map");
        verify();
    }

    /**
     * Tests Map.size()
     */
    @Test
    public void testMapSize() {
        resetEmpty();
        assertEquals(0, getMap().size(),
                "Map.size() should be 0 with an empty map");
        verify();

        resetFull();
        assertEquals(getSampleKeys().length, getMap().size(),
                "Map.size() should equal the number of entries " + "in the map");
        verify();
    }

    /**
     * Tests {@link Map#clear()}.  If the map {@link #isRemoveSupported()
     * can add and remove elements}, then {@link Map#size()} and
     * {@link Map#isEmpty()} are used to ensure that map has no elements after
     * a call to clear.  If the map does not support adding and removing
     * elements, this method checks to ensure clear throws an
     * UnsupportedOperationException.
     */
    @Test
    public void testMapClear() {
        if (!isRemoveSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().clear(),
                    "Expected UnsupportedOperationException on clear");
            return;
        }

        resetEmpty();
        getMap().clear();
        getConfirmed().clear();
        verify();

        resetFull();
        getMap().clear();
        getConfirmed().clear();
        verify();
    }

    /**
     * Tests Map.containsKey(Object) by verifying it returns false for all
     * sample keys on a map created using an empty map and returns true for
     * all sample keys returned on a full map.
     */
    @Test
    public void testMapContainsKey() {
        final Object[] keys = getSampleKeys();

        resetEmpty();
        for (final Object key : keys) {
            assertFalse(getMap().containsKey(key), "Map must not contain key when map is empty");
        }
        verify();

        resetFull();
        for (final Object key : keys) {
            assertTrue(getMap().containsKey(key), "Map must contain key for a mapping in the map. " +
                    "Missing: " + key);
        }
        for (final Object key : getOtherKeys()) {
            assertFalse(getMap().containsKey(key), "Map must not contain other key");
        }
        verify();
    }

    /**
     * Tests Map.containsValue(Object) by verifying it returns false for all
     * sample values on an empty map and returns true for all sample values on
     * a full map.
     */
    @Test
    public void testMapContainsValue() {
        final Object[] values = getSampleValues();

        resetEmpty();
        for (final Object value : values) {
            assertFalse(getMap().containsValue(value), "Empty map must not contain value");
        }
        verify();

        resetFull();
        for (final Object value : values) {
            assertTrue(getMap().containsValue(value),
                    "Map must contain value for a mapping in the map.");
        }
        for (final Object value : getOtherValues()) {
            assertFalse(getMap().containsValue(value), "Map must not contain other values");
        }
        verify();
    }


    /**
     * Tests Map.equals(Object)
     */
    @Test
    public void testMapEquals() {
        resetEmpty();
        assertEquals(getMap(), confirmed, "Empty maps unequal.");
        verify();

        resetFull();
        assertEquals(getMap(), confirmed, "Full maps unequal.");
        verify();

        resetFull();
        // modify the HashMap created from the full map and make sure this
        // change results in map.equals() to return false.
        final Iterator<K> iter = confirmed.keySet().iterator();
        iter.next();
        iter.remove();
        assertFalse(getMap().equals(confirmed), "Different maps equal.");

        resetFull();
        assertFalse(getMap().equals(null), "equals(null) returned true.");
        assertFalse(getMap().equals(new Object()), "equals(new Object()) returned true.");
        verify();
    }

    /**
     * Tests Map.get(Object)
     */
    @Test
    public void testMapGet() {
        final Object[] keys = getSampleKeys();
        final Object[] otherKeys = getOtherKeys();
        final Object[] values = getSampleValues();
        final Object expectMissing = getMissingEntryGetExpectValue();

        resetEmpty();
        for (final Object key : keys) {
            assertEquals(expectMissing, getMap().get(key), "Empty map.get() should return null.");
        }
        if (!isGetStructuralModify())
            verify();

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], getMap().get(keys[i]),
                    "Full map.get() should return value from mapping.");
        }
        for (final Object key : otherKeys) {
            assertEquals(expectMissing, getMap().get(key), "Other keys with map.get() should return null.");
        }
        if (!isGetStructuralModify())
            verify();
    }

    @Test
    public void testMapGetOrDefault() {
        final Object[] keys = getSampleKeys();
        final Object[] otherKeys = getOtherKeys();
        final Object[] values = getSampleValues();
        final V missingValue = (V) "abc";

        resetEmpty();
        for (final Object key : keys) {
            assertEquals(missingValue, getMap().getOrDefault(key, missingValue),
                    "getOrDefault should always return parameter for missing");
        }
        verify();

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], getMap().getOrDefault(keys[i], missingValue),
                    "Full map.getOrDefault() should return value from mapping.");
        }
        for (final Object key : otherKeys) {
            assertEquals(missingValue, getMap().getOrDefault(key, missingValue),
                    "getOrDefault should always return parameter for missing");
        }
        verify();
    }

    @Test
    public void testMapPutIfAbsent() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        if (!isTestFunctionalMethods()) {
            return;
        }
        if (!isPutAddSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().putIfAbsent(otherKeys[0], otherValues[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            assertNull(getMap().putIfAbsent(keys[i], values[i]),
                    "putIfAbsent should always return null for missing");
            getConfirmed().put(keys[i], values[i]);
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            V oldValue = values[i];
            V replaceValue = newValues[i];
            if (oldValue != null) {
                assertEquals(oldValue, getMap().putIfAbsent(key, replaceValue),
                        "putIfAbsent should return existing value from map.");
            } else if (replaceValue != null) {
                assertNull(getMap().putIfAbsent(key, replaceValue),
                        "putIfAbsent should return existing value from map.");
                getConfirmed().put(key, replaceValue);
            } else {
                assertFalse(getMap().containsKey(key));
                assertNull(getMap().putIfAbsent(key, null), "putIfAbsent should return existing value from map.");
                assertFalse(getMap().containsKey(key));
            }
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            assertNull(getMap().putIfAbsent(otherKeys[i], otherValues[i]),
                    "putIfAbsent should always return null for missing");
            getConfirmed().put(otherKeys[i], otherValues[i]);
            verify();
        }
    }

    @Test
    public void testMapComputeIfAbsent() {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();

        if (!isTestFunctionalMethods()) {
            return;
        }
        if (!isPutAddSupported()) {
            resetEmpty();
            assertThrows(UnsupportedOperationException.class, () -> getMap().computeIfAbsent(keys[0], k -> values[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V value = values[i];
            final Function<? super K, ? extends V> mappingFunction = k -> value;
            if (value != null) {
                assertEquals(value, getMap().computeIfAbsent(key, mappingFunction),
                        "computeIfAbsent should always return mapped value for missing");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, value);
            } else {
                assertNull(getMap().computeIfAbsent(key, mappingFunction),
                        "computeIfAbsent should always return mapped value for missing");
                assertFalse(getMap().containsKey(key));
            }
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            if (oldValue == null && replaceValue == null) {
                final Function<? super K, ? extends V> mappingFunction = k -> null;
                assertNull(getMap().computeIfAbsent(key, mappingFunction),
                        "computeIfAbsent should return existing value from map.");
                assertTrue(getMap().containsKey(key));
            } else if (oldValue == null) {
                final Function<? super K, ? extends V> mappingFunction = k -> replaceValue;
                assertEquals(replaceValue, getMap().computeIfAbsent(key, mappingFunction),
                        "computeIfAbsent should return new value from function");
                getConfirmed().put(key, replaceValue);
            } else {
                final Function<? super K, ? extends V> mappingFunction = k -> fail("should not call");
                assertEquals(oldValue, getMap().computeIfAbsent(key, mappingFunction),
                        "computeIfAbsent should return existing value from map.");
            }
            verify();
        }
        final K[] otherKeys = getOtherKeys();
        final V[] otherValues = getOtherValues();
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V value = otherValues[i];
            final Function<? super K, ? extends V> mappingFunction = k -> value;
            assertEquals(value, getMap().computeIfAbsent(key, mappingFunction),
                    "computeIfAbsent should always return mapped value for missing");
            assertEquals(value, getMap().get(key));
            getConfirmed().put(key, value);
            verify();
        }

        resetEmpty();
        assertThrows(ArithmeticException.class,
                () -> getMap().computeIfAbsent(getOtherKeys()[0], k -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().computeIfAbsent(getOtherKeys()[0], null));
        verify();
        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            resetFull();
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfAbsent(getOtherKeys()[0], e -> getMap().put(getOtherKeys()[1], getOtherValues()[1])));
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfAbsent(getOtherKeys()[2], e -> getMap().remove(getSampleKeys()[0])));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfAbsent(getOtherKeys()[3], e -> { getMap().clear(); return getOtherValues()[0];} ));
        }
    }

    @Test
    public void testMapComputeIfPresent() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();

        if (!isTestFunctionalMethods()) {
            return;
        }
        if (!isPutChangeSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().computeIfPresent(keys[0], (k, v) -> newValues[0]));
            verify();
            return;
        }

        resetEmpty();
        for (final K key : keys) {
            final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                fail("shouldn't call func");
                return v;
            };
            assertNull(getMap().computeIfPresent(key, mappingFunction),
                    "computeIfPresent should return null for missing");
            assertFalse(getMap().containsKey(key));
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            if (oldValue == null) {
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    fail("shouldn't call func");
                    return v;
                };
                assertNull(getMap().computeIfPresent(key, mappingFunction),
                        "computeIfPresent should return null for missing");
                if (isAllowNullValue())
                    assertTrue(getMap().containsKey(key), "still should be in map if null supported");
            } else if (replaceValue == null) {
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(oldValue, v);
                    return null;
                };
                if (isRemoveSupported()) {
                    assertNull(getMap().computeIfPresent(key, mappingFunction),
                            "computeIfPresent should return the new value");
                    assertFalse(getMap().containsKey(key), "should be removed from map");
                    getConfirmed().remove(key);
                } else {
                    assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                            () -> getMap().computeIfPresent(key, mappingFunction),
                            "computeIfPresent should throw for remove attempt, without any change");
                }
            } else {
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(oldValue, v);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().computeIfPresent(key, mappingFunction),
                        "computeIfPresent should return the new value");
                getConfirmed().put(key, replaceValue);

                assertThrows(ArithmeticException.class,
                        () -> getMap().computeIfPresent(key, (k, v) -> { throw new ArithmeticException(); }));
                verify();
            }
            verify();
        }
        for (final K key : otherKeys) {
            final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                fail("shouldn't call func");
                return v;
            };
            assertNull(getMap().computeIfPresent(key, mappingFunction),
                    "computeIfPresent should return null for missing");
            verify();
        }

        resetFull();
        assertThrows(ArithmeticException.class,
                () -> getMap().computeIfPresent(getSampleKeys()[0], (k, v) -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().computeIfPresent(getSampleKeys()[0], null));
        verify();
        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            resetFull();
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfPresent(getSampleKeys()[0], (k, v) -> getMap().put(getOtherKeys()[0], getOtherValues()[0])));
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfPresent(getSampleKeys()[1], (k, v) -> getMap().remove(getSampleKeys()[3])));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().computeIfPresent(getSampleKeys()[2], (k, v) -> { getMap().clear(); return getOtherValues()[0];} ));
        }
    }

    @Test
    public void testMapCompute() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        if (!isTestFunctionalMethods()) {
            return;
        }
        if (!isPutAddSupported() || !isPutChangeSupported() || !isRemoveSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().compute(keys[0], (k, v) -> newValues[0]));
            assertThrows(UnsupportedOperationException.class, () -> getMap().compute(keys[0], (k, v) -> null));
            assertThrows(UnsupportedOperationException.class, () -> getMap().compute(otherKeys[0], (k, v) -> otherValues[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            if (i % 2 == 0) {
                // leave every second absent
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertNull(v);
                    return null;
                };
                assertNull(getMap().compute(key, mappingFunction), "compute should return null for missing");
                assertFalse(getMap().containsKey(key));
            } else if (replaceValue != null && isPutAddSupported()) {
                // add new value
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertNull(v);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().compute(key, mappingFunction),
                        "compute should return new value from mapping function");
                assertEquals(replaceValue, getMap().get(key));
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
            }
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V originalValue = values[i];
            final V replaceValue = newValues[i];
            if ((i % 2 == 0 || replaceValue == null) && isRemoveSupported()) {
                // delete every second
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(originalValue, v);
                    return null;
                };
                assertTrue(getMap().containsKey(key));
                assertNull(getMap().compute(key, mappingFunction), "compute should return null for remove");
                assertFalse(getMap().containsKey(key));
                getConfirmed().remove(key);
                verify();
            } else if (replaceValue != null && isPutChangeSupported()) {
                // change value
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(originalValue, v);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().compute(key, mappingFunction),
                        "compute should return new value from mapping function");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify();

                assertThrows(ArithmeticException.class,
                        () -> getMap().compute(key, (k, v) -> { throw new ArithmeticException(); }));
                verify();
            }
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            if (i % 2 == 1) {
                // leave every second absent
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertNull(v);
                    return null;
                };
                assertNull(getMap().compute(key, mappingFunction), "compute should return null for missing");
                assertFalse(getMap().containsKey(key));
                verify();
            } else if (replaceValue != null && isPutAddSupported()) {
                // add new value
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertNull(v);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().compute(key, mappingFunction),
                        "compute should return new value from mapping function");
                assertEquals(replaceValue, getMap().get(key));
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify();

                assertThrows(ArithmeticException.class,
                        () -> getMap().compute(key, (k, v) -> { throw new ArithmeticException(); }));
                verify();
            }
        }

        resetFull();
        assertThrows(ArithmeticException.class,
                () -> getMap().compute(getSampleKeys()[0], (k, v) -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().compute(getSampleKeys()[0], null));
        verify();
        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            resetEmpty();
            assertThrows(ConcurrentModificationException.class, () -> getMap().compute(getSampleKeys()[0], (k, v) -> {
                getMap().put(getOtherKeys()[0], getOtherValues()[0]);
                return getSampleValues()[0];
            }));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            resetFull();
            assertThrows(ConcurrentModificationException.class, () -> getMap().compute(getOtherKeys()[0], (k, v) -> {
                getMap().remove(getSampleKeys()[0]);
                return getOtherValues()[0];
            }));
            assertThrows(ConcurrentModificationException.class, () -> getMap().compute(getSampleKeys()[0], (k, v) -> {
                getMap().clear();
                return getOtherValues()[0];
            }));
        }
    }

    @Test
    public void testMapMerge() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();
        final V dummy = otherValues[0];

        if (!isTestFunctionalMethods()) {
            return;
        }
        if (!isPutAddSupported() || !isPutChangeSupported() || !isRemoveSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().merge(keys[0], newValues[0], (k, v) -> newValues[1]));
            assertThrows(UnsupportedOperationException.class, () -> getMap().merge(keys[0], newValues[0], (k, v) -> null));
            assertThrows(UnsupportedOperationException.class, () -> getMap().merge(otherKeys[0], newValues[0], (k, v) -> fail()));
            verify();
            return;
        }

        resetEmpty();
        // since merge replaceValue is a not null parameter there's no leave missing entry unchanged path like the other tests
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            if (replaceValue != null && isPutAddSupported()) {
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    fail("shouldn't call func");
                    return null;
                };
                assertEquals(replaceValue, getMap().merge(key, replaceValue, mappingFunction),
                        "merge should return new value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
            }
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V originalValue = values[i];
            final V replaceValue = newValues[i];
            if ((i % 2 == 0 || replaceValue == null) && isRemoveSupported()) {
                // delete every second
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(originalValue, v);
                    assertEquals(dummy, p);
                    return null;
                };
                assertTrue(getMap().containsKey(key));
                assertNull(getMap().merge(key, dummy, mappingFunction), "merge should return null after remove");
                assertFalse(getMap().containsKey(key));
                getConfirmed().remove(key);
            } else if (originalValue == null && replaceValue != null && isPutChangeSupported()) {
                // change value
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    fail("shouldn't call func");
                    return null;
                };
                assertNull(getMap().get(key));
                assertTrue(getMap().containsKey(key));
                assertEquals(replaceValue, getMap().merge(key, replaceValue, mappingFunction),
                        "merge should return new value from param");
                assertEquals(replaceValue, getMap().get(key));
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
            } else if (isPutChangeSupported() && replaceValue != null) {
                // change value
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(originalValue, v);
                    assertEquals(dummy, p);
                    return replaceValue;
                };
                assertEquals(originalValue, getMap().get(key));
                assertEquals(replaceValue, getMap().merge(key, dummy, mappingFunction),
                        "merge should return new value from mapping function");
                assertEquals(replaceValue, getMap().get(key));
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);

                assertThrows(ArithmeticException.class,
                        () -> getMap().merge(key, dummy, (v, p) -> { throw new ArithmeticException(); }));
            }
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            if (replaceValue != null && isPutAddSupported()) { // not null parameter
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    fail("shouldn't call func");
                    return null;
                };
                assertEquals(replaceValue, getMap().merge(key, replaceValue, mappingFunction), "merge should return new value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
            }
            verify();
        }

        resetFull();
        assertThrows(ArithmeticException.class,
                () -> getMap().merge(getSampleKeys()[0], dummy, (k, v) -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().merge(getSampleKeys()[0], dummy, null));
        assertThrows(NullPointerException.class,
                () -> getMap().merge(getOtherKeys()[0], null, (k, v) -> v));
        verify();
        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            resetFull();
            assertThrows(ConcurrentModificationException.class, () -> getMap().merge(getSampleKeys()[0], dummy, (k, v) -> {
                getMap().put(getOtherKeys()[0], getOtherValues()[0]);
                return getNewSampleValues()[0];
            }));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            resetFull();
            assertThrows(ConcurrentModificationException.class, () -> getMap().merge(getSampleKeys()[0], dummy, (k, v) -> {
                getMap().remove(getSampleKeys()[0]);
                return null;
            }));
            resetFull();
            assertThrows(ConcurrentModificationException.class, () -> getMap().merge(getSampleKeys()[0], dummy, (k, v) -> {
                getMap().clear();
                return null;
            }));
        }
    }
    
    @Test
    public void testMapRemove2() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        if (!isRemoveSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().remove(keys[0], values[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V notValue = newValues[i];
            assertFalse(getMap().remove(key, notValue), "remove should do nothing on empty");
            assertFalse(getMap().containsKey(key));
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V notValue = newValues[i];
            assertFalse(getMap().remove(key, notValue), "remove should do nothing on wrong value");
            assertTrue(getMap().containsKey(key));
            verify();
            assertTrue(getMap().remove(key, oldValue), "remove should return old value");
            assertFalse(getMap().containsKey(key));
            getConfirmed().remove(key);
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V notValue = otherValues[i];
            assertFalse(getMap().remove(key, notValue), "remove should do nothing on empty");
            assertFalse(getMap().containsKey(key));
            verify();
        }
    }
    
    @Test
    public void testMapReplace2() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        if (!isPutChangeSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().replace(keys[0], newValues[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            assertNull(getMap().replace(key, replaceValue),
                    "replace should do nothing on empty");
            assertFalse(getMap().containsKey(key));
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            assertEquals(oldValue, getMap().replace(key, replaceValue),
                    "replace should return old value");
            assertTrue(getMap().containsKey(key));
            getConfirmed().put(key, replaceValue);
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            assertNull(getMap().replace(key, replaceValue), "replace should do nothing for for missing");
            assertFalse(getMap().containsKey(key));
            verify();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapReplace3() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();
        final V dummy = (V) "xyz";

        if (!isPutChangeSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class,
                    () -> getMap().replace(keys[0], values[0], newValues[0]));
            verify();
            return;
        }

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            assertFalse(getMap().replace(key, oldValue, replaceValue),
                    "replace should do nothing on empty");
            assertFalse(getMap().containsKey(key));
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            if (!Objects.equals(oldValue, replaceValue)) {
                assertFalse(getMap().replace(key, replaceValue, oldValue),
                        "replace should return false wrong value");
                verify(); // no change expected

                assertTrue(getMap().replace(key, oldValue, replaceValue),
                    "replace should return true");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify(); // change should match

                assertFalse(getMap().replace(key, oldValue, replaceValue),
                        "replace should return false since that's no longer current");
                verify(); // no change expected
            }
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            assertFalse(getMap().replace(key, replaceValue, dummy), "replace should do nothing for for missing");
            assertFalse(getMap().containsKey(key));
            verify();
        }
    }

    @Test
    public void testForeach() {
        resetEmpty();
        HashMap<K, V> seen = new HashMap<>();
        getMap().forEach((k, v) -> seen.put(k, v));
        assertEquals(makeConfirmedMap(), seen);

        resetFull();
        seen.clear();
        getMap().forEach((k, v) -> seen.put(k, v));
        assertEquals(makeConfirmedFullMap(), seen);
    }

    @Test
    public void testForeachErrors() {
        resetFull();

        resetFull();
        assertThrows(ArithmeticException.class,
                () -> getMap().forEach((k, v) -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().forEach(null));
        verify();

        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().forEach((k, v) -> getMap().put(getOtherKeys()[0], getOtherValues()[0])));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().forEach((k, v) -> getMap().remove(getSampleKeys()[0])));
            assertThrows(ConcurrentModificationException.class,
                    () -> getMap().forEach((k, v) -> getMap().clear()));
        }
    }

    @Test
    public void testReplaceAll() {
        if (!isSetValueSupported())
            return;

        final Queue<V> newValueQueueMap = new LinkedList<>(Arrays.asList(getNewSampleValues()));

        resetFull();
        final HashMap<K, V> seen = new HashMap<>();
        final HashMap<K, V> after = new HashMap<>();
        getMap().replaceAll((k, v) -> {
            seen.put(k, v);
            V replace = newValueQueueMap.poll();
            after.put(k, replace);
            return replace;
        });
        assertEquals(makeConfirmedFullMap(), seen);

        // can't use same sequence of values on confirmed since contract doesn't guarantee order
        // just use lookup
        getConfirmed().replaceAll((k, v) -> after.get(k));
        verify();
    }

    @Test
    public void testReplaceAllErrors() {
        if (!isSetValueSupported())
            return;

        resetFull();
        assertThrows(ArithmeticException.class,
                () -> getMap().replaceAll((k, v) -> { throw new ArithmeticException(); }));
        assertThrows(NullPointerException.class,
                () -> getMap().replaceAll(null));
        verify();

        if (isFailFastFunctionalExpected() && isPutAddSupported()) {
            assertThrows(ConcurrentModificationException.class, () -> getMap().replaceAll((k, v) -> {
                getMap().put(getOtherKeys()[0], getOtherValues()[0]);
                return v;
            }));
        }
        if (isFailFastFunctionalExpected() && isRemoveSupported()) {
            assertThrows(ConcurrentModificationException.class, () -> getMap().replaceAll((k, v) -> {
                getMap().remove(getSampleKeys()[0]);
                return v;
            }));
            assertThrows(ConcurrentModificationException.class, () -> getMap().replaceAll((k, v) -> {
                getMap().clear();
                return v;
            }));
        }
    }

    /**
     * Tests Map.hashCode()
     */
    @Test
    public void testMapHashCode() {
        resetEmpty();
        assertEquals(getMap().hashCode(), confirmed.hashCode(), "Empty maps have different hashCodes.");

        resetFull();
        assertEquals(getMap().hashCode(), confirmed.hashCode(), "Equal maps have different hashCodes.");
    }

    /**
     * Tests Map.toString().  Since the format of the string returned by the
     * toString() method is not defined in the Map interface, there is no
     * common way to test the results of the toString() method.  Therefore,
     * it is encouraged that Map implementations override this test with one
     * that checks the format matches any format defined in its API.  This
     * default implementation just verifies that the toString() method does
     * not return null.
     */
    @Test
    @Disabled
    public void testMapToString() {
        resetEmpty();
        assertNotNull(getMap().toString(), "Empty map toString() should not return null");
        String confirmedString = getConfirmed().toString(), mapString = getMap().toString();
        if (isToStringLikeCommonMaps() && stringsHaveDifferentTokens(confirmedString, mapString)) {
            fail("toString from map is not standard\nexpected: " + confirmedString + "\nactual: " + mapString);
        }

        verify();

        resetFull();
        assertNotNull(getMap().toString(), "Full map toString() should not return null");
        confirmedString = getConfirmed().toString();
        mapString = getMap().toString();
        if (isToStringLikeCommonMaps() && stringsHaveDifferentTokens(confirmedString, mapString)) {
            fail("toString from map is not standard\nexpected: " + confirmedString + "\nactual: " + mapString);
        }
        verify();
    }

    private boolean stringsHaveDifferentTokens(String strA, String strB) {
        Pattern pattern = Pattern.compile("(\\{|, |})");
        String[] partsA = pattern.split(strA);
        String[] partsB = pattern.split(strB);
        Arrays.sort(partsA);
        Arrays.sort(partsB);
        return !Arrays.equals(partsA, partsB);
    }

    /**
     * Compare the current serialized form of the Map
     * against the canonical version in SCM.
     */
    @Test
    public void testEmptyMapCompatibility() throws Exception {
        confirmed = makeObject();
        if (!(confirmed instanceof Serializable) || skipSerializedCanonicalTests() || !isTestSerialization()) {
            return;
        }

        // Create canonical objects with this line
        // writeExternalFormToDisk((Serializable) confirmed, getCanonicalEmptyCollectionName(confirmed));

        // test to make sure the canonical form has been preserved
        map = (Map<K, V>) readExternalFormFromDisk(getCanonicalEmptyCollectionName(confirmed));
        assertEquals(0, map.size(), "Map is empty");
        assertEquals(confirmed.getClass(), map.getClass(), "serialized test data doesn't produce same type");
        views();
        verify();
    }

    /**
     * Compare the current serialized form of the Map
     * against the canonical version in SCM.
     */
    @Test
    public void testFullMapCompatibility() throws Exception {
        confirmed = makeFullMap();
        if (!(confirmed instanceof Serializable) || skipSerializedCanonicalTests() || !isTestSerialization()) {
            return;
        }

        // Create canonical objects with this line
        // writeExternalFormToDisk((Serializable) confirmed, getCanonicalFullCollectionName(confirmed));

        // test to make sure the canonical form has been preserved
        map = (Map<K, V>) readExternalFormFromDisk(getCanonicalFullCollectionName(confirmed));
        assertEquals(getSampleKeys().length, map.size(), "Map is the right size");
        assertEquals(confirmed.getClass(), map.getClass(), "serialized test data doesn't produce same type");
        views();
        verify();
    }

    /**
     * Tests Map.put(Object, Object)
     */
    @Test
    public void testMapPut() {
        resetEmpty();
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();

        if (isPutAddSupported()) {
            for (int i = 0; i < keys.length; i++) {
                final Object o = getMap().put(keys[i], values[i]);
                getConfirmed().put(keys[i], values[i]);
                verify();
                assertNull(o, "First map.put should return null");
                assertTrue(getMap().containsKey(keys[i]),
                        "Map should contain key after put");
                assertTrue(getMap().containsValue(values[i]),
                        "Map should contain value after put");
            }
            if (isPutChangeSupported()) {
                for (int i = 0; i < keys.length; i++) {
                    final Object o = getMap().put(keys[i], newValues[i]);
                    getConfirmed().put(keys[i], newValues[i]);
                    verify();
                    assertEquals(values[i], o, "Map.put should return previous value when changed");
                    assertTrue(getMap().containsKey(keys[i]),
                            "Map should still contain key after put when changed");
                    assertTrue(getMap().containsValue(newValues[i]),
                            "Map should contain new value after put when changed");

                    // if duplicates are allowed, we're not guaranteed that the value
                    // no longer exists, so don't try checking that.
                    if (!isAllowDuplicateValues()) {
                        assertFalse(getMap().containsValue(values[i]), "Map should not contain old value after put when changed");
                    }
                }
            } else {
                assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                        ()->getMap().put(keys[0], newValues[0]),
                        "Expected IllegalArgumentException or UnsupportedOperationException on put (change)");
            }

        } else if (isPutChangeSupported()) {
            resetEmpty();
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    ()->getMap().put(keys[0], values[0]),
                    "Expected UnsupportedOperationException or IllegalArgumentException on put (add) when fixed size");

            resetFull();
            int i = 0;
            for (final Iterator<K> it = getMap().keySet().iterator(); it.hasNext() && i < newValues.length; i++) {
                final K  key = it.next();
                final V o = getMap().put(key, newValues[i]);
                final V value = getConfirmed().put(key, newValues[i]);
                verify();
                assertEquals(value, o, "Map.put should return previous value when changed");
                assertTrue(getMap().containsKey(key),
                        "Map should still contain key after put when changed");
                assertTrue(getMap().containsValue(newValues[i]),
                        "Map should contain new value after put when changed");

                // if duplicates are allowed, we're not guaranteed that the value
                // no longer exists, so don't try checking that.
                if (!isAllowDuplicateValues()) {
                    assertFalse(getMap().containsValue(values[i]), "Map should not contain old value after put when changed");
                }
            }
        } else {
            assertThrows(UnsupportedOperationException.class, () -> getMap().put(keys[0], values[0]),
                    "Expected UnsupportedOperationException on put (add)");
        }
    }

    /**
     * Tests Map.put(null, value)
     */
    @Test
    public void testMapPutNullKey() {
        resetFull();
        final V[] values = getSampleValues();

        if (isPutAddSupported()) {
            if (isAllowNullKey()) {
                getMap().put(null, values[0]);
            } else {
                assertThrowsEither(NullPointerException.class, IllegalArgumentException.class,
                        () -> getMap().put(null, values[0]),
                        "put(null, value) should throw NPE/IAE");
            }
        }
    }

    /**
     * Tests Map.put(null, value)
     */
    @Test
    public void testMapPutNullValue() {
        resetFull();
        final K[] keys = getSampleKeys();

        if (isPutAddSupported()) {
            if (isAllowNullValue()) {
                getMap().put(keys[0], null);
            } else {
                assertThrowsEither(NullPointerException.class, IllegalArgumentException.class,
                        () -> getMap().put(keys[0], null),
                                "put(null, value) should throw NPE/IAE");
            }
        }
    }

    /**
     * Tests Map.putAll(map)
     */
    @Test
    public void testMapPutAll() {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final K[] otherKeys = getOtherKeys();
        final V[] otherValues = getOtherValues();

        if (isPutAddSupported() || isPutChangeSupported()) {
            // check putAll OK adding empty map to empty map
            resetEmpty();
            assertEquals(0, getMap().size());
            getMap().putAll(new HashMap<K, V>());
            assertEquals(0, getMap().size());
            verify();

            // check putAll OK adding empty map to non-empty map
            resetFull();
            getMap().putAll(new HashMap<K, V>());
            verify();

            // check putAll OK adding JDK map with current values
            resetFull();
            final Map<K, V> m1 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m1.put(keys[i], values[i]);
            }
            getMap().putAll(m1);
            getConfirmed().putAll(m1);
            verify();
        } else {
            // check putAll rejects adding empty map to empty map
            resetEmpty();
            assertEquals(0, getMap().size());
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(new HashMap<>()),
                    "Expected UnsupportedOperationException on putAll");
            verify();

            // check putAll rejects adding empty map to non-empty map
            resetFull();
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(new HashMap<>()),
                    "Expected UnsupportedOperationException on putAll");
            verify();

            // check putAll rejects adding map to itself
            resetFull();
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(getMap()),
                    "Expected UnsupportedOperationException on putAll");
            verify();

            // check putAll rejects adding JDK map with current values
            resetFull();
            final Map<K, V> m1 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m1.put(keys[i], values[i]);
            }
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m1),
                    "Expected UnsupportedOperationException on putAll");
            verify();
        }

        if (isPutAddSupported()) {
            // check putAll OK adding non-empty map to empty map
            resetEmpty();
            final Map<K, V> m2 = makeFullMap();
            getMap().putAll(m2);
            getConfirmed().putAll(m2);
            verify();

            // check putAll OK adding non-empty JDK map to empty map
            resetEmpty();
            final Map<K, V> m3 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m3.put(keys[i], values[i]);
            }
            getMap().putAll(m3);
            getConfirmed().putAll(m3);
            verify();

            // check putAll OK adding non-empty JDK map to non-empty map
            resetFull();
            final Map<K, V> m4 = makeConfirmedMap();
            for (int i = 0; i < otherKeys.length; i++) {
                m4.put(otherKeys[i], otherValues[i]);
            }
            getMap().putAll(m4);
            getConfirmed().putAll(m4);
            verify();
        } else {
            // check putAll rejects adding non-empty map to empty map
            resetEmpty();
            final Map<K, V> m2 = makeFullMap();

            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m2),
                    "Expected IllegalArgumentException on putAll");
            verify();

            // check putAll rejects adding non-empty JDK map to empty map
            resetEmpty();
            final Map<K, V> m3 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m3.put(keys[i], values[i]);
            }
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m3),
                    "Expected IllegalArgumentException on putAll");
            verify();

            // check putAll rejects adding non-empty JDK map to non-empty map
            resetFull();
            final Map<K, V> m4 = makeConfirmedMap();
            for (int i = 0; i < otherKeys.length; i++) {
                m4.put(otherKeys[i], otherValues[i]);
            }
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m4),
                    "Expected IllegalArgumentException on putAll");
            verify();
        }

        if (isPutChangeSupported()) {
            // check putAll OK adding one changed value
            resetFull();
            final Map<K, V> m5 = makeConfirmedMap();
            m5.put(keys[0], newValues[0]);
            getMap().putAll(m5);
            getConfirmed().putAll(m5);
            verify();

            // check putAll OK adding changed values
            resetFull();
            final Map<K, V> m6 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m6.put(keys[i], newValues[i]);
            }
            getMap().putAll(m6);
            getConfirmed().putAll(m6);
            verify();
        } else {
            // check putAll rejects adding one changed value
            resetFull();
            final Map<K, V> m5 = makeConfirmedMap();
            m5.put(keys[0], newValues[0]);
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m5),
                    "Expected IllegalArgumentException on putAll");
            verify();

            // check putAll rejects adding changed values
            resetFull();
            final Map<K, V> m6 = makeConfirmedMap();
            for (int i = 0; i < keys.length; i++) {
                m6.put(keys[i], newValues[i]);
            }
            assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m6),
                    "Expected IllegalArgumentException on putAll");
            verify();
        }
    }

    /**
     * Tests Map.remove(Object)
     */
    @Test
    public void testMapRemove() {
        if (!isRemoveSupported()) {
            resetFull();
            assertThrows(UnsupportedOperationException.class, () -> getMap().remove(getMap().keySet().iterator().next()),
                    "Expected UnsupportedOperationException on remove");
            return;
        }

        resetEmpty();

        final Object[] keys = getSampleKeys();
        final Object[] values = getSampleValues();
        for (final Object key : keys) {
            final Object o = getMap().remove(key);
            assertNull(o, "First map.remove should return null");
        }
        verify();

        resetFull();

        for (int i = 0; i < keys.length; i++) {
            final Object o = getMap().remove(keys[i]);
            getConfirmed().remove(keys[i]);
            verify();

            assertEquals(values[i], o,
                    "map.remove with valid key should return value");
        }

        final Object[] other = getOtherKeys();

        resetFull();
        final int size = getMap().size();
        for (final Object element : other) {
            final Object o = getMap().remove(element);
            assertNull(o, "map.remove for nonexistent key should return null");
            assertEquals(size, getMap().size(),
                    "map.remove for nonexistent key should not " + "shrink map");
        }
        verify();
    }

    /**
     * Tests that the {@link Map#values} collection is backed by
     * the underlying map for clear().
     */
    @Test
    public void testValuesClearChangesMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // clear values, reflected in map
        resetFull();
        Collection<V> values = getMap().values();
        assertFalse(getMap().isEmpty());
        assertFalse(values.isEmpty());
        values.clear();
        assertTrue(getMap().isEmpty());
        assertTrue(values.isEmpty());

        // clear map, reflected in values
        resetFull();
        values = getMap().values();
        assertFalse(getMap().isEmpty());
        assertFalse(values.isEmpty());
        getMap().clear();
        assertTrue(getMap().isEmpty());
        assertTrue(values.isEmpty());
    }

    /**
     * Tests that the {@link Map#keySet} collection is backed by
     * the underlying map for clear().
     */
    @Test
    public void testKeySetClearChangesMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // clear values, reflected in map
        resetFull();
        Set<K> keySet = getMap().keySet();
        assertFalse(getMap().isEmpty());
        assertFalse(keySet.isEmpty());
        keySet.clear();
        assertTrue(getMap().isEmpty());
        assertTrue(keySet.isEmpty());

        // clear map, reflected in values
        resetFull();
        keySet = getMap().keySet();
        assertFalse(getMap().isEmpty());
        assertFalse(keySet.isEmpty());
        getMap().clear();
        assertTrue(getMap().isEmpty());
        assertTrue(keySet.isEmpty());
    }

    /**
     * Tests that the {@link Map#entrySet()} collection is backed by
     * the underlying map for clear().
     */
    @Test
    public void testEntrySetClearChangesMap() {
        if (!isRemoveSupported()) {
            return;
        }

        // clear values, reflected in map
        resetFull();
        Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        assertFalse(getMap().isEmpty());
        assertFalse(entrySet.isEmpty());
        entrySet.clear();
        assertTrue(getMap().isEmpty());
        assertTrue(entrySet.isEmpty());

        // clear map, reflected in values
        resetFull();
        entrySet = getMap().entrySet();
        assertFalse(getMap().isEmpty());
        assertFalse(entrySet.isEmpty());
        getMap().clear();
        assertTrue(getMap().isEmpty());
        assertTrue(entrySet.isEmpty());
    }

    @Test
    public void testEntrySetContains1() {
        resetFull();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        assertTrue(entrySet.contains(entry));
    }

    @Test
    public void testEntrySetContains2() {
        resetFull();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        final Map.Entry<K, V> test = cloneMapEntry(entry);
        assertTrue(entrySet.contains(test));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEntrySetContains3() {
        resetFull();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        final HashMap<K, V> temp = new HashMap<>();
        temp.put(entry.getKey(), (V) "A VERY DIFFERENT VALUE");
        final Map.Entry<K, V> test = temp.entrySet().iterator().next();
        assertFalse(entrySet.contains(test));
    }

    @Test
    public void testEntrySetRemove1() {
        if (!isRemoveSupported()) {
            return;
        }
        resetFull();
        final int size = getMap().size();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        final K key = entry.getKey();

        assertTrue(entrySet.remove(entry));
        assertFalse(getMap().containsKey(key));
        assertEquals(size - 1, getMap().size());
    }

    @Test
    public void testEntrySetRemove2() {
        if (!isRemoveSupported()) {
            return;
        }
        resetFull();
        final int size = getMap().size();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        final K key = entry.getKey();
        final Map.Entry<K, V> test = cloneMapEntry(entry);

        assertTrue(entrySet.remove(test));
        assertFalse(getMap().containsKey(key));
        assertEquals(size - 1, getMap().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEntrySetRemove3() {
        if (!isRemoveSupported()) {
            return;
        }
        resetFull();
        final int size = getMap().size();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final Map.Entry<K, V> entry = entrySet.iterator().next();
        final K key = entry.getKey();
        final HashMap<K, V> temp = new HashMap<>();
        temp.put(entry.getKey(), (V) "A VERY DIFFERENT VALUE");
        final Map.Entry<K, V> test = temp.entrySet().iterator().next();

        assertFalse(entrySet.remove(test));
        assertTrue(getMap().containsKey(key));
        assertEquals(size, getMap().size());
    }

    /**
     * Tests that the {@link Map#values} collection is backed by
     * the underlying map by removing from the values collection
     * and testing if the value was removed from the map.
     * <p>
     * We should really test the "vice versa" case--that values removed
     * from the map are removed from the values collection--also,
     * but that's a more difficult test to construct (lacking a
     * "removeValue" method.)
     * </p>
     * <p>
     * See bug <a href="https://issues.apache.org/jira/browse/COLLECTIONS-92">
     * COLLECTIONS-92</a>.
     * </p>
     */
    @Test
    public void testValuesRemoveChangesMap() {
        resetFull();
        final V[] sampleValues = getSampleValues();
        final Collection<V> values = getMap().values();
        for (final V sampleValue : sampleValues) {
            if (map.containsValue(sampleValue)) {
                int j = 0;  // loop counter prevents infinite loops when remove is broken
                while (values.contains(sampleValue) && j < 10000) {
                    try {
                        values.remove(sampleValue);
                    } catch (final UnsupportedOperationException e) {
                        // if values.remove is unsupported, just skip this test
                        return;
                    }
                    j++;
                }
                assertTrue(j < 10000, "values().remove(obj) is broken");
                assertFalse(getMap().containsValue(sampleValue), "Value should have been removed from the underlying map.");
            }
        }
    }

    /**
     * Tests values.removeAll.
     */
    @Test
    public void testValuesRemoveAll() {
        resetFull();
        final Collection<V> values = getMap().values();
        final List<V> sampleValuesAsList = Arrays.asList(getSampleValues());
        if (!values.equals(sampleValuesAsList)) {
            return;
        }
        try {
            assertFalse(values.removeAll(Collections.<V>emptySet()));
        } catch (final UnsupportedOperationException e) {
            // if values.removeAll is unsupported, just skip this test
            return;
        }
        assertEquals(sampleValuesAsList.size(), getMap().size());
        try {
            assertTrue(values.removeAll(sampleValuesAsList));
        } catch (final UnsupportedOperationException e) {
            // if values.removeAll is unsupported, just skip this test
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Test values.retainAll.
     */
    @Test
    public void testValuesRetainAll() {
        resetFull();
        final Collection<V> values = getMap().values();
        final List<V> sampleValuesAsList = Arrays.asList(getSampleValues());
        if (!values.equals(sampleValuesAsList)) {
            return;
        }
        try {
            assertFalse(values.retainAll(sampleValuesAsList));
        } catch (final UnsupportedOperationException e) {
            // if values.retainAll is unsupported, just skip this test
            return;
        }
        assertEquals(sampleValuesAsList.size(), getMap().size());
        try {
            assertTrue(values.retainAll(Collections.<V>emptySet()));
        } catch (final UnsupportedOperationException e) {
            // if values.retainAll is unsupported, just skip this test
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Verifies that values.iterator.remove changes the underlying map.
     */
    @Test
    @SuppressWarnings("boxing") // OK in test code
    public void testValuesIteratorRemoveChangesMap() {
        resetFull();
        final List<V> sampleValuesAsList = Arrays.asList(getSampleValues());
        final Map<V, Integer> cardinality = CollectionUtils.getCardinalityMap(sampleValuesAsList);
        final Collection<V> values = getMap().values();
        for (final Iterator<V> iter = values.iterator(); iter.hasNext();) {
            final V value = iter.next();
            Integer count = cardinality.get(value);
            if (count == null) {
                return;
            }
            try {
                iter.remove();
                cardinality.put(value, --count);
            } catch (final UnsupportedOperationException e) {
                // if values.iterator.remove is unsupported, just skip this test
                return;
            }
            final boolean expected = count > 0;
            final StringBuilder msg = new StringBuilder("Value should ");
            msg.append(expected ? "yet " : "no longer ");
            msg.append("be present in the underlying map");
            assertEquals(expected, getMap().containsValue(value), msg.toString());
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Tests that the {@link Map#keySet} set is backed by
     * the underlying map by removing from the keySet set
     * and testing if the key was removed from the map.
     */
    @Test
    public void testKeySetRemoveChangesMap() {
        resetFull();
        final K[] sampleKeys = getSampleKeys();
        final Set<K> keys = getMap().keySet();
        for (final K sampleKey : sampleKeys) {
            try {
                keys.remove(sampleKey);
            } catch (final UnsupportedOperationException e) {
                // if key.remove is unsupported, just skip this test
                return;
            }
            assertFalse(getMap().containsKey(sampleKey), "Key should have been removed from the underlying map.");
        }
    }

    /**
     * Test keySet.removeAll.
     */
    @Test
    public void testKeySetRemoveAll() {
        resetFull();
        final Set<K> keys = getMap().keySet();
        final List<K> sampleKeysAsList = Arrays.asList(getSampleKeys());
        if (!keys.equals(sampleKeysAsList)) {
            return;
        }
        try {
            assertFalse(keys.removeAll(Collections.<K>emptySet()));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertEquals(sampleKeysAsList, keys);
        try {
            assertTrue(keys.removeAll(sampleKeysAsList));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Test keySet.retainAll.
     */
    @Test
    public void testKeySetRetainAll() {
        resetFull();
        final Set<K> keys = getMap().keySet();
        final List<K> sampleKeysAsList = Arrays.asList(getSampleKeys());
        if (!keys.equals(sampleKeysAsList)) {
            return;
        }
        try {
            assertFalse(keys.retainAll(sampleKeysAsList));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertEquals(sampleKeysAsList, keys);
        try {
            assertTrue(keys.retainAll(Collections.<K>emptySet()));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Verify that keySet.iterator.remove changes the underlying map.
     */
    @Test
    public void testKeySetIteratorRemoveChangesMap() {
        resetFull();
        for (final Iterator<K> iter = getMap().keySet().iterator(); iter.hasNext();) {
            final K key = iter.next();
            try {
                iter.remove();
            } catch (final UnsupportedOperationException e) {
                return;
            }
            assertFalse(getMap().containsKey(key));
        }
    }

    /**
     * Tests that the {@link Map#entrySet} set is backed by
     * the underlying map by removing from the entrySet set
     * and testing if the entry was removed from the map.
     */
    @Test
    public void testEntrySetRemoveChangesMap() {
        resetFull();
        final K[] sampleKeys = getSampleKeys();
        final V[] sampleValues = getSampleValues();
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        for (int i = 0; i < sampleKeys.length; i++) {
            try {
                entrySet.remove(new DefaultMapEntry<>(sampleKeys[i], sampleValues[i]));
            } catch (final UnsupportedOperationException e) {
                // if entrySet removal is unsupported, just skip this test
                return;
            }
            assertFalse(getMap().containsKey(sampleKeys[i]), "Entry should have been removed from the underlying map.");
        }
    }

    /**
     * Test entrySet.removeAll.
     */
    @Test
    public void testEntrySetRemoveAll() {
        resetFull();
        final K[] sampleKeys = getSampleKeys();
        final V[] sampleValues = getSampleValues();
        //verify map looks as expected:
        for (int i = 0; i < sampleKeys.length; i++) {
            if (!getMap().containsKey(sampleKeys[i])) {
                return;
            }
            final V value = sampleValues[i];
            final V test = getMap().get(sampleKeys[i]);
            if (value == test || value != null && value.equals(test)) {
                continue;
            }
            return;
        }
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final HashSet<Map.Entry<K, V>> comparisonSet = new HashSet<>(entrySet);
        try {
            assertFalse(entrySet.removeAll(Collections.<Map.Entry<K, V>>emptySet()));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertEquals(sampleKeys.length, getMap().size());
        try {
            assertTrue(entrySet.removeAll(comparisonSet));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Test entrySet.retainAll.
     */
    @Test
    public void testEntrySetRetainAll() {
        resetFull();
        final K[] sampleKeys = getSampleKeys();
        final V[] sampleValues = getSampleValues();
        //verify map looks as expected:
        for (int i = 0; i < sampleKeys.length; i++) {
            if (!getMap().containsKey(sampleKeys[i])) {
                return;
            }
            final V value = sampleValues[i];
            final V test = getMap().get(sampleKeys[i]);
            if (value == test || value != null && value.equals(test)) {
                continue;
            }
            return;
        }
        final Set<Map.Entry<K, V>> entrySet = getMap().entrySet();
        final HashSet<Map.Entry<K, V>> comparisonSet = new HashSet<>(entrySet);
        try {
            assertFalse(entrySet.retainAll(comparisonSet));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertEquals(sampleKeys.length, getMap().size());
        try {
            assertTrue(entrySet.retainAll(Collections.<Map.Entry<K, V>>emptySet()));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(getMap().isEmpty());
    }

    /**
     * Verify that entrySet.iterator.remove changes the underlying map.
     */
    @Test
    public void testEntrySetIteratorRemoveChangesMap() {
        resetFull();
        for (final Iterator<Map.Entry<K, V>> iter = getMap().entrySet().iterator(); iter.hasNext();) {
            final K key = iter.next().getKey();
            try {
                iter.remove();
            } catch (final UnsupportedOperationException e) {
                return;
            }
            assertFalse(getMap().containsKey(key));
        }
    }

    @Test
    public void testCopyEmpty() {
        if (!isCopyConstructorCheckable())
            return;

        confirmed = makeConfirmedMap();
        map = makeObjectCopy(confirmed);
        views();
        assertNotSame(confirmed, map);
        assertNotSame(confirmed.values(), map.values());
        assertNotSame(confirmed.keySet(), map.keySet());
        assertNotSame(confirmed.entrySet(), map.entrySet());
        verify();
    }

    @Test
    public void testCopyFull() {
        if (!isCopyConstructorCheckable())
            return;

        confirmed = makeConfirmedFullMap();
        map = makeObjectCopy(confirmed);
        views();
        assertNotSame(confirmed, map);
        assertNotSame(confirmed.values(), map.values());
        assertNotSame(confirmed.keySet(), map.keySet());
        assertNotSame(confirmed.entrySet(), map.entrySet());
        verify();
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
    private Map.Entry<K, V>[] makeEntryArray(final K[] keys, final V[] values) {
        final Map.Entry<K, V>[] result = new Map.Entry[keys.length];
        for (int i = 0; i < keys.length; i++) {
            final Map<K, V> map = makeConfirmedMap();
            map.put(keys[i], values[i]);
            result[i] = map.entrySet().iterator().next();
        }
        return result;
    }

    /**
     * Bulk test {@link Map#entrySet()}.  This method runs through all of
     * the tests in {@link AbstractSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.
     */
    @Nested
    public class TestMapEntrySet extends AbstractSetTest<Map.Entry<K, V>> {
        public TestMapEntrySet() {
            super("MapEntrySet");
        }

        // Have to implement manually; entrySet doesn't support addAll
        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<K, V>[] getFullElements() {
            return getFullNonNullElements();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map.Entry<K, V>[] getFullNonNullElements() {
            final K[] k = getSampleKeys();
            final V[] v = getSampleValues();
            return makeEntryArray(k, v);
        }

        // Have to implement manually; entrySet doesn't support addAll
        @Override
        public Map.Entry<K, V>[] getOtherElements() {
            final K[] k = getOtherKeys();
            final V[] v = getOtherValues();
            return makeEntryArray(k, v);
        }

        @Override
        public Set<Map.Entry<K, V>> makeObject() {
            return AbstractMapTest.this.makeObject().entrySet();
        }

        @Override
        public Set<Map.Entry<K, V>> makeFullCollection() {
            return makeFullMap().entrySet();
        }

        @Override
        public boolean isAddSupported() {
            // Collection views don't support add operations.
            return false;
        }

        @Override
        public boolean isRemoveSupported() {
            // Entry set should only support remove if map does
            return AbstractMapTest.this.isRemoveSupported();
        }

        public boolean isGetStructuralModify() {
            return AbstractMapTest.this.isGetStructuralModify();
        }

        @Override
        public boolean areEqualElementsIndistinguishable() {
            return AbstractMapTest.this.areEqualElementsIndistinguishable();
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }

        @Override
        public void resetFull() {
            AbstractMapTest.this.resetFull();
            setCollection(AbstractMapTest.this.getMap().entrySet());
            TestMapEntrySet.this.setConfirmed(AbstractMapTest.this.getConfirmed().entrySet());
        }

        @Override
        public void resetEmpty() {
            AbstractMapTest.this.resetEmpty();
            setCollection(AbstractMapTest.this.getMap().entrySet());
            TestMapEntrySet.this.setConfirmed(AbstractMapTest.this.getConfirmed().entrySet());
        }

        @Override
        protected IterationBehaviour getIterationBehaviour(){
            return AbstractMapTest.this.getIterationBehaviour();
        }

        @Test
        public void testMapEntrySetIteratorEntry() {
            resetFull();
            int count = 0;
            for (final Entry<K, V> entry : getCollection()) {
                assertTrue(AbstractMapTest.this.getMap().containsKey(entry.getKey()));
                assertTrue(AbstractMapTest.this.getMap().containsValue(entry.getValue()));
                if (!isGetStructuralModify()) {
                    assertEquals(AbstractMapTest.this.getMap().get(entry.getKey()), entry.getValue());
                }
                count++;
            }
            assertEquals(getCollection().size(), count);
        }

        @Test
        public void testMapEntrySetIteratorEntrySetValue() {
            final K key1 = getSampleKeys()[0];
            final K key2 = getSampleKeys().length == 1 ? getSampleKeys()[0] : getSampleKeys()[1];
            final V newValue1 = getNewSampleValues()[0];
            final V newValue2 = getNewSampleValues().length ==1 ? getNewSampleValues()[0] : getNewSampleValues()[1];

            resetFull();
            // explicitly get entries as sample values/keys are connected for some maps
            // such as BeanMap
            Iterator<Map.Entry<K, V>> it = TestMapEntrySet.this.getCollection().iterator();
            final Map.Entry<K, V> entry1 = getEntry(it, key1);
            it = TestMapEntrySet.this.getCollection().iterator();
            final Map.Entry<K, V> entry2 = getEntry(it, key2);
            Iterator<Map.Entry<K, V>> itConfirmed = TestMapEntrySet.this.getConfirmed().iterator();
            final Map.Entry<K, V> entryConfirmed1 = getEntry(itConfirmed, key1);
            itConfirmed = TestMapEntrySet.this.getConfirmed().iterator();
            final Map.Entry<K, V> entryConfirmed2 = getEntry(itConfirmed, key2);
            verify();

            if (!isSetValueSupported()) {
                assertThrows(UnsupportedOperationException.class, () -> entry1.setValue(newValue1));
                return;
            }

            entry1.setValue(newValue1);
            entryConfirmed1.setValue(newValue1);
            assertEquals(newValue1, entry1.getValue());
            assertTrue(AbstractMapTest.this.getMap().containsKey(entry1.getKey()));
            assertTrue(AbstractMapTest.this.getMap().containsValue(newValue1));
            assertEquals(newValue1, AbstractMapTest.this.getMap().get(entry1.getKey()));
            verify();

            entry1.setValue(newValue1);
            entryConfirmed1.setValue(newValue1);
            assertEquals(newValue1, entry1.getValue());
            assertTrue(AbstractMapTest.this.getMap().containsKey(entry1.getKey()));
            assertTrue(AbstractMapTest.this.getMap().containsValue(newValue1));
            assertEquals(newValue1, AbstractMapTest.this.getMap().get(entry1.getKey()));
            verify();

            entry2.setValue(newValue2);
            entryConfirmed2.setValue(newValue2);
            assertEquals(newValue2, entry2.getValue());
            assertTrue(AbstractMapTest.this.getMap().containsKey(entry2.getKey()));
            assertTrue(AbstractMapTest.this.getMap().containsValue(newValue2));
            assertEquals(newValue2, AbstractMapTest.this.getMap().get(entry2.getKey()));
            verify();
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntrySetIteratorEntrySetValueMixedPutsClonedKeys() {
            K key1 = getSampleKeys()[0];
            V newValue1 = getNewSampleValues()[0];
            V newValue2 = getNewSampleValues().length ==1 ? getNewSampleValues()[0] : getNewSampleValues()[1];
            resetFull();
            final Iterator<Map.Entry<K, V>> it = TestMapEntrySet.this.getCollection().iterator();
            final Map.Entry<K, V> entry1 = getEntry(it, key1);
            Iterator<Map.Entry<K, V>> itConfirmed = TestMapEntrySet.this.getConfirmed().iterator();
            final Map.Entry<K, V> entryConfirmed1 = getEntry(itConfirmed, key1);

            if (isSetValueSupported()) {
                key1 = copyKey(key1);
                newValue1 = copyValue(newValue1);

                map.put(key1, newValue1);
                confirmed.put(key1, newValue1);
                verify();

                key1 = copyKey(key1);
                newValue1 = copyValue(newValue1);

                entry1.setValue(newValue1);
                entryConfirmed1.setValue(newValue1);
                verify();

                key1 = copyKey(key1);
                newValue1 = copyValue(newValue1);

                map.put(key1, newValue1);
                confirmed.put(key1, newValue1);
                verify();

                key1 = copyKey(key1);
                newValue1 = copyValue( newValue1);

                entry1.setValue(newValue1);
                entryConfirmed1.setValue(newValue1);
                verify();

                key1 = copyKey(key1);
                newValue2 = copyValue(newValue2);

                map.put(key1, newValue2);
                confirmed.put(key1, newValue2);
                verify();

                newValue1 = copyValue(newValue1);

                entry1.setValue(newValue1);
                entryConfirmed1.setValue(newValue1);
                verify();
            } else {
                assertThrows(UnsupportedOperationException.class, () -> entry1.setValue(getNewSampleValues()[0]));
            }
        }

        @SuppressWarnings("unchecked")
        private K copyKey(K key) {
            if (key == null)
                return null;
            else if (key instanceof String)
                return (K) new String((String) key);
            else {
                try {
                    return (K) serializeDeserialize(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private V copyValue(V value) {
            if (value == null)
                return null;
            else if (value instanceof String)
                return (V) new String((String) value);
            else {
                try {
                    return (V) serializeDeserialize(value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public Map.Entry<K, V> getEntry(final Iterator<Map.Entry<K, V>> itConfirmed, final K key) {
            Map.Entry<K, V> entry = null;
            while (itConfirmed.hasNext()) {
                final Map.Entry<K, V> temp = itConfirmed.next();
                if (temp.getKey() == null) {
                    if (key == null) {
                        entry = temp;
                        break;
                    }
                } else if (temp.getKey().equals(key)) {
                    entry = temp;
                    break;
                }
            }
            assertNotNull(entry, "No matching entry in map for key '" + key + "'");
            return entry;
        }

        @Test
        public void testMapEntrySetRemoveNonMapEntry() {
            if (!isRemoveSupported()) {
                return;
            }
            resetFull();
            assertFalse(getCollection().remove(null));
            assertFalse(getCollection().remove(new Object()));
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntryInArray() {
            if (!isSetValueInArraySupported()) //  || isGetStructuralModify()
                return;

            resetFull();
            Object[] arrayObject = getCollection().toArray();
            assertEquals(getCollection().size(), arrayObject.length);
            for (int i = 0; i < getCollection().size(); ++i) {
                final Entry<K,V> entry = (Entry<K, V>) arrayObject[i];
                final K key = entry.getKey();
                final V value = entry.getValue();
                final V newValue = getNewSampleValues()[i];
                assertEquals(value, getMap().get(key));
                assertTrue(getMap().containsValue(value));
                entry.setValue(newValue);
                AbstractMapTest.this.getConfirmed().put(key, newValue);
                assertEquals(key, entry.getKey());
                assertEquals(newValue, entry.getValue());
                assertEquals(newValue, getMap().get(key));
                assertTrue(getMap().containsValue(newValue));
                verify();
            }

            resetFull();
            Entry<K, V>[] arrayTyped = getCollection().toArray(new Entry[0]);
            assertEquals(getCollection().size(), arrayTyped.length);
            for (int i = 0; i < getCollection().size(); ++i) {
                final Entry<K,V> entry = arrayTyped[i];
                final K key = entry.getKey();
                final V value = entry.getValue();
                final V newValue = getNewSampleValues()[i];
                assertEquals(value, getMap().get(key));
                assertTrue(getMap().containsValue(value));
                entry.setValue(newValue);
                AbstractMapTest.this.getConfirmed().put(key, newValue);
                assertEquals(key, entry.getKey());
                assertEquals(newValue, entry.getValue());
                assertEquals(newValue, getMap().get(key));
                assertTrue(getMap().containsValue(newValue));
                verify();
            }
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntryInArrayUnsupported() {
            if (isSetValueInArraySupported())
                return;

            final V newValue = getNewSampleValues()[0];

            resetFull();
            Object[] arrayObject = getCollection().toArray();
            assertEquals(getCollection().size(), arrayObject.length);
            for (Object entryObject : arrayObject) {
                final Entry<K,V> entry = (Entry<K, V>) entryObject;
                assertEquals(entry.getValue(), getMap().get(entry.getKey()));
                assertThrows(UnsupportedOperationException.class, () -> entry.setValue(newValue));
            }
            verify();

            Entry<K, V>[] arrayTyped = getCollection().toArray(new Entry[0]);
            assertEquals(getCollection().size(), arrayTyped.length);
            for (Entry<K,V> entry : arrayTyped) {
                assertEquals(entry.getValue(), getMap().get(entry.getKey()));
                assertThrows(UnsupportedOperationException.class, () -> entry.setValue(newValue));
            }
            verify();
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntryInForeachModifiable() {
            if (!isSetValueInArraySupported() || isGetStructuralModify())
                return;

            final Queue<V> newValueQueue = new LinkedList<>(Arrays.asList(getNewSampleValues()));

            resetFull();
            getCollection().forEach(entry -> {
                final K key = entry.getKey();
                final V value = entry.getValue();
                final V newValue = newValueQueue.remove();
                assertEquals(value, getMap().get(key));
                assertTrue(getMap().containsValue(value));
                entry.setValue(newValue);
                confirmed.put(key, newValue);
                assertEquals(key, entry.getKey());
                assertEquals(newValue, entry.getValue());
                assertEquals(newValue, getMap().get(key));
                assertTrue(getMap().containsValue(newValue));
            });
            verify();
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntryInForeachReadOnly() {
            if (isSetValueInArraySupported())
                return;

            final V newValue = getNewSampleValues()[0];

            resetFull();
            getCollection().forEach(entry -> {
                assertEquals(entry.getValue(), getMap().get(entry.getKey()));
                assertThrows(UnsupportedOperationException.class, () -> entry.setValue(newValue));
            });
            verify();
        }

        @Test
        @SuppressWarnings("unchecked")
        public void testMapEntryInRemoveIfVaried() {
            if (!isSetValueSupported() || isGetStructuralModify())
                return;

            resetFull();
            final V newValue0 = getNewSampleValues()[0];
            Predicate<Entry<K,V>> predicate = (Entry<K,V> entry) -> {
                    assertEquals(entry.getValue(), getMap().get(entry.getKey()));
                    try {
                        // acceptable to apply the value change if done correctly - for some collections at least
                        entry.setValue(newValue0);
                    } catch (UnsupportedOperationException ex) {
                        // but maybe more correct to throw exception
                        return false;
                    }
                    assertEquals(newValue0, entry.getValue());
                    assertEquals(newValue0, getMap().get(entry.getKey()));
                    assertTrue(getMap().containsKey(entry.getKey()));
                    assertTrue(getMap().containsValue(entry.getValue()));
                    AbstractMapTest.this.getConfirmed().put(entry.getKey(), newValue0);
                    return false;
            };
            try {
                assertFalse(getCollection().removeIf(predicate));
            } catch (UnsupportedOperationException ex) {
                // exception on the method is also good
            }
            verify();
        }

        @Override
        public void verify() {
            super.verify();
            AbstractMapTest.this.verify();
        }
    }


    /**
     * Bulk test {@link Map#keySet()}.  This method runs through all of
     * the tests in {@link AbstractSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.
     */
    @Nested
    public class TestMapKeySet extends AbstractSetTest<K> {
        public TestMapKeySet() {
            super("");
        }

        @Override
        public K[] getFullElements() {
            return getSampleKeys();
        }

        @Override
        public K[] getFullNonNullElements() {
            return getSampleKeys();
        }

        @Override
        public K[] getOtherElements() {
            return getOtherKeys();
        }

        @Override
        public Set<K> makeObject() {
            return AbstractMapTest.this.makeObject().keySet();
        }

        @Override
        public Set<K> makeFullCollection() {
            return AbstractMapTest.this.makeFullMap().keySet();
        }

        @Override
        public boolean isNullSupported() {
            return AbstractMapTest.this.isAllowNullKey();
        }

        @Override
        public boolean isAddSupported() {
            return false;
        }

        @Override
        public boolean isRemoveSupported() {
            return AbstractMapTest.this.isRemoveSupported();
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }

        @Override
        public boolean areEqualElementsIndistinguishable() {
            return AbstractMapTest.this.areEqualElementsIndistinguishable();
        }

        @Override
        public void resetEmpty() {
            AbstractMapTest.this.resetEmpty();
            setCollection(AbstractMapTest.this.getMap().keySet());
            TestMapKeySet.this.setConfirmed(AbstractMapTest.this.getConfirmed().keySet());
        }

        @Override
        public void resetFull() {
            AbstractMapTest.this.resetFull();
            setCollection(AbstractMapTest.this.getMap().keySet());
            TestMapKeySet.this.setConfirmed(AbstractMapTest.this.getConfirmed().keySet());
        }

        @Override
        public void verify() {
            super.verify();
            AbstractMapTest.this.verify();
        }

        @Override
        protected IterationBehaviour getIterationBehaviour(){
            return AbstractMapTest.this.getIterationBehaviour();
        }

    }

    /**
     * Bulk test {@link Map#values()}.  This method runs through all of
     * the tests in {@link AbstractCollectionTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the map and the other collection views are still valid.

     */
    @Nested
    public class TestMapValues extends AbstractCollectionTest<V> {
        public TestMapValues() {
            super("");
        }

        @Override
        public V[] getFullElements() {
            return getSampleValues();
        }

        @Override
        public V[] getOtherElements() {
            return getOtherValues();
        }

        @Override
        public Collection<V> makeObject() {
            return AbstractMapTest.this.makeObject().values();
        }

        @Override
        public Collection<V> makeFullCollection() {
            return AbstractMapTest.this.makeFullMap().values();
        }

        @Override
        public boolean isNullSupported() {
            return AbstractMapTest.this.isAllowNullKey();
        }

        @Override
        public boolean isAddSupported() {
            return false;
        }

        @Override
        public boolean isRemoveSupported() {
            return AbstractMapTest.this.isRemoveSupported();
        }

        @Override
        public boolean isRemoveElementSupported() {
            return AbstractMapTest.this.isSpecificValueRemoveSupported();
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }

        @Override
        public boolean areEqualElementsIndistinguishable() {
            // equal values are associated with different keys, so they are
            // distinguishable.
            return true;
        }

        @Override
        public Collection<V> makeConfirmedCollection() {
            // never gets called, reset methods are overridden
            return null;
        }

        @Override
        public Collection<V> makeConfirmedFullCollection() {
            // never gets called, reset methods are overridden
            return null;
        }

        @Override
        public void resetFull() {
            AbstractMapTest.this.resetFull();
            setCollection(map.values());
            TestMapValues.this.setConfirmed(AbstractMapTest.this.getConfirmed().values());
        }

        @Override
        public void resetEmpty() {
            AbstractMapTest.this.resetEmpty();
            setCollection(map.values());
            TestMapValues.this.setConfirmed(AbstractMapTest.this.getConfirmed().values());
        }

        @Override
        public void verify() {
            super.verify();
            AbstractMapTest.this.verify();
        }

        @Override
        protected IterationBehaviour getIterationBehaviour(){
            // other collections can be sorted by the key which matches their iteration behaviour
            // however values collection will still be sorted by key but which doesn't match the
            // values returned in iteration.
            return AbstractMapTest.this.getIterationBehaviour() == IterationBehaviour.FULLY_SORTED
                    ? IterationBehaviour.STABLE_SEQUENCE
                    : AbstractMapTest.this.getIterationBehaviour();
        }

        // TODO: should test that a remove on the values collection view
        // removes the proper mapping and not just any mapping that may have
        // the value equal to the value returned from the values iterator.
    }


    /**
     * Resets the {@link #map}, {@link #entrySet}, {@link #keySet},
     * {@link #values} and {@link #confirmed} fields to empty.
     */
    public void resetEmpty() {
        this.map = makeObject();
        views();
        this.confirmed = makeConfirmedMap();
    }

    /**
     * Resets the {@link #map}, {@link #entrySet}, {@link #keySet},
     * {@link #values} and {@link #confirmed} fields to full.
     */
    public void resetFull() {
        this.map = makeFullMap();
        views();
        this.confirmed = makeConfirmedFullMap();
    }

    /**
     *  Returns a confirmed full map. The returned map
     *  should use the same mappings as {@link #getSampleKeys} and {@link #getSampleValues()}.
     *
     *  @return a confirmed full map
     */
    protected Map<K, V> makeConfirmedFullMap() {
        Map<K, V> map = makeConfirmedMap();
        addSampleMappingsUnchecked(map);
        return map;
    }

    /**
     * Resets the collection view fields.
     */
    protected void views() {
        this.keySet = getMap().keySet();
        // see verifyValues: retrieve the values collection only when verifying them
        // this.values = getMap().values();
        this.entrySet = getMap().entrySet();
    }

    /**
     * Verifies that {@link #map} is still equal to {@link #confirmed}.
     * This method checks that the map is equal to the HashMap,
     * <I>and</I> that the map's collection views are still equal to
     * the HashMap's collection views.  An <Code>equals</Code> test
     * is done on the maps and their collection views; their size and
     * <Code>isEmpty</Code> results are compared; their hashCodes are
     * compared; and <Code>containsAll</Code> tests are run on the
     * collection views.
     */
    public void verify() {
        verifyMap();
        verifyEntrySet();
        verifyKeySet();
        verifyValues();
    }

    public void verifyMap() {
        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        assertEquals(size, getMap().size(), "Map should be same size as HashMap");
        assertEquals(empty, getMap().isEmpty(), "Map should be empty if HashMap is");
        assertEquals(getConfirmed().hashCode(), getMap().hashCode(), "hashCodes should be the same");
        // changing the order of the assertion below fails for LRUMap because confirmed is
        // another collection (e.g. treemap) and confirmed.equals() creates a normal iterator (not
        // #mapIterator()), which modifies the parent expected modCount of the map object, causing
        // concurrent modification exceptions.
        // Because of this we have assertEquals(map, confirmed), and not the other way around.
        assertEquals(map, confirmed, "Map should still equal HashMap");
        assertEquals(getMap(), getConfirmed(), "Map should still equal HashMap");
    }

    public void verifyEntrySet() {
        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        assertEquals(size, entrySet.size(),
                "entrySet should be same size as HashMap's" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(empty, entrySet.isEmpty(),
                "entrySet should be empty if HashMap is" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertTrue(entrySet.containsAll(getConfirmed().entrySet()),
                "entrySet should contain all HashMap's elements" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertTrue(getConfirmed().entrySet().containsAll(entrySet),
                "HashMap should contain all entrySet's elements" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(getConfirmed().entrySet().hashCode(), entrySet.hashCode(),
                "entrySet hashCodes should be the same" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(getConfirmed().entrySet(), entrySet,
                "Map's entry set should still equal HashMap's");
    }

    public void verifyKeySet() {
        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        assertEquals(size, keySet.size(),
                "keySet should be same size as HashMap's" +
                        "\nTest: " + keySet + "\nReal: " + getConfirmed().keySet());
        assertEquals(empty, keySet.isEmpty(),
                "keySet should be empty if HashMap is" +
                        "\nTest: " + keySet + "\nReal: " + getConfirmed().keySet());
        assertTrue(keySet.containsAll(getConfirmed().keySet()),
                "keySet should contain all HashMap's elements" +
                        "\nTest: " + keySet + "\nReal: " + getConfirmed().keySet());
        assertEquals(getConfirmed().keySet().hashCode(), keySet.hashCode(),
                "keySet hashCodes should be the same" +
                        "\nTest: " + keySet + "\nReal: " + getConfirmed().keySet());
        assertEquals(getConfirmed().keySet(), keySet,
                "Map's key set should still equal HashMap's");
    }

    public void verifyValues() {
        final List<V> known = new ArrayList<>(getConfirmed().values());

        values = getMap().values();

        final List<V> test = new ArrayList<>(values);

        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        assertEquals(size, values.size(),
                "values should be same size as HashMap's" +
                        "\nTest: " + test + "\nReal: " + known);
        assertEquals(empty, values.isEmpty(),
                "values should be empty if HashMap is" +
                        "\nTest: " + test + "\nReal: " + known);
        assertTrue(test.containsAll(known),
                "values should contain all HashMap's elements" +
                        "\nTest: " + test + "\nReal: " + known);
        assertTrue(known.containsAll(test),
                "values should contain all HashMap's elements" +
                        "\nTest: " + test + "\nReal: " + known);
        // originally coded to use a HashBag, but now separate jar so...
        for (final V v : known) {
            final boolean removed = test.remove(v);
            assertTrue(removed, "Map's values should still equal HashMap's");
        }
        assertTrue(test.isEmpty(), "Map's values should still equal HashMap's");
    }

    /**
     * Erases any leftover instance variables by setting them to null.
     */
    @AfterEach
    public void tearDown() throws Exception {
        map = null;
        keySet = null;
        entrySet = null;
        values = null;
        confirmed = null;
    }

    /**
     * Get the map.
     *
     * @return Map<K, V>
     */
    public Map<K, V> getMap() {
        return map;
    }

    /**
     * Get the confirmed.
     * @return Map<K, V>
     */
    public Map<K, V> getConfirmed() {
        return confirmed;
    }

}
