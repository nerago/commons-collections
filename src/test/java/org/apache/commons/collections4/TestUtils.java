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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
     * @see #assertSameAfterSerialization(Object)
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
     * Asserts that deserialization of the object returns the same object as the
     * one that was serialized.
     * <p>
     * Effect of method call is the same as:
     * {@code assertSameAfterSerialization(null, o)}.
     *
     * @param o object that will be tested.
     * @see #assertSameAfterSerialization(String, Object)
     */
    public static void assertSameAfterSerialization(final Object o) {
        assertSameAfterSerialization(null, o);
    }

    public static <E> void assertThrowsOptional(final Class<E> e, final Executable executable, final String message) {
        try {
            executable.execute();
        } catch (final Throwable throwable) {
            if (e.isInstance(throwable)) {
                return;
            }

            fail(message + " - Unexpected exception type thrown", throwable);
        }
    }

    public static <E> void assertThrowsOrNull(final Class<E> e, final Supplier<Object> executable, final String message) {
        try {
            assertNull(executable.get());
        } catch (final Throwable throwable) {
            if (!e.isInstance(throwable)) {
                fail(message + " - Unexpected exception type thrown", throwable);
            }
        }
    }

    public static <E> void assertThrowsOrFalse(final Class<E> e, final BooleanSupplier executable, final String message) {
        try {
            assertFalse(executable.getAsBoolean());
        } catch (final Throwable throwable) {
            if (!e.isInstance(throwable)) {
                fail(message + " - Unexpected exception type thrown", throwable);
            }
        }
    }

    public static <E1, E2> void assertThrowsEither(final Class<E1> e1, final Class<E2> e2, final Executable executable) {
        try {
            executable.execute();
        } catch (final Throwable throwable) {
            if (e1.isInstance(throwable) || e2.isInstance(throwable)) {
                return;
            }

            fail("Unexpected exception type thrown", throwable);
        }

        fail("Expected exception to be thrown, but nothing was thrown.");
    }

    public static <E1, E2> void assertThrowsEither(final Class<E1> e1, final Class<E2> e2, final Executable executable, final String message) {
        try {
            executable.execute();
        } catch (final Throwable throwable) {
            if (e1.isInstance(throwable) || e2.isInstance(throwable)) {
                return;
            }

            fail(message + " - Unexpected exception type thrown", throwable);
        }

        fail(message + " - Expected exception to be thrown, but nothing was thrown.");
    }
}
