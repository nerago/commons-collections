package org.apache.commons.collections4.trie;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.collection.ReverseCollection;
import org.apache.commons.collections4.set.ReverseSequencedSet;

public class ReversedTrie<K, V> implements Trie<K, V, IterableSortedMap<K, V, ?>> {
    private final Trie<K, V, IterableSortedMap<K, V, ?>> decorated;

    public ReversedTrie(final Trie<K, V, ?> trie) {
        this.decorated = (Trie<K, V, IterableSortedMap<K, V, ?>>) trie;
    }

    @Override
    public IterableSortedMap<K, V, ?> reversed() {
        return decorated;
    }

    @Override
    public int size() {
        return decorated.size();
    }

    @Override
    public boolean isEmpty() {
        return decorated.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return decorated.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return decorated.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return decorated.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        return decorated.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return decorated.remove(key);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return decorated.descendingMapIterator();
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return decorated.mapIterator();
    }

    @Override
    public K firstKey() {
        return decorated.lastKey();
    }

    @Override
    public K lastKey() {
        return decorated.firstKey();
    }

    @Override
    public K nextKey(final K key) {
        return decorated.previousKey(key);
    }

    @Override
    public K previousKey(final K key) {
        return decorated.nextKey(key);
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return decorated.getKeyRange().reversed();
    }

    @Override
    public IterableSortedMap<K, V, ?> subMap(final SortedMapRange<K> range) {
        return decorated.subMap(range.reversed()).reversed();
    }

    @Override
    public IterableSortedMap<K, V, ?> prefixMap(final K key) {
        return decorated.prefixMap(key).reversed();
    }

    @Override
    public Comparator<? super K> comparator() {
        return decorated.comparator().reversed();
    }

    @Override
    public SequencedSet<K> keySet() {
        return new ReverseSequencedSet<>(decorated.keySet());
    }

    @Override
    public SequencedSet<Entry<K, V>> entrySet() {
        return new ReverseSequencedSet<>(decorated.entrySet());
    }

    @Override
    public SequencedCollection<V> values() {
        return new ReverseCollection<>(decorated.values());
    }

    @Override
    public V putFirst(final K k, final V v) {
        return decorated.putLast(k, v);
    }

    @Override
    public V putLast(final K k, final V v) {
        return decorated.putFirst(k, v);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return decorated.lastEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return decorated.firstEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return decorated.pollLastEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return decorated.pollFirstEntry();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        // order added may not match expectations but sorted collections don't care
        decorated.putAll(m);
    }

    @Override
    public void putAll(final MapIterator<? extends K, ? extends V> it) {
        // order added may not match expectations but sorted collections don't care
        decorated.putAll(it);
    }

    @Override
    public void clear() {
        decorated.clear();
    }

    @Override
    public boolean equals(Object o) {
        return decorated.equals(o);
    }

    @Override
    public int hashCode() {
        return decorated.hashCode();
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return decorated.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        decorated.descendingMapIterator().forEachRemaining(action);
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        decorated.reversed().replaceAll(function);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        return decorated.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return decorated.remove(key, value);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return decorated.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(final K key, final V value) {
        return decorated.replace(key, value);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return decorated.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return decorated.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return decorated.compute(key, remappingFunction);
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return decorated.merge(key, value, remappingFunction);
    }

    @Override
    public boolean containsMapping(final Object key, final Object value) {
        return decorated.containsMapping(key, value);
    }

    @Override
    public boolean removeMapping(final Object key, final Object value) {
        return decorated.removeMapping(key, value);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        decorated.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        decorated.readExternal(in);
    }
}
