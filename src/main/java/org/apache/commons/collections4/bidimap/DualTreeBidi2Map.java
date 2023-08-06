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
package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


/**
 * Implementation of {@link BidiMap} that uses two {@link TreeMap} instances.
 * <p>
 * The setValue() method on iterators will succeed only if the new value being set is
 * not already in the bidi map.
 * </p>
 * <p>
 * When considering whether to use this class, the {@link TreeBidiMap} class should
 * also be considered. It implements the interface using a dedicated design, and does
 * not store each object twice, which can save on memory use.
 * </p>
 * <p>
 * NOTE: From Commons Collections 3.1, all subclasses will use {@link TreeMap}
 * and the flawed {@code createMap} method is ignored.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class DualTreeBidi2Map<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapBase<K, V> implements Externalizable {

    private static final long serialVersionUID = 721969328361809L;

    int modificationCount = 0;

    /**
     * Creates an empty {@link DualTreeBidi2Map}.
     */
    public DualTreeBidi2Map() {
        this(ComparatorUtils.naturalComparator(), ComparatorUtils.naturalComparator());
    }

    /**
     * Constructs a {@link DualTreeBidi2Map} and copies the mappings from
     * specified {@link Map}.
     *
     * @param map the map whose mappings are to be placed in this map
     */
    public DualTreeBidi2Map(final Map<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Constructs a {@link DualTreeBidi2Map} using the specified {@link Comparator}.
     *
     * @param keyComparator   the comparator
     * @param valueComparator the values comparator to use
     */
    public DualTreeBidi2Map(final Comparator<? super K> keyComparator, final Comparator<? super V> valueComparator) {
        super(new TreeMap<>(keyComparator), SortedMapRange.full(keyComparator),
                new TreeMap<>(valueComparator), SortedMapRange.full(valueComparator));
    }

    protected DualTreeBidi2Map(final NavigableMap<K, V> keyMap, final SortedMapRange<K> keyRange,
                               final NavigableMap<V, K> valueMap, final SortedMapRange<V> valueRange) {
        super(keyMap, keyRange, valueMap, valueRange);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(keyComparator);
        out.writeObject(valueComparator);
        out.writeObject(keyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        keyComparator = (Comparator<? super K>) in.readObject();
        valueComparator = (Comparator<? super V>) in.readObject();
        final TreeMap<K, V> storedMap = (TreeMap<K, V>) in.readObject();
        putAll(storedMap);
    }

    @Override
    protected void modified() {
        modificationCount++;
    }

    @Override
    protected DualTreeBidi2Map<K, V> primaryMap() {
        return this;
    }

    @Override
    protected DualTreeBidi2MapBase<K, V> createDescending() {
        return new DualTreeBidi2Map<>(keyMap.descendingMap(), getKeyRange().reversed(), valueMap, valueRange);
    }

    @Override
    protected DualTreeBidi2Map<V, K> createInverse() {
        return new DualTreeBidi2Map<>(valueMap, valueRange, keyMap, getKeyRange());
    }

    @Override
    protected NavigableBoundMap<K, V> decorateDerived(final NavigableMap<K, V> map, final SortedMapRange<K> keyRange) {
        return new DualTreeBidi2MapSubMap<>(map, keyRange, this);
    }

    @Override
    protected NavigableSet<K> createKeySet(final boolean descending) {
        return new KeySetUsingKeyMap<>(this, descending, getKeyRange(), this);
    }

    @Override
    protected Set<V> createValueSet() {
        return new ValueSetUsingKeyEntrySet<>(keyMap.entrySet(), this);
    }

    @Override
    protected Set<Entry<K, V>> createEntrySet() {
        return new EntrySetUsingKeyMap<>(keyMap.entrySet(), this);
    }
}
