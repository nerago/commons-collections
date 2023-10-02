package org.apache.commons.collections4.map;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractNestedMapTest<K, V> {

    // These instance variables are initialized with the reset method.
    // Tests for map methods that alter the map (put, putAll, remove)
    // first call reset() to create the map and its views; then perform
    // the modification on the map; perform the same modification on the
    // confirmed; and then call verify() to ensure that the map is equal
    // to the confirmed, that the already-constructed collection views
    // are still equal to the confirmed collection's views.

    /**
     * Map created by reset().
     */
    private Map<K, V> map;
    /**
     * HashMap created by reset().
     */
    private Map<K, V> confirmed;

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
     *
     * @return Map<K, V>
     */
    public Map<K, V> getConfirmed() {
        return confirmed;
    }

    public abstract AbstractMapTest<K, V> outerTest();

    protected Map<K,V> makeObject() {
        return outerTest().makeObject();
    }

    private K[] getSampleKeys() {
        return outerTest().getSampleKeys();
    }

    private V[] getSampleValues() {
        return outerTest().getSampleValues();
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
        Assertions.assertEquals(getSampleKeys().length, getMap().size(),
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
        if (!outerTest().isRemoveSupported()) {
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
        verify();
    }

    /**
     * Tests Map.equals(Object)
     */
    @SuppressWarnings("SimplifiableAssertion")
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
        resetEmpty();

        final Object[] keys = getSampleKeys();
        final Object[] values = getSampleValues();

        for (final Object key : keys) {
            assertNull(getMap().get(key), "Empty map.get() should return null.");
        }
        verify();

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], getMap().get(keys[i]),
                    "Full map.get() should return value from mapping.");
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
    public void testMapToString() {
        resetEmpty();
        assertNotNull(getMap().toString(), "Empty map toString() should not return null");
        verify();

        resetFull();
        assertNotNull(getMap().toString(), "Empty map toString() should not return null");
        verify();
    }

    /**
     * Compare the current serialized form of the Map
     * against the canonical version in SCM.
     */
    @Test
    public void testEmptyMapCompatibility() throws Exception {
        /*
         * Create canonical objects with this code
        Map map = makeEmptyMap();
        if (!(map instanceof Serializable)) return;

        writeExternalFormToDisk((Serializable) map, getCanonicalEmptyCollectionName(map));
        */

        // test to make sure the canonical form has been preserved
        final Map<K, V> map = makeObject();
        if (map instanceof Serializable && !outerTest().skipSerializedCanonicalTests() && outerTest().isTestSerialization()) {
            @SuppressWarnings("unchecked") final Map<K, V> map2 = (Map<K, V>) outerTest().readExternalFormFromDisk(outerTest().getCanonicalEmptyCollectionName(map));
            assertEquals(0, map2.size(), "Map is empty");
        }
    }

    /**
     * Compare the current serialized form of the Map
     * against the canonical version in SCM.
     */
    @Test
    public void testFullMapCompatibility() throws Exception {
        /*
         * Create canonical objects with this code
        Map map = makeFullMap();
        if (!(map instanceof Serializable)) return;

        writeExternalFormToDisk((Serializable) map, getCanonicalFullCollectionName(map));
        */

        // test to make sure the canonical form has been preserved
        final Map<K, V> map = makeFullMap();
        if (map instanceof Serializable && !outerTest().skipSerializedCanonicalTests() && outerTest().isTestSerialization()) {
            @SuppressWarnings("unchecked") final Map<K, V> map2 = (Map<K, V>) outerTest().readExternalFormFromDisk(outerTest().getCanonicalFullCollectionName(map));
            Assertions.assertEquals(getSampleKeys().length, map2.size(), "Map is the right size");
        }
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

        if (outerTest().isPutAddSupported()) {
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
            if (outerTest().isPutChangeSupported()) {
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
                    if (!outerTest().isAllowDuplicateValues()) {
                        assertFalse(getMap().containsValue(values[i]), "Map should not contain old value after put when changed");
                    }
                }
            } else {
                AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                        () -> getMap().put(keys[0], newValues[0]),
                        "Expected IllegalArgumentException or UnsupportedOperationException on put (change)");
            }

        } else if (outerTest().isPutChangeSupported()) {
            resetEmpty();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().put(keys[0], values[0]),
                    "Expected UnsupportedOperationException or IllegalArgumentException on put (add) when fixed size");

            resetFull();
            int i = 0;
            for (final Iterator<K> it = getMap().keySet().iterator(); it.hasNext() && i < newValues.length; i++) {
                final K key = it.next();
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
                if (!outerTest().isAllowDuplicateValues()) {
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

        if (outerTest().isPutAddSupported()) {
            if (outerTest().isAllowNullKey()) {
                getMap().put(null, values[0]);
            } else {
                AbstractObjectTest.assertThrowsEither(NullPointerException.class, IllegalArgumentException.class,
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

        if (outerTest().isPutAddSupported()) {
            if (outerTest().isAllowNullValue()) {
                getMap().put(keys[0], null);
            } else {
                AbstractObjectTest.assertThrowsEither(NullPointerException.class, IllegalArgumentException.class,
                        () -> getMap().put(keys[0], null),
                        "put(null, value) should throw NPE/IAE");
            }
        }
    }

    /**
     * Tests Map.putAll(map) where no change is expected, starting with empty maps
     */
    @Test
    public void testMapPutAllNoChangeOnEmpty() {
        if (outerTest().isPutAddSupported() || outerTest().isPutChangeSupported()) {
            // check putAll OK adding empty map to empty map
            resetEmpty();
            getMap().putAll(new HashMap<>());
            assertEquals(0, getMap().size());
            verify();
        } else {
            // check putAll rejects adding empty map to empty map
            resetEmpty();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(new HashMap<>()),
                    "Expected UnsupportedOperationException on putAll");
            assertEquals(0, getMap().size());
            verify();
        }
    }

    /**
     * Tests Map.putAll(map) where no change is expected, starting with full maps
     */
    @Test
    public void testMapPutAllNoChangeOnFull() {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();

        final Map<K, V> m1 = makeConfirmedMap();
        for (int i = 0; i < keys.length; i++) {
            m1.put(keys[i], values[i]);
        }

        if (outerTest().isPutAddSupported() || outerTest().isPutChangeSupported()) {
            // check putAll OK adding empty map to non-empty map
            resetFull();
            getMap().putAll(new HashMap<>());
            verify();

            // check putAll OK adding JDK map with current values (no change)
            resetFull();
            getMap().putAll(m1);
            verify();
        } else {
            // check putAll rejects adding empty map to non-empty map
            resetFull();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(new HashMap<>()),
                    "Expected UnsupportedOperationException on putAll");
            verify();


            // check putAll rejects adding JDK map with current values
            resetFull();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m1),
                    "Expected UnsupportedOperationException on putAll");
            verify();
        }
    }

    /**
     * Tests Map.putAll(map) where content should be added to initially empty map
     */
    @Test
    public void testMapPutAllAddingKeysOnEmpty() {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();

        final Map<K, V> m2 = makeFullMap();

        final Map<K, V> m3 = makeConfirmedMap();
        for (int i = 0; i < keys.length; i++) {
            m3.put(keys[i], values[i]);
        }

        if (isPutAddSupported()) {
            // check putAll OK adding non-empty map to empty map
            resetEmpty();
            getMap().putAll(m2);
            getConfirmed().putAll(m2);
            verify();

            // check putAll OK adding non-empty JDK map to empty map
            resetEmpty();
            getMap().putAll(m3);
            getConfirmed().putAll(m3);
            verify();
        } else {
            // check putAll rejects adding non-empty map to empty map
            resetEmpty();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m2),
                    "Expected IllegalArgumentException on putAll");
            verify();

            // check putAll rejects adding non-empty JDK map to empty map
            resetEmpty();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m3),
                    "Expected IllegalArgumentException on putAll");
            verify();
        }
    }

    /**
     * Tests Map.putAll(map) where extra keys should be added to existing map
     */
    @Test
    public void testMapPutAllAddingKeysOnFull() {
        final K[] otherKeys = getOtherKeys();
        final V[] otherValues = getOtherValues();

        final Map<K, V> m4 = makeConfirmedMap();
        for (int i = 0; i < otherKeys.length; i++) {
            m4.put(otherKeys[i], otherValues[i]);
        }

        if (isPutAddSupported()) {
            // check putAll OK adding non-empty JDK map to non-empty map
            resetFull();
            getMap().putAll(m4);
            getConfirmed().putAll(m4);
            verify();
        } else {
            // check putAll rejects adding non-empty JDK map to non-empty map
            resetFull();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m4),
                    "Expected IllegalArgumentException on putAll");
            verify();
        }
    }

    /**
     * Tests Map.putAll(map) where existing keys get set to updated values
     */
    @Test
    public void testMapPutAllChangeValues() {
        final K[] keys = getSampleKeys();
        final V[] newValues = getNewSampleValues();

        final Map<K, V> m5 = makeConfirmedMap();
        m5.put(keys[0], newValues[0]);

        final Map<K, V> m6 = makeConfirmedMap();
        for (int i = 0; i < keys.length; i++) {
            m6.put(keys[i], newValues[i]);
        }

        if (isPutChangeSupported()) {
            // check putAll OK setting one changed value
            resetFull();
            getMap().putAll(m5);
            getConfirmed().putAll(m5);
            verify();

            // check putAll OK setting all changed values
            resetFull();
            getMap().putAll(m6);
            getConfirmed().putAll(m6);
            verify();
        } else {
            // check putAll rejects setting one changed value
            resetFull();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
                    () -> getMap().putAll(m5),
                    "Expected IllegalArgumentException on putAll");
            verify();

            // check putAll rejects setting all changed values
            resetFull();
            AbstractObjectTest.assertThrowsEither(IllegalArgumentException.class, UnsupportedOperationException.class,
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
        final Map.Entry<K, V> test = AbstractMapTest.cloneMapEntry(entry);
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
        final Map.Entry<K, V> test = AbstractMapTest.cloneMapEntry(entry);

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
        for (final Iterator<V> iter = values.iterator(); iter.hasNext(); ) {
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
        assertTrue(CollectionUtils.isEqualCollection(keys, sampleKeysAsList));
        try {
            assertFalse(keys.removeAll(Collections.<K>emptySet()));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(CollectionUtils.isEqualCollection(keys, sampleKeysAsList));
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
        assertTrue(CollectionUtils.isEqualCollection(keys, sampleKeysAsList));
        try {
            assertFalse(keys.retainAll(sampleKeysAsList));
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertTrue(CollectionUtils.isEqualCollection(keys, sampleKeysAsList));
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
        for (final Iterator<K> iter = getMap().keySet().iterator(); iter.hasNext(); ) {
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
            if (Objects.equals(value, test)) {
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
            if (Objects.equals(value, test)) {
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
        for (final Iterator<Map.Entry<K, V>> iter = getMap().entrySet().iterator(); iter.hasNext(); ) {
            final K key = iter.next().getKey();
            try {
                iter.remove();
            } catch (final UnsupportedOperationException e) {
                return;
            }
            assertFalse(getMap().containsKey(key));
        }
    }

    /**
     * Resets the {@link #map} and {@link #confirmed} fields to empty.
     */
    public void resetEmpty() {
        this.map = makeObject();
        this.confirmed = makeConfirmedMap();
    }

    /**
     * Resets the {@link #map} and {@link #confirmed} fields to full.
     */
    public void resetFull() {
        this.map = makeFullMap();
        this.confirmed = makeConfirmedMap();
        final K[] k = getSampleKeys();
        final V[] v = getSampleValues();
        for (int i = 0; i < k.length; i++) {
            confirmed.put(k[i], v[i]);
        }
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

    protected void verifyMap() {
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

    protected void verifyEntrySet() {
        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        final Set<Map.Entry<K, V>> entrySet = map.entrySet();
        assertEquals(size, entrySet.size(),
                "entrySet should be same size as HashMap's" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(empty, entrySet.isEmpty(),
                "entrySet should be empty if HashMap is" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertTrue(entrySet.containsAll(getConfirmed().entrySet()),
                "entrySet should contain all HashMap's elements" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(getConfirmed().entrySet().hashCode(), entrySet.hashCode(),
                "entrySet hashCodes should be the same" +
                        "\nTest: " + entrySet + "\nReal: " + getConfirmed().entrySet());
        assertEquals(getConfirmed().entrySet(), entrySet,
                "Map's entry set should still equal HashMap's");
    }

    protected void verifyKeySet() {
        final int size = getConfirmed().size();
        final boolean empty = getConfirmed().isEmpty();
        final Set<K> keySet = map.keySet();
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

    protected void verifyValues() {
        final List<V> known = new ArrayList<>(getConfirmed().values());

        final Collection<V> values = getMap().values();

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
        confirmed = null;
    }
}
