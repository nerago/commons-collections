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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;

public class EntrySetSpliterator<K, V>
        extends AbstractSpliteratorDecorator<Map.Entry<K, V>, EntrySetSpliterator<K, V>>
        implements MapSpliterator<K, V> {
    public EntrySetSpliterator(final Map<K, V> map) {
        this(map.entrySet());
    }

    public EntrySetSpliterator(final Collection<Map.Entry<K,V>> entrySet) {
        this(entrySet.spliterator());
    }

    public EntrySetSpliterator(final Spliterator<Map.Entry<K,V>> split) {
        super(split);
    }

    @Override
    protected EntrySetSpliterator<K, V> decorateSplit(final Spliterator<Map.Entry<K, V>> split) {
        return new EntrySetSpliterator<>(split);
    }

    @Override
    public boolean tryAdvance(final BiConsumer<? super K, ? super V> action) {
        return decorated().tryAdvance(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public void forEachRemaining(final BiConsumer<? super K, ? super V> action) {
        decorated().forEachRemaining(entry -> action.accept(entry.getKey(), entry.getValue()));
    }
}
