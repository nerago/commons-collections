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
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SingletonMapSpliterator<K, V> implements MapSpliterator<K, V> {
    private final K key;
    private final V value;
    private boolean isComplete = false;

    public SingletonMapSpliterator(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean tryAdvance(BiConsumer<? super K, ? super V> action) {
        if (!isComplete) {
            action.accept(key, value);
            isComplete = true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        if (!isComplete) {
            action.accept(key, value);
            isComplete = true;
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        if (!isComplete) {
            action.accept(new UnmodifiableMapEntry<>(key, value));
            isComplete = true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        if (!isComplete) {
            action.accept(new UnmodifiableMapEntry<>(key, value));
            isComplete = true;
        }
    }

    @Override
    public MapSpliterator<K, V> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.NONNULL;
    }
}
