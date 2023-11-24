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
package org.apache.commons.collections4.trie;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIterator;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;
import org.apache.commons.collections4.map.UnmodifiableSequencedEntrySet;
import org.apache.commons.collections4.map.UnmodifiableSortedMap;

/**
 * An unmodifiable {@link Trie}.
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 4.0
 */
public class UnmodifiableTrie<K, V, TSubMap extends IterableSortedMap<K, V, TSubMap>>
        implements Trie<K, V, TSubMap>, Unmodifiable {

    /** Serialization version */
    private static final long serialVersionUID = -7156426030315945159L;

    private Trie<K, V, ?> delegate;

    /**
     * Factory method to create an unmodifiable trie.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param trie  the trie to decorate, must not be null
     * @return a new unmodifiable trie
     * @throws NullPointerException if trie is null
     */
    public static <K, V> Trie<K, V, ?> unmodifiableTrie(final Trie<K, V, ?> trie) {
        if (trie instanceof Unmodifiable) {
            @SuppressWarnings("unchecked") // safe to upcast
            final Trie<K, V, ?> tmpTrie = (Trie<K, V, ?>) trie;
            return tmpTrie;
        }
        return new UnmodifiableTrie<>(trie);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param trie  the trie to decorate, must not be null
     * @throws NullPointerException if trie is null
     */
    public UnmodifiableTrie(final Trie<K, V, ?> trie) {
        @SuppressWarnings("unchecked") // safe to upcast
        final Trie<K, V, ?> tmpTrie = (Trie<K, V, ?>) Objects.requireNonNull(trie, "trie");
        this.delegate = tmpTrie;
    }

    @Override
    public SequencedSet<Entry<K, V>> entrySet() {
        return UnmodifiableSequencedEntrySet.unmodifiableEntrySet(delegate.entrySet());
    }

    @Override
    public SequencedSet<K> keySet() {
        return Collections.unmodifiableSequencedSet(delegate.keySet());
    }

    @Override
    public SequencedCollection<V> values() {
        return Collections.unmodifiableSequencedCollection(delegate.values());
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final MapIterator<? extends K, ? extends V> it) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public K firstKey() {
        return delegate.firstKey();
    }

    @Override
    public K lastKey() {
        return delegate.lastKey();
    }

    @Override
    public TSubMap reversed() {
        return (TSubMap) new ReversedTrie<>(this);
    }

    @Override
    public TSubMap subMap(final SortedMapRange<K> range) {
        return (TSubMap) UnmodifiableSortedMap.unmodifiableSortedMap(range.applyToMap(delegate));
    }

    @Override
    public TSubMap prefixMap(final K key) {
        return (TSubMap) Collections.unmodifiableSortedMap(delegate.prefixMap(key));
    }

    @Override
    public Comparator<? super K> comparator() {
        return delegate.comparator();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        final OrderedMapIterator<K, V> it = delegate.mapIterator();
        return UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it);
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        final OrderedMapIterator<K, V> it = delegate.descendingMapIterator();
        return UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it);
    }

    @Override
    public K nextKey(final K key) {
        return delegate.nextKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return delegate.previousKey(key);
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return delegate.getKeyRange();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(delegate);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        delegate = (Trie<K, V, ?>) in.readObject();
    }
}
