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

import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;

/**
 * Defines a map that allows bidirectional lookup between key and values
 * and retains and provides access to an ordering.
 * <p>
 * Implementations should allow a value to be looked up from a key and
 * a key to be looked up from a value with equal performance.
 * </p>
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 *
 * @since 3.0
 */
public interface OrderedBidiMap<K, V, TSubMap extends OrderedBidiMap<K, V, ?, ?>, TInverseMap extends OrderedBidiMap<V, K, ?, ?>>
        extends BidiMap<K, V, TInverseMap>,
                OrderedMap<K, V> {

    @Override
    SequencedSet<V> values();

    @Override
    default SequencedSet<V> sequencedValues() {
        return values();
    }

    @Override
    TSubMap reversed();
}
