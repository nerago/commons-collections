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


import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmptyMapSpliterator<K, V> implements MapSpliterator<K, V> {
    @SuppressWarnings("rawtypes")
    public static final MapSpliterator INSTANCE = new EmptyMapSpliterator();

    @SuppressWarnings("unchecked")
    public static <K, V> MapSpliterator<K, V> emptyMapSpliterator() {
        return INSTANCE;
    }

    @Override
    public boolean tryAdvance(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        return false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        Objects.requireNonNull(action);
        return false;
    }

    @Override
    public void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public MapSpliterator<K, V> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED;
    }
}
