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

import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class UnmodifiableMapSpliterator<K, V> extends EntrySetSpliterator<K, V> implements Unmodifiable {
    public static <K, V> MapSpliterator<K, V> unmodifiableMapSpliterator(final Spliterator<Map.Entry<K, V>> spliterator) {
        return new UnmodifiableMapSpliterator<>(spliterator);
    }

    protected UnmodifiableMapSpliterator(final Spliterator<Map.Entry<K, V>> spliterator) {
        super(spliterator);
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Map.Entry<K, V>> action) {
        return decorated().tryAdvance(entry -> action.accept(new UnmodifiableMapEntry<>(entry)));
    }

    @Override
    public void forEachRemaining(final Consumer<? super Map.Entry<K, V>> action) {
        decorated().forEachRemaining(entry -> action.accept(new UnmodifiableMapEntry<>(entry)));
    }

    @Override
    protected EntrySetSpliterator<K, V> decorateSplit(final Spliterator<Map.Entry<K, V>> split) {
        return new UnmodifiableMapSpliterator<>(split);
    }

    @Override
    public int characteristics() {
        return decorated().characteristics() | Spliterator.IMMUTABLE;
    }
}
