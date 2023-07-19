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

import static org.apache.commons.collections4.map.LazyMap.lazyMap;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.*;
import org.junit.jupiter.api.Test;

/**
 * Extension of {@link AbstractMapTest} for exercising the
 * {@link LazyMap} implementation.
 *
 * @since 3.0
 */
@SuppressWarnings("boxing")
public class LazyMapTest<K, V> extends AbstractIterableMapTest<K, V> {

    private static final int FACTORY = 42;
    private static final Factory<Integer> defaultFactory = FactoryUtils.constantFactory(FACTORY);

    @SuppressWarnings("unchecked")
    private final V FACTORY_V = (V) (Integer) 42;

    public LazyMapTest() {
        super(LazyMapTest.class.getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IterableMap<K, V> makeObject() {
        return lazyMap(new HashMap<>(), (Factory<? extends V>) defaultFactory);
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.OTHER_DECORATOR;
    }

    @Override
    public boolean isGetStructuralModify() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getMissingEntryGetExpectValue() {
        return (V) (Integer) FACTORY;
    }

    @Test
    public void mapGetWithFactory() {
        Map<String, Number> map = lazyMap(new HashMap<>(), defaultFactory);
        assertEquals(0, map.size());
        final Number i1 = map.get("Five");
        assertEquals(FACTORY, i1);
        assertEquals(1, map.size());
        final Number i2 = map.get(new String(new char[] {'F', 'i', 'v', 'e'}));
        assertEquals(FACTORY, i2);
        assertEquals(1, map.size());
        assertSame(i1, i2);

        map = lazyMap(new HashMap<>(), FactoryUtils.<Long>nullFactory());
        final Object o = map.get("Five");
        assertNull(o);
        assertEquals(1, map.size());
    }

    @Test
    public void mapGetWithTransformer() {
        final Transformer<Number, Integer> intConverter = input -> {
            assertEquals(123L, input);
            return input.intValue();
        };
        final Map<Long, Number> map = lazyMap(new HashMap<>(), intConverter);
        assertEquals(0, map.size());
        final Number i1 = map.get(123L);
        assertEquals(Integer.class, i1.getClass());
        assertEquals(123, i1);
        assertEquals(1, map.size());
    }

    /**
     *  LazyMap.getOrDefault should pretend that all elements are already in the map.
     *  Should add factory value if called with missing.
     **/
    @Test
    @Override
    public void testMapGetOrDefault() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V missingValue = (V) "abc";

        resetEmpty();
        for (final K key : keys) {
            assertEquals(FACTORY, getMap().getOrDefault(key, missingValue),
                    "LazyMap.getOrDefault should always return factory for missing");
            getConfirmed().put(key, FACTORY_V);
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            assertEquals(values[i], getMap().getOrDefault(keys[i], missingValue),
                    "Full map.getOrDefault() should return value from mapping.");
            verify();
        }
        for (final K key : otherKeys) {
            assertEquals(FACTORY, getMap().getOrDefault(key, missingValue),
                    "LazyMap.getOrDefault should always return factory for missing");
            getConfirmed().put(key, FACTORY_V);
            verify();
        }
    }

    /**
     *  LazyMap.putIfAbsent should pretend that no element is ever absent
     *  Should add factory value if called with missing.
     *  Handling of null values is more debatable, but this method contract equates null with absence so should act the same,
     *  thus even existing explicit stored null values need to be replaced with factory.
     **/
    @Test
    @Override
    public void testMapPutIfAbsent() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            assertEquals(FACTORY, getMap().putIfAbsent(keys[i], values[i]),
                    "LazyMap.putIfAbsent should always return factory for missing");
            getConfirmed().put(keys[i], FACTORY_V);
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            V oldValue = values[i];
            V replaceValue = newValues[i];
            assertTrue(getMap().containsKey(key)); // just checking test setup ok
            if (oldValue == null) {
                assertEquals(FACTORY, getMap().putIfAbsent(key, replaceValue),
                        "LazyMap.putIfAbsent should return factory for null");
                getConfirmed().put(key, FACTORY_V);
            } else {
                assertEquals(oldValue, getMap().putIfAbsent(key, replaceValue),
                        "putIfAbsent should return existing value from map.");
            }
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            assertEquals(FACTORY,getMap().putIfAbsent(otherKeys[i], otherValues[i]),
                    "LazyMap.putIfAbsent should always return factory for missing");
            getConfirmed().put(otherKeys[i], FACTORY_V);
            verify();
        }
    }

    /**
     *  See {@link #testMapPutIfAbsent}, should work similarly.
     **/
    @Test
    @Override
    public void testMapComputeIfAbsent() {
        final K[] keys = getSampleKeys();
        final V[] values = getSampleValues();

        final Function<? super K, ? extends V> mappingFunction = k -> fail("mapping function should never be called");

        resetEmpty();
        for (final K key : keys) {
            assertEquals(FACTORY, getMap().computeIfAbsent(key, mappingFunction),
                    "LazyMap.computeIfAbsent should always return factory value for missing");
            getConfirmed().put(key, FACTORY_V);
            verify();
        }
        // we don't need to test exception case for LazyMap since it never needs mapping function

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            if (oldValue == null) {
                assertEquals(FACTORY, getMap().computeIfAbsent(key, mappingFunction),
                        "LazyMap.computeIfAbsent should replace null with factory value");
                getConfirmed().put(key, FACTORY_V);
            } else {
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
            assertEquals(FACTORY, getMap().computeIfAbsent(key, mappingFunction),
                    "LazyMap.computeIfAbsent should always return factory value for missing");
            getConfirmed().put(key, FACTORY_V);
            verify();
        }
    }

    /**
     *  LazyMap.computeIfPresent should generally pretend that all elements are already present.
     *  Missing values should use the factory to provide parameter to the compute function and then the result stored.
     *  The contract computeIfPresent is clear that only non-null values should be handled, thus consumers wouldn't
     *  expect null values. Existing nulls will be left unmapped and unchanged, although its debatable if they should be
     *  replaced via factory.
     *  Remove is supported through null result as LazyMap always uses normal remove behaviour.
     **/
    @Test
    @Override
    @SuppressWarnings("unchecked")
    public void testMapComputeIfPresent() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V value = (V) (Integer) (FACTORY + i);
            final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                assertEquals(key, k);
                assertEquals(FACTORY, v);
                return value;
            };
            assertEquals(value, getMap().computeIfPresent(key, mappingFunction),
                    "LazyMap.computeIfPresent should return function value for missing");
            assertTrue(getMap().containsKey(key));
            assertEquals(value, getMap().get(key));
            getConfirmed().put(key, value);
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
                        "computeIfPresent should return null for explicit stored null");
                assertTrue(getMap().containsKey(key), "still should be in map");
                assertNull(getMap().get(key), "still should be null");
                verify();
            } else if (replaceValue == null) {
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(oldValue, v);
                    return null;
                };
                assertNull(getMap().computeIfPresent(key, mappingFunction),
                        "computeIfPresent should return the new value");
                assertFalse(getMap().containsKey(key), "should be removed from map");
                getConfirmed().remove(key);
                verify();
            } else {
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(oldValue, v);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().computeIfPresent(keys[i], mappingFunction),
                        "computeIfPresent should return the new value");
                getConfirmed().put(key, replaceValue);
                verify();

                assertThrows(ArithmeticException.class,
                        () -> getMap().computeIfPresent(key, (k, v) -> { throw new ArithmeticException(); }));
                verify();
            }
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V value = (V) (Integer) (FACTORY + i);
            final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                assertEquals(key, k);
                assertEquals(FACTORY, v);
                return value;
            };
            assertEquals(value, getMap().computeIfPresent(key, mappingFunction),
                    "LazyMap.computeIfPresent should return function value for missing");
            assertTrue(getMap().containsKey(key));
            assertEquals(value, getMap().get(key));
            getConfirmed().put(key, value);
            verify();
        }
    }

    /**
     * LazyMap should generally pretend that no element is ever absent.
     * Missing values should use the factory to provide parameter to the compute function and then the result stored.
     * Existing explicit null values can be passed to the remappingFunction as unlike with computeIfPresent that's
     * expected by users. Again it could also be reasonable to substitute them with a factory value.
     * Remove is supported through null result as LazyMap always uses normal remove behaviour.
     */
    @Test
    @Override
    public void testMapCompute() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            if (i % 2 == 0) {
                // leave every second absent (should look much like a remove to user)
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(FACTORY, v);
                    return null;
                };
                assertNull(getMap().compute(key, mappingFunction), "compute should return null for missing");
                assertFalse(getMap().containsKey(key));
            } else if (replaceValue != null) {
                // add new value
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(FACTORY, v);
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
        assertThrows(ArithmeticException.class,
                () -> getMap().compute(otherKeys[0], (k, v) -> { throw new ArithmeticException(); }));
        verify();

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V originalValue = values[i];
            final V replaceValue = newValues[i];
            if (i % 2 == 0 || replaceValue == null) {
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
            } else {
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
                    assertEquals(FACTORY, v);
                    return null;
                };
                assertNull(getMap().compute(key, mappingFunction), "compute should return null for missing");
                assertFalse(getMap().containsKey(key));
            } else if (replaceValue != null) {
                // add new value
                final BiFunction<? super K, ? super V, ? extends V> mappingFunction = (k, v) -> {
                    assertEquals(key, k);
                    assertEquals(FACTORY, v);
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
            verify();
        }
    }

    /**
     * See {@link #testMapCompute}, should work similarly.
     **/
    @Test
    @Override
    public void testMapMerge() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();
        final V dummy = (V) "xyz";

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            if (replaceValue != null) {
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(FACTORY, v);
                    assertEquals(dummy, p);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().merge(key, dummy, mappingFunction),
                        "merge should return new value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
            } else {
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(FACTORY, v);
                    assertEquals(dummy, p);
                    return null;
                };
                assertNull(getMap().merge(key, dummy, mappingFunction),
                        "merge should return new value");
                assertFalse(getMap().containsKey(key));
            }
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V originalValue = values[i];
            final V replaceValue = newValues[i];
            if (i % 2 == 0 || replaceValue == null) {
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
                verify();
            } else if (originalValue == null) {
                // change value
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    fail("shouldn't call func");
                    return null;
                };
                assertTrue(getMap().containsKey(key));
                assertNull(getMap().get(key));
                assertEquals(replaceValue, getMap().merge(key, replaceValue, mappingFunction),
                        "merge should return new value from param");
                assertTrue(getMap().containsKey(key));
                assertEquals(replaceValue, getMap().get(key));
                getConfirmed().put(key, replaceValue);
                verify();
            } else {
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
                verify();

                assertThrows(ArithmeticException.class,
                        () -> getMap().merge(key, dummy, (v, p) -> { throw new ArithmeticException(); }));
                verify();
            }
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            if (replaceValue != null) {
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(FACTORY, v);
                    assertEquals(dummy, p);
                    return replaceValue;
                };
                assertEquals(replaceValue, getMap().merge(key, dummy, mappingFunction),
                        "merge should return new value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify();

                assertThrows(ArithmeticException.class,
                        () -> getMap().merge(key, dummy, (v, p) -> { throw new ArithmeticException(); }));
                verify();
            } else {
                final BiFunction<? super V, ? super V, ? extends V> mappingFunction = (v, p) -> {
                    assertEquals(FACTORY, v);
                    assertEquals(dummy, p);
                    return null;
                };
                assertNull(getMap().merge(key, dummy, mappingFunction),
                        "merge should return new value");
                assertFalse(getMap().containsKey(key));
                verify();
            }
        }
    }

    /** Extra check that remove(K, V) pretends that collection has the factory default value. */
    @Test
    @Override
    public void testMapRemove2() {
        super.testMapRemove2();

        resetEmpty();
        for (final K key : getSampleKeys()) {
            assertTrue(getMap().remove(key, FACTORY),
                    "remove should pretend to remove value matching factory");
            assertFalse(getMap().containsKey(key));
            verify();
        }
    }

    /** Should pretend that every element is present and ready for replacement */
    @Test
    @Override
    public void testMapReplace2() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V replaceValue = newValues[i];
            assertEquals(FACTORY, getMap().replace(key, replaceValue),
                    "LazyMap.replace(K,V) should set the value, while pretending old value was the factory default");
            getConfirmed().put(key, replaceValue);
            verify();
        }

        resetFull();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            assertEquals(oldValue, getMap().replace(key, replaceValue), "replace should return old value");
            getConfirmed().put(key, replaceValue);
            verify();
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V replaceValue = otherValues[i];
            assertEquals(FACTORY, getMap().replace(key, replaceValue),
                    "LazyMap.replace(K,V) should set the value, while pretending old value was the factory default");
            getConfirmed().put(key, replaceValue);
            verify();
        }
    }

    @Test
    @Override
    public void testMapReplace3() {
        final K[] keys = getSampleKeys();
        final K[] otherKeys = getOtherKeys();
        final V[] values = getSampleValues();
        final V[] newValues = getNewSampleValues();
        final V[] otherValues = getOtherValues();
        final V dummy = (V) "xyz";

        resetEmpty();
        for (int i = 0; i < keys.length; i++) {
            final K key = keys[i];
            final V oldValue = values[i];
            final V replaceValue = newValues[i];
            if (i % 2 == 0) {
                assertFalse(getMap().replace(key, oldValue, replaceValue),
                        "LazyMap.replace should do nothing on empty with wrong value");
                assertFalse(getMap().containsKey(key));
                verify();
            } else {
                assertTrue(getMap().replace(key, FACTORY_V, replaceValue),
                        "LazyMap.replace should pretend it had the factory value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify();
            }
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

                assertFalse(getMap().replace(key, FACTORY_V, replaceValue),
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
            } else {
                getMap().replace(key, oldValue, oldValue);
                verify();
            }
        }
        for (int i = 0; i < otherKeys.length; i++) {
            final K key = otherKeys[i];
            final V oldValue = values[i];
            final V replaceValue = otherValues[i];
            if (i % 2 == 0) {
                assertFalse(getMap().replace(key, oldValue, replaceValue),
                        "LazyMap.replace should do nothing on empty with wrong value");
                assertFalse(getMap().containsKey(key));
                verify();
            } else {
                assertTrue(getMap().replace(key, FACTORY_V, replaceValue),
                        "LazyMap.replace should pretend it had the factory value");
                assertTrue(getMap().containsKey(key));
                getConfirmed().put(key, replaceValue);
                verify();
            }
        }
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/LazyMap.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk(
//            (java.io.Serializable) map,
//            "src/test/resources/data/test/LazyMap.fullCollection.version4.obj");
//    }

}
