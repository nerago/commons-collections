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
package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MapSpliterator<K, V> extends Spliterator<Map.Entry<K, V>> {
    boolean tryAdvance(BiConsumer<? super K, ? super V> action);

    default void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        do { } while (tryAdvance(action));
    }

    @Override
    default boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        return tryAdvance((k, v) -> action.accept(new UnmodifiableMapEntry<>(k, v)));
    }

    @Override
    default void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        do { } while (tryAdvance((k, v) -> action.accept(new UnmodifiableMapEntry<>(k, v))));
    }

    @Override
    MapSpliterator<K, V> trySplit();
}
