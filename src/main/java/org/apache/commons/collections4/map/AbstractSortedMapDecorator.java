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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedMapUtils;
import org.apache.commons.collections4.iterators.SortedMapOrderedMapIterator;

/**
 * Provides a base decorator that enables additional functionality to be added
 * to a Map via decoration.
 * <p>
 * Methods are forwarded directly to the decorated map.
 * </p>
 * <p>
 * This implementation does not perform any special processing with the map views.
 * Instead it simply returns the set/collection from the wrapped map. This may be
 * undesirable, for example if you are trying to write a validating implementation
 * it would provide a loophole around the validation.
 * But, you might want that loophole, so this class is kept simple.
 * </p>
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 * @since 3.0
 */
public abstract class AbstractSortedMapDecorator<K, V,
                TDecorated extends SortedMap<K, V>,
                TSubMap extends IterableSortedMap<K, V, TSubMap>>
        extends AbstractMapDecorator<K, V, TDecorated>
        implements IterableSortedMap<K, V, TSubMap> {

    private static final long serialVersionUID = 4710068155190191469L;

    /** The range of this sub-map */
    transient SortedMapRange<K> keyRange;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since 3.1
     */
    protected AbstractSortedMapDecorator() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public AbstractSortedMapDecorator(final TDecorated map) {
        super(map);
        this.keyRange = SortedMapRange.full(map.comparator());
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map      the map to decorate, must not be null
     * @param keyRange the range of keys accepted by the map
     * @throws NullPointerException if the map is null
     */
    public AbstractSortedMapDecorator(final TDecorated map, final SortedMapRange<K> keyRange) {
        super(map);
        this.keyRange = Objects.requireNonNull(keyRange);
    }

    protected abstract TSubMap decorateDerived(final TDecorated subMap, final SortedMapRange<K> keyRange);

    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public Comparator<? super K> comparator() {
        return decorated().comparator();
    }

    /**
     * Sorted maps don't allow insert by position.
     * @throws UnsupportedOperationException always
     */
    @Override
    public final V putFirst(final K k, final V v) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sorted maps don't allow insert by position.
     * @throws UnsupportedOperationException always
     */
    @Override
    public final V putLast(final K k, final V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<K, V> firstEntry() {
        return decorated().firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return decorated().lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return decorated().pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return decorated().pollLastEntry();
    }

    @Override
    public K firstKey() {
        return decorated().firstKey();
    }

    @Override
    public K lastKey() {
        return decorated().lastKey();
    }

    // just override to make final
    @Override
    public final TSubMap subMap(final K fromKey, final K toKey) {
        return IterableSortedMap.super.subMap(fromKey, toKey);
    }

    // just override to make final
    @Override
    public final TSubMap headMap(final K toKey) {
        return IterableSortedMap.super.headMap(toKey);
    }

    // just override to make final
    @Override
    public final TSubMap tailMap(final K fromKey) {
        return IterableSortedMap.super.tailMap(fromKey);
    }

    @Override
    public TSubMap subMap(final SortedMapRange<K> range) {
        return decorateDerived(range.applyToMap(decorated()), range);
    }

    @Override
    public K previousKey(final K key) {
        return SortedMapUtils.previousKey(decorated(), key);
    }

    @Override
    public K nextKey(final K key) {
        return SortedMapUtils.nextKey(this, key);
    }

    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return decorated().sequencedEntrySet();
    }

    @Override
    public SequencedSet<K> sequencedKeySet() {
        return decorated().sequencedKeySet();
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
        return decorated().sequencedValues();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return SortedMapOrderedMapIterator.sortedMapIterator(decorated());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TSubMap reversed() {
        return decorateDerived((TDecorated) decorated().reversed(), keyRange.reversed());
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(keyRange);
        super.writeExternal(out);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        keyRange = (SortedMapRange<K>) in.readObject();
        super.readExternal(in);
    }

    /** Simple wrapper class to decorate a SortedMap with IterableSortedMap interfaces but no changed behaviour */
    public static final class BasicWrapper<K, V> extends AbstractSortedMapDecorator<K, V, SortedMap<K, V>, BasicWrapper<K, V>> {
        private static final long serialVersionUID = -4795013829872876714L;

        /** Default wrapping contractor
         * @param map map to decorate
         * */
        public BasicWrapper(final SortedMap<K, V> map) {
            super(map);
        }

        /** Private sub-map contractor */
        private BasicWrapper(final SortedMap<K, V> map, final SortedMapRange<K> keyRange) {
            super(map, keyRange);
        }

        @Override
        protected BasicWrapper<K, V> decorateDerived(final SortedMap<K, V> subMap, final SortedMapRange<K> keyRange) {
            return new BasicWrapper<>(subMap, keyRange);
        }
    }
}
