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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSerializationUtils {
    /**
     * Path to test data resources
     */
    public static final String TEST_DATA_PATH = "src/test/resources/org/apache/commons/collections4/data/test/";
    /**
     * Path to test properties resources
     */
    public static final String TEST_PROPERTIES_PATH = "src/test/resources/org/apache/commons/collections4/properties/";


    /**
     * Writes a Serializable or Externalizable object as
     * a file at the given path.  NOT USEFUL as part
     * of a unit test; this is just a utility method
     * for creating disk-based objects in SCM that can become
     * the basis for compatibility tests using
     * {@link #readExternalFormFromDisk(String path)}
     *
     * @param o Object to serialize
     * @param path path to write the serialized Object
     * @throws IOException
     */
    public static void writeExternalFormToDisk(final Serializable o, final String path) throws IOException {
        try (final ObjectOutputStream oStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(path)))) {
            oStream.writeObject(o);
        }
    }

    /**
     * Converts a Serializable or Externalizable object to
     * bytes.  Useful for in-memory tests of serialization
     *
     * @param o Object to convert to bytes
     * @return serialized form of the Object
     * @throws IOException
     */
    public static byte[] writeExternalFormToBytes(final Serializable o) throws IOException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (final ObjectOutputStream oStream = new ObjectOutputStream(byteStream)) {
            oStream.writeObject(o);
        }
        return byteStream.toByteArray();
    }

    /**
     * Reads a Serialized or Externalized Object from disk.
     * Useful for creating compatibility tests between
     * different SCM versions of the same class
     *
     * @param path path to the serialized Object
     * @return the Object at the given path
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readExternalFormFromDisk(final String path) throws IOException, ClassNotFoundException {
        try (final ObjectInputStream oStream = new ObjectInputStream(Files.newInputStream(Paths.get(path)))) {
            return oStream.readObject();
        }
    }

    /**
     * Read a Serialized or Externalized Object from bytes.
     * Useful for verifying serialization in memory.
     *
     * @param bytes byte array containing a serialized Object
     * @return Object contained in the bytes
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readExternalFormFromBytes(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (final ObjectInputStream oStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return oStream.readObject();
        }
    }

}
