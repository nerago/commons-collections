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

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class EntrySetUtil {
    @SuppressWarnings("unchecked")
    public static <K, V> Object[] toArrayUnmodifiable(Collection<Map.Entry<K, V>> collection) {
        final Object[] array = collection.toArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = new UnmodifiableMapEntry<>((Map.Entry<K, V>) array[i]);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <V, K, T> T[] toArrayUnmodifiable(Collection<Map.Entry<K,V>> collection, T[] array) {
        Object[] result = array;
        if (array.length > 0) {
            // we must create a new array to handle multithreaded situations
            // where another thread could access data before we decorate it
            result = (Object[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        result = collection.toArray(result);
        for (int i = 0; i < result.length; i++) {
            result[i] = new UnmodifiableMapEntry<>((Map.Entry<K, V>) result[i]);
        }

        // check to see if result should be returned straight
        if (result.length > array.length) {
            return (T[]) result;
        }

        // copy back into input array to fulfill the method contract
        System.arraycopy(result, 0, array, 0, result.length);
        if (array.length > result.length) {
            array[result.length] = null;
        }
        return array;
    }
}
