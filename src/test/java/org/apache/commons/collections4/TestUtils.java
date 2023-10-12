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
package org.apache.commons.collections4;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class TestUtils {

    private TestUtils() {}

    /**
     * Asserts that deserialization of the object returns the same object as the
     * one that was serialized. Object is first serialized, then deserialized
     * and finally check is performed to see if original and deserialized
     * object references are the same.
     * <p>
     * This method is especially good for testing singleton pattern on classes
     * that support serialization.
     *
     * @param msg the identifying message for the {@code AssertionError}.
     * @param o object that will be tested.
     */
    public static void assertSameAfterSerialization(final String msg, final Object o) {
        try {
            // write object to byte buffer
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();

            // read same object from byte buffer
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(is);
            final Object object = ois.readObject();
            ois.close();

            // assert that original object and deserialized objects are the same
            assertSame(o, object, msg);
        } catch (final IOException | ClassNotFoundException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Does method throw any one of the specified exception types or their subclasses.
     * <p>
     * Uses runtime validation of exception classes since java can't enforce generic types with vararg anyway.
     */
    public static void assertThrowsAnyOf(final Executable executable, final String message, final Class<?>... exceptionsAllowed) {
        validateExceptionsAllowed(message, exceptionsAllowed);

        try {
            executable.execute();
        } catch (final Throwable caught) {
            checkCaughtIsAllowedType(message, caught, exceptionsAllowed);
        }

        fail(message + " ==> Expected exception to be thrown, but nothing was thrown.");
    }

    /**
     * Does method throw any one of the specified exception types or pass through without error.
     * Presumably the alternate should be a no-op but that needs follow-up verification.
     * <p>
     * Uses runtime validation of exception classes since java can't enforce generic types with vararg anyway.
     */
    public static void assertOptionallyThrowsAnyOf(final Executable executable, final String message, final Class<?>... exceptionsAllowed) {
        validateExceptionsAllowed(message, exceptionsAllowed);

        try {
            executable.execute();
        } catch (final Throwable caught) {
            checkCaughtIsAllowedType(message, caught, exceptionsAllowed);
        }
    }

    /**
     * Does method throw any one of the specified exception types or return false.
     * Should be used where either result is equivalent for passing a test.
     * <p>
     * Uses runtime validation of exception classes since java can't enforce generic types with vararg anyway.
     */
    public static void assertReturnsFalseOrThrowsAnyOf(final BooleanSupplier executable, final Class<?>... exceptionsAllowed) {
        validateExceptionsAllowed("", exceptionsAllowed);

        final boolean result;
        try {
            result = executable.getAsBoolean();
        } catch (final Throwable caught) {
            checkCaughtIsAllowedType("", caught, exceptionsAllowed);
            return;
        }

        assertFalse(result);
    }

    /**
     * Does method throw any one of the specified exception types or return null.
     * Should be used where either result is equivalent for passing a test.
     * <p>
     * Uses runtime validation of exception classes since java can't enforce generic types with vararg anyway.
     */
    public static void assertReturnsNullOrThrowsAnyOf(final Supplier<Object> executable, final Class<?>... exceptionsAllowed) {
        validateExceptionsAllowed("", exceptionsAllowed);

        final Object result;
        try {
            result = executable.get();
        } catch (final Throwable caught) {
            checkCaughtIsAllowedType("", caught, exceptionsAllowed);
            return;
        }

        assertNull(result);
    }

    /**
     * Does method throw NullPointerException or return false.
     */
    public static void assertReturnsFalseOrThrowsNPE(final BooleanSupplier executable) {
        final boolean result;
        try {
            result = executable.getAsBoolean();
        } catch (final Throwable caught) {
            assertInstanceOf(NullPointerException.class, caught);
            return;
        }
        assertFalse(result);
    }

    /**
     * Does method throw NullPointerException or return null.
     */
    public static void assertReturnsNullOrThrowsNPE(final Supplier<Object> executable) {
        final Object result;
        try {
            result = executable.get();
        } catch (final Throwable caught) {
            assertInstanceOf(NullPointerException.class, caught);
            return;
        }
        assertNull(result);
    }

    private static void validateExceptionsAllowed(final String message, final Class<?>[] exceptionsAllowed) {
        assertNotEquals(0, exceptionsAllowed.length, message + " ==> No exception types specified");
        for (final Class<?> exceptType : exceptionsAllowed) {
            assertTrue(Throwable.class.isAssignableFrom(exceptType), message + " ==> Not an Throwable type " + exceptType.getName());
        }
    }

    private static void checkCaughtIsAllowedType(final String message, final Throwable caught, final Class<?>[] exceptionsAllowed) {
        for (final Class<?> exceptType : exceptionsAllowed) {
            if (exceptType.isInstance(caught)) {
                return;
            }
        }
        fail(message + " ==> Unexpected exception type thrown. " + message, caught);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(final T obj) throws Exception {
        return (T) serializeDeserialize(obj);
    }

    public static Object serializeDeserialize(final Object obj) throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (final ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(obj);
        }

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            return in.readObject();
        }
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
     * Assert the arrays contain the same elements, ignoring the order.
     *
     * <p>Note this does not test the arrays are deeply equal. Array elements are compared
     * using {@link Object#equals(Object)}.
     *
     * @param a1 First array
     * @param a2 Second array
     * @param msg Failure message prefix
     */
    public static void assertUnorderedArrayEquals(final Object[] a1, final Object[] a2, final String msg) {
        assertEquals(a1.length, a2.length, () -> msg + ": length");
        final int size = a1.length;
        // Track values that have been matched once (and only once)
        final boolean[] matched = new boolean[size];
        NEXT_OBJECT:
        for (final Object o : a1) {
            for (int i = 0; i < size; i++) {
                if (matched[i]) {
                    // skip values already matched
                    continue;
                }
                if (Objects.equals(o, a2[i])) {
                    // values matched
                    matched[i] = true;
                    // continue to the outer loop
                    continue NEXT_OBJECT;
                }
            }
            fail(msg + ": array 2 does not have object: " + o);
        }
    }
}
