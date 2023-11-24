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

import java.util.Iterator;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;

/**
 * Adapts a Map entrySet to the MapIterator interface.
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 *
 * @since 4.0
 */
public class EntrySetToOrderedMapIteratorAdapter<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {

    protected transient Supplier<Iterator<Map.Entry<K, V>>> supplier;

    /** The resettable iterator in use. */
    protected transient Iterator<Map.Entry<K, V>> iterator;

    /** The currently positioned Map entry. */
    protected transient Map.Entry<K, V> entry;

    /**
     * Create a new EntrySetToMapIteratorAdapter.
     * @param entrySet  the entrySet to adapt
     */
    public EntrySetToOrderedMapIteratorAdapter(final SequencedSet<Map.Entry<K, V>> entrySet) {
        this.supplier = entrySet::iterator;
        reset();
    }

    @Override
    public K getKey() {
        return current().getKey();
    }

    @Override
    public V getValue() {
        return current().getValue();
    }

    @Override
    public V setValue(final V value) {
        return current().setValue(value);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public K next() {
        entry = iterator.next();
        return getKey();
    }

    @Override
    public boolean hasPrevious() {
        throw new IllegalStateException();
    }

    @Override
    public K previous() {
        throw new IllegalStateException();
    }

    @Override
    public void reset() {
        iterator = supplier.get();
        entry = null;
    }

    @Override
    public void remove() {
        iterator.remove();
        entry = null;
    }

    /**
     * Get the currently active entry.
     * @return Map.Entry&lt;K, V&gt;
     */
    protected Map.Entry<K, V> current() {
        if (entry == null) {
            throw new IllegalStateException();
        }
        return entry;
    }
}
